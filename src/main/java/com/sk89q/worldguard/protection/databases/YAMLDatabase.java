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

package com.sk89q.worldguard.protection.databases;

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
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion.CircularInheritanceException;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.representer.Representer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A store that persists regions in a YAML-encoded file.
 */
public class YAMLDatabase extends AbstractAsynchronousDatabase {

    /**
     * Used to dump YAML when an error occurs
     */
    private static Yaml yaml;

    private Map<String, ProtectedRegion> regions;
    private final File file;
    private final Logger logger;

    /**
     * Create a new instance.
     *
     * @param file the file
     * @param logger a logger
     * @throws ProtectionDatabaseException
     * @throws FileNotFoundException
     */
    public YAMLDatabase(File file, Logger logger) throws ProtectionDatabaseException, FileNotFoundException {
        this.logger = logger;
        this.file = file;

        if (!file.exists()) { // shouldn't be necessary, but check anyways
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new FileNotFoundException(file.getAbsolutePath());
            }
        }
    }

    private YAMLProcessor createYamlProcessor(File file) {
        return new YAMLProcessor(file, false, YAMLFormat.COMPACT);
    }

    @Override
    public void performLoad() throws ProtectionDatabaseException {
        YAMLProcessor config = createYamlProcessor(file);

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

        // Warning for WORLDGUARD-3094
        for (Flag<?> flag : DefaultFlag.getFlags()) {
            if (flag == null) {
                logger.severe("Some 3rd-party plugin has registered an invalid 'null' custom flag with WorldGuard, though we can't tell you which plugin did it - this may cause major problems in other places");
                break;
            }
        }

        Map<String,ProtectedRegion> regions = new HashMap<String,ProtectedRegion>();
        Map<ProtectedRegion,String> parentSets = new LinkedHashMap<ProtectedRegion, String>();

        for (Map.Entry<String, YAMLNode> entry : regionData.entrySet()) {
            String id = entry.getKey().toLowerCase().replace(".", "");
            YAMLNode node = entry.getValue();

            String type = node.getString("type");
            ProtectedRegion region;

            try {
                if (type == null) {
                    logger.warning("Undefined region type for region '" + id + "'!\n" +
                            "Here is what the region data looks like:\n\n" + dumpAsYaml(entry.getValue().getMap()) + "\n");
                    continue;
                } else if (type.equals("cuboid")) {
                    Vector pt1 = checkNonNull(node.getVector("min"));
                    Vector pt2 = checkNonNull(node.getVector("max"));
                    BlockVector min = Vector.getMinimum(pt1, pt2).toBlockVector();
                    BlockVector max = Vector.getMaximum(pt1, pt2).toBlockVector();
                    region = new ProtectedCuboidRegion(id, min, max);
                } else if (type.equals("poly2d")) {
                    Integer minY = checkNonNull(node.getInt("min-y"));
                    Integer maxY = checkNonNull(node.getInt("max-y"));
                    List<BlockVector2D> points = node.getBlockVector2dList("points", null);
                    region = new ProtectedPolygonalRegion(id, points, minY, maxY);
                } else if (type.equals("global")) {
                    region = new GlobalProtectedRegion(id);
                } else {
                    logger.warning("Unknown region type for region '" + id + "'!\n" +
                            "Here is what the region data looks like:\n\n" + dumpAsYaml(entry.getValue().getMap()) + "\n");
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
                logger.log(Level.WARNING,
                        "Unexpected NullPointerException encountered during parsing for the region '" + id + "'!\n" +
                                "Here is what the region data looks like:\n\n" + dumpAsYaml(entry.getValue().getMap()) +
                                "\n\nNote: This region will disappear as a result!", e);
            }
        }

        // Relink parents
        for (Map.Entry<ProtectedRegion, String> entry : parentSets.entrySet()) {
            ProtectedRegion parent = regions.get(entry.getValue());
            if (parent != null) {
                try {
                    entry.getKey().setParent(parent);
                } catch (CircularInheritanceException e) {
                    logger.warning("Circular inheritance detect with '" + entry.getValue() + "' detected as a parent");
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
            if (flag == null) {
                // Some plugins that add custom flags to WorldGuard are doing
                // something very wrong -- see WORLDGUARD-3094
                continue;
            }

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

    @SuppressWarnings("deprecation")
    private DefaultDomain parseDomain(YAMLNode node) {
        if (node == null) {
            return new DefaultDomain();
        }

        DefaultDomain domain = new DefaultDomain();

        for (String name : node.getStringList("players", null)) {
            domain.addPlayer(name);
        }

        for (String stringId : node.getStringList("unique-ids", null)) {
            try {
                domain.addPlayer(UUID.fromString(stringId));
            } catch (IllegalArgumentException e) {
                logger.log(Level.WARNING, "Failed to parse UUID '" + stringId +"'", e);
            }
        }

        for (String name : node.getStringList("groups", null)) {
            domain.addGroup(name);
        }

        return domain;
    }

    @Override
    protected void performSave() throws ProtectionDatabaseException {
        File tempFile = new File(file.getParentFile(), file.getName() + ".tmp");
        YAMLProcessor config = createYamlProcessor(tempFile);

        config.clear();

        for (Map.Entry<String, ProtectedRegion> entry : regions.entrySet()) {
            ProtectedRegion region = entry.getValue();
            YAMLNode node = config.addNode("regions." + entry.getKey());

            if (region instanceof ProtectedCuboidRegion) {
                ProtectedCuboidRegion cuboid = (ProtectedCuboidRegion) region;
                node.setProperty("type", "cuboid");
                node.setProperty("min", cuboid.getMinimumPoint());
                node.setProperty("max", cuboid.getMaximumPoint());
            } else if (region instanceof ProtectedPolygonalRegion) {
                ProtectedPolygonalRegion poly = (ProtectedPolygonalRegion) region;
                node.setProperty("type", "poly2d");
                node.setProperty("min-y", poly.getMinimumPoint().getBlockY());
                node.setProperty("max-y", poly.getMaximumPoint().getBlockY());

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
            ProtectedRegion parent = region.getParent();
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

        file.delete();
        if (!tempFile.renameTo(file)) {
            throw new ProtectionDatabaseException("Failed to rename temporary regions file to " + file.getAbsolutePath());
        }
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
    private <V> void addMarshalledFlag(Map<String, Object> flagData, Flag<V> flag, Object val) {
        if (val == null) {
            return;
        }

        flagData.put(flag.getName(), flag.marshal((V) val));
    }

    @SuppressWarnings("deprecation")
    private Map<String, Object> getDomainData(DefaultDomain domain) {
        Map<String, Object> domainData = new HashMap<String, Object>();

        setDomainData(domainData, "players", domain.getPlayers());
        setDomainData(domainData, "unique-ids", domain.getUniqueIds());
        setDomainData(domainData, "groups", domain.getGroups());

        return domainData;
    }

    private void setDomainData(Map<String, Object> domainData, String key, Set<?> domain) {
        if (domain.isEmpty()) {
            return;
        }

        List<String> list = new ArrayList<String>();

        for (Object str : domain) {
            list.add(String.valueOf(str));
        }

        domainData.put(key, list);
    }

    @Override
    public Map<String, ProtectedRegion> getRegions() {
        return regions;
    }

    @Override
    public void setRegions(Map<String, ProtectedRegion> regions) {
        this.regions = regions;
    }

    /**
     * Dump the given object as YAML for debugging purposes.
     *
     * @param object the object
     * @return the YAML string or an error string if dumping fals
     */
    private static String dumpAsYaml(Object object) {
        if (yaml == null) {
            DumperOptions options = new DumperOptions();
            options.setIndent(4);
            options.setDefaultFlowStyle(FlowStyle.AUTO);

            yaml = new Yaml(new SafeConstructor(), new Representer(), options);
        }

        try {
            return yaml.dump(object).replaceAll("(?m)^", "\t");
        } catch (Throwable t) {
            return "<error while dumping object>";
        }
    }

}
