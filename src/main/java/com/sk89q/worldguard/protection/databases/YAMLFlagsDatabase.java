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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.sk89q.util.yaml.YAMLFormat;
import com.sk89q.util.yaml.YAMLNode;
import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.worldguard.protection.flags.AllFlags.FlagType;
import com.sk89q.worldguard.protection.flags.BooleanFlag;
import com.sk89q.worldguard.protection.flags.CommandStringFlag;
import com.sk89q.worldguard.protection.flags.DoubleFlag;
import com.sk89q.worldguard.protection.flags.EntityTypeFlag;
import com.sk89q.worldguard.protection.flags.EnumFlag;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.FlagsList;
import com.sk89q.worldguard.protection.flags.IntegerFlag;
import com.sk89q.worldguard.protection.flags.LocationFlag;
import com.sk89q.worldguard.protection.flags.RegionGroup;
import com.sk89q.worldguard.protection.flags.RegionGroupFlag;
import com.sk89q.worldguard.protection.flags.SetFlag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.StringFlag;
import com.sk89q.worldguard.protection.flags.VectorFlag;

public class YAMLFlagsDatabase {

    private YAMLProcessor config;
    private Map<String, FlagsList> allFlags;
    private final Logger logger;

    public YAMLFlagsDatabase(File file, Logger logger) throws ProtectionDatabaseException, FileNotFoundException {
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

    /**
     * Load flags from file
     * @throws ProtectionDatabaseException
     */
    @SuppressWarnings({"rawtypes", "unchecked" })
    public void load() throws ProtectionDatabaseException {
        try {
            config.load();
        } catch (IOException e) {
            throw new ProtectionDatabaseException(e);
        }
        
        Map<String, YAMLNode> flagData = config.getNodes("custom-flags");
        
        // No flags are even configured
        if (flagData == null) {
            this.allFlags = new HashMap<String, FlagsList>();
            return;
        }

        Map<String, FlagsList> allFlags = new HashMap<String, FlagsList>();

        for (Map.Entry<String, YAMLNode> pluginNode : flagData.entrySet()) {
            String plugin = pluginNode.getKey().replace(".", "");
            FlagsList flags = new FlagsList();
            Map<String, YAMLNode> flagNodes = config.getNodes("custom-flags." + plugin);

            for (Map.Entry<String, YAMLNode> flagNode : flagNodes.entrySet()) {
                String name = flagNode.getKey().toLowerCase().replace(".", "");
                YAMLNode node = flagNode.getValue();

                Boolean def = null;
                RegionGroup regionGroup = null;
                FlagType flagType = null;
                Class<?> enumClass = null;
                try {
                    flagType = Enum.valueOf(FlagType.class, node.getString("type"));
                    if (flagType == FlagType.STATE) {
                        def = node.getBoolean("default");
                        if (def == null) {
                            logger.warning("No default for flag '" + name + "'");
                            continue;
                        }
                    }
                    if (flagType == FlagType.ENUM) {
                        try {
                            enumClass = Class.forName(node.getString("class"));
                            if (!enumClass.isEnum()) {
                                throw new Exception();
                            }
                        } catch (Exception e) {
                            logger.warning("Enum class '" + e.getMessage() + "' not found, skipping flag: " + name);
                            continue;
                        }
                    }
                    
                    
                    String groupNode = node.getString("region-group");
                    if (groupNode != null) {
                        regionGroup = Enum.valueOf(RegionGroup.class, groupNode);
                    }

                    if (regionGroup != null) {
                        switch (flagType) {
                        case BOOLEAN:
                            flags.add(new BooleanFlag(name, regionGroup)); break;
                        case COMMANDSTRING:
                            flags.add(new CommandStringFlag(name, regionGroup)); break;
                        case DOUBLE:
                            flags.add(new DoubleFlag(name, regionGroup)); break;
                        case ENTITYTYPE:
                            flags.add(new EntityTypeFlag(name, regionGroup)); break;
                        case ENUM:
                            flags.add(new EnumFlag(name, enumClass, regionGroup)); break;
                        case INTEGER:
                            flags.add(new IntegerFlag(name, regionGroup)); break;
                        case LOCATION:
                            flags.add(new LocationFlag(name, regionGroup)); break;
                        case REGIONGROUP:
                            flags.add(new RegionGroupFlag(name, regionGroup)); break;
//TODO add support for SetFlag
//                        case SET:
//                            flags.add(new SetFlag<flag>(name, regionGroup, flag)); break;
                        case STATE:
                            flags.add(new StateFlag(name, def, regionGroup)); break;
                        case STRING:
                            flags.add(new StringFlag(name, regionGroup)); break;
                        case VECTOR:
                            flags.add(new VectorFlag(name, regionGroup)); break;
                        default:
                            break;
                        }
                    } else {
                        switch (flagType) {
                        case BOOLEAN:
                            flags.add(new BooleanFlag(name)); break;
                        case COMMANDSTRING:
                            flags.add(new CommandStringFlag(name)); break;
                        case DOUBLE:
                            flags.add(new DoubleFlag(name)); break;
                        case ENTITYTYPE:
                            flags.add(new EntityTypeFlag(name)); break;
                        case ENUM:
                            flags.add(new EnumFlag(name, enumClass)); break;
                        case INTEGER:
                            flags.add(new IntegerFlag(name)); break;
                        case LOCATION:
                            flags.add(new LocationFlag(name)); break;
//TODO add support for SetFlag
//                        case SET:
//                            flags.add(new SetFlag<flag>(name, regionGroup, flag)); break;
                        case STATE:
                            flags.add(new StateFlag(name, def)); break;
                        case STRING:
                            flags.add(new StringFlag(name)); break;
                        case VECTOR:
                            flags.add(new VectorFlag(name)); break;
                        default:
                            break;
                        }
                    }

                } catch (Exception e) {
                    logger.warning("Invalid Enum for flag '" + name + "' " + e.getMessage());
                    continue;
                }
            }
            allFlags.put(plugin, flags);
        }

        this.allFlags = allFlags;
    }

    /**
     * Commit flags to file
     */
    public void save(){
        config.clear();
        
        for (Map.Entry<String, FlagsList> plugins : allFlags.entrySet()) {
            ArrayList<Flag<?>> flags = plugins.getValue();
            for (Flag<?> flag : flags) {
                YAMLNode node = config.addNode("custom-flags." + plugins.getKey() + "." + flag.getName());

                if (flag instanceof BooleanFlag) {
                    node.setProperty("type", FlagType.BOOLEAN.name());
                } else if (flag instanceof CommandStringFlag) {
                    node.setProperty("type", FlagType.COMMANDSTRING.name());
                } else if (flag instanceof DoubleFlag) {
                    node.setProperty("type", FlagType.DOUBLE.name());
                } else if (flag instanceof EntityTypeFlag) {
                    node.setProperty("type", FlagType.ENTITYTYPE.name());
                } else if (flag instanceof EnumFlag<?>) {
                    node.setProperty("type", FlagType.ENUM.name());
                    node.setProperty("class", ((EnumFlag<?>) flag).getEnumClass().getName());
                } else if (flag instanceof IntegerFlag) {
                    node.setProperty("type", FlagType.INTEGER.name());
                } else if (flag instanceof LocationFlag) {
                    node.setProperty("type", FlagType.LOCATION.name());
                } else if (flag instanceof RegionGroupFlag) {
                    node.setProperty("type", FlagType.REGIONGROUP.name());
                } else if (flag instanceof SetFlag<?>) {
                    config.removeProperty("custom-flags." + plugins.getKey() + "." + flag.getName());
                    continue;
//TODO add support for SetFlag
//                    node.setProperty("type", FlagType.SET.name());
//                    node.setProperty("class", ((SetFlag<?>) flag));
                } else if (flag instanceof StateFlag) {
                    node.setProperty("type", FlagType.STATE.name());
                    node.setProperty("default", ((StateFlag) flag).getDefault());
                } else if (flag instanceof StringFlag) {
                    node.setProperty("type", FlagType.STRING.name());
                } else if (flag instanceof VectorFlag) {
                    node.setProperty("type", FlagType.VECTOR.name());
                } else {
                    continue;
                }
                if (flag.getRegionGroupFlag().getDefault() != RegionGroup.NON_MEMBERS) {
                    node.setProperty("region-group", flag.getRegionGroupFlag().getDefault().name());
                }
            }
        }

        config.setHeader("#\r\n" +
                "# WorldGuard regions file\r\n" +
                "#\r\n" +
                "# WARNING: THIS FILE IS AUTOMATICALLY GENERATED. If you modify this file by\r\n" +
                "# hand, be aware that A SINGLE MISTYPED CHARACTER CAN CORRUPT THE FILE. If\r\n" +
                "# WorldGuard is unable to parse the file, your custom flags will FAIL TO LOAD and\r\n" +
                "# the contents of this file will reset. Please use a YAML validator such as\r\n" +
                "# http://yaml-online-parser.appspot.com (for smaller files).\r\n" +
                "#\r\n" +
                "# REMEMBER TO KEEP PERIODICAL BACKUPS.\r\n" +
                "#");
        config.save();
    }

    /**
     * Total count of custom flags
     * 
     * @return int
     */
    public int size() {
        int size = 0;
        for(ArrayList<Flag<?>> flags : this.allFlags.values()) {
            size += flags.size();
        }
        return size;
    }

    /**
     * Get map of all custom flags, 
     * map keys are plugin names and values are FlagsLists
     * 
     * @return map of custom flags
     */
    public Map<String, FlagsList> getFlags() {
        return this.allFlags;
    }

}
