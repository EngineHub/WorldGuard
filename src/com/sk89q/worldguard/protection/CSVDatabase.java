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

package com.sk89q.worldguard.protection;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.*;
import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.AreaFlags.State;

/**
 * Represents a protected area database that uses CSV files.
 * 
 * @author sk89q
 */
public class CSVDatabase implements ProtectionDatabase {
    private static Logger logger = Logger.getLogger("Minecraft.WorldGuard");
    
    /**
     * References the CSV file.
     */
    private File file;
    /**
     * Holds the list of regions.
     */
    private Map<String,ProtectedRegion> regions;
    
    /**
     * Construct the database with a path to a file. No file is read or
     * written at this time.
     * 
     * @param file
     */
    public CSVDatabase(File file) {
        this.file = file;
    }

    /**
     * Saves the database.
     */
    @Override
    public void save() throws IOException {
        CSVWriter writer = new CSVWriter(new FileWriter(file));
        
        try {
            for (Map.Entry<String,ProtectedRegion> entry : regions.entrySet()) {
                String id = entry.getKey();
                ProtectedRegion region = entry.getValue();
                
                if (!(region instanceof ProtectedCuboidRegion)) {
                    logger.warning("The CSV database only supports cuboid regions.");
                    continue;
                }
                
                ProtectedCuboidRegion cuboid = (ProtectedCuboidRegion)region;
                BlockVector min = cuboid.getMinimumPoint();
                BlockVector max = cuboid.getMaximumPoint();
                
                writer.writeNext(new String[] {
                        id,
                        "cuboid",
                        String.valueOf(min.getBlockX()),
                        String.valueOf(min.getBlockY()),
                        String.valueOf(min.getBlockZ()),
                        String.valueOf(max.getBlockX()),
                        String.valueOf(max.getBlockY()),
                        String.valueOf(max.getBlockZ()),
                        String.valueOf(cuboid.getPriority()),
                        writeDomains(cuboid.getOwners()),
                        writeFlags(cuboid.getFlags()),
                        cuboid.getEnterMessage(),
                        });
            }
        } finally {
            try {
                writer.close();
            } catch (IOException e) {
            }
        }
    }

    /**
     * Load the database from file.
     */
    @Override
    public void load() throws IOException {
        Map<String,ProtectedRegion> regions =
                new HashMap<String,ProtectedRegion>();
        
        CSVReader reader = new CSVReader(new FileReader(file));
        
        try {
            String[] line;
            
            while ((line = reader.readNext()) != null) {
                if (line.length >= 12) {
                    String type = line[1];
                    if (!type.equalsIgnoreCase("cuboid")) {
                        logger.warning("Only cuboid region types are supported: "
                                + line);
                        break;
                        
                    }
                    
                    String id = line[0];
                    BlockVector min = new BlockVector(
                            Integer.parseInt(line[2]),
                            Integer.parseInt(line[3]),
                            Integer.parseInt(line[4]));
                    BlockVector max = new BlockVector(
                            Integer.parseInt(line[5]),
                            Integer.parseInt(line[6]),
                            Integer.parseInt(line[7]));
                    int priority = Integer.parseInt(line[8]);
                    String ownersData = line[9];
                    String flagsData = line[10];
                    String enterMessage = line[11];
                    
                    ProtectedRegion region = new ProtectedCuboidRegion(min, max);
                    region.setPriority(priority);
                    region.setOwners(this.parseDomains(ownersData));
                    region.setEnterMessage(enterMessage);
                    region.setFlags(parseFlags(flagsData));
                    regions.put(id, region);
                } else {
                    logger.warning("Line has invalid: " + line);
                }
            }
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
            }
        }
        
