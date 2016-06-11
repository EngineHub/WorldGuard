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

package com.sk89q.worldguard.session.handler;

import com.google.common.collect.Sets;
import com.sk89q.worldguard.bukkit.commands.CommandUtils;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.session.MoveType;
import com.sk89q.worldguard.session.Session;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.Set;

public class FarewellFlag extends Handler {

    public static final Factory FACTORY = new Factory();
    public static class Factory extends Handler.Factory<FarewellFlag> {
        @Override
        public FarewellFlag create(Session session) {
            return new FarewellFlag(session);
        }
    }

    private Set<String> lastMessageStack = Collections.emptySet();

    public FarewellFlag(Session session) {
        super(session);
    }

    private Set<String> getMessages(Player player, ApplicableRegionSet set) {
        return Sets.newLinkedHashSet(set.queryAllValues(getPlugin().wrapPlayer(player), DefaultFlag.FAREWELL_MESSAGE));
    }

    @Override
    public void initialize(Player player, Location current, ApplicableRegionSet set) {
        lastMessageStack = getMessages(player, set);
    }

    @Override
    public boolean onCrossBoundary(Player player, Location from, Location to, ApplicableRegionSet toSet, Set<ProtectedRegion> entered, Set<ProtectedRegion> exited, MoveType moveType) {
        Set<String> messages = getMessages(player, toSet);

        if (!messages.isEmpty()) {
            // Due to flag priorities, we have to collect the lower
            // priority flag values separately
            for (ProtectedRegion region : toSet) {
                String message = region.getFlag(DefaultFlag.FAREWELL_MESSAGE);
                if (message != null) {
                    messages.add(message);
                }
            }
        }

        for (String message : lastMessageStack) {
            if (!messages.contains(message)) {
                String effective = CommandUtils.replaceColorMacros(message);
                effective = getPlugin().replaceMacros(player, effective);
                player.sendMessage(effective.replaceAll("\\\\n", "\n").split("\\n"));
                break;
            }
        }

        lastMessageStack = messages;

        return true;
    }

}
