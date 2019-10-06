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

package com.sk89q.worldguard.commands;

import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.serializer.legacy.LegacyComponentSerializer;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Command-related utility methods.
 */
public final class CommandUtils {

    private CommandUtils() {
    }

    /**
     * Replace color macros in a string.
     *
     * @param str the string
     * @return the new string
     */
    public static String replaceColorMacros(String str) {
        // TODO: Make this more efficient

        str = str.replace("`r", "&c");
        str = str.replace("`R", "&4");

        str = str.replace("`y", "&e");
        str = str.replace("`Y", "&6");

        str = str.replace("`g", "&a");
        str = str.replace("`G", "&2");

        str = str.replace("`c", "&b");
        str = str.replace("`C", "&3");

        str = str.replace("`b", "&9");
        str = str.replace("`B", "&1");

        str = str.replace("`p", "&d");
        str = str.replace("`P", "&5");

        str = str.replace("`0", "&0");
        str = str.replace("`1", "&8");
        str = str.replace("`2", "&7");
        str = str.replace("`w", "&F");

        str = str.replace("`k", "&k");

        str = str.replace("`l", "&l");
        str = str.replace("`m", "&m");
        str = str.replace("`n", "&n");
        str = str.replace("`o", "&o");

        str = str.replace("`x", "&r");

        // MC classic
        // FIXME: workaround for https://github.com/KyoriPowered/text/issues/50
        // remove when fixed upstream and updated in WorldEdit
        str = Arrays.stream(str.split("\n")).map(line -> {
            TextComponent comp = LegacyComponentSerializer.INSTANCE.deserialize(line, '&');
            return LegacyComponentSerializer.INSTANCE.serialize(comp);
        }).collect(Collectors.joining("\n"));

        return str;
    }


    /**
     * Get the name of the given owner object.
     *
     * @param owner the owner object
     * @return a name
     */
    public static String getOwnerName(@Nullable Object owner) {
        if (owner == null) {
            return "?";
        } else if (owner instanceof Actor) {
            return ((Actor) owner).getName();
        } else {
            return "?";
        }
    }

    /**
     * Return a function that accepts a string to send a message to the
     * given sender.
     *
     * @param sender the sender
     * @return a function
     */
    public static Function<String, ?> messageFunction(final Actor sender) {
        return s -> {
            sender.printRaw(s);
            return null;
        };
    }

    /**
     * Return a function that accepts a TextComponent to send a message to the
     * given sender.
     *
     * @param sender the sender
     * @return a function
     */
    public static Function<TextComponent, ?> messageComponentFunction(final Actor sender) {
        return s -> {
            sender.print(s);
            return null;
        };
    }

}
