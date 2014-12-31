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

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.bukkit.listener.FlagStateManager;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;

public final class RegionQueryUtil {

    private RegionQueryUtil() {
    }

    public static boolean isInvincible(WorldGuardPlugin plugin, Player player) {
        return isInvincible(plugin, player, null);
    }

    public static boolean isInvincible(WorldGuardPlugin plugin, Player player,
                                       ApplicableRegionSet set) {
        Location loc = player.getLocation();
        World world = player.getWorld();
        
        FlagStateManager.PlayerFlagState state = plugin.getFlagStateManager().getState(player);

        if (state.lastInvincibleWorld == null ||
                !state.lastInvincibleWorld.equals(world) ||
                state.lastInvincibleX != loc.getBlockX() ||
                state.lastInvincibleY != loc.getBlockY() ||
                state.lastInvincibleZ != loc.getBlockZ()) {
            state.lastInvincibleX = loc.getBlockX();
            state.lastInvincibleY = loc.getBlockY();
            state.lastInvincibleZ = loc.getBlockZ();
            state.lastInvincibleWorld = world;

            if (set == null) {
                Vector vec = new Vector(state.lastInvincibleX,
                        state.lastInvincibleY, state.lastInvincibleZ);
                RegionManager mgr = plugin.getGlobalRegionManager().get(world);
                set = mgr.getApplicableRegions(vec);
            }

            state.wasInvincible = set.allows(DefaultFlag.INVINCIBILITY, plugin.wrapPlayer(player));
        }

        return state.wasInvincible;
    }

    public static Boolean isAllowedInvinciblity(WorldGuardPlugin plugin, Player player) {
        World world = player.getWorld();
        FlagStateManager.PlayerFlagState state = plugin.getFlagStateManager().getState(player);
        Vector vec = new Vector(state.lastInvincibleX, state.lastInvincibleY, state.lastInvincibleZ);

        StateFlag.State regionState = plugin.getGlobalRegionManager().get(world).
                getApplicableRegions(vec).getFlag(DefaultFlag.INVINCIBILITY, plugin.wrapPlayer(player));
        if (regionState == StateFlag.State.ALLOW) {
            return Boolean.TRUE;
        } else if (regionState == StateFlag.State.DENY) {
            return Boolean.FALSE;
        } else {
            return null;
        }
    }
}
