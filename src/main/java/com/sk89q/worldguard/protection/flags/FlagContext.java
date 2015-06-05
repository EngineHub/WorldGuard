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

package com.sk89q.worldguard.protection.flags;

import com.google.common.collect.Maps;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.command.CommandSender;

import javax.annotation.Nullable;
import java.util.Map;

public class FlagContext {

    private Map<String, Object> context = Maps.newHashMap();

    public static FlagContextBuilder create() {
        return new FlagContextBuilder();
    }

    public void put(String name, Object value) {
        context.put(name, value);
    }

    public String getString(String name) throws InvalidFlagFormat {
        Object val = get(name);
        if (val == null) return null;
        return (val instanceof String ? ((String) val).trim() : val.toString());
    }

    public String getUserInput() throws InvalidFlagFormat {
        return getString("input");
    }

    public CommandSender getSender() throws InvalidFlagFormat {
        Object val = get("sender");
        if (val == null) throw new InvalidFlagFormat("Missing CommandSender!");
        if (val instanceof CommandSender) {
            return ((CommandSender) val);
        } else {
            throw new InvalidFlagFormat("Sender is an impostor!");
        }
    }

    public Integer getInt(String name) throws InvalidFlagFormat {
        Object val = get(name);
        if (val == null) return null;
        try {
            return Integer.valueOf(((String) val).trim());
        } catch (NumberFormatException e) {
            throw new InvalidFlagFormat("Expected number for " + name + ", but got " + val);
        }
    }

    @Nullable
    public Object get(String name) {
        return get(name, null);
    }

    @Nullable
    public Object get(String name, Object defaultValue) {
        return context.getOrDefault(name, defaultValue);
    }

    public FlagContext copy() {
        FlagContext clone = new FlagContext();
        clone.context.putAll(this.context);
        return clone;
    }

    public static class FlagContextBuilder {

        private FlagContext context;

        public FlagContextBuilder() {
            this.context = new FlagContext();
        }

        // TODO do we even need this? everything just uses WorldGuardPlugin.inst() anyway
        public FlagContextBuilder setPlugin(WorldGuardPlugin plugin) {
            context.put("plugin", plugin);
            return this;
        }

        public FlagContextBuilder setSender(CommandSender sender) {
            context.put("sender", sender);
            return this;
        }

        public FlagContextBuilder setInput(String input) {
            context.put("input", input);
            return this;
        }

        public FlagContextBuilder setRegion(ProtectedRegion region) {
            context.put("region", region);
            return this;
        }

        public FlagContext build() {
            return context;
        }
    }
}
