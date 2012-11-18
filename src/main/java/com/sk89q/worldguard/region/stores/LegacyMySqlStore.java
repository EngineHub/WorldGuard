// $Id$
/*
 * MySQL WordGuard Region Database
 * Copyright (C) 2011 Nicholas Steicke <http://narthollis.net>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.sk89q.worldguard.region.stores;

import com.jolbox.bonecp.BoneCP;
import com.sk89q.rebar.util.LoggerUtils;
import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.region.Region;
import com.sk89q.worldguard.region.flags.DefaultFlag;
import com.sk89q.worldguard.region.flags.Flag;
import com.sk89q.worldguard.region.indices.RegionIndex;
import com.sk89q.worldguard.region.indices.RegionIndexFactory;
import com.sk89q.worldguard.region.shapes.Cuboid;
import com.sk89q.worldguard.region.shapes.Everywhere;
import com.sk89q.worldguard.region.shapes.ExtrudedPolygon;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.representer.Representer;

import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.logging.Logger;

/**
 * A MySQL store that overly normalizes all the data, resulting in extremely slow
 * load and save performance.
 * <p>
 * This store cannot be used anymore as it no longer knows how to save data.
 */
@Deprecated
public class LegacyMySqlStore implements RegionStore {

    private static final Logger defaultLogger = LoggerUtils.getLogger(YamlStore.class);

    private Logger logger = defaultLogger;
    private final BoneCP connPool;
    private final String id;
    private int internalId = -1;
    private final Yaml yaml;

    /**
     * Create a new MySQL store.
     *
     * @param connPool connection pool
     * @param id ID for this store
     */
    public LegacyMySqlStore(BoneCP connPool, String id) {
        this.connPool = connPool;
        this.id = id;

        DumperOptions options = new DumperOptions();
        options.setIndent(2);
        options.setDefaultFlowStyle(FlowStyle.FLOW);
        Representer representer = new Representer();
        representer.setDefaultFlowStyle(FlowStyle.FLOW);

        // We have to use this in order to properly save non-string values
        yaml = new Yaml(new SafeConstructor(), new Representer(), options);
    }

    /**
     * Do initial setup that that is required to get the internal ID used per-world
     * (or set of regions).
     *
     * @throws SQLException on an SQL error
     */
    private void setup(Connection conn) throws SQLException {
        PreparedStatement stmt;
        ResultSet result;

        // Does the database know about this world?
        stmt = conn.prepareStatement("SELECT id FROM world WHERE name = ? LIMIT 0, 1");
        stmt.setString(1, id);
        result = stmt.executeQuery();

        if (result.next() == false) { // Looks like we have to add it
            stmt = conn.prepareStatement(
                    "INSERT INTO world (id, name) VALUES (null, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, id);
            stmt.execute();

            ResultSet generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.first()) {
                internalId = generatedKeys.getInt(1);
            }
        }
    }

    @Override
    public RegionIndex load(RegionIndexFactory factory) throws IOException {
        RegionIndex index = factory.newIndex();

        try {
            performLoad(index);
        } catch (SQLException e) {
            throw new IOException("Failed to load regions from database", e);
        }

        return index;
    }

    /**
     * Actually perform the load.
     *
     * @param index the index to put the regions into
     * @throws SQLException on an SQL error
     */
    private void performLoad(RegionIndex index) throws SQLException {
        Connection conn = null;
        Map<Region, String> parentSets = new HashMap<Region, String>();

        try {
            conn = connPool.getConnection();

            // Make sure that we have the database setup for this particular store
            if (internalId == -1) {
                setup(conn);
            }

            // Load the regions
            loadCuboid(conn, index, parentSets);
            loadExtrudedPoly(conn, index, parentSets);
            loadGlobal(conn, index, parentSets);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }

        // Re-link parents
        for (Map.Entry<Region, String> entry : parentSets.entrySet()) {
            Region parent = index.get(entry.getValue());
            if (parent != null) {
                try {
                    entry.getKey().setParent(parent);
                } catch (IllegalArgumentException e) {
                    logger.warning("Circular inheritance detect with '"
                            + entry.getValue() + "' detected as a parent");
                }
            } else {
                logger.warning("Unknown region parent: " + entry.getValue());
            }
        }

        index.reindex(); // Important!
    }

