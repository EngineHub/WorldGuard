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

import com.google.common.base.Function;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class InteropUtils {

    @Nullable
    private static final Class<?> forgeFakePlayerClass;
    // This UUID is from Minecraft Forge (see FakePlayerFactory)
    private static final UUID forgeFakePlayerUuid = UUID.fromString("41c82c87-7afb-4024-ba57-13d2c99cae77");
    private static final PlayerHandleFunction playerHandleFunction;

    static {
        forgeFakePlayerClass = findClass("net.minecraftforge.common.util.FakePlayer");

        PlayerHandleFunction function;
        try {
            function = new PlayerHandleFunction();
        } catch (Exception e) {
            function = null;
        }
        playerHandleFunction = function;
    }

    @Nullable
    private static Class<?> findClass(String name) {
        try {
            return Class.forName(name);
        } catch (Throwable ignored) {
            return null;
        }
    }

    private InteropUtils() {
    }

    /**
     * Return whether the given player is a fake player.
     *
     * @param player the player
     * @return true if a fake player
     */
    public static boolean isFakePlayer(Player player) {
        UUID uuid = player.getUniqueId();
        String name = player.getName();

        if (uuid.equals(forgeFakePlayerUuid)) {
            return true;
        }

        if (forgeFakePlayerClass != null && playerHandleFunction != null) {
            Object handle = playerHandleFunction.apply(player);
            if (handle != null) {
                if (forgeFakePlayerClass.isAssignableFrom(handle.getClass())) {
                    return true;
                }
            }
        }

        if (name.length() >= 3 && name.charAt(0) == '[' && name.charAt(name.length() - 1) == ']') {
            return true;
        }

        return false;
    }

    private static final class PlayerHandleFunction implements Function<Object, Object> {
        private final Class<?> craftPlayerClass;
        private final Method getHandleMethod;

        private PlayerHandleFunction() throws NoSuchMethodException, ClassNotFoundException {
            craftPlayerClass = Class.forName(Bukkit.getServer().getClass().getCanonicalName().replaceAll("CraftServer$", "entity.CraftPlayer"));
            getHandleMethod = craftPlayerClass.getMethod("getHandle");
        }

        @Nullable
        @Override
        public Object apply(Object o) {
            if (craftPlayerClass.isAssignableFrom(o.getClass())) {
                try {
                    return getHandleMethod.invoke(o);
                } catch (Throwable ignored) {
                }
            }

            return null;
        }
    }
}
