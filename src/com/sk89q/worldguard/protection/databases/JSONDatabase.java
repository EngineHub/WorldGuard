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

import com.sk89q.worldguard.protection.managers.RegionManager;
import com.google.gson.Gson;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Represents a protected area database that uses JSON files.
 *
 * @author Redecouverte
 */
public class JSONDatabase implements ProtectionDatabase {
    protected static Logger logger = Logger.getLogger("Minecraft.WorldGuard");

    /**
     * References the json db folder.
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
    public JSONDatabase(File file) {
        this.file = file;
    }

    /**
     * Helper function to read a file into a String
     */
    private static String readFileAsString(File file) throws java.io.IOException {
        byte[] buffer = new byte[(int) file.length()];
        BufferedInputStream f = null;
        try {
            f = new BufferedInputStream(new FileInputStream(file.getAbsolutePath()));
            f.read(buffer);
        } finally {
            if (f != null) {
                try {
                    f.close();
                } catch (IOException ignored) {
                }
            }
        }

        for(int i = 0; i < buffer.length; i++)
        {
            if(buffer[i] < 0x20 || buffer[i] > 0x126)
            {
                buffer[i] = 0x20;
            }
        }

        return new String(buffer);
    }

    /**
     * Load the database from file.
     */
    public void load() throws IOException {

         Gson gson = new Gson();
	 JSONContainer jContainer = gson.fromJson(readFileAsString(file), JSONContainer.class);
         this.regions = jContainer.getRegions();
    }

    /**
     * Saves the database.
     */
    public void save() throws IOException {

        Gson gson = new Gson();
        String jsonData = gson.toJson(new JSONContainer(this.regions), JSONContainer.class);
        writeStringToFile(jsonData, this.file);
    }


    /**
     * Writes a String to a file.
     * 
     * @param string 
     * @param file 
     * @throws IOException
     */
    public static void writeStringToFile(String string, File file) throws IOException {
        FileWriter writer = null;

        try {
            writer = new FileWriter(file);
            writer.write(string);
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
            }
        }
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
     * Get a list of protected regions.
     *
     * @return
     */
    public Map<String,ProtectedRegion> getRegions() {
        return regions;
    }

    /**
     * Get a list of protected regions.
     */
    public void setRegions(Map<String,ProtectedRegion> regions) {
        this.regions = regions;
    }
}
