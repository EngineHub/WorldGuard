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
import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.regions.GlobalProtectedRegion;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion.CircularInheritanceException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;

class RegionLoader extends AbstractJob implements Callable<Map<String, ProtectedRegion>> {

    /*
        ========= Everything below is a nightmare. =========
     */

    private final int worldId;

    private Map<String, ProtectedRegion> cuboidRegions;
    private Map<String, ProtectedRegion> poly2dRegions;
    private Map<String, ProtectedRegion> globalRegions;
    private Map<ProtectedRegion, String> parentSets;

    RegionLoader(MySQLDatabaseImpl database, Connection conn) {
        super(database, conn);
        this.worldId = database.getWorldId();
    }

    @Override
    public Map<String, ProtectedRegion> call() {
        parentSets = new HashMap<ProtectedRegion, String>();

        // We load the cuboid regions first, as this is likely to be the
        // largest dataset. This should save time in regards to the putAll()s
        this.loadCuboid();
        Map<String, ProtectedRegion> regions = this.cuboidRegions;
        this.cuboidRegions = null;

        this.loadPoly2d();
        regions.putAll(this.poly2dRegions);
        this.poly2dRegions = null;

        this.loadGlobal();
        regions.putAll(this.globalRegions);
        this.globalRegions = null;

        // Relink parents // Taken verbatim from YAMLDatabase
        for (Map.Entry<ProtectedRegion, String> entry : parentSets.entrySet()) {
            ProtectedRegion parent = regions.get(entry.getValue());
            if (parent != null) {
                try {
                    entry.getKey().setParent(parent);
                } catch (CircularInheritanceException e) {
                    logger.warning("Circular inheritance detect with '"
                            + entry.getValue() + "' detected as a parent");
                }
            } else {
                logger.warning("Unknown region parent: " + entry.getValue());
            }
        }

        return regions;
    }

    private void loadFlags(ProtectedRegion region) {
        // @TODO: Iterate _ONCE_
        PreparedStatement flagsStatement = null;
        ResultSet flagsResultSet = null;
        try {
            flagsStatement = this.conn.prepareStatement(
                    "SELECT " +
                            "`region_flag`.`flag`, " +
                            "`region_flag`.`value` " +
                            "FROM `" + config.sqlTablePrefix + "region_flag` AS `region_flag` " +
                            "WHERE `region_id` = ? " +
                            "AND `world_id` = " + this.worldId
            );

            flagsStatement.setString(1, region.getId().toLowerCase());
            flagsResultSet = flagsStatement.executeQuery();

            Map<String, Object> regionFlags = new HashMap<String, Object>();
            while (flagsResultSet.next()) {
                regionFlags.put(
                        flagsResultSet.getString("flag"),
                        sqlUnmarshal(flagsResultSet.getString("value"))
                );
            }

            // @TODO: Make this better
            for (Flag<?> flag : DefaultFlag.getFlags()) {
                Object o = regionFlags.get(flag.getName());
                if (o != null) {
                    setFlag(region, flag, o);
                }
            }
        } catch (SQLException ex) {
            logger.warning(
                    "Unable to load flags for region "
                            + region.getId().toLowerCase() + ": " + ex.getMessage()
            );
        } finally {
            closeQuietly(flagsResultSet);
            closeQuietly(flagsStatement);
        }
    }

    private <T> void setFlag(ProtectedRegion region, Flag<T> flag, Object rawValue) {
        T val = flag.unmarshal(rawValue);
        if (val == null) {
            logger.warning("Failed to parse flag '" + flag.getName() + "' with value '" + rawValue + "'");
            return;
        }
        region.setFlag(flag, val);
    }

