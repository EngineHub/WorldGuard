// $Id$
/*
 * WorldGuard
 * Copyright (C) 2010 sk89q <http://www.sk89q.com>
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

import java.util.logging.Logger;
import java.util.logging.Level;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.PreparedStatement;

/**
 *
 * @author sk89q
 */
public class DatabaseLoggerHandler implements BlacklistLoggerHandler {
    /**
     * Logger.
     */
    private static final Logger logger = Logger.getLogger("Minecraft.WorldGuard");

    /**
     * DSN.
     */
    private String dsn;
    /**
     * Username.
     */
    private String user;
    /**
     * Password.
     */
    private String pass;
    /**
     * Table.
     */
    private String table;
    /**
     * Database connection.
     */
    private Connection conn;

    /**
     * Construct the object.
     * 
     * @param dsn
     * @param user
     * @param pass
     */
    public DatabaseLoggerHandler(String dsn, String user, String pass, String table) {
        this.dsn = dsn;
        this.user = user;
        this.pass = pass;
        this.table = table;
    }

    /**
     * Gets the database connection.
     * 
     * @return
     * @throws SQLException
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
     * @param event
     * @param name
     * @param x
     * @param y
     * @param z
     * @param item
     */
    private void logEvent(String event, String name, int x, int y, int z, int item) {
        try {
            Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO " + table
                  + "(event, player, x, y, z, item, time) VALUES "
                  + "(?, ?, ?, ?, ?, ?, ?)");
            stmt.setString(1, event);
            stmt.setString(2, name);
            stmt.setInt(3, x);
            stmt.setInt(4, y);
            stmt.setInt(5, z);
            stmt.setInt(6, item);
            stmt.setInt(7, (int)(System.currentTimeMillis() / 1000));
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to log blacklist event to database: "
                    + e.getMessage());
        }
    }
    
    /**
     * Log a block destroy attempt.
     *
     * @param player
     * @param block
     */
    public void logDestroyAttempt(Player player, Block block) {
        logEvent("DESTROY", player.getName(),
                block.getX(), block.getY(), block.getZ(), block.getType());
    }

    /**
     * Log a right click on attempt.
     *
     * @param player
     * @param block
     */
    public void logUseAttempt(Player player, Block block) {
        logEvent("USE", player.getName(),
                block.getX(), block.getY(), block.getZ(), block.getType());
    }
    
    /**
     * Right a left click attempt.
     *
     * @param player
     * @param item
     */
    public void logDestroyWithAttempt(Player player, int item) {
        logEvent("DESTROY_WITH", player.getName(),
                (int)Math.floor(player.getX()), (int)Math.floor(player.getY()),
                (int)Math.floor(player.getZ()), item);
    }

    /**
     * Log a right click attempt.
     *
     * @param player
     * @param item
     */
    public void logCreateAttempt(Player player, int item) {
        logEvent("CREATE", player.getName(),
                (int)Math.floor(player.getX()), (int)Math.floor(player.getY()),
                (int)Math.floor(player.getZ()), item);
    }

    /**
     * Log a drop attempt.
     *
     * @param player
     * @param item
     */
    public void logDropAttempt(Player player, int item) {
        logEvent("DROP", player.getName(),
                (int)Math.floor(player.getX()), (int)Math.floor(player.getY()),
                (int)Math.floor(player.getZ()), item);
    }

    /**
     * Close the connection.
     */
    public void close() {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
            }
        } catch (SQLException e) {
            
        }
    }
}