    /**
     * Load cuboid regions.
     *
     * @param conn the database connection
     * @param index the index to put the regions into
     * @param parentSets map of regions that need parents set for them
     * @throws SQLException on SQL error
     */
    private void loadCuboid(Connection conn, RegionIndex index,
            Map<Region, String> parentSets) throws SQLException {

        PreparedStatement stmt;
        ResultSet result;

        stmt = conn.prepareStatement(
                "SELECT c.min_z, c.min_y, c.min_x, c.max_z, c.max_y, c.max_x, " +
                        "r.id, r.priority, r.id AS parent " +
                "FROM region_cuboid AS c " +
                "LEFT JOIN region AS r ON (region_cuboid.region_id = r.id " +
                    "AND region_cuboid.world_id = r.world_id) " +
                "LEFT JOIN region AS p ON (region.p = p.id AND region.world_id = p.world_id) " +
                "WHERE region.world_id = ?");
        stmt.setInt(1, internalId);
        result = stmt.executeQuery();

        while (result.next()) {
            Vector pt1 = new Vector(result.getInt("min_x"),
                    result.getInt("min_y"), result.getInt("min_z") );
            Vector pt2 = new Vector(result.getInt("max_x"),
                    result.getInt("max_y"), result.getInt("max_z") );

            Cuboid cuboid = new Cuboid(pt1, pt2);
            Region region = new Region(result.getString("id"), cuboid);
            region.setPriority(result.getInt("priority"));

            loadFlags(conn, region);
            importDomain(conn, region);

            index.add(region);

            String parentId = result.getString("parent");
            if (parentId != null) {
                parentSets.put(region, parentId);
            }
        }
    }

    /**
     * Load extruded polygonal regions.
     *
     * @param conn the database connection
     * @param index the index to put the regions into
     * @param parentSets map of regions that need parents set for them
     * @throws SQLException on SQL error
     */
    private void loadExtrudedPoly(Connection conn, RegionIndex index,
            Map<Region, String> parentSets) throws SQLException {

        PreparedStatement polyStmt = conn.prepareStatement(
                "SELECT y.min_y, y.max_y, r.id, r.priority, p.id AS parent " +
                "FROM region_poly2d AS y " +
                "LEFT JOIN region AS r ON (y.region_id = r.id " +
                    "AND y.world_id = r.world_id) " +
                "LEFT JOIN region AS p  ON (r.p = p.id AND r.world_id = p.world_id) " +
                "WHERE r.world_id = ?");
        polyStmt.setInt(1, internalId);
        ResultSet polyResult = polyStmt.executeQuery();

        PreparedStatement pointStmt = conn.prepareStatement(
                "SELECT r.x, r.z " +
                "FROM region_poly2d_point AS r " +
                "WHERE r.region_id = ? " +
                    "AND r.world_id = ?");

        while (polyResult.next()) {
            Integer minY = polyResult.getInt("min_y");
            Integer maxY = polyResult.getInt("max_y");

            // Fetch the points
            pointStmt.setString(1, id);
            pointStmt.setInt(2, internalId);
            ResultSet pointResult = pointStmt.executeQuery();

            List<BlockVector2D> points = new ArrayList<BlockVector2D>();
            while (pointResult.next()) {
                points.add(new BlockVector2D(
                        pointResult.getInt("x"),
                        pointResult.getInt("z")));
            }

            ExtrudedPolygon polygon = new ExtrudedPolygon(points, minY, maxY);

            Region region = new Region(polyResult.getString("id"), polygon);
            region.setPriority(polyResult.getInt("priority"));

            loadFlags(conn, region);
            importDomain(conn, region);

            index.add(region);

            String parentId = polyResult.getString("parent");
            if (parentId != null) {
                parentSets.put(region, parentId);
            }
        }
    }

