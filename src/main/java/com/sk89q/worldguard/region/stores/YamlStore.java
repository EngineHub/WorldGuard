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
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.apache.commons.lang.Validate;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;

import com.sk89q.rebar.config.ConfigurationException;
import com.sk89q.rebar.config.ConfigurationNode;
import com.sk89q.rebar.config.YamlConfiguration;
import com.sk89q.rebar.config.YamlConfigurationFile;
import com.sk89q.rebar.config.YamlStyle;
import com.sk89q.rebar.config.types.BlockVector2dLoaderBuilder;
import com.sk89q.rebar.config.types.ListBuilder;
import com.sk89q.rebar.config.types.VectorLoaderBuilder;
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
import com.sk89q.worldguard.region.shapes.IndexableShape;

/**
 * A YAML-based region store that uses {@link YamlConfiguration} in order to store
 * region data.
 * <p>
 * The main advantage of this format is that output files can easily be edited by
 * hand, but the disadvantage is that it's not fast as a format that writes more
 * native data types. In addition, while YAML is not nearly verbose as XML, being a
 * text-based format making heavy use of indentation, the size of data files written
 * by this format can be quite large.
 * <p>
 * To mitigate some of the impacts of this format, this store uses only an indentation
 * size of two spaces to reduce disk space usage.
 */
public class YamlStore implements RegionStore {

    private static final Logger defaultLogger = LoggerUtils.getLogger(YamlStore.class);
    private static final YamlStyle style = new YamlStyle(FlowStyle.FLOW, 2);
    private static final VectorLoaderBuilder vectorLB = new VectorLoaderBuilder();
    private static final BlockVector2dLoaderBuilder blockVec2dLB = new BlockVector2dLoaderBuilder();

    private Logger logger = defaultLogger;
    private final File file;

    /**
     * Create a new YAML-based store that uses the provided file for storing data.
     * <p>
     * The file does not yet have to exist, but it does need to be readable and
     * writable in order for regions to be loaded or saved.
     *
     * @param file file to store data in
     */
    public YamlStore(File file) {
        this.file = file;
    }

    /**
     * Get the logger assigned to this store.
     * <p>
     * Messages will be relayed through the logger set on this store if possible.
     *
     * @return logger, or null
     */
    public Logger getLogger() {
        return logger;
    }

