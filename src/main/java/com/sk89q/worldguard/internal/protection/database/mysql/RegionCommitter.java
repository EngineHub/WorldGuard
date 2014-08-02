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

package com.sk89q.worldguard.internal.protection.database.mysql;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.databases.ProtectionDatabaseException;
import com.sk89q.worldguard.protection.databases.RegionDBUtil;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.regions.GlobalProtectedRegion;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import static com.google.common.base.Preconditions.checkNotNull;

class RegionCommitter extends AbstractJob implements Callable<Object> {

    private final Map<String, ProtectedRegion> regions;
    private final int worldId;

    RegionCommitter(MySQLDatabaseImpl database, Connection conn, Map<String, ProtectedRegion> regions) {
        super(database, conn);
        checkNotNull(regions);
        this.regions = regions;
        this.worldId = database.getWorldId();
    }

    @Override
    public Object call() throws SQLException, ProtectionDatabaseException {

        /*
        * As we don't get notified on the creation/removal of regions:
        *  1) We get a list of all of the in-database regions
        *  2) We iterate over all of the in-memory regions
        *  2a) If the region is in the database, we update the database and
        *      remove the region from the in-database list
        *   b) If the region is not in the database, we insert it
        *  3) We iterate over what remains of the in-database list and remove
        *     them from the database
        *
        * TODO: Look at adding/removing/updating the database when the in
        *       memory region is created/remove/updated
        *
        * @see com.sk89q.worldguard.protection.databases.ProtectionDatabase#save()
        */

        List<String> regionsInDatabase = new ArrayList<String>();

        PreparedStatement getAllRegionsStatement = null;
        ResultSet getAllRegionsResult = null;
        try {
            getAllRegionsStatement = this.conn.prepareStatement(
                    "SELECT `region`.`id` FROM " +
                            "`" + config.sqlTablePrefix + "region` AS `region` " +
                            "WHERE `world_id` = ? "
            );

            getAllRegionsStatement.setInt(1, this.worldId);
            getAllRegionsResult = getAllRegionsStatement.executeQuery();

            while(getAllRegionsResult.next()) {
                regionsInDatabase.add(getAllRegionsResult.getString("id"));
            }
        } catch (SQLException ex) {
            logger.warning("Could not get region list for save comparison: " + ex.getMessage());
        } finally {
            closeQuietly(getAllRegionsResult);
            closeQuietly(getAllRegionsStatement);
        }

        for (Map.Entry<String, ProtectedRegion> entry : regions.entrySet()) {
            String name = entry.getKey();
            ProtectedRegion region = entry.getValue();

            try {
                if (regionsInDatabase.contains(name)) {
                    regionsInDatabase.remove(name);

                    if (region instanceof ProtectedCuboidRegion) {
                        updateRegionCuboid( (ProtectedCuboidRegion) region );
                    } else if (region instanceof ProtectedPolygonalRegion) {
                        updateRegionPoly2D( (ProtectedPolygonalRegion) region );
                    } else if (region instanceof GlobalProtectedRegion) {
                        updateRegionGlobal( (GlobalProtectedRegion) region );
                    } else {
                        this.updateRegion(region, region.getClass().getCanonicalName());
                    }
                } else {
                    if (region instanceof ProtectedCuboidRegion) {
                        insertRegionCuboid( (ProtectedCuboidRegion) region );
                    } else if (region instanceof ProtectedPolygonalRegion) {
                        insertRegionPoly2D( (ProtectedPolygonalRegion) region );
                    } else if (region instanceof GlobalProtectedRegion) {
                        insertRegionGlobal( (GlobalProtectedRegion) region );
                    } else {
                        this.insertRegion(region, region.getClass().getCanonicalName());
                    }
                }
            } catch (SQLException ex) {
                logger.warning("Could not save region " + region.getId().toLowerCase() + ": " + ex.getMessage());
                throw new ProtectionDatabaseException(ex);
            }
        }

        for (Map.Entry<String, ProtectedRegion> entry : regions.entrySet()) {
            PreparedStatement setParentStatement = null;
            try {
                if (entry.getValue().getParent() == null) continue;

                setParentStatement = this.conn.prepareStatement(
                        "UPDATE `" + config.sqlTablePrefix + "region` SET " +
                                "`parent` = ? " +
                                "WHERE `id` = ? AND `world_id` = " + this.worldId
                );

                setParentStatement.setString(1, entry.getValue().getParent().getId().toLowerCase());
                setParentStatement.setString(2, entry.getValue().getId().toLowerCase());

                setParentStatement.execute();
            } catch (SQLException ex) {
                logger.warning("Could not save region parents " + entry.getValue().getId().toLowerCase() + ": " + ex.getMessage());
                throw new ProtectionDatabaseException(ex);
            } finally {
                closeQuietly(setParentStatement);
            }
        }

        for (String name : regionsInDatabase) {
            PreparedStatement removeRegion = null;
            try {
                removeRegion = this.conn.prepareStatement(
                        "DELETE FROM `" + config.sqlTablePrefix + "region` WHERE `id` = ? "
                );

                removeRegion.setString(1, name);
                removeRegion.execute();
            } catch (SQLException ex) {
                logger.warning("Could not remove region from database " + name + ": " + ex.getMessage());
            } finally {
                closeQuietly(removeRegion);
            }
        }

        return null;
    }

