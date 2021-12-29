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

package com.sk89q.worldguard.domains.registry;

import com.google.common.collect.Maps;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldguard.commands.CommandInputContext;

import javax.annotation.Nullable;
import java.util.Map;

public final class CustomDomainContext extends CommandInputContext<InvalidDomainFormatException> {

    private CustomDomainContext(Actor sender, String input, Map<String, Object> values) {
        super(sender, input, values);
    }


    public static CustomDomainContext.CustomDomainContextBuilder create() {
        return new CustomDomainContext.CustomDomainContextBuilder();
    }

    /**
     * Create a copy of this CustomDomainContext, with optional substitutions for values
     *
     * <p>If any supplied variable is null, it will be ignored.
     * If a map is supplied, it will override this CustomDomainContext's values of the same key,
     * but unprovided keys will not be overriden and will be returned as shallow copies.</p>
     *
     * @param commandSender CommandSender for the new CustomDomainContext to run under
     * @param s String of the user input for the new CustomDomainContext
     * @param values map of values to override from the current CustomDomainContext
     * @return a copy of this CustomDomainContext
     */
    public CustomDomainContext copyWith(@Nullable Actor commandSender, @Nullable String s, @Nullable Map<String, Object> values) {
        Map<String, Object> map = Maps.newHashMap();
        map.putAll(context);
        if (values != null) {
            map.putAll(values);
        }
        return new CustomDomainContext(commandSender == null ? this.sender : commandSender, s == null ? this.input : s, map);
    }

    @Override
    protected InvalidDomainFormatException createException(String str) {
        return new InvalidDomainFormatException(str);
    }

    public static class CustomDomainContextBuilder {
        private Actor sender;
        private String input;
        private Map<String, Object> map = Maps.newHashMap();

        public CustomDomainContextBuilder setSender(Actor sender) {
            this.sender = sender;
            return this;
        }

        public CustomDomainContextBuilder setInput(String input) {
            this.input = input;
            return this;
        }

        public CustomDomainContextBuilder setObject(String key, Object value) {
            this.map.put(key, value);
            return this;
        }

        public boolean tryAddToMap(String key, Object value) {
            if (map.containsKey(key)) return false;
            this.map.put(key, value);
            return true;
        }

        public CustomDomainContext build() {
            return new CustomDomainContext(sender, input, map);
        }
    }

}

