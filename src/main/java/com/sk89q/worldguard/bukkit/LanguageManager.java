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
import java.io.IOException;
import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 * The language Manager Class of the WorldGuard plugin
 *
 */
public class LanguageManager {

    private final WorldGuardPlugin plugin;
    private final YamlConfiguration lang;
    private String prefix;

    public LanguageManager(WorldGuardPlugin plugin) {
        this.plugin = plugin;
        lang = new YamlConfiguration();
    }

    /**
     * Loads the messages.yml files into memory, it must be called on reloads.
     *
     * @throws IOException
     * @throws InvalidConfigurationException
     */
    public void load() throws IOException, InvalidConfigurationException {
        File langFile = new File(plugin.getDataFolder(), "messages.yml");

        if (!langFile.exists()) {
            plugin.saveResource("messages.yml", true);
        }

        lang.load(langFile);

        this.prefix = ChatColor.translateAlternateColorCodes('&', lang.getString("prefix"));
        if (this.prefix == null) {
            this.prefix = "";
        }
    }

    /**
     * Get a translated text from messages file and apply chat colours.
     *
     * @param messageId - The ID of the message
     * @return if exists the transalted text, else the given ID Message.
     */
    public String getText(String messageId) {
        return getText(messageId, true);
    }

    /**
     * Get a translated text from messages file and apply chat colours.
     *
     * @param messageId - The ID of the message
     * @param usePrefix - When it is true adds the prefix to the text.
     * @return if exists the transalted text, else the given ID Message.
     */
    public String getText(String messageId, boolean usePrefix) {
        String result;
        result = lang.getString(messageId);
        if (result != null) {
            result = ChatColor.translateAlternateColorCodes('&', result);
        } else {
            result = messageId;
        }
        if (usePrefix) {
            return prefix + result;
        } else {
            return result;
        }
    }

    /**
     * Get a translated text from messages file.
     *
     * @param messageId - The ID of the message
     * @return if exists the transalted text, else the given ID Message.
     */
    public String getVerbatimText(String messageId) {
        String result;
        result = lang.getString(messageId);
        if (result == null) {
            result = messageId;
        }
        return result;
    }

    /**
     * Get a translated text from messages file, add chatcolors and replaces a
     * variable.
     *
     * @param messageId - The ID of the message
     * @param target - The sequence of char values to be replaced
     * @param replacement - The replacement sequence of char values
     * @return
     */
    public String getText(String messageId, String target, String replacement) {
        return getText(messageId, target, replacement, true);
    }

    /**
     * Get a translated text from messages file, add chatcolors and replaces a
     * variable.
     *
     * @param messageId - The ID of the message
     * @param target - The sequence of char values to be replaced
     * @param replacement - The replacement sequence of char values
     * @param usePrefix - When it is true adds the prefix to the text.
     * @return
     */
    public String getText(String messageId, String target, String replacement,
            boolean usePrefix) {
        String result;
        result = getText(messageId, usePrefix);
        return result.replace(target, replacement);
    }

    /**
     * Get a translated text from messages file and replaces a variable.
     *
     * @param messageId - The ID of the message
     * @param target - The sequence of char values to be replaced
     * @param replacement - The replacement sequence of char values
     * @return formated text
     */
    public String getVerbatimText(String messageId, String target, String replacement) {
        String result;
        result = getVerbatimText(messageId);
        return result.replace(target, replacement);
    }

    /**
     * get the plugin prefix configured in messages.yml
     * @return the plugin prefix
     */
    public String getPrefix() {
        return prefix;
    }
    
}
