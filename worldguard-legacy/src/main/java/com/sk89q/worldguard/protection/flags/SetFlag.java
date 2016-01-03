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

import com.google.common.collect.Sets;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Stores a set of types.
 */
public class SetFlag<T> extends Flag<Set<T>> {

    private Flag<T> subFlag;

    public SetFlag(String name, RegionGroup defaultGroup, Flag<T> subFlag) {
        super(name, defaultGroup);
        this.subFlag = subFlag;
    }

    public SetFlag(String name, Flag<T> subFlag) {
        super(name);
        this.subFlag = subFlag;
    }

    /**
     * Get the flag that is stored in this flag.
     *
     * @return the stored flag type
     */
    public Flag<T> getType() {
        return subFlag;
    }

    @Override
    public Set<T> parseInput(WorldGuardPlugin plugin, CommandSender sender, String input) throws InvalidFlagFormat {
        if (input.isEmpty()) {
            return Sets.newHashSet();
        } else {
            Set<T> items = Sets.newHashSet();

            for (String str : input.split(",")) {
                items.add(subFlag.parseInput(plugin, sender, str.trim()));
            }

            return items;
        }
    }

    @Override
    public Set<T> unmarshal(Object o) {
        if (o instanceof Collection<?>) {
            Collection<?> collection = (Collection<?>) o;
            Set<T> items = new HashSet<T>();

            for (Object sub : collection) {
                T item = subFlag.unmarshal(sub);
                if (item != null) {
                    items.add(item);
                }
            }

            return items;
        } else {
            return null;
        }
    }

    @Override
    public Object marshal(Set<T> o) {
        List<Object> list = new ArrayList<Object>();
        for (T item : o) {
            list.add(subFlag.marshal(item));
        }

        return list;
    }
    
}
