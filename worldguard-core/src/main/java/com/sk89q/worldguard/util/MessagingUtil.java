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

package com.sk89q.worldguard.util;

import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.commands.CommandUtils;

import java.util.Map;

public final class MessagingUtil {

    private MessagingUtil() {
    }

    public static void sendStringToChat(LocalPlayer player, String message) {
        sendStringToChat(player, message,
                WorldGuard.getInstance().getPlatform().getMatcher().replacements(player));
    }

    public static void sendStringToChat(LocalPlayer player, String message, Map<String, String> resolver) {
        if (WorldGuard.getInstance().getPlatform().hasMiniMessage()) {
            player.sendMiniMessage(message, resolver);
        } else {
            String effective = CommandUtils.replaceColorMacros(message);
            effective = WorldGuard.getInstance().getPlatform().getMatcher().replaceMacros(player, effective, resolver);
            for (String mess : effective.replaceAll("\\\\n", "\n").split("\\n")) {
                player.printRaw(mess);
            }
        }
    }

    public static void sendStringToTitle(LocalPlayer player, String message) {
        String[] parts = message.replaceAll("\\\\n", "\n").split("\\n", 2);
        Map<String, String> resolvers = WorldGuard.getInstance().getPlatform().getMatcher().replacements(player);

        if (WorldGuard.getInstance().getPlatform().hasMiniMessage()) {
            player.sendMiniMessageTitle(parts[0], parts.length > 1 ? parts[1] : "", resolvers);
        } else {
            String title = CommandUtils.replaceColorMacros(parts[0]);
            title = WorldGuard.getInstance().getPlatform().getMatcher().replaceMacros(player, title, resolvers);
            if (parts.length > 1) {
                String subtitle = CommandUtils.replaceColorMacros(parts[1]);
                subtitle = WorldGuard.getInstance().getPlatform().getMatcher().replaceMacros(player, subtitle, resolvers);
                player.sendTitle(title, subtitle);
            } else {
                player.sendTitle(title, null);
            }
        }
    }

}
