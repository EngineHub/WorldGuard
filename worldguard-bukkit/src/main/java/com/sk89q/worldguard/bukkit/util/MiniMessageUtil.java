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

package com.sk89q.worldguard.bukkit.util;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.BukkitPlayer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.title.Title;
import org.bukkit.ChatColor;

import java.util.Map;

public abstract class MiniMessageUtil {
    private MiniMessageUtil() {

    }

    private static TagResolver mapToResolvers(Map<String, String> replacements) {
        TagResolver.Builder builder = TagResolver.builder();
        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            builder.resolver(Placeholder.unparsed(entry.getKey(), entry.getValue()));
        }
        return builder.build();
    }

    public static void sendMiniMessage(BukkitPlayer player, String message, Map<String, String> replacements) {
        player.getPlayer().sendMessage(MiniMessage.miniMessage().deserialize(message, mapToResolvers(replacements)));
    }

    public static void sendMiniMessageTitle(BukkitPlayer player, String title, String subtitle, Map<String, String> replacements) {
        TagResolver tagResolver = mapToResolvers(replacements);
        Component titleComponent = MiniMessage.miniMessage().deserialize(title, tagResolver);
        Component subtitleComponent = MiniMessage.miniMessage().deserialize(subtitle, tagResolver);
        if (WorldGuard.getInstance().getPlatform().getGlobalStateManager().get(player.getWorld()).forceDefaultTitleTimes) {
            player.getPlayer().showTitle(Title.title(titleComponent, subtitleComponent, Title.DEFAULT_TIMES));
        } else {
            player.getPlayer().showTitle(Title.title(titleComponent, subtitleComponent));
        }
    }

    public static String legacyToMiniMessage(String message) {
        if (message.indexOf(ChatColor.COLOR_CHAR) != -1) {
            WorldGuardPlugin.inst().getLogger().info("Converting Message "+ message);
            TextComponent deserialize = LegacyComponentSerializer.legacySection().deserialize(message);
            return MiniMessage.miniMessage().serialize(deserialize);
        }
        return message;
    }
}