    /**
     * Load global regions.
     *
     * @param conn the database connection
     * @param index the index to put the regions into
     * @param parentSets map of regions that need parents set for them
     * @throws SQLException on SQL error
     */
    private void loadGlobal(Connection conn, RegionIndex index,
            Map<Region, String> parentSets) throws SQLException {

        PreparedStatement stmt;
        ResultSet result;

        stmt = conn.prepareStatement(
                "SELECT r.id, r.priority, p.id AS parent " +
                "FROM region AS r " +
                "LEFT JOIN region AS p ON (r.p = p.id " +
                    "AND r.world_id = p.world_id) " +
                "WHERE r.type = 'global' AND r.world_id = ?");
        stmt.setInt(1, internalId);
        result = stmt.executeQuery();

        while (result.next()) {
            Everywhere everywhere = new Everywhere();

            Region region = new Region(result.getString("id"), everywhere);
            region.setPriority(result.getInt("priority"));

            loadFlags(conn, region);
            importDomain(conn, region);

            index.add(region);

            String parentId = result.getString("parent");
            if (parentId != null) {
                parentSets.put(region, parentId);
            }
        }
    }

    /**
     * Load the flags for the given region.
     *
     * @param conn
     * @param region
     * @throws SQLException
     */
    private void loadFlags(Connection conn, Region region) throws SQLException {
        PreparedStatement stmt;
        ResultSet result;

        stmt = conn.prepareStatement(
                "SELECT f.flag, f.value " +
                "FROM region_flag AS f " +
                "WHERE f.region_id = ? " +
                "AND f.world_id = ?");
        stmt.setString(1, region.getId());
        stmt.setInt(2, internalId);
        result = stmt.executeQuery();

        Map<String, String> data = new HashMap<String, String>();
        while (result.next()) {
            data.put(result.getString("flag"), result.getString("value"));
        }

        // Apply flag
        for (Flag<?> flag : DefaultFlag.getFlags()) {
            String o = data.get(flag.getName());
            if (o != null) {
                region.setFlagUnsafe(flag, unmarshal(o));
            }

            Flag<?> groupFlag = flag.getRegionGroupFlag();
            if (groupFlag != null) {
                o = data.get(groupFlag.getName());
                if (o != null) {
                    region.setFlagUnsafe(flag, unmarshal(o));
                }
            }
        }
    }

    /**
     * Load old owners/members information.
     *
     * @param conn database connection
     * @param region region
     * @throws SQLException on SQL error
     */
    private void importDomain(Connection conn, Region region) throws SQLException {
        PreparedStatement stmt;
        ResultSet result;

        List<String> owners = new ArrayList<String>();
        List<String> members = new ArrayList<String>();

        // First load players
        stmt = conn.prepareStatement(
                "SELECT o.name, p.owner " +
                "FROM region_players AS p " +
                "LEFT JOIN user AS o ON (p.user_id = o.id) " +
                "WHERE p.region_id = ? " +
                "AND p.world_id = ?");
        stmt.setString(1, region.getId());
        stmt.setInt(2, internalId);
        result = stmt.executeQuery();

        while (result.next()) {
            if (result.getBoolean("owner")) {
                owners.add("player:" + result.getString("name"));
            } else {
                members.add("player:" + result.getString("name"));
            }
        }

        // Then load groups
        stmt = conn.prepareStatement(
                "SELECT o.name, g.owner " +
                "FROM region_groups AS g" +
                "LEFT JOIN o AS o ON (g.group_id = o.id) " +
                "WHERE g.region_id = ? " +
                "AND g.world_id = ?");
        stmt.setString(1, region.getId());
        stmt.setInt(2, internalId);
        result = stmt.executeQuery();

        while (result.next()) {
            if (result.getBoolean("owner")) {
                owners.add("group:" + result.getString("name"));
            } else {
                members.add("group:" + result.getString("name"));
            }
        }

        // TODO: Something here
    }

    /**
     * Load flag data from the database, unmarshalling as needed.
     * <p>
     * Errors will be returned, and the raw value will be returned.
     *
     * @param rawValue the raw value
     * @return the valid value
     */
    private Object unmarshal(String rawValue) {
        try {
            return yaml.load(rawValue);
        } catch (YAMLException e) {
            return String.valueOf(rawValue);
        }
    }

    @Override
    public void save(Collection<Region> regions) throws IOException {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void close() throws IOException {
        // We don't actually have to do anything because we are using a connection
        // pool and that is already taken care of
    }

}
