/*
 * WorldGuard, a suite of tools for Minecraft
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldGuard team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.worldguard.protection.managers.storage.sql;

import com.google.common.collect.Lists;
import com.sk89q.worldguard.internal.util.sql.StatementUtils;
import com.sk89q.worldguard.util.io.Closer;
import com.sk89q.worldguard.util.sql.DataSourceConfig;

import javax.annotation.Nullable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Stores a cache of entries from a table for fast lookup and
 * creates new rows whenever required.
 *
 * @param <V> the type of entry
 */
abstract class TableCache<V> {

    private static final Logger log = Logger.getLogger(TableCache.class.getCanonicalName());

    private static final int MAX_NUMBER_PER_QUERY = 100;
    private static final Object LOCK = new Object();

    private final Map<V, Integer> cache = new HashMap<V, Integer>();
    private final DataSourceConfig config;
    private final Connection conn;
    private final String tableName;
    private final String fieldName;

    /**
     * Create a new instance.
     *
     * @param config the data source config
     * @param conn the connection to use
     * @param tableName the table name
     * @param fieldName the field name
     */
    protected TableCache(DataSourceConfig config, Connection conn, String tableName, String fieldName) {
        this.config = config;
        this.conn = conn;
        this.tableName = tableName;
        this.fieldName = fieldName;
    }

    /**
     * Convert from the type to the string representation.
     *
     * @param o the object
     * @return the string representation
     */
    protected abstract String fromType(V o);

    /**
     * Convert from the string representation to the type.
     *
     * @param o the string
     * @return the object
     */
    protected abstract V toType(String o);

    /**
     * Convert the object to the version that is stored as a key in the map.
     *
     * @param object the object
     * @return the key version
     */
    protected abstract V toKey(V object);

    @Nullable
    public Integer find(V object) {
        return cache.get(object);
    }

    /**
     * Fetch from the database rows that match the given entries, otherwise
     * create new entries and assign them an ID.
     *
     * @param entries a list of entries
     * @throws SQLException thrown on SQL error
     */
    public void fetch(Collection<V> entries) throws SQLException {
        synchronized (LOCK) { // Lock across all cache instances
            checkNotNull(entries);

            // Get a list of missing entries
            List<V> fetchList = new ArrayList<V>();
            for (V entry : entries) {
                if (!cache.containsKey(toKey(entry))) {
                    fetchList.add(entry);
                }
            }

            if (fetchList.isEmpty()) {
                return; // Nothing to do
            }

            // Search for entries
            for (List<V> partition : Lists.partition(fetchList, MAX_NUMBER_PER_QUERY)) {
                Closer closer = Closer.create();
                try {
                    PreparedStatement statement = closer.register(conn.prepareStatement(
                            String.format(
                                    "SELECT id, " + fieldName + " " +
                                    "FROM `" + config.getTablePrefix() + tableName + "` " +
                                    "WHERE " + fieldName + " IN (%s)",
                                    StatementUtils.preparePlaceHolders(partition.size()))));

                    String[] values = new String[partition.size()];
                    int i = 0;
                    for (V entry : partition) {
                        values[i] = fromType(entry);
                        i++;
                    }

                    StatementUtils.setValues(statement, values);
                    ResultSet results = closer.register(statement.executeQuery());
                    while (results.next()) {
                        cache.put(toKey(toType(results.getString(fieldName))), results.getInt("id"));
                    }
                } finally {
                    closer.closeQuietly();
                }
            }

            List<V> missing = new ArrayList<V>();
            for (V entry : fetchList) {
                if (!cache.containsKey(toKey(entry))) {
                    missing.add(entry);
                }
            }

            // Insert entries that are missing
            if (!missing.isEmpty()) {
                Closer closer = Closer.create();
                try {
                    PreparedStatement statement = closer.register(conn.prepareStatement(
                            "INSERT INTO `" + config.getTablePrefix() + tableName + "` (id, " + fieldName + ") VALUES (null, ?)",
                            Statement.RETURN_GENERATED_KEYS));

                    for (V entry : missing) {
                        statement.setString(1, fromType(entry));
                        statement.execute();

                        ResultSet generatedKeys = statement.getGeneratedKeys();
                        if (generatedKeys.next()) {
                            cache.put(toKey(entry), generatedKeys.getInt(1));
                        } else {
                            log.warning("Could not get the database ID for entry " + entry);
                        }
                    }
                } finally {
                    closer.closeQuietly();
                }
            }
        }
    }

    /**
     * An index of user rows that utilize the name field.
     */
    static class UserNameCache extends TableCache<String> {
        protected UserNameCache(DataSourceConfig config, Connection conn) {
            super(config, conn, "user", "name");
        }

        @Override
        protected String fromType(String o) {
            return o;
        }

        @Override
        protected String toType(String o) {
            return o;
        }

        @Override
        protected String toKey(String object) {
            return object.toLowerCase();
        }
    }

    /**
     * An index of user rows that utilize the UUID field.
     */
    static class UserUuidCache extends TableCache<UUID> {
        protected UserUuidCache(DataSourceConfig config, Connection conn) {
            super(config, conn, "user", "uuid");
        }

        @Override
        protected String fromType(UUID o) {
            return o.toString();
        }

        @Override
        protected UUID toType(String o) {
            return UUID.fromString(o);
        }

        @Override
        protected UUID toKey(UUID object) {
            return object;
        }
    }

    /**
     * An index of group rows.
     */
    static class GroupNameCache extends TableCache<String> {
        protected GroupNameCache(DataSourceConfig config, Connection conn) {
            super(config, conn, "group", "name");
        }

        @Override
        protected String fromType(String o) {
            return o;
        }

        @Override
        protected String toType(String o) {
            return o;
        }

        @Override
        protected String toKey(String object) {
            return object.toLowerCase();
        }
    }

}