        this.regions = regions;
    }

    /**
     * Load the list of regions into a region manager.
     * 
     * @throws IOException
     */
    public void load(RegionManager manager) throws IOException {
        load();
        manager.setRegions(regions);
    }
    
    /**
     * Save the list of regions from a region manager.
     * 
     * @throws IOException
     */
    public void save(RegionManager manager) throws IOException {
        regions = manager.getRegions();
        save();
    }
    
    /**
     * Used to parse the specified domain in the CSV file.
     * 
     * @param data
     * @return
     */
    private DefaultDomain parseDomains(String data) {
        DefaultDomain domain = new DefaultDomain();
        Pattern pattern = Pattern.compile("^([A-Za-z]):(.*)$");
        
        String[] parts = data.split(",");
        
        for (String part : parts) {
            Matcher matcher = pattern.matcher(part);
            
            if (!matcher.matches()) {
                logger.warning("Invalid owner specification: " + part);
                continue;
            }

            String type = matcher.group(1);
            String id = matcher.group(2);

            if (type.equals("u")) {
                domain.addPlayer(id);
            } else if (type.equals("g")) {
                domain.addGroup(id);
            } else {
                logger.warning("Unknown owner specification: " + type);
            }
        }
        
        return domain;
    }
    
    /**
     * Used to parse the list of flags.
     * 
     * @param data
     * @return
     */
    private AreaFlags parseFlags(String data) {
        AreaFlags flags = new AreaFlags();
        State curState = State.ALLOW;
        
        for (int i = 0; i < data.length(); i++) {
            char flag = data.charAt(i);
            if (flag == '+') {
                curState = State.ALLOW;
            } else if (flag == '-') {
                curState = State.DENY;
            } else if (flag == 'b') {
                flags.allowBuild = curState;
            } else if (flag == 'p') {
                flags.allowPvP = curState;
            } else if (flag == 'm') {
                flags.allowMobDamage = curState;
            } else if (flag == 'c') {
                flags.allowCreeperExplosions = curState;
            } else if (flag == 't') {
                flags.allowTNT = curState;
            } else if (flag == 'l') {
                flags.allowLighter = curState;
            } else if (flag == 'f') {
                flags.allowFireSpread = curState;
            } else if (flag == 'F') {
                flags.allowLavaFire = curState;
            } else {
                logger.warning("Unknown area flag/flag modifier: " + flag);
            }
        }
        
        return flags;
    }
    
    /**
     * Used to write the list of domains.
     * 
     * @param domain
     * @return
     */
    private String writeDomains(DefaultDomain domain) {
        StringBuilder str = new StringBuilder();
        
        for (String player : domain.getPlayers()) {
            str.append("u:" + player + ",");
        }
        
        for (String group : domain.getGroups()) {
            str.append("g:" + group + ",");
        }
        
        return str.length() > 0 ?
                str.toString().substring(0, str.length() - 1) : "";
    }
    
    /**
     * Helper method to prepend '+' or '-' in front of a flag according
     * to the flag's state.
     * 
     * @param state
     * @param flag
     * @return
     */
    private String writeFlag(State state, String flag) {
        if (state == State.ALLOW) {
            return "+" + flag;
        } else if (state == State.DENY) {
            return "-" + flag;
        }
        
        return "";
    }
    
    /**
     * Used to write the list of flags.
     * 
     * @param flags
     * @return
     */
    private String writeFlags(AreaFlags flags) {
        StringBuilder str = new StringBuilder();
        str.append(writeFlag(flags.allowBuild, "b"));
        str.append(writeFlag(flags.allowPvP, "p"));
        str.append(writeFlag(flags.allowMobDamage, "m"));
        str.append(writeFlag(flags.allowCreeperExplosions, "c"));
        str.append(writeFlag(flags.allowTNT, "t"));
        str.append(writeFlag(flags.allowLighter, "l"));
        str.append(writeFlag(flags.allowFireSpread, "f"));
        str.append(writeFlag(flags.allowLavaFire, "F"));
        return str.toString();
    }

    /**
     * Get a list of protected regions.
     *
     * @return
     */
    public Map<String,ProtectedRegion> getRegions() {
        return regions;
    }

    /**
     * Get a list of protected regions.
     *
     * @return
     */
    public void setRegions(Map<String,ProtectedRegion> regions) {
        this.regions = regions;
    }
}
