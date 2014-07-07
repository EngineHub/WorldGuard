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

package com.sk89q.worldguard.protection.flags;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.logging.Level;

import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.databases.ProtectionDatabaseException;
import com.sk89q.worldguard.protection.databases.YAMLFlagsDatabase;

/**
 *
 * This class keeps track of custom flags.
 * It loads custom flags as needed.
 *
 */
public final class CustomFlags {

    /**
     * Reference to the plugin.
     */
    private WorldGuardPlugin plugin;

    /**
     * Custom flags database.
     */
    private static YAMLFlagsDatabase customFlags;

    /**
     * Stores modification date for custom flags file.
     * This allows WorldGuard to reload custom flags as needed.
     */
    private static Long lastModified;

    /**
     * Construct the object.
     *
     * @param plugin The plugin instance
     */
    public CustomFlags(WorldGuardPlugin plugin) {
        this.plugin = plugin;
        customFlags = null;
        lastModified = null;
    }

    /**
     * Get the path for the custom flags file.
     *
     * @return The custom flags file path
     */
    private File getPath() {
        return new File(plugin.getDataFolder(), "custom-flags.yml");
    }

    /**
     * Loads custom flags.
     */
    public void load() {
        YAMLFlagsDatabase database;
        File file = null;

        try {
            file = getPath();
            database = new YAMLFlagsDatabase(file, plugin.getLogger());

            // Store the last modification date so we can track changes
            lastModified = file.lastModified();

            database.load();

            if (database.size() > 0) {
                plugin.getLogger().info(database.size() + " custom flags loaded");
            }

            CustomFlags.customFlags = database;

        } catch (ProtectionDatabaseException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load custom flags : " + e.getMessage());
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            plugin.getLogger().log(Level.SEVERE, "Error loading custom flags : " + e.toString() + "\n\t" + e.getMessage());
            e.printStackTrace();
        }

    }

    /**
     * Reloads custom flags from file when file has changed
     */
    public void reloadChanged() {

        File file = getPath();

        Long oldDate = lastModified;

        if (oldDate == null) {
            oldDate = 0L;
        }

        try {
            if (file.lastModified() > oldDate) {
                load();
            }
        } catch (Exception ignore) {
        }
    }

    /**
     * Unload custom flags.
     */
    public void unload() {
        customFlags =null;
        lastModified = null;
    }

    /**
     * Save custom flags.
     */
    public void save(){
        if (customFlags == null) {
            return;
        }
        customFlags.save();
    }

    /**
     * Add a flag for {@code yourPlugin}, 
     * will not add flag if another plugin's flag or a default flag already exists with the same name.
     * 
     * @param yourPlugin an instance of the plugin to add the flag for
     * @param flag the flag to be added
     * @return <tt>true</tt> if a flag got added
     */
    public boolean addYourFlag(JavaPlugin yourPlugin, Flag<?> flag) {
        if (customFlags == null) {
            return false;
        }

        FlagsList allFlags = getAllCustomFlags();
        FlagsList flags = getYourFlags(yourPlugin);
        if (flags.contains(flag)) {
            removeYourFlag(yourPlugin, flag.getName());
        }
        if (allFlags.contains(flag)) {
            return false;
        }
        boolean added = flags.add(flag);
        customFlags.getFlags().put(yourPlugin.getName(), flags);
        return added;
    }

    /**
     * Remove the flag
     * 
     * @param yourPlugin an instance of the plugin to remove the flag from
     * @param flag the flag to be removed
     * @return <tt>true</tt> if a flag got removed
     */
    public boolean removeYourFlag(JavaPlugin yourPlugin, Flag<?> flag) {
        if (customFlags == null) {
            return false;
        }
        return  customFlags.getFlags().get(plugin.getName()).remove(flag);
    }

    /**
     * Remove the first flag that has the exact name: {@code name}, the flag may be of any type
     * 
     * @param yourPlugin an instance of the plugin to remove the flag from
     * @param name exact name of a flag
     * @return <tt>true</tt> if a flag got removed
     */
    public boolean removeYourFlag(JavaPlugin yourPlugin, String name) {
        if (customFlags == null) {
            return false;
        }
        return  customFlags.getFlags().get(plugin.getName()).remove(name);
    }

    /**
     * Clears all the custom flags for one plugin
     * 
     * @param yourPlugin an instance of the plugin to clear flags for
     * @return <tt>null</tt> or the flags that just got cleared
     */
    public static FlagsList clearYourFlags(JavaPlugin yourPlugin) {
        if (customFlags == null) {
            return null;
        }
        FlagsList yourFlags = customFlags.getFlags().remove(yourPlugin.getName());
        return yourFlags;
    }

    /**
     * Get a list of custom flags for one plugin
     * 
     * @param yourPlugin an instance of the plugin to get flags for
     * @return flags list
     */
    public static FlagsList getYourFlags(JavaPlugin yourPlugin) {
        if (customFlags == null) {
            return new FlagsList();
        }
        FlagsList yourFlags = customFlags.getFlags().get(yourPlugin.getName());
        if (yourFlags == null) {
            return new FlagsList();
        }
        return yourFlags;
    }

    /**
     * Get a list of custom flags for all plugins
     * 
     * @return flags list
     */
    public static FlagsList getAllCustomFlags() {
        FlagsList allFlags = new FlagsList();
        if (customFlags == null) {
            return allFlags;
        }
        for(FlagsList flags : customFlags.getFlags().values()) {
            allFlags.addAll(flags);
        }
        return allFlags;
    }
}
