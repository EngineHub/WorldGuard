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

package com.sk89q.worldguard.blacklist;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.ChatColor;

import com.sk89q.worldedit.blocks.ItemType;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.blacklist.events.BlacklistEvent;

/**
 *
 * @author sk89q
 */
public abstract class Blacklist {

    /**
     * List of entries by block ID.
     */
    private Map<Integer,List<BlacklistEntry>> blacklist
            = new HashMap<Integer,List<BlacklistEntry>>();
    /**
     * Logger.
     */
    private BlacklistLogger blacklistLogger = new BlacklistLogger();
    /**
     * Last event.
     */
    private BlacklistEvent lastEvent;
    /**
     * Used to prevent flooding.
     */
    Map<String,BlacklistTrackedEvent> lastAffected =
            new HashMap<String,BlacklistTrackedEvent>();

    private boolean useAsWhitelist;

    private final Logger logger;

    public Blacklist(Boolean useAsWhitelist, Logger logger) {
        this.useAsWhitelist = useAsWhitelist;
        this.logger = logger;
    }

    /**
     * Returns whether the list is empty.
     *
     * @return whether the blacklist is empty
     */
    public boolean isEmpty() {
        return blacklist.isEmpty();
    }

    /**
     * Get the entries for an item or list.
     *
     * @param id The item id to get blacklist information for
     * @return The blacklist entries for {@code id}
     */
    public List<BlacklistEntry> getEntries(int id) {
        return blacklist.get(id);
    }

    /**
     * Get the number of individual items that have blacklist entries.
     *
     * @return The number of items in the blacklist
     */
    public int getItemCount() {
        return blacklist.size();
    }

    /**
     * Returns whether the blacklist is used as a whitelist.
     *
     * @return whether the blacklist is be used as a whitelist
     */
    public boolean isWhitelist() {
        return useAsWhitelist;
    }

    /**
     * Get the logger.
     *
     * @return The logger used in this blacklist
     */
    public BlacklistLogger getLogger() {
        return blacklistLogger;
    }

    /**
     * Method to handle the event.
     *
     * @param event The event to check
     * @param forceRepeat Whether to force quickly repeating notifications
     * @param silent Whether to force-deny notifications
     * @return Whether the event is allowed
     */
    public boolean check(BlacklistEvent event, boolean forceRepeat, boolean silent) {
        List<BlacklistEntry> entries = getEntries(event.getType());
        if (entries == null) {
            return true;
        }
        boolean ret = true;
        for (BlacklistEntry entry : entries) {
            if (!entry.check(useAsWhitelist, event, forceRepeat, silent)) {
                ret = false;
            }
        }
        return ret;
    }

