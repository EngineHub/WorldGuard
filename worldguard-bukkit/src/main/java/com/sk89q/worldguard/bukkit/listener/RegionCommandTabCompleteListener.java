/*
 * WorldGuard, a suite of tools for Minecraft
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldGuard team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.sk89q.worldguard.bukkit.listener;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.TabCompleteEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class RegionCommandTabCompleteListener implements Listener {

    private static final Set<String> REGION_COMMANDS = new HashSet<>(Arrays.asList(
            "redefine", "update", "move",
            "select", "sel", "s",
            "info", "i",
            "flag", "f", "flags",
            "setpriority", "priority", "pri",
            "setparent", "parent", "par",
            "remove", "delete", "del", "rem",
            "teleport", "tp",
            "addmember", "addmem", "am",
            "addowner", "ao",
            "removemember", "remmember", "removemem", "remmem", "rm",
            "removeowner", "remowner", "ro"
    ));

    private static final Set<String> PARENT_COMMANDS = new HashSet<>(Arrays.asList(
            "setparent", "parent", "par"
    ));

    private static final String VALUE_FLAGS = "wghpi";

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onTabComplete(TabCompleteEvent event) {
        if (!(event.getSender() instanceof Player)) {
            return;
        }

        String buffer = event.getBuffer();
        String[] tokens = buffer.split(" ", -1);
        if (tokens.length < 3 || !isRegionRoot(tokens[0])) {
            return;
        }

        String subCommand = tokens[1].toLowerCase(Locale.ROOT);
        if (!REGION_COMMANDS.contains(subCommand)) {
            return;
        }

        int positionalIndex = currentPositionalIndex(tokens);
        if (positionalIndex != 0 && !(positionalIndex == 1 && PARENT_COMMANDS.contains(subCommand))) {
            return;
        }

        String prefix = tokens[tokens.length - 1].toLowerCase(Locale.ROOT);
        Player player = (Player) event.getSender();
        List<String> completions = new ArrayList<>(event.getCompletions());

        WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery()
                .getApplicableRegions(BukkitAdapter.adapt(player.getLocation()))
                .getRegions().stream()
                .map(ProtectedRegion::getId)
                .filter(id -> id.toLowerCase(Locale.ROOT).startsWith(prefix))
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .filter(id -> !completions.contains(id))
                .forEach(completions::add);

        event.setCompletions(completions);
    }

    private static boolean isRegionRoot(String token) {
        String root = token.startsWith("/") ? token.substring(1) : token;
        return root.equalsIgnoreCase("rg")
                || root.equalsIgnoreCase("region")
                || root.equalsIgnoreCase("regions");
    }

    private static int currentPositionalIndex(String[] tokens) {
        int position = 0;
        boolean skipValue = false;
        for (int i = 2; i < tokens.length - 1; i++) {
            String token = tokens[i];
            if (skipValue) {
                skipValue = false;
            } else if (takesValue(token)) {
                skipValue = true;
            } else if (!token.startsWith("-")) {
                position++;
            }
        }

        String current = tokens[tokens.length - 1];
        if (skipValue || current.startsWith("-")) {
            return -1;
        }
        return position;
    }

    private static boolean takesValue(String token) {
        if (!token.startsWith("-") || token.length() < 2) {
            return false;
        }
        for (int i = 1; i < token.length(); i++) {
            if (VALUE_FLAGS.indexOf(Character.toLowerCase(token.charAt(i))) >= 0) {
                return true;
            }
        }
        return false;
    }
}
