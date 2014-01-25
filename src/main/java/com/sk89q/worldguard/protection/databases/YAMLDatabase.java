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

package com.sk89q.worldguard.protection.databases;

import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import com.sk89q.util.yaml.YAMLFormat;
import com.sk89q.util.yaml.YAMLNode;
import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.regions.GlobalProtectedRegion;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedCylinderRegion;
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion.CircularInheritanceException;

public class YAMLDatabase extends AbstractProtectionDatabase {
    
    private static final String GROUPS = "groups";
    private static final String PLAYERS = "players";
    private static final String PARENT = "parent";
    private static final String MEMBERS = "members";
    private static final String OWNERS = "owners";
    private static final String FLAGS = "flags";
    private static final String GLOBAL = "global";
    private static final String CUBOID = "cuboid";
    private static final String CYLINDER = "cylinder";
    private static final String Z = "z";
    private static final String X = "x";
    private static final String POLY2D = "poly2d";
    private static final String TYPE = "type";
    private static final String MAX = "max";
    private static final String MIN = "min";
    private static final String POINTS = "points";
    private static final String CENTER_Z = "center-z";
    private static final String CENTER_X = "center-x";
    private static final String RADIUS_Z = "radius-z";
    private static final String MIN_Y = "min-y";
    private static final String MAX_Y = "max-y";
    private static final String RADIUS_X = "radius-x";
    
    private YAMLProcessor config;
    private Map<String, ProtectedRegion> regions;
    private final Logger logger;
    
