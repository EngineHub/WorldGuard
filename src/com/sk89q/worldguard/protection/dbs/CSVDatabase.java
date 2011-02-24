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

package com.sk89q.worldguard.protection.dbs;

import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regionmanager.RegionManager;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.*;
import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.regions.AreaFlags;
import com.sk89q.worldguard.protection.regions.AreaFlags.State;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion.CircularInheritanceException;
import com.sk89q.worldguard.util.ArrayReader;

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
                        "cuboid.2",
                        String.valueOf(min.getBlockX()),
                        String.valueOf(min.getBlockY()),
                        String.valueOf(min.getBlockZ()),
                        String.valueOf(max.getBlockX()),
                        String.valueOf(max.getBlockY()),
                        String.valueOf(max.getBlockZ()),
                        String.valueOf(cuboid.getPriority()),
                        cuboid.getParent() != null ? cuboid.getParent().getId() : "",
                        writeDomains(cuboid.getOwners()),
                        writeDomains(cuboid.getMembers()),
                        writeFlags(cuboid.getFlags()),
                        cuboid.getEnterMessage() != null ? cuboid.getEnterMessage() : "",
                        cuboid.getLeaveMessage() != null ? cuboid.getLeaveMessage() : "",
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
        Map<ProtectedRegion,String> parentSets =
                new LinkedHashMap<ProtectedRegion, String>();
        
        CSVReader reader = new CSVReader(new FileReader(file));
        
        try {
            String[] line;
            
            while ((line = reader.readNext()) != null) {
                if (line.length < 2) {
                    logger.warning("Invalid region definition: " + line);
                    continue;
                }
                
                String id = line[0].toLowerCase();
                String type = line[1];
                ArrayReader<String> entries = new ArrayReader<String>(line);
                
                if (type.equalsIgnoreCase("cuboid")) {
                    if (line.length < 8) {
                        logger.warning("Invalid region definition: " + line);
                        continue;
                    }
                    
                    Vector pt1 = new Vector(
                            Integer.parseInt(line[2]),
                            Integer.parseInt(line[3]),
                            Integer.parseInt(line[4]));
                    Vector pt2 = new Vector(
                            Integer.parseInt(line[5]),
                            Integer.parseInt(line[6]),
                            Integer.parseInt(line[7]));

                    BlockVector min = Vector.getMinimum(pt1, pt2).toBlockVector();
                    BlockVector max = Vector.getMaximum(pt1, pt2).toBlockVector();
                    
                    int priority = entries.get(8) == null ? 0 : Integer.parseInt(entries.get(8));
                    String ownersData = entries.get(9);
                    String flagsData = entries.get(10);
                    String enterMessage = nullEmptyString(entries.get(11));
                    
                    ProtectedRegion region = new ProtectedCuboidRegion(id, min, max);
                    region.setPriority(priority);
                    region.setFlags(parseFlags(flagsData));
                    region.setOwners(this.parseDomains(ownersData));
                    region.setEnterMessage(enterMessage);
                    regions.put(id, region);
                } else if (type.equalsIgnoreCase("cuboid.2")) {
                    Vector pt1 = new Vector(
                            Integer.parseInt(line[2]),
                            Integer.parseInt(line[3]),
                            Integer.parseInt(line[4]));
                    Vector pt2 = new Vector(
                            Integer.parseInt(line[5]),
                            Integer.parseInt(line[6]),
                            Integer.parseInt(line[7]));

                    BlockVector min = Vector.getMinimum(pt1, pt2).toBlockVector();
                    BlockVector max = Vector.getMaximum(pt1, pt2).toBlockVector();
                    
                    int priority = entries.get(8) == null ? 0 : Integer.parseInt(entries.get(8));
                    String parentId = entries.get(9);
                    String ownersData = entries.get(10);
                    String membersData = entries.get(11);
                    String flagsData = entries.get(12);
                    String enterMessage = nullEmptyString(entries.get(13));
                    String leaveMessage = nullEmptyString(entries.get(14));
                    
                    ProtectedRegion region = new ProtectedCuboidRegion(id, min, max);
                    region.setPriority(priority);
                    region.setFlags(parseFlags(flagsData));
                    region.setOwners(this.parseDomains(ownersData));
                    region.setMembers(this.parseDomains(membersData));
                    region.setEnterMessage(enterMessage);
                    region.setLeaveMessage(leaveMessage);
                    regions.put(id, region);
                    
                    // Link children to parents later
                    if (parentId.length() > 0) {
                        parentSets.put(region, parentId);
                    }
                }
            }
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
            }
        }
        
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
        if (data == null) {
            return new DefaultDomain();
        }
        
        DefaultDomain domain = new DefaultDomain();
        Pattern pattern = Pattern.compile("^([A-Za-z]):(.*)$");
        
        String[] parts = data.split(",");
        
        for (String part : parts) {
            if (part.trim().length() == 0) {
                continue;
            }
            
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
        if (data == null) {
            return new AreaFlags();
        }
        
        AreaFlags flags = new AreaFlags();
        State curState = State.ALLOW;
        
        for (int i = 0; i < data.length(); i++) {
            char k = data.charAt(i);
            if (k == '+') {
                curState = State.ALLOW;
            } else if (k == '-') {
                curState = State.DENY;
            } else {
                String flag;
                if (k == '_') {
                    if (i == data.length() - 1) {
                        logger.warning("_ read ahead fail");
                        break;
                    }
                    flag = "_" + data.charAt(i + 1);
                    i++;
                } else {
                    flag = String.valueOf(k);
                }
                flags.set(flag, curState);
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
     * Returns a null if a string is null or empty.
     * 
     * @param str
     * @return
     */
    private String nullEmptyString(String str) {
        if (str == null) {
            return null;
        } else if (str.length() == 0) {
            return null;
        } else {
            return str;
        }
    }
    
    /**
     * Used to write the list of flags.
     * 
     * @param flags
     * @return
     */
    private String writeFlags(AreaFlags flags) {
        StringBuilder str = new StringBuilder();
        for (Map.Entry<String, State> entry : flags.entrySet()) {
            str.append(writeFlag(entry.getValue(), entry.getKey()));
        }
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
