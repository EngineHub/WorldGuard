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
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.title.Title;

public final class MessagingUtil {
    private MessagingUtil() {
    }

    public static void sendStringToChat(LocalPlayer player, String message) {
        player.sendMessage(WorldGuard.getInstance().getMiniMessage().deserialize(message,
                WorldGuard.getInstance().getPlatform().getMatcher().replacements(player)));
    }

    public static void formatTitleFromString(LocalPlayer player, String message) {
        String[] parts = message.replaceAll("\\\\n", "\n").split("\\n", 2);
        TagResolver resolvers = WorldGuard.getInstance().getPlatform().getMatcher().replacements(player);

        Component title = WorldGuard.getInstance().getMiniMessage().deserialize(parts[0], resolvers);
        Component subtitle = parts.length > 1 ? WorldGuard.getInstance().getMiniMessage().deserialize(parts[1], resolvers) : Component.empty();

        Title t = WorldGuard.getInstance().getPlatform().getGlobalStateManager().get(player.getWorld()).forceDefaultTitleTimes ?
                Title.title(title, subtitle, Title.DEFAULT_TIMES) : Title.title(title, subtitle);
        player.showTitle(t);
    }

}
