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

package com.sk89q.worldguard.region.stores;

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
import com.sk89q.worldguard.region.flags.DefaultFlag;
import com.sk89q.worldguard.region.flags.Flag;
import com.sk89q.worldguard.region.shapes.Cuboid;
import com.sk89q.worldguard.region.shapes.ExtrudedPolygon;
import com.sk89q.worldguard.region.shapes.GlobalProtectedRegion;
import com.sk89q.worldguard.region.shapes.Region;
import com.sk89q.worldguard.region.shapes.Region.CircularInheritanceException;

public class YamlStore extends AbstractProtectionDatabase {
    
    private YAMLProcessor config;
    private Map<String, Region> regions;
    private final Logger logger;
    
    public YamlStore(File file, Logger logger) throws ProtectionDatabaseException, FileNotFoundException {
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
            this.regions = new HashMap<String, Region>();
            return;
        }

        Map<String,Region> regions =
            new HashMap<String,Region>();
        Map<Region,String> parentSets =
            new LinkedHashMap<Region, String>();
        
        for (Map.Entry<String, YAMLNode> entry : regionData.entrySet()) {
            String id = entry.getKey().toLowerCase().replace(".", "");
            YAMLNode node = entry.getValue();
            
            String type = node.getString("type");
            Region region;
            
            try {
                if (type == null) {
                    logger.warning("Undefined region type for region '" + id + '"');
                    continue;
                } else if (type.equals("cuboid")) {
                    Vector pt1 = checkNonNull(node.getVector("min"));
                    Vector pt2 = checkNonNull(node.getVector("max"));
                    BlockVector min = Vector.getMinimum(pt1, pt2).toBlockVector();
                    BlockVector max = Vector.getMaximum(pt1, pt2).toBlockVector();
                    region = new Cuboid(id, min, max);
                } else if (type.equals("poly2d")) {
                    Integer minY = checkNonNull(node.getInt("min-y"));
                    Integer maxY = checkNonNull(node.getInt("max-y"));
                    List<BlockVector2D> points = node.getBlockVector2dList("points", null);
                    region = new ExtrudedPolygon(id, points, minY, maxY);
                } else if (type.equals("global")) {
                    region = new GlobalProtectedRegion(id);
                } else {
                    logger.warning("Unknown region type for region '" + id + '"');
                    continue;
                }
                
                Integer priority = checkNonNull(node.getInt("priority"));
                region.setPriority(priority);
                setFlags(region, node.getNode("flags"));
                region.setOwners(parseDomain(node.getNode("owners")));
                region.setMembers(parseDomain(node.getNode("members")));
                regions.put(id, region);
                
                String parentId = node.getString("parent");
                if (parentId != null) {
                    parentSets.put(region, parentId);
                }
            } catch (NullPointerException e) {
                logger.warning("Missing data for region '" + id + '"');
            }
        }
        
        // Relink parents
        for (Map.Entry<Region, String> entry : parentSets.entrySet()) {
            Region parent = regions.get(entry.getValue());
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
    
    private void setFlags(Region region, YAMLNode flagsData) {
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
    
    private <T> void setFlag(Region region, Flag<T> flag, Object rawValue) {
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
        
        for (String name : node.getStringList("players", null)) {
            domain.addPlayer(name);
        }
        
        for (String name : node.getStringList("groups", null)) {
            domain.addGroup(name);
        }
        
        return domain;
    }

    public void save() throws ProtectionDatabaseException {
        config.clear();
        
        for (Map.Entry<String, Region> entry : regions.entrySet()) {
            Region region = entry.getValue();
            YAMLNode node = config.addNode("regions." + entry.getKey());
            
            if (region instanceof Cuboid) {
                Cuboid cuboid = (Cuboid) region;
                node.setProperty("type", "cuboid");
                node.setProperty("min", cuboid.getAABBMin());
                node.setProperty("max", cuboid.getAABBMax());
            } else if (region instanceof ExtrudedPolygon) {
                ExtrudedPolygon poly = (ExtrudedPolygon) region;
                node.setProperty("type", "poly2d");
                node.setProperty("min-y", poly.getAABBMin().getBlockY());
                node.setProperty("max-y", poly.getAABBMax().getBlockY());
                
                List<Map<String, Object>> points = new ArrayList<Map<String,Object>>();
                for (BlockVector2D point : poly.getPoints()) {
                    Map<String, Object> data = new HashMap<String, Object>();
                    data.put("x", point.getBlockX());
                    data.put("z", point.getBlockZ());
                    points.add(data);
                }
                
                node.setProperty("points", points);
            } else if (region instanceof GlobalProtectedRegion) {
                node.setProperty("type", "global");
            } else {
                node.setProperty("type", region.getClass().getCanonicalName());
            }

            node.setProperty("priority", region.getPriority());
            node.setProperty("flags", getFlagData(region));
            node.setProperty("owners", getDomainData(region.getOwners()));
            node.setProperty("members", getDomainData(region.getMembers()));
            Region parent = region.getParent();
            if (parent != null) {
                node.setProperty("parent", parent.getId());
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
    
    private Map<String, Object> getFlagData(Region region) {
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

        setDomainData(domainData, "players", domain.getPlayers());
        setDomainData(domainData, "groups", domain.getGroups());
        
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

    public Map<String, Region> getRegions() {
        return regions;
    }

    public void setRegions(Map<String, Region> regions) {
        this.regions = regions;
    }
    
}
