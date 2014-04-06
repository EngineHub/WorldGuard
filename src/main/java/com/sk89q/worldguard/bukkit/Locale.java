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
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Locale {

    /**
     * Reference to the plugin.
     */
    private WorldGuardPlugin plugin;

    /**
     * The language configuration.
     */
    private String language;

    /**
     * The locale configuration
     */
    private ResourceBundle localeDefault;
    private ResourceBundle localeCustom;

    /**
     * Construct the object.
     *
     * @param plugin The plugin instance
     */
    public Locale(WorldGuardPlugin plugin, String language) {
        this.plugin = plugin;
        this.language = language;
    }

    /**
     * Load the configuration.
     */
    public void load() {

        // Create the default locale file
        plugin.createDefaultConfiguration(
                new File(plugin.getDataFolder(), "locale_en.properties"), "locale_en.properties");

        try {
            URL[] urls = { plugin.getDataFolder().toURI().toURL() };
            try {
                localeCustom = ResourceBundle.getBundle("locale", new java.util.Locale(language), new URLClassLoader(urls));
            } catch (MissingResourceException e) {
            	plugin.getLogger().severe("Error reading locale file. Trying to load from defaults anyway...");
            }
            try {
                localeDefault = ResourceBundle.getBundle("defaults/locale_en");
            } catch (MissingResourceException e) {
                plugin.getLogger().severe("Failed to load locale from defaults...");
                e.printStackTrace();
                plugin.getServer().shutdown();
            }            
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        plugin.getLogger().info("Loaded locale");
    }

    /**
     * Get a locale string given an identifier.
     *
     * @param identifier The locale string identifier.
     * @return The locale string
     */
    public String get(String identifier) {
    	try {
    		return localeCustom.getString(identifier);	
    	} catch (Exception e) {
        	try {
        		return localeDefault.getString(identifier);	
            } catch (Exception e2) {
                return "Can't find localized string with identifier: " + identifier;
            }
        }
    }

    /**
     * Get a locale string given a identifier.
     *
     * @param identifier The locale string identifier.
     * @param args The args to format with locale string.
     * @return The locale string
     */
    public String get(String identifier, Object... args) {
        return String.format(get(identifier), args);
    }
}