    /**
     * Set the logger assigned to this store.
     * <p>
     * Messages will be relayed through the logger set on this store if possible.
     *
     * @param logger or null
     */
    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    @Override
    public RegionIndex load(RegionIndexFactory factory) throws IOException {
        YamlConfiguration config = new YamlConfigurationFile(file);
        try {
            config.load(); // Load data (may raise an exception)
        } catch (ConfigurationException e) {
            throw new IOException("Fatal syntax or other error in YAML-formatted data", e);
        }

        // Create an index
        RegionIndex index = factory.newIndex();

        // Store a list of parent relationships that we have to set later on
        Map<Region, String> parentSets = new LinkedHashMap<Region, String>();

        for (Entry<String, ConfigurationNode> entry : config.getNodes("regions").entrySet()) {
            ConfigurationNode node = entry.getValue();

            try {
                IndexableShape shape;
                String id = node.getString("id");
                String type = node.getString("type");

                Validate.notNull(id, "Missing an 'id' parameter for: " + node);
                Validate.notNull(type, "Missing a 'type' parameter for: " + node);

                // Axis-aligned cuboid type
                if (type.equals("cuboid")) {
                    Vector pt1 = config.getOf("min", vectorLB);
                    Vector pt2 = config.getOf("max", vectorLB);
                    Validate.notNull(pt1, "Missing a 'min' parameter for: " + node);
                    Validate.notNull(pt2, "Missing a 'max' parameter for: " + node);
                    shape = new Cuboid(pt1, pt2);

                // Extruded polygon type
                } else if (type.equals("poly2d")) {
                    Integer minY = node.getInt("min-y");
                    Integer maxY = node.getInt("max-y");
                    Validate.notNull(minY, "Missing a 'min-y' parameter for: " + node);
                    Validate.notNull(maxY, "Missing a 'max-y' parameter for: " + node);
                    List<BlockVector2D> points = node.listOf("points", blockVec2dLB);
                    // Note: Invalid points are discarded!
                    shape = new ExtrudedPolygon(points, minY, maxY);

                // "Everywhere" type
                } else if (type.equals("global")) {
                    shape = new Everywhere();

                // ???
                } else {
                    throw new IllegalArgumentException("Don't know what type of shape '" +
                            type + "' is! In: " + node);
                }

                Region region = new Region(id, shape);
                region.setPriority(node.getInt("priority", 0));
                loadFlags(region, node.getNode("flags"));

                // Remember the parent so that it can be linked lateron
                String parentId = node.getString("parent");
                if (parentId != null) {
                    parentSets.put(region, parentId);
                }
            } catch (IllegalArgumentException e) {
                logger.warning(e.getMessage());
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

        return index;
    }

    @Override
    public void save(Collection<Region> regions) throws IOException {
        YamlConfiguration config = new YamlConfigurationFile(file, style);

        for (Region region : regions) {
            ConfigurationNode node = config.setNode("regions").setNode(region.getId());
            IndexableShape shape = region.getShape();

            if (shape instanceof Cuboid) {
                Cuboid cuboid = (Cuboid) shape;
                node.set("type", "cuboid");
                node.set("min", cuboid.getAABBMin(), blockVec2dLB);
                node.set("max", cuboid.getAABBMax(), blockVec2dLB);
            } else if (shape instanceof ExtrudedPolygon) {
                ExtrudedPolygon poly = (ExtrudedPolygon) shape;
                node.set("type", "poly2d");
                node.set("min-y", poly.getAABBMin().getBlockY());
                node.set("max-y", poly.getAABBMax().getBlockY());
                node.set("points", poly.getProjectedVerts(),
                        new ListBuilder<BlockVector2D>(blockVec2dLB));
            } else if (shape instanceof Everywhere) {
                node.set("type", "global");
            } else {
                // This means that it's not supported!
                node.set("type", region.getClass().getCanonicalName());
            }

            node.set("priority", region.getPriority());
            node.set("flags", buildFlags(region));
            Region parent = region.getParent();
            if (parent != null) {
                node.set("parent", parent.getId());
            }
        }

        config.setHeader("#\r\n" +
                "# WARNING: THIS FILE IS AUTOMATICALLY GENERATED.\r\n" +
                "# A minor error in this file WILL DESTROY ALL YOUR DATA.\r\n" +
                "#\r\n" +
                "# REMEMBER TO KEEP PERIODICAL BACKUPS.\r\n" +
                "#");

        config.save();
    }

    /**
     * Read flag data from the given node and apply it to the region.
     *
     * @param region region to apply flags to
     * @param node node, or null
     */
    private void loadFlags(Region region, ConfigurationNode node) {
        if (node == null) {
            return;
        }

        for (Flag<?> flag : DefaultFlag.getFlags()) {
            Flag<?> groupFlag = flag.getRegionGroupFlag();
            Object rawValue = node.get(flag.getName());

            if (rawValue != null) {
                setFlagFromRaw(region, flag, rawValue);
            }

            // Also get the group flag
            if (groupFlag != null) {
                rawValue = node.get(groupFlag.getName());

                if (rawValue != null) {
                    setFlagFromRaw(region, groupFlag, rawValue);
                }
            }
        }
    }

    /**
     * Try to set a flag on a region from its raw value. The flag will be properly
     * unmarshalled, and if that fails, then the flag won't be set.
     *
     * @param region region to affect
     * @param flag the flag to set
     * @param rawValue the raw value before unmarshalling
     */
    private void setFlagFromRaw(Region region, Flag<?> flag, Object rawValue) {
        Object value = flag.unmarshal(rawValue);
        if (value == null) {
            logger.warning("Failed to parse flag '" + flag.getName()
                    + "' with value '" + rawValue.toString() + "'");
        } else {
            region.setFlagUnsafe(flag, flag.unmarshal(rawValue));
        }
    }

    /**
     * Build a map of flag data to store to disk.
     *
     * @param region the region to read the flags from
     * @return map of flag data
     */
    private Map<String, Object> buildFlags(Region region) {
        Map<String, Object> data = new HashMap<String, Object>();

        for (Map.Entry<Flag<?>, Object> entry : region.getFlags().entrySet()) {
            storeFlagFromValue(data, entry.getKey(), entry.getValue());
        }

        return data;
    }

    /**
     * Marshal a flag's value into something YAML-compatible.
     * <p>
     * if the given value is null, the flag is ignored.
     *
     * @param data map to put the data into
     * @param flag the flag
     * @param val the value, or null
     */
    @SuppressWarnings("unchecked")
    private <V> void storeFlagFromValue(Map<String, Object> data, Flag<V> flag, Object val) {
        if (val == null) {
            return;
        } else {
            data.put(flag.getName(), flag.marshal((V) val));
        }
    }

    @Override
    public void close() throws IOException {
        // Do nothing
    }

}