    private void loadOwnersAndMembers(ProtectedRegion region) {
        DefaultDomain owners = new DefaultDomain();
        DefaultDomain members = new DefaultDomain();

        ResultSet userSet = null;
        PreparedStatement usersStatement = null;
        try {
            usersStatement = this.conn.prepareStatement(
                    "SELECT " +
                            "`user`.`name`, `user`.`uuid`, " +
                            "`region_players`.`owner` " +
                            "FROM `" + config.sqlTablePrefix + "region_players` AS `region_players` " +
                            "LEFT JOIN `" + config.sqlTablePrefix + "user` AS `user` ON ( " +
                            "`region_players`.`user_id` = " +
                            "`user`.`id`) " +
                            "WHERE `region_players`.`region_id` = ? " +
                            "AND `region_players`.`world_id` = " + this.worldId
            );

            usersStatement.setString(1, region.getId().toLowerCase());
            userSet = usersStatement.executeQuery();
            while (userSet.next()) {
                DefaultDomain domain;
                if (userSet.getBoolean("owner")) {
                    domain = owners;
                } else {
                    domain = members;
                }

                String name = userSet.getString("name");
                String uuid = userSet.getString("uuid");
                if (name != null) {
                    domain.addPlayer(name);
                } else if (uuid != null) {
                    try {
                        domain.addPlayer(UUID.fromString(uuid));
                    } catch (IllegalArgumentException e) {
                        logger.warning("Invalid UUID in database: " + uuid);
                    }
                }
            }
        } catch (SQLException ex) {
            logger.warning("Unable to load users for region " + region.getId().toLowerCase() + ": " + ex.getMessage());
        } finally {
            closeQuietly(userSet);
            closeQuietly(usersStatement);
        }

        PreparedStatement groupsStatement = null;
        ResultSet groupSet = null;
        try {
            groupsStatement = this.conn.prepareStatement(
                    "SELECT " +
                            "`group`.`name`, " +
                            "`region_groups`.`owner` " +
                            "FROM `" + config.sqlTablePrefix + "region_groups` AS `region_groups` " +
                            "LEFT JOIN `" + config.sqlTablePrefix + "group` AS `group` ON ( " +
                            "`region_groups`.`group_id` = " +
                            "`group`.`id`) " +
                            "WHERE `region_groups`.`region_id` = ? " +
                            "AND `region_groups`.`world_id` = " + this.worldId
            );

            groupsStatement.setString(1, region.getId().toLowerCase());
            groupSet = groupsStatement.executeQuery();
            while (groupSet.next()) {
                if (groupSet.getBoolean("owner")) {
                    owners.addGroup(groupSet.getString("name"));
                } else {
                    members.addGroup(groupSet.getString("name"));
                }
            }
        } catch (SQLException ex) {
            logger.warning("Unable to load groups for region " + region.getId().toLowerCase() + ": " + ex.getMessage());
        } finally {
            closeQuietly(groupSet);
            closeQuietly(groupsStatement);
        }

        region.setOwners(owners);
        region.setMembers(members);
    }