    public YAMLDatabase(File file, Logger logger) throws ProtectionDatabaseException, FileNotFoundException {
        this.logger = logger;
        if (!file.exists()) { // shouldn't be necessary, but check anyways
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new FileNotFoundException(file.getAbsolutePath());
            }
        }
        config = new YAMLProcessor(file, false, YAMLFormat.COMPACT);
    }

    public void load() throws ProtectionDatabaseException {
        try {
            config.load();
        } catch (IOException e) {
            throw new ProtectionDatabaseException(e);
        }
        
        Map<String, YAMLNode> regionData = config.getNodes("regions");
        
        // No regions are even configured
        if (regionData == null) {
            this.regions = new HashMap<String, ProtectedRegion>();
            return;
        }

        Map<String,ProtectedRegion> regions =
            new HashMap<String,ProtectedRegion>();
        Map<ProtectedRegion,String> parentSets =
            new LinkedHashMap<ProtectedRegion, String>();
        
        for (Map.Entry<String, YAMLNode> entry : regionData.entrySet()) {
            String id = entry.getKey().toLowerCase().replace(".", "");
            YAMLNode node = entry.getValue();
            
            String type = node.getString(TYPE);
            ProtectedRegion region;
            
            try {
                if (type == null) {
                    logger.warning("Undefined region type for region '" + id + '"');
                    continue;
                } else if (type.equals(CUBOID)) {
                    Vector pt1 = checkNonNull(node.getVector(MIN));
                    Vector pt2 = checkNonNull(node.getVector(MAX));
                    BlockVector min = Vector.getMinimum(pt1, pt2).toBlockVector();
                    BlockVector max = Vector.getMaximum(pt1, pt2).toBlockVector();
                    region = new ProtectedCuboidRegion(id, min, max);
                } else if (type.equals(POLY2D)) {
                    Integer minY = checkNonNull(node.getInt(MIN_Y));
                    Integer maxY = checkNonNull(node.getInt(MAX_Y));
                    List<BlockVector2D> points = node.getBlockVector2dList(POINTS, null);
                    region = new ProtectedPolygonalRegion(id, points, minY, maxY);
                } else if (type.equals(CYLINDER)) {
                    Integer minY = checkNonNull(node.getInt(MIN_Y));
                    Integer maxY = checkNonNull(node.getInt(MAX_Y));
                    Integer radiusX = checkNonNull(node.getInt(RADIUS_X));
                    Integer radiusZ = checkNonNull(node.getInt(RADIUS_Z));
                    Integer centerX = checkNonNull(node.getInt(CENTER_X));
                    Integer centerZ = checkNonNull(node.getInt(CENTER_Z));
                    BlockVector2D center = new BlockVector2D(centerX, centerZ);
                    BlockVector2D radius = new BlockVector2D(radiusX, radiusZ);
                    region = new ProtectedCylinderRegion(id, center, radius, minY, maxY);
                } else if (type.equals(GLOBAL)) {
                    region = new GlobalProtectedRegion(id);
                } else {
                    logger.warning("Unknown region type for region '" + id + '"');
                    continue;
                }
                
                Integer priority = checkNonNull(node.getInt("priority"));
                region.setPriority(priority);
                setFlags(region, node.getNode(FLAGS));
                region.setOwners(parseDomain(node.getNode(OWNERS)));
                region.setMembers(parseDomain(node.getNode(MEMBERS)));
                regions.put(id, region);
                
                String parentId = node.getString(PARENT);
                if (parentId != null) {
                    parentSets.put(region, parentId);
                }
            } catch (NullPointerException e) {
                logger.warning("Missing data for region '" + id + '"');
            }
        }
        
        // Relink parents
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
        
        this.regions = regions;
    }
    
    private <V> V checkNonNull(V val) throws NullPointerException {
        if (val == null) {
            throw new NullPointerException();
        }
        
        return val;
    }
    
    private void setFlags(ProtectedRegion region, YAMLNode flagsData) {
        if (flagsData == null) {
            return;
        }
        
        // @TODO: Make this better
        for (Flag<?> flag : DefaultFlag.getFlags()) {
            Object o = flagsData.getProperty(flag.getName());
            if (o != null) {
                setFlag(region, flag, o);
            }
            
            if (flag.getRegionGroupFlag() != null) {
            Object o2 = flagsData.getProperty(flag.getRegionGroupFlag().getName());
                if (o2 != null) {
                    setFlag(region, flag.getRegionGroupFlag(), o2);
                }
            }
        }
    }
    
    private <T> void setFlag(ProtectedRegion region, Flag<T> flag, Object rawValue) {
        T val = flag.unmarshal(rawValue);
        if (val == null) {
            logger.warning("Failed to parse flag '" + flag.getName()
                    + "' with value '" + rawValue.toString() + "'");
            return;
        }
        region.setFlag(flag, val);
    }
    
    private DefaultDomain parseDomain(YAMLNode node) {
        if (node == null) {
            return new DefaultDomain();
        }
        
        DefaultDomain domain = new DefaultDomain();
        
        for (String name : node.getStringList(PLAYERS, null)) {
            domain.addPlayer(name);
        }
        
        for (String name : node.getStringList(GROUPS, null)) {
            domain.addGroup(name);
        }
        
        return domain;
    }

    public void save() throws ProtectionDatabaseException {
        config.clear();
        
        for (Map.Entry<String, ProtectedRegion> entry : regions.entrySet()) {
            ProtectedRegion region = entry.getValue();
            YAMLNode node = config.addNode("regions." + entry.getKey());
            
            if (region instanceof ProtectedCuboidRegion) {
                ProtectedCuboidRegion cuboid = (ProtectedCuboidRegion) region;
                node.setProperty(TYPE, CUBOID);
                node.setProperty(MIN, cuboid.getMinimumPoint());
                node.setProperty(MAX, cuboid.getMaximumPoint());
            } else if (region instanceof ProtectedCylinderRegion) {
                ProtectedCylinderRegion cylReg = (ProtectedCylinderRegion) region;
                node.setProperty(TYPE, CYLINDER);
                node.setProperty(MIN_Y, cylReg.getMinY());
                node.setProperty(MAX_Y, cylReg.getMaxY());
                node.setProperty(RADIUS_X, cylReg.getRadius().getBlockX());
                node.setProperty(RADIUS_Z, cylReg.getRadius().getBlockZ());
                node.setProperty(CENTER_X, cylReg.getCenter().getBlockX());
                node.setProperty(CENTER_Z, cylReg.getCenter().getBlockZ());
            } else if (region instanceof ProtectedPolygonalRegion) {
                ProtectedPolygonalRegion poly = (ProtectedPolygonalRegion) region;
                node.setProperty(TYPE, POLY2D);
                node.setProperty(MIN_Y, poly.getMinimumPoint().getBlockY());
                node.setProperty(MAX_Y, poly.getMaximumPoint().getBlockY());
                
                List<Map<String, Object>> points = new ArrayList<Map<String,Object>>();
                for (BlockVector2D point : poly.getPoints()) {
                    Map<String, Object> data = new HashMap<String, Object>();
                    data.put(X, point.getBlockX());
                    data.put(Z, point.getBlockZ());
                    points.add(data);
                }
                
                node.setProperty(POINTS, points);
            } else if (region instanceof GlobalProtectedRegion) {
                node.setProperty(TYPE, GLOBAL);
            } else {
                node.setProperty(TYPE, region.getClass().getCanonicalName());
            }

            node.setProperty("priority", region.getPriority());
            node.setProperty(FLAGS, getFlagData(region));
            node.setProperty(OWNERS, getDomainData(region.getOwners()));
            node.setProperty(MEMBERS, getDomainData(region.getMembers()));
            ProtectedRegion parent = region.getParent();
            if (parent != null) {
                node.setProperty(PARENT, parent.getId());
            }
        }
        
        config.setHeader("#\r\n" +
                "# WorldGuard regions file\r\n" +
                "#\r\n" +
                "# WARNING: THIS FILE IS AUTOMATICALLY GENERATED. If you modify this file by\r\n" +
                "# hand, be aware that A SINGLE MISTYPED CHARACTER CAN CORRUPT THE FILE. If\r\n" +
                "# WorldGuard is unable to parse the file, your regions will FAIL TO LOAD and\r\n" +
                "# the contents of this file will reset. Please use a YAML validator such as\r\n" +
                "# http://yaml-online-parser.appspot.com (for smaller files).\r\n" +
                "#\r\n" +
                "# REMEMBER TO KEEP PERIODICAL BACKUPS.\r\n" +
                "#");
        config.save();
    }
    
    private Map<String, Object> getFlagData(ProtectedRegion region) {
        Map<String, Object> flagData = new HashMap<String, Object>();
        
        for (Map.Entry<Flag<?>, Object> entry : region.getFlags().entrySet()) {
            Flag<?> flag = entry.getKey();
            addMarshalledFlag(flagData, flag, entry.getValue());
        }
        
        return flagData;
    }
    
    @SuppressWarnings("unchecked")
    private <V> void addMarshalledFlag(Map<String, Object> flagData,
            Flag<V> flag, Object val) {
        if (val == null) {
            return;
        }
        flagData.put(flag.getName(), flag.marshal((V) val));
    }
    
    private Map<String, Object> getDomainData(DefaultDomain domain) {
        Map<String, Object> domainData = new HashMap<String, Object>();

        setDomainData(domainData, PLAYERS, domain.getPlayers());
        setDomainData(domainData, GROUPS, domain.getGroups());
        
        return domainData;
    }
    
    private void setDomainData(Map<String, Object> domainData,
            String key, Set<String> domain) {
        if (domain.size() == 0) {
            return;
        }
        
        List<String> list = new ArrayList<String>();
        
        for (String str : domain) {
            list.add(str);
        }
        
        domainData.put(key, list);
    }

    public Map<String, ProtectedRegion> getRegions() {
        return regions;
    }

    public void setRegions(Map<String, ProtectedRegion> regions) {
        this.regions = regions;
    }
    
}