    /**
     * Load the blacklist.
     *
     * @param file The file to load from
     * @throws IOException if an error occurred reading from the file
     */
    public void load(File file) throws IOException {
        FileReader input = null;
        Map<Integer,List<BlacklistEntry>> blacklist =
                new HashMap<Integer,List<BlacklistEntry>>();

        try {
            input = new FileReader(file);
            BufferedReader buff = new BufferedReader(input);

            String line;
            List<BlacklistEntry> currentEntries = null;
            while ((line = buff.readLine()) != null) {
                line = line.trim();

                // Blank line
                if (line.length() == 0) {
                    continue;
                } else if (line.charAt(0) == ';' || line.charAt(0) == '#') {
                    continue;
                }

                if (line.matches("^\\[.*\\]$")) {
                    String[] items = line.substring(1, line.length() - 1).split(",");
                    currentEntries = new ArrayList<BlacklistEntry>();

                    for (String item : items) {
                        int id;

                        try {
                            id = Integer.parseInt(item.trim());
                        } catch (NumberFormatException e) {
                            id = getItemID(item.trim());
                            if (id == 0) {
                                logger.log(Level.WARNING, "Unknown block name: "
                                        + item);
                                break;
                            }
                        }

                        BlacklistEntry entry = new BlacklistEntry(this);
                        if (blacklist.containsKey(id)) {
                            blacklist.get(id).add(entry);
                        } else {
                            List<BlacklistEntry> entries = new ArrayList<BlacklistEntry>();
                            entries.add(entry);
                            blacklist.put(id, entries);
                        }
                        currentEntries.add(entry);
                    }
                } else if (currentEntries != null) {
                    String[] parts = line.split("=");

                    if (parts.length == 1) {
                        logger.log(Level.WARNING, "Found option with no value "
                                + file.getName() + " for '" + line + "'");
                        continue;
                    }

                    boolean unknownOption = false;

                    for (BlacklistEntry entry : currentEntries) {
                        if (parts[0].equalsIgnoreCase("ignore-groups")) {
                            entry.setIgnoreGroups(parts[1].split(","));
                        } else if (parts[0].equalsIgnoreCase("ignore-perms")) {
                            entry.setIgnorePermissions(parts[1].split(","));
                        } else if (parts[0].equalsIgnoreCase("on-break")) {
                            entry.setBreakActions(parts[1].split(","));
                        } else if (parts[0].equalsIgnoreCase("on-destroy-with")) {
                            entry.setDestroyWithActions(parts[1].split(","));
                        } else if (parts[0].equalsIgnoreCase("on-place")) {
                            entry.setPlaceActions(parts[1].split(","));
                        } else if (parts[0].equalsIgnoreCase("on-interact")) {
                            entry.setInteractActions(parts[1].split(","));
                        } else if (parts[0].equalsIgnoreCase("on-use")) {
                            entry.setUseActions(parts[1].split(","));
                        } else if (parts[0].equalsIgnoreCase("on-drop")) {
                            entry.setDropActions(parts[1].split(","));
                        } else if (parts[0].equalsIgnoreCase("on-acquire")) {
                            entry.setAcquireActions(parts[1].split(","));
                        } else if (parts[0].equalsIgnoreCase("message")) {
                            entry.setMessage(parts[1].trim());
                        } else if (parts[0].equalsIgnoreCase("comment")) {
                            entry.setComment(parts[1].trim());
                        } else {
                            unknownOption = true;
                        }
                    }

                    if (unknownOption) {
                        logger.log(Level.WARNING, "Unknown option '" + parts[0]
                                + "' in " + file.getName() + " for '" + line + "'");
                    }
                } else {
                    logger.log(Level.WARNING, "Found option with no heading "
                            + file.getName() + " for '" + line + "'");
                }
            }

            this.blacklist = blacklist;
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (IOException ignore) {
            }
        }
    }

    /**
     * Get the last event.
     *
     * @return The last event
     */
    public BlacklistEvent getLastEvent() {
        return lastEvent;
    }

    /**
     * Notify administrators.
     *
     * @param event The event to notify about
     * @param comment The comment to notify with
     */
    public void notify(BlacklistEvent event, String comment) {
        lastEvent = event;

        broadcastNotification(ChatColor.GRAY + "WG: "
                + ChatColor.LIGHT_PURPLE + event.getPlayer().getName()
                + ChatColor.GOLD + " (" + event.getDescription() + ") "
                + ChatColor.WHITE
                + getFriendlyItemName(event.getType())
                + (comment != null ? " (" + comment + ")" : "") + ".");
    }

    /**
     * Sends a notification to all subscribing users.
     *
     * @param msg The message to broadcast
     */
    public abstract void broadcastNotification(String msg);

    /**
     * Forget a player.
     *
     * @param player The player to forget
     */
    public void forgetPlayer(LocalPlayer player) {
        lastAffected.remove(player.getName());
    }

    /**
     * Forget all players.
     */
    public void forgetAllPlayers() {
        lastAffected.clear();
    }

    /**
     * Get an item's ID from its name.
     *
     * @param name the name of the item to look up
     * @return the id for name if contained in ItemId, else -1
     */
    private static int getItemID(String name) {
        ItemType type = ItemType.lookup(name);
        if (type != null) {
            return type.getID();
        } else {
            return -1;
        }
    }

    /**
     * Get an item's friendly name with its ID.
     *
     * @param id The id to look up
     * @return The item's friendly name
     */
    private static String getFriendlyItemName(int id) {
        ItemType type = ItemType.fromID(id);
        if (type != null) {
            return type.getName() + " (#" + id + ")";
        } else {
            return "#" + id + "";
        }
    }
}
