/*
 * WorldGuard, a suite of tools for Minecraft
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldGuard team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.worldguard.blacklist;

import com.sk89q.worldguard.blacklist.action.Action;
import com.sk89q.worldguard.blacklist.action.ActionType;
import com.sk89q.worldguard.blacklist.event.BlacklistEvent;
import com.sk89q.worldguard.blacklist.event.EventType;
import com.sk89q.worldguard.blacklist.target.TargetMatcher;
import com.sk89q.worldguard.blacklist.target.TargetMatcherParseException;
import com.sk89q.worldguard.blacklist.target.TargetMatcherParser;
import com.sk89q.guavabackport.cache.CacheBuilder;
import com.sk89q.guavabackport.cache.CacheLoader;
import com.sk89q.guavabackport.cache.LoadingCache;
import com.sk89q.worldguard.bukkit.commands.CommandUtils;
import org.bukkit.ChatColor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class Blacklist {

    private static final Logger log = Logger.getLogger(Blacklist.class.getCanonicalName());

    private MatcherIndex index = MatcherIndex.getEmptyInstance();
    private final BlacklistLoggerHandler blacklistLogger = new BlacklistLoggerHandler();
    private BlacklistEvent lastEvent;
    private boolean useAsWhitelist;
    private LoadingCache<String, TrackedEvent> repeatingEventCache = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .expireAfterAccess(30, TimeUnit.SECONDS)
            .build(new CacheLoader<String, TrackedEvent>() {
                @Override
                public TrackedEvent load(String s) throws Exception {
                    return new TrackedEvent();
                }
            });

    public Blacklist(boolean useAsWhitelist) {
        this.useAsWhitelist = useAsWhitelist;
    }

    /**
     * Returns whether the list is empty.
     *
     * @return whether the blacklist is empty
     */
    public boolean isEmpty() {
        return index.isEmpty();
    }

    /**
     * Get the number of individual items that have blacklist entries.
     *
     * @return The number of items in the blacklist
     */
    public int getItemCount() {
        return index.size();
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
     * Get the log.
     *
     * @return The logger used in this blacklist
     */
    public BlacklistLoggerHandler getLogger() {
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
        List<BlacklistEntry> entries = index.getEntries(event.getTarget());

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
        MatcherIndex.Builder builder = new MatcherIndex.Builder();
        TargetMatcherParser targetMatcherParser = new TargetMatcherParser();

        try {
            input = new FileReader(file);
            BufferedReader buff = new BufferedReader(input);

            String line;
            List<BlacklistEntry> currentEntries = null;
            while ((line = buff.readLine()) != null) {
                line = line.trim();

                // Blank line
                if (line.isEmpty()) {
                    continue;
                } else if (line.charAt(0) == ';' || line.charAt(0) == '#') {
                    continue;
                }

                if (line.matches("^\\[.*\\]$")) {
                    String[] items = line.substring(1, line.length() - 1).split(",");
                    currentEntries = new ArrayList<BlacklistEntry>();

                    for (String item : items) {
                        try {
                            TargetMatcher matcher = targetMatcherParser.fromInput(item.trim());
                            BlacklistEntry entry = new BlacklistEntry(this);
                            builder.add(matcher, entry);
                            currentEntries.add(entry);
                        } catch (TargetMatcherParseException e) {
                            log.log(Level.WARNING, "Could not parse a block/item heading: " + e.getMessage());
                        }
                    }
                } else if (currentEntries != null) {
                    String[] parts = line.split("=");

                    if (parts.length == 1) {
                        log.log(Level.WARNING, "Found option with no value " + file.getName() + " for '" + line + "'");
                        continue;
                    }

                    boolean unknownOption = false;

                    for (BlacklistEntry entry : currentEntries) {
                        if (parts[0].equalsIgnoreCase("ignore-groups")) {
                            entry.setIgnoreGroups(parts[1].split(","));

                        } else if (parts[0].equalsIgnoreCase("ignore-perms")) {
                            entry.setIgnorePermissions(parts[1].split(","));

                        } else if (parts[0].equalsIgnoreCase("message")) {
                            entry.setMessage(CommandUtils.replaceColorMacros(parts[1].trim()));

                        } else if (parts[0].equalsIgnoreCase("comment")) {
                            entry.setComment(CommandUtils.replaceColorMacros(parts[1].trim()));

                        } else {
                            boolean found = false;

                            for (EventType type : EventType.values()) {
                                if (type.getRuleName().equalsIgnoreCase(parts[0])) {
                                    entry.getActions(type.getEventClass()).addAll(parseActions(entry, parts[1]));
                                    found = true;
                                    break;
                                }
                            }

                            if (!found) {
                                unknownOption = true;
                            }
                        }
                    }

                    if (unknownOption) {
                        log.log(Level.WARNING, "Unknown option '" + parts[0] + "' in " + file.getName() + " for '" + line + "'");
                    }
                } else {
                    log.log(Level.WARNING, "Found option with no heading "
                            + file.getName() + " for '" + line + "'");
                }
            }

            this.index = builder.build();
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (IOException ignore) {
            }
        }
    }

    private List<Action> parseActions(BlacklistEntry entry, String raw) {
        String[] split = raw.split(",");
        List<Action> actions = new ArrayList<Action>();

        for (String name : split) {
            name = name.trim();

            boolean found = false;

            for (ActionType type : ActionType.values()) {
                if (type.getActionName().equalsIgnoreCase(name)) {
                    actions.add(type.parseInput(this, entry));
                    found = true;
                    break;
                }
            }

            if (!found) {
                log.log(Level.WARNING, "Unknown blacklist action: " + name);
            }
        }

        return actions;
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
                + ChatColor.LIGHT_PURPLE + event.getCauseName()
                + ChatColor.GOLD + " (" + event.getDescription() + ") "
                + ChatColor.WHITE
                + event.getTarget().getFriendlyName()
                + (comment != null ? " (" + comment + ")" : "") + ".");
    }

    /**
     * Sends a notification to all subscribing users.
     *
     * @param msg The message to broadcast
     */
    public abstract void broadcastNotification(String msg);

    public LoadingCache<String, TrackedEvent> getRepeatingEventCache() {
        return repeatingEventCache;
    }

}
