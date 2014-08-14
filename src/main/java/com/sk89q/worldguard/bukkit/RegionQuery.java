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

package com.sk89q.worldguard.bukkit;

import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.GlobalRegionManager;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This object allows easy spatial queries involving region data.
 *
 * <p>Results may be cached for brief amounts of time.</p>
 */
public class RegionQuery {

    private final ConfigurationManager config;
    private final GlobalRegionManager globalManager;
    @Nullable
    private final LocalPlayer localPlayer;

    /**
     * Create a new instance.
     *
     * @param plugin the plugin
     * @param player an optional player
     */
    RegionQuery(WorldGuardPlugin plugin, @Nullable Player player) {
        this(plugin, player != null ? plugin.wrapPlayer(player) : null);
    }

    /**
     * Create a new instance.
     *
     * @param plugin the plugin
     * @param player an optional player
     */
    RegionQuery(WorldGuardPlugin plugin, @Nullable LocalPlayer player) {
        checkNotNull(plugin);

        this.config = plugin.getGlobalStateManager();
        //noinspection deprecation
        this.globalManager = plugin.getGlobalRegionManager();
        this.localPlayer = player;
    }

    /**
     * Test whether the player (which must not be {@code null} can build at
     * the given location, using only the membership information and the state
     * of the {@link DefaultFlag#BUILD} flag to determine status.
     *
     * <p>This method is used to check blocks and entities for which there
     * are no other related flags for (i.e. beds have the
     * {@link DefaultFlag#SLEEP} flag).</p>
     *
     * <p>If region data is not available (it failed to load or region support
     * is disabled), then either {@code true} or {@code false} may be returned
     * depending on the configuration.</p>
     *
     * @param location the location
     * @return true if building is permitted
     * @throws NullPointerException if there is no player for this query
     */
    public boolean testPermission(Location location) {
        checkNotNull(location);
        checkNotNull(localPlayer, "testPermission() requires a player for the query");
        
        World world = location.getWorld();
        WorldConfiguration worldConfig = config.get(world);

        if (!worldConfig.useRegions) {
            return true;
        }

        if (globalManager.hasBypass(localPlayer, world)) {
            return true;
        } else {
            RegionManager manager = globalManager.get(location.getWorld());
            return manager == null || manager.getApplicableRegions(BukkitUtil.toVector(location)).canBuild(localPlayer);
        }
    }

    /**
     * Test whether the player (which must not be {@code null} can build at
     * the given location, using the membership information, state
     * of the {@link DefaultFlag#BUILD} flag, and the state of any passed
     * flags.
     *
     * <p>This method is used to check blocks and entities for which there
     * are other related flags for (i.e. beds have the
     * {@link DefaultFlag#SLEEP} flag). The criteria under which this method
     * returns true is subject to change (i.e. all flags must be true or
     * one cannot be DENY, etc.).</p>
     *
     * <p>If region data is not available (it failed to load or region support
     * is disabled), then either {@code true} or {@code false} may be returned
     * depending on the configuration.</p>
     *
     * @param location the location to test
     * @param flags an array of flags
     * @return true if the flag tests true
     */
    public boolean testPermission(Location location, StateFlag... flags) {
        checkNotNull(location);
        checkNotNull(flags);
        checkNotNull(localPlayer, "testPermission() requires a player for the query");

        World world = location.getWorld();
        WorldConfiguration worldConfig = config.get(world);

        if (!worldConfig.useRegions) {
            return true;
        }

        RegionManager manager = globalManager.get(location.getWorld());

        if (manager != null) {
            ApplicableRegionSet result = manager.getApplicableRegions(BukkitUtil.toVector(location));

            if (result.canBuild(localPlayer)) {
                return true;
            }

            for (StateFlag flag : flags) {
                if (result.allows(flag, localPlayer)) {
                    return true;
                }
            }

            return false;
        } else{
            return true; // null manager -> return true for now
        }
    }

    /**
     * Test whether a {@link StateFlag} is evaluates to {@code ALLOW}.
     *
     * <p>This method is to check whether certain functionality
     * is enabled (i.e. water flow). The player, if provided, may be used
     * in evaluation of the flag.</p>
     *
     * <p>If region data is not available (it failed to load or region support
     * is disabled), then either {@code true} or {@code false} may be returned
     * depending on the configuration.</p>
     *
     * @param location the location
     * @param flag the flag
     * @return true if the flag evaluates to {@code ALLOW}
     */
    public boolean testEnabled(Location location, StateFlag flag) {
        checkNotNull(location);
        checkNotNull(flag);

        World world = location.getWorld();
        WorldConfiguration worldConfig = config.get(world);

        if (!worldConfig.useRegions) {
            return true;
        }

        if (globalManager.hasBypass(localPlayer, world)) {
            return true;
        } else {
            RegionManager manager = globalManager.get(location.getWorld());
            return manager == null || manager.getApplicableRegions(BukkitUtil.toVector(location)).allows(flag, localPlayer);
        }
    }

}
