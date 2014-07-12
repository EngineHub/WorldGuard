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
package com.sk89q.worldguard.bukkit;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.bukkit.World;

import com.sk89q.worldguard.protection.databases.ProtectionDatabaseException;
import com.sk89q.worldguard.protection.databases.CSVDatabase;
import com.sk89q.worldguard.protection.managers.RegionManager;

/**
 * Utility methods for porting from legacy versions.
 *
 * @author sk89q
 */
public class LegacyWorldGuardMigration {

    /**
     * Port over the blacklist.
     *
     * @param plugin The plugin instance
     */
    public static void migrateBlacklist(WorldGuardPlugin plugin) {
        LanguageManager lang = plugin.getLang();
        World mainWorld = plugin.getServer().getWorlds().get(0);
        String mainWorldName = mainWorld.getName();
        String newPath = "worlds/" + mainWorldName + "/blacklist.txt";

        File oldFile = new File(plugin.getDataFolder(), "blacklist.txt");
        File newFile = new File(plugin.getDataFolder(), newPath);

        if (!newFile.exists() && oldFile.exists()) {
            plugin.getLogger().warning(
                    lang.getVerbatimText("update-blacklist-from-older-version"));

            // Need to make root directories
            newFile.getParentFile().mkdirs();

            if (copyFile(oldFile, newFile)) {
                oldFile.renameTo(new File(plugin.getDataFolder(),
                        "blacklist.txt.old"));
            } else {
                plugin.getLogger().warning(
                        lang.getVerbatimText("blacklist-converted-to-vnewpath", "%NEWPATH%", newPath));
                plugin.getLogger().warning(lang.getVerbatimText("other-worlds-have-no-blacklist"));
            }

        }
    }

    /**
     * Migrate region settings.
     *
     * @param plugin The plugin instance
     */
    public static void migrateRegions(WorldGuardPlugin plugin) {
        LanguageManager lang = plugin.getLang();
        try {
            File oldDatabase = new File(plugin.getDataFolder(), "regions.txt");
            if (!oldDatabase.exists()) {
                return;
            }

            plugin.getLogger().info(lang.getVerbatimText("region-database-will-be-converted"));

            World w = plugin.getServer().getWorlds().get(0);
            RegionManager mgr = plugin.getGlobalRegionManager().get(w);

            // First load up the old database using the CSV loader
            CSVDatabase db = new CSVDatabase(oldDatabase, plugin.getLogger());
            db.load();

            // Then save the new database
            mgr.setRegions(db.getRegions());
            mgr.save();

            oldDatabase.renameTo(new File(plugin.getDataFolder(), "regions.txt.old"));

            plugin.getLogger().info(lang.getVerbatimText("regions-database-converted"));
        } catch (ProtectionDatabaseException e) {
            plugin.getLogger().warning(
                    lang.getVerbatimText("error-reading-regions", "%ERROR%", e.getMessage()));
        }
    }

    /**
     * Copies a file.
     *
     * @param from The source file
     * @param to The destination file
     * @return true if successful
     */
    private static boolean copyFile(File from, File to) {
        InputStream in = null;
        OutputStream out = null;

        try {
            in = new FileInputStream(from);
            out = new FileOutputStream(to);

            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }

            in.close();
            out.close();

            return true;
        } catch (FileNotFoundException ignore) {
        } catch (IOException ignore) {
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ignore) {
                }
            }

            if (out != null) {
                try {
                    out.close();
                } catch (IOException ignore) {
                }
            }
        }

        return false;
    }
}
