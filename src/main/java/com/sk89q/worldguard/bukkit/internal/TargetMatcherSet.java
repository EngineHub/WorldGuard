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

package com.sk89q.worldguard.bukkit.internal;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.sk89q.worldguard.blacklist.target.MaterialTarget;
import com.sk89q.worldguard.blacklist.target.Target;
import com.sk89q.worldguard.blacklist.target.TargetMatcher;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;

import static com.google.common.base.Preconditions.checkNotNull;

public class TargetMatcherSet {

    private final Multimap<Integer, TargetMatcher> entries = HashMultimap.create();

    public boolean add(TargetMatcher matcher) {
        checkNotNull(matcher);
        return entries.put(matcher.getMatchedTypeId(), matcher);
    }

    public boolean test(Target target) {
        Collection<TargetMatcher> matchers = entries.get(target.getTypeId());

        for (TargetMatcher matcher : matchers) {
            if (matcher.test(target)) {
                return true;
            }
        }

        return false;
    }

    public boolean test(Material material) {
        return test(new MaterialTarget(material.getId(), (short) 0));
    }

    public boolean test(Block block) {
        return test(new MaterialTarget(block.getTypeId(), block.getData()));
    }

    public boolean test(BlockState state) {
        return test(new MaterialTarget(state.getTypeId(), state.getRawData()));
    }

    public boolean test(ItemStack itemStack) {
        return test(new MaterialTarget(itemStack.getTypeId(), itemStack.getDurability()));
    }

    @Override
    public String toString() {
        return entries.toString();
    }

}
