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

package com.sk89q.worldguard.blacklist.logger;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.blacklist.event.BlacklistEvent;
import com.sk89q.worldguard.blacklist.event.EventType;

import javax.annotation.Nullable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseHandler implements LoggerHandler {

    private final String dsn;
    private final String user;
    private final String pass;
    private final String table;
    private final String worldName;
    private Connection conn;
    
    private final Logger logger;

    /**
     * Construct the object.
     *
     * @param dsn The DSN for the connection
     * @param user The username to connect with
     * @param pass The password to connect with
     * @param table The table to log to
     * @param worldName The name of the world to log
     * @param logger The logger to log errors to
     */
    public DatabaseHandler(String dsn, String user, String pass, String table, String worldName, Logger logger) {
        this.dsn = dsn;
        this.user = user;
        this.pass = pass;
        this.table = table;
        this.worldName = worldName;
        this.logger = logger;
    }

    /**
     * Gets the database connection.
     *
     * @return The database connection
     * @throws SQLException when the connection cannot be created
     */
    private Connection getConnection() throws SQLException {
        if (conn == null || conn.isClosed()) {
            conn = DriverManager.getConnection(dsn, user, pass);
        }
        return conn;
    }

    /**
     * Log an event to the database.
     *
     * @param eventType The event type to log
     * @param player The player associated with the event
     * @param pos The location of the event
     * @param item The item used
     * @param comment The comment associated with the event
     */
    private void logEvent(EventType eventType, @Nullable LocalPlayer player, Vector pos, int item, String comment) {
        try {
            Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO " + table
                      + "(event, world, player, x, y, z, item, time, comment) VALUES "
                      + "(?, ?, ?, ?, ?, ?, ?, ?, ?)");
            stmt.setString(1, eventType.name());
            stmt.setString(2, worldName);
            stmt.setString(3, player != null ? player.getName() : "");
            stmt.setInt(4, pos.getBlockX());
            stmt.setInt(5, pos.getBlockY());
            stmt.setInt(6, pos.getBlockZ());
            stmt.setInt(7, item);
            stmt.setInt(8, (int)(System.currentTimeMillis() / 1000));
            stmt.setString(9, comment);
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to log blacklist event to database: " + e.getMessage());
        }
    }

    @Override
    public void logEvent(BlacklistEvent event, String comment) {
        logEvent(event.getEventType(), event.getPlayer(), event.getLoggedPosition(), event.getTarget().getTypeId(), comment);
    }

    @Override
    public void close() {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
            }
        } catch (SQLException ignore) {

        }
    }

}
