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
import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

public class LocaleManager {

    /**
     * The bundled locale
     */
    private static ResourceBundle localeDefault;

    /**
     * The user setting's locale
     */
    private static ResourceBundle localeSetting;
    
    private static final Pattern PATTERN_DBLQUOTES = Pattern.compile("''");

    /**
     * Load the bundled and target locale.
     *
     * @param plugin The plugin instance
     * @param locale The target locale
     */
    public static void loadLocale(WorldGuardPlugin plugin, Locale locale)
    {
        // Create the default locale file
        plugin.createDefaultConfiguration(
                new File(plugin.getDataFolder(), "locale_en.properties"), "locale_en.properties");

        try {
            URL[] urls = { plugin.getDataFolder().toURI().toURL() };
            localeDefault = ResourceBundle.getBundle("defaults.locale", Locale.ENGLISH);            	
            localeSetting = ResourceBundle.getBundle("locale", locale, new URLClassLoader(urls));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (MissingResourceException e) {
            plugin.getLogger().severe("Failed to load locale...");
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
    public static String tr(String identifier) {
        try {
            try {
                if (localeSetting != null) {
            	    return PATTERN_DBLQUOTES.matcher(localeSetting.getString(identifier)).replaceAll("'");
                }
                else if (localeDefault != null) {
                    return PATTERN_DBLQUOTES.matcher(localeDefault.getString(identifier)).replaceAll("'");            	
                }
            } catch (MissingResourceException e1) {
            	if (localeDefault != null)
            	{
                    return PATTERN_DBLQUOTES.matcher(localeDefault.getString(identifier)).replaceAll("'");
            	}
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        }

        return "{" + identifier + "}";
    }

    /**
     * Get a locale string given an identifier.
     *
     * @param identifier The locale string identifier.
     * @param args The args to format with locale string.
     * @return The locale string
     */
    public static String tr(String identifier, Object... args) {
        MessageFormat formatter = new MessageFormat(tr(identifier));
        try {
            return formatter.format(args);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "{" + identifier + "}:" + args;
    }    
}