    private void loadGlobal() {
        Map<String, ProtectedRegion> regions =
                new HashMap<String, ProtectedRegion>();

        PreparedStatement globalRegionStatement = null;
        ResultSet globalResultSet = null;
        try {
            globalRegionStatement = this.conn.prepareStatement(
                    "SELECT " +
                            "`region`.`id`, " +
                            "`region`.`priority`, " +
                            "`parent`.`id` AS `parent` " +
                            "FROM `" + config.sqlTablePrefix + "region` AS `region` " +
                            "LEFT JOIN `" + config.sqlTablePrefix + "region` AS `parent` " +
                            "ON (`region`.`parent` = `parent`.`id` " +
                            "AND `region`.`world_id` = `parent`.`world_id`) " +
                            "WHERE `region`.`type` = 'global' " +
                            "AND `region`.`world_id` = ? "
            );

            globalRegionStatement.setInt(1, this.worldId);
            globalResultSet = globalRegionStatement.executeQuery();

            while (globalResultSet.next()) {
                ProtectedRegion region = new GlobalProtectedRegion(globalResultSet.getString("id"));

                region.setPriority(globalResultSet.getInt("priority"));

                this.loadFlags(region);
                this.loadOwnersAndMembers(region);

                regions.put(globalResultSet.getString("id"), region);

                String parentId = globalResultSet.getString("parent");
                if (parentId != null) {
                    parentSets.put(region, parentId);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            logger.warning("Unable to load regions from sql database: " + ex.getMessage());
            Throwable t = ex.getCause();
            while (t != null) {
                logger.warning("\t\tCause: " + t.getMessage());
                t = t.getCause();
            }
        } finally {
            closeQuietly(globalResultSet);
            closeQuietly(globalRegionStatement);
        }

        globalRegions = regions;
    }

    private void loadCuboid() {
        Map<String, ProtectedRegion> regions = new HashMap<String, ProtectedRegion>();

        PreparedStatement cuboidRegionStatement = null;
        ResultSet cuboidResultSet = null;
        try {
            cuboidRegionStatement = this.conn.prepareStatement(
                    "SELECT " +
                            "`region_cuboid`.`min_z`, " +
                            "`region_cuboid`.`min_y`, " +
                            "`region_cuboid`.`min_x`, " +
                            "`region_cuboid`.`max_z`, " +
                            "`region_cuboid`.`max_y`, " +
                            "`region_cuboid`.`max_x`, " +
                            "`region`.`id`, " +
                            "`region`.`priority`, " +
                            "`parent`.`id` AS `parent` " +
                            "FROM `" + config.sqlTablePrefix + "region_cuboid` AS `region_cuboid` " +
                            "LEFT JOIN `" + config.sqlTablePrefix + "region` AS `region` " +
                            "ON (`region_cuboid`.`region_id` = `region`.`id` " +
                            "AND `region_cuboid`.`world_id` = `region`.`world_id`) " +
                            "LEFT JOIN `" + config.sqlTablePrefix + "region` AS `parent` " +
                            "ON (`region`.`parent` = `parent`.`id` " +
                            "AND `region`.`world_id` = `parent`.`world_id`) " +
                            "WHERE `region`.`world_id` = ? "
            );

            cuboidRegionStatement.setInt(1, this.worldId);
            cuboidResultSet = cuboidRegionStatement.executeQuery();

            while (cuboidResultSet.next()) {
                Vector pt1 = new Vector(
                        cuboidResultSet.getInt("min_x"),
                        cuboidResultSet.getInt("min_y"),
                        cuboidResultSet.getInt("min_z")
                );
                Vector pt2 = new Vector(
                        cuboidResultSet.getInt("max_x"),
                        cuboidResultSet.getInt("max_y"),
                        cuboidResultSet.getInt("max_z")
                );

                BlockVector min = Vector.getMinimum(pt1, pt2).toBlockVector();
                BlockVector max = Vector.getMaximum(pt1, pt2).toBlockVector();
                ProtectedRegion region = new ProtectedCuboidRegion(
                        cuboidResultSet.getString("id"),
                        min,
                        max
                );

                region.setPriority(cuboidResultSet.getInt("priority"));

                this.loadFlags(region);
                this.loadOwnersAndMembers(region);

                regions.put(cuboidResultSet.getString("id"), region);

                String parentId = cuboidResultSet.getString("parent");
                if (parentId != null) {
                    parentSets.put(region, parentId);
                }
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            logger.warning("Unable to load regions from sql database: " + ex.getMessage());
            Throwable t = ex.getCause();
            while (t != null) {
                logger.warning("\t\tCause: " + t.getMessage());
                t = t.getCause();
            }
        } finally {
            closeQuietly(cuboidResultSet);
            closeQuietly(cuboidRegionStatement);
        }

        cuboidRegions = regions;
    }

    private void loadPoly2d() {
        Map<String, ProtectedRegion> regions = new HashMap<String, ProtectedRegion>();

        PreparedStatement poly2dRegionStatement = null;
        ResultSet poly2dResultSet = null;
        PreparedStatement poly2dVectorStatement = null;
        try {
            poly2dRegionStatement = this.conn.prepareStatement(
                    "SELECT " +
                            "`region_poly2d`.`min_y`, " +
                            "`region_poly2d`.`max_y`, " +
                            "`region`.`id`, " +
                            "`region`.`priority`, " +
                            "`parent`.`id` AS `parent` " +
                            "FROM `" + config.sqlTablePrefix + "region_poly2d` AS `region_poly2d` " +
                            "LEFT JOIN `" + config.sqlTablePrefix + "region` AS `region` " +
                            "ON (`region_poly2d`.`region_id` = `region`.`id` " +
                            "AND `region_poly2d`.`world_id` = `region`.`world_id`) " +
                            "LEFT JOIN `" + config.sqlTablePrefix + "region` AS `parent` " +
                            "ON (`region`.`parent` = `parent`.`id` " +
                            "AND `region`.`world_id` = `parent`.`world_id`) " +
                            "WHERE `region`.`world_id` = ? "
            );

            poly2dRegionStatement.setInt(1, this.worldId);
            poly2dResultSet = poly2dRegionStatement.executeQuery();

            poly2dVectorStatement = this.conn.prepareStatement(
                    "SELECT " +
                            "`region_poly2d_point`.`x`, " +
                            "`region_poly2d_point`.`z` " +
                            "FROM `" + config.sqlTablePrefix + "region_poly2d_point` AS `region_poly2d_point` " +
                            "WHERE `region_poly2d_point`.`region_id` = ? " +
                            "AND `region_poly2d_point`.`world_id` = " + this.worldId
            );

            while (poly2dResultSet.next()) {
                String id = poly2dResultSet.getString("id");

                Integer minY = poly2dResultSet.getInt("min_y");
                Integer maxY = poly2dResultSet.getInt("max_y");
                List<BlockVector2D> points = new ArrayList<BlockVector2D>();

                poly2dVectorStatement.setString(1, id);
                ResultSet poly2dVectorResultSet = poly2dVectorStatement.executeQuery();

                while (poly2dVectorResultSet.next()) {
                    points.add(new BlockVector2D(
                            poly2dVectorResultSet.getInt("x"),
                            poly2dVectorResultSet.getInt("z")
                    ));
                }

                if (points.size() < 3) {
                    logger.warning(String.format("Invalid polygonal region '%s': region only has %d point(s). Ignoring.", id, points.size()));
                    continue;
                }

                closeQuietly(poly2dVectorResultSet);

                ProtectedRegion region = new ProtectedPolygonalRegion(id, points, minY, maxY);

                region.setPriority(poly2dResultSet.getInt("priority"));

                this.loadFlags(region);
                this.loadOwnersAndMembers(region);

                regions.put(poly2dResultSet.getString("id"), region);

                String parentId = poly2dResultSet.getString("parent");
                if (parentId != null) {
                    parentSets.put(region, parentId);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            logger.warning("Unable to load regions from sql database: " + ex.getMessage());
            Throwable t = ex.getCause();
            while (t != null) {
                logger.warning("\t\tCause: " + t.getMessage());
                t = t.getCause();
            }
        } finally {
            closeQuietly(poly2dResultSet);
            closeQuietly(poly2dRegionStatement);
            closeQuietly(poly2dVectorStatement);
        }

        poly2dRegions = regions;
    }

}