    /*
     * Returns the database id for the user
     * If it doesn't exits it adds the user and returns the id.
     */
    private Map<String,Integer> getUserIds(String... usernames) {
        Map<String,Integer> users = new HashMap<String,Integer>();

        if (usernames.length < 1) return users;

        ResultSet findUsersResults = null;
        PreparedStatement insertUserStatement = null;
        PreparedStatement findUsersStatement = null;
        try {
            findUsersStatement = this.conn.prepareStatement(
                    String.format(
                            "SELECT " +
                                    "`user`.`id`, " +
                                    "`user`.`name` " +
                                    "FROM `" + config.sqlTablePrefix + "user` AS `user` " +
                                    "WHERE `name` IN (%s)",
                            RegionDBUtil.preparePlaceHolders(usernames.length)
                    )
            );

            RegionDBUtil.setValues(findUsersStatement, usernames);

            findUsersResults = findUsersStatement.executeQuery();

            while(findUsersResults.next()) {
                users.put(findUsersResults.getString("name"), findUsersResults.getInt("id"));
            }

            insertUserStatement = this.conn.prepareStatement(
                    "INSERT INTO " +
                            "`" + config.sqlTablePrefix + "user` ( " +
                            "`id`, " +
                            "`name`" +
                            ") VALUES (null, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );

            for (String username : usernames) {
                if (!users.containsKey(username)) {
                    insertUserStatement.setString(1, username);
                    insertUserStatement.execute();
                    ResultSet generatedKeys = insertUserStatement.getGeneratedKeys();
                    if (generatedKeys.first()) {
                        users.put(username, generatedKeys.getInt(1));
                    } else {
                        logger.warning("Could not get the database id for user " + username);
                    }
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            logger.warning("Could not get the database id for the users " + usernames.toString() + "\n\t" + ex.getMessage());
            Throwable t = ex.getCause();
            while (t != null) {
                logger.warning(t.getMessage());
                t = t.getCause();
            }
        } finally {
            closeQuietly(findUsersResults);
            closeQuietly(findUsersStatement);
            closeQuietly(insertUserStatement);
        }

        return users;
    }

    /*
     * Returns the database id for the groups
     * If it doesn't exits it adds the group and returns the id.
     */
    private Map<String,Integer> getGroupIds(String... groupnames) {
        Map<String,Integer> groups = new HashMap<String,Integer>();

        if (groupnames.length < 1) return groups;

        PreparedStatement findGroupsStatement = null;
        ResultSet findGroupsResults = null;
        PreparedStatement insertGroupStatement = null;
        try {
            findGroupsStatement = this.conn.prepareStatement(
                    String.format(
                            "SELECT " +
                                    "`group`.`id`, " +
                                    "`group`.`name` " +
                                    "FROM `" + config.sqlTablePrefix + "group` AS `group` " +
                                    "WHERE `name` IN (%s)",
                            RegionDBUtil.preparePlaceHolders(groupnames.length)
                    )
            );

            RegionDBUtil.setValues(findGroupsStatement, groupnames);

            findGroupsResults = findGroupsStatement.executeQuery();

            while(findGroupsResults.next()) {
                groups.put(findGroupsResults.getString("name"), findGroupsResults.getInt("id"));
            }

            insertGroupStatement = this.conn.prepareStatement(
                    "INSERT INTO " +
                            "`" + config.sqlTablePrefix + "group` ( " +
                            "`id`, " +
                            "`name`" +
                            ") VALUES (null, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );

            for (String groupname : groupnames) {
                if (!groups.containsKey(groupname)) {
                    insertGroupStatement.setString(1, groupname);
                    insertGroupStatement.execute();
                    ResultSet generatedKeys = insertGroupStatement.getGeneratedKeys();
                    if (generatedKeys.first()) {
                        groups.put(groupname, generatedKeys.getInt(1));
                    } else {
                        logger.warning("Could not get the database id for user " + groupname);
                    }
                }
            }
        } catch (SQLException ex) {
            logger.warning("Could not get the database id for the groups " + groupnames.toString() + ex.getMessage());
        } finally {
            closeQuietly(findGroupsResults);
            closeQuietly(findGroupsStatement);
            closeQuietly(insertGroupStatement);
        }

        return groups;
    }

    private void updateFlags(ProtectedRegion region) throws SQLException {
        PreparedStatement clearCurrentFlagStatement = null;
        try {
            clearCurrentFlagStatement = this.conn.prepareStatement(
                    "DELETE FROM `" + config.sqlTablePrefix + "region_flag` " +
                            "WHERE `region_id` = ? " +
                            "AND `world_id` = " + this.worldId
            );

            clearCurrentFlagStatement.setString(1, region.getId().toLowerCase());
            clearCurrentFlagStatement.execute();

            for (Map.Entry<Flag<?>, Object> entry : region.getFlags().entrySet()) {
                if (entry.getValue() == null) continue;

                Object flag = sqlMarshal(marshalFlag(entry.getKey(), entry.getValue()));

                PreparedStatement insertFlagStatement = null;
                try {
                    insertFlagStatement = this.conn.prepareStatement(
                            "INSERT INTO `" + config.sqlTablePrefix + "region_flag` ( " +
                                    "`id`, " +
                                    "`region_id`, " +
                                    "`world_id`, " +
                                    "`flag`, " +
                                    "`value` " +
                                    ") VALUES (null, ?, " + this.worldId + ", ?, ?)"
                    );

                    insertFlagStatement.setString(1, region.getId().toLowerCase());
                    insertFlagStatement.setString(2, entry.getKey().getName());
                    insertFlagStatement.setObject(3, flag);

                    insertFlagStatement.execute();
                } finally {
                    closeQuietly(insertFlagStatement);
                }
            }
        } finally {
            closeQuietly(clearCurrentFlagStatement);
        }
    }

    private void updatePlayerAndGroups(ProtectedRegion region, Boolean owners) throws SQLException {
        DefaultDomain domain;

        if (owners) {
            domain = region.getOwners();
        } else {
            domain = region.getMembers();
        }

        PreparedStatement deleteUsersForRegion = null;
        PreparedStatement insertUsersForRegion = null;
        PreparedStatement deleteGroupsForRegion = null;
        PreparedStatement insertGroupsForRegion = null;

        try {
            deleteUsersForRegion = this.conn.prepareStatement(
                    "DELETE FROM `" + config.sqlTablePrefix + "region_players` " +
                            "WHERE `region_id` = ? " +
                            "AND `world_id` = " + this.worldId + " " +
                            "AND `owner` = ?"
            );

            deleteUsersForRegion.setString(1, region.getId().toLowerCase());
            deleteUsersForRegion.setBoolean(2, owners);
            deleteUsersForRegion.execute();

            insertUsersForRegion = this.conn.prepareStatement(
                    "INSERT INTO `" + config.sqlTablePrefix + "region_players` " +
                            "(`region_id`, `world_id`, `user_id`, `owner`) " +
                            "VALUES (?, " + this.worldId + ",  ?, ?)"
            );

            Set<String> var = domain.getPlayers();

            for (Integer player : getUserIds(var.toArray(new String[var.size()])).values()) {
                insertUsersForRegion.setString(1, region.getId().toLowerCase());
                insertUsersForRegion.setInt(2, player);
                insertUsersForRegion.setBoolean(3, owners);

                insertUsersForRegion.execute();
            }

            deleteGroupsForRegion = this.conn.prepareStatement(
                    "DELETE FROM `" + config.sqlTablePrefix + "region_groups` " +
                            "WHERE `region_id` = ? " +
                            "AND `world_id` = " + this.worldId + " " +
                            "AND `owner` = ?"
            );

            deleteGroupsForRegion.setString(1, region.getId().toLowerCase());
            deleteGroupsForRegion.setBoolean(2, owners);
            deleteGroupsForRegion.execute();

            insertGroupsForRegion = this.conn.prepareStatement(
                    "INSERT INTO `" + config.sqlTablePrefix + "region_groups` " +
                            "(`region_id`, `world_id`, `group_id`, `owner`) " +
                            "VALUES (?, " + this.worldId + ",  ?, ?)"
            );

            Set<String> groupVar = domain.getGroups();
            for (Integer group : getGroupIds(groupVar.toArray(new String[groupVar.size()])).values()) {
                insertGroupsForRegion.setString(1, region.getId().toLowerCase());
                insertGroupsForRegion.setInt(2, group);
                insertGroupsForRegion.setBoolean(3, owners);

                insertGroupsForRegion.execute();
            }
        } finally {
            closeQuietly(deleteGroupsForRegion);
            closeQuietly(deleteUsersForRegion);
            closeQuietly(insertGroupsForRegion);
            closeQuietly(insertUsersForRegion);
        }
    }

    @SuppressWarnings("unchecked")
    private <V> Object marshalFlag(Flag<V> flag, Object val) {
        return flag.marshal( (V) val );
    }

    private void insertRegion(ProtectedRegion region, String type) throws SQLException {
        PreparedStatement insertRegionStatement = null;
        try {
            insertRegionStatement = this.conn.prepareStatement(
                    "INSERT INTO `" + config.sqlTablePrefix + "region` (" +
                            "`id`, " +
                            "`world_id`, " +
                            "`type`, " +
                            "`priority`, " +
                            "`parent` " +
                            ") VALUES (?, ?, ?, ?, null)"
            );

            insertRegionStatement.setString(1, region.getId().toLowerCase());
            insertRegionStatement.setInt(2, this.worldId);
            insertRegionStatement.setString(3, type);
            insertRegionStatement.setInt(4, region.getPriority());

            insertRegionStatement.execute();
        } finally {
            closeQuietly(insertRegionStatement);
        }

        updateFlags(region);

        updatePlayerAndGroups(region, false);
        updatePlayerAndGroups(region, true);
    }

    private void insertRegionCuboid(ProtectedCuboidRegion region) throws SQLException {
        insertRegion(region, "cuboid");

        PreparedStatement insertCuboidRegionStatement = null;
        try {
            insertCuboidRegionStatement = this.conn.prepareStatement(
                    "INSERT INTO `" + config.sqlTablePrefix + "region_cuboid` (" +
                            "`region_id`, " +
                            "`world_id`, " +
                            "`min_z`, " +
                            "`min_y`, " +
                            "`min_x`, " +
                            "`max_z`, " +
                            "`max_y`, " +
                            "`max_x` " +
                            ") VALUES (?, " + this.worldId + ", ?, ?, ?, ?, ?, ?)"
            );

            BlockVector min = region.getMinimumPoint();
            BlockVector max = region.getMaximumPoint();

            insertCuboidRegionStatement.setString(1, region.getId().toLowerCase());
            insertCuboidRegionStatement.setInt(2, min.getBlockZ());
            insertCuboidRegionStatement.setInt(3, min.getBlockY());
            insertCuboidRegionStatement.setInt(4, min.getBlockX());
            insertCuboidRegionStatement.setInt(5, max.getBlockZ());
            insertCuboidRegionStatement.setInt(6, max.getBlockY());
            insertCuboidRegionStatement.setInt(7, max.getBlockX());

            insertCuboidRegionStatement.execute();
        } finally {
            closeQuietly(insertCuboidRegionStatement);
        }
    }

    private void insertRegionPoly2D(ProtectedPolygonalRegion region) throws SQLException {
        insertRegion(region, "poly2d");

        PreparedStatement insertPoly2dRegionStatement = null;
        try {
            insertPoly2dRegionStatement = this.conn.prepareStatement(
                    "INSERT INTO `" + config.sqlTablePrefix + "region_poly2d` (" +
                            "`region_id`, " +
                            "`world_id`, " +
                            "`max_y`, " +
                            "`min_y` " +
                            ") VALUES (?, " + this.worldId + ", ?, ?)"
            );

            insertPoly2dRegionStatement.setString(1, region.getId().toLowerCase());
            insertPoly2dRegionStatement.setInt(2, region.getMaximumPoint().getBlockY());
            insertPoly2dRegionStatement.setInt(3, region.getMinimumPoint().getBlockY());

            insertPoly2dRegionStatement.execute();
        } finally {
            closeQuietly(insertPoly2dRegionStatement);
        }

        updatePoly2dPoints(region);
    }

    private void updatePoly2dPoints(ProtectedPolygonalRegion region) throws SQLException {
        PreparedStatement clearPoly2dPointsForRegionStatement = null;
        PreparedStatement insertPoly2dPointStatement = null;

        try {
            clearPoly2dPointsForRegionStatement = this.conn.prepareStatement(
                    "DELETE FROM `" + config.sqlTablePrefix + "region_poly2d_point` " +
                            "WHERE `region_id` = ? " +
                            "AND `world_id` = " + this.worldId
            );

            clearPoly2dPointsForRegionStatement.setString(1, region.getId().toLowerCase());

            clearPoly2dPointsForRegionStatement.execute();

            insertPoly2dPointStatement = this.conn.prepareStatement(
                    "INSERT INTO `" + config.sqlTablePrefix + "region_poly2d_point` (" +
                            "`id`, " +
                            "`region_id`, " +
                            "`world_id`, " +
                            "`z`, " +
                            "`x` " +
                            ") VALUES (null, ?, " + this.worldId + ", ?, ?)"
            );

            String lowerId = region.getId().toLowerCase();
            for (BlockVector2D point : region.getPoints()) {
                insertPoly2dPointStatement.setString(1, lowerId);
                insertPoly2dPointStatement.setInt(2, point.getBlockZ());
                insertPoly2dPointStatement.setInt(3, point.getBlockX());

                insertPoly2dPointStatement.execute();
            }
        } finally {
            closeQuietly(clearPoly2dPointsForRegionStatement);
            closeQuietly(insertPoly2dPointStatement);
        }
    }

    private void insertRegionGlobal(GlobalProtectedRegion region) throws SQLException {
        insertRegion(region, "global");
    }

    private void updateRegion(ProtectedRegion region, String type) throws SQLException  {
        PreparedStatement updateRegionStatement = null;
        try {
            updateRegionStatement = this.conn.prepareStatement(
                    "UPDATE `" + config.sqlTablePrefix + "region` SET " +
                            "`priority` = ? WHERE `id` = ? AND `world_id` = " + this.worldId
            );

            updateRegionStatement.setInt(1, region.getPriority());
            updateRegionStatement.setString(2, region.getId().toLowerCase());

            updateRegionStatement.execute();
        } finally {
            closeQuietly(updateRegionStatement);
        }

        updateFlags(region);

        updatePlayerAndGroups(region, false);
        updatePlayerAndGroups(region, true);
    }

    private void updateRegionCuboid(ProtectedCuboidRegion region) throws SQLException  {
        updateRegion(region, "cuboid");

        PreparedStatement updateCuboidRegionStatement = null;
        try {
            updateCuboidRegionStatement = this.conn.prepareStatement(
                    "UPDATE `" + config.sqlTablePrefix + "region_cuboid` SET " +
                            "`min_z` = ?, " +
                            "`min_y` = ?, " +
                            "`min_x` = ?, " +
                            "`max_z` = ?, " +
                            "`max_y` = ?, " +
                            "`max_x` = ? " +
                            "WHERE `region_id` = ? " +
                            "AND `world_id` = " + this.worldId
            );

            BlockVector min = region.getMinimumPoint();
            BlockVector max = region.getMaximumPoint();

            updateCuboidRegionStatement.setInt(1, min.getBlockZ());
            updateCuboidRegionStatement.setInt(2, min.getBlockY());
            updateCuboidRegionStatement.setInt(3, min.getBlockX());
            updateCuboidRegionStatement.setInt(4, max.getBlockZ());
            updateCuboidRegionStatement.setInt(5, max.getBlockY());
            updateCuboidRegionStatement.setInt(6, max.getBlockX());
            updateCuboidRegionStatement.setString(7, region.getId().toLowerCase());

            updateCuboidRegionStatement.execute();
        } finally {
            closeQuietly(updateCuboidRegionStatement);
        }
    }

    private void updateRegionPoly2D(ProtectedPolygonalRegion region) throws SQLException  {
        updateRegion(region, "poly2d");

        PreparedStatement updatePoly2dRegionStatement = null;
        try {
            updatePoly2dRegionStatement = this.conn.prepareStatement(
                    "UPDATE `" + config.sqlTablePrefix + "region_poly2d` SET " +
                            "`max_y` = ?, " +
                            "`min_y` = ? " +
                            "WHERE `region_id` = ? " +
                            "AND `world_id` = " + this.worldId
            );

            updatePoly2dRegionStatement.setInt(1, region.getMaximumPoint().getBlockY());
            updatePoly2dRegionStatement.setInt(2, region.getMinimumPoint().getBlockY());
            updatePoly2dRegionStatement.setString(3, region.getId().toLowerCase());

            updatePoly2dRegionStatement.execute();
        } finally {
            closeQuietly(updatePoly2dRegionStatement);
        }
        updatePoly2dPoints(region);
    }

    private void updateRegionGlobal(GlobalProtectedRegion region) throws SQLException {
        updateRegion(region, "global");
    }

}
