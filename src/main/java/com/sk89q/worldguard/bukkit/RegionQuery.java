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
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.StateFlag.State;
import com.sk89q.worldguard.protection.managers.RegionManager;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.Collection;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This object allows easy spatial queries involving region data.
 *
 * <p>Results may be cached for brief amounts of time.</p>
 */
public class RegionQuery {

    private final WorldGuardPlugin plugin;
    private final ConfigurationManager config;
    @SuppressWarnings("deprecation")
    private final GlobalRegionManager globalManager;
    private final QueryCache cache;

    /**
     * Create a new instance.
     *
     * @param plugin the plugin
     * @param cache the query cache
     */
    RegionQuery(WorldGuardPlugin plugin, QueryCache cache) {
        checkNotNull(plugin);
        checkNotNull(cache);

        this.plugin = plugin;
        this.config = plugin.getGlobalStateManager();
        this.cache = cache;
        //noinspection deprecation
        this.globalManager = plugin.getGlobalRegionManager();
    }

    /**
     * Query for regions containing the given location.
     *
     * <p>An instance of {@link ApplicableRegionSet} will always be returned,
     * even if regions are disabled or region data failed to load. An
     * appropriate "virtual" set will be returned in such a case
     * (for example, if regions are disabled, the returned set
     * would permit all activities).</p>
     *
     * @param location the location
     * @return a region set
     */
    public ApplicableRegionSet getApplicableRegions(Location location) {
        checkNotNull(location);

        World world = location.getWorld();
        WorldConfiguration worldConfig = config.get(world);

        if (!worldConfig.useRegions) {
            return ApplicableRegionSet.getEmpty();
        }

        RegionManager manager = globalManager.get(location.getWorld());
        if (manager != null) {
            return cache.queryContains(manager, location);
        } else {
            return ApplicableRegionSet.getEmpty();
        }
    }

    /**
     * Test whether the given player is permitted to modify or interact with
     * blocks at the given location. Additional flags to be considered can be
     * provided. The {@code BUILD} flag is already included in the list of
     * flags considered.
     *
     * @param location the location
     * @param player the player
     * @param flags zero or more flags
     * @return true if permission is granted
     * @see ApplicableRegionSet#testBuild(LocalPlayer, StateFlag...)
     */
    public boolean testBuild(Location location, Player player, StateFlag... flags) {
        checkNotNull(location);
        checkNotNull(player);
        checkNotNull(flags);

        World world = location.getWorld();
        WorldConfiguration worldConfig = config.get(world);

        if (!worldConfig.useRegions) {
            return true;
        }

        if (player.hasPermission("worldguard.region.bypass." + world.getName())) {
            return true;
        }

        LocalPlayer localPlayer = plugin.wrapPlayer(player);
        return getApplicableRegions(location).testBuild(localPlayer, flags);
    }

    /**
     * Get the effective value for a flag. If there are multiple values
     * (for example, if there are multiple regions with the same priority
     * but with different farewell messages set, there would be multiple
     * completing values), then the selected (or "winning") value will depend
     * on the flag type.
     *
     * <p>This method does <strong>not</strong> properly process build
     * permissions. Instead, use {@link #testBuild(Location, Player, StateFlag...)}
     * for that purpose.</p>
     *
     * <p>This method does the same as
     * {@link #queryState(Location, Player, StateFlag...)} except that it
     * returns a boolean when the result is {@code ALLOW}.</p>
     *
     * @param location the location
     * @param player an optional player, which would be used to determine the region group to apply
     * @param flag the flag
     * @return true if the result was {@code ALLOW}
     * @see ApplicableRegionSet#queryValue(LocalPlayer, Flag)
     */
    public boolean testState(Location location, @Nullable Player player, StateFlag flag) {
        return StateFlag.test(queryState(location, player, flag));
    }

    /**
     * Get the effective value for a list of state flags. The rules of
     * states is observed here; that is, {@code DENY} overrides {@code ALLOW},
     * and {@code ALLOW} overrides {@code NONE}.
     *
     * <p>This method does <strong>not</strong> properly process build
     * permissions. Instead, use {@link #testBuild(Location, Player, StateFlag...)}
     * for that purpose.</p>
     *
     * See {@link ApplicableRegionSet#queryState(LocalPlayer, StateFlag...)}
     * for more information.
     *
     * @param location the location
     * @param player an optional player, which would be used to determine the region groups that apply
     * @param flags a list of flags to check
     * @return a state
     * @see ApplicableRegionSet#queryState(LocalPlayer, StateFlag...)
     */
    @Nullable
    public State queryState(Location location, @Nullable Player player, StateFlag... flags) {
        LocalPlayer localPlayer = player != null ? plugin.wrapPlayer(player) : null;
        return getApplicableRegions(location).queryState(localPlayer, flags);
    }

    /**
     * Get the effective value for a flag. If there are multiple values
     * (for example, if there are multiple regions with the same priority
     * but with different farewell messages set, there would be multiple
     * completing values), then the selected (or "winning") value will depend
     * on the flag type.
     *
     * <p>This method does <strong>not</strong> properly process build
     * permissions. Instead, use {@link #testBuild(Location, Player, StateFlag...)}
     * for that purpose.</p>
     *
     * <p>See {@link ApplicableRegionSet#queryValue(LocalPlayer, Flag)} for
     * more information.</p>
     *
     * @param location the location
     * @param player an optional player, which would be used to determine the region group to apply
     * @param flag the flag
     * @return a value, which could be {@code null}
     * @see ApplicableRegionSet#queryValue(LocalPlayer, Flag)
     */
    @Nullable
    public <V> V queryValue(Location location, @Nullable Player player, Flag<V> flag) {
        LocalPlayer localPlayer = player != null ? plugin.wrapPlayer(player) : null;
        return getApplicableRegions(location).queryValue(localPlayer, flag);
    }

    /**
     * Get the effective values for a flag, returning a collection of all
     * values. It is up to the caller to determine which value, if any,
     * from the collection will be used.
     *
     * <p>This method does <strong>not</strong> properly process build
     * permissions. Instead, use {@link #testBuild(Location, Player, StateFlag...)}
     * for that purpose.</p>
     *
     * <p>See {@link ApplicableRegionSet#queryAllValues(LocalPlayer, Flag)}
     * for more information.</p>
     *
     * @param location the location
     * @param player an optional player, which would be used to determine the region group to apply
     * @param flag the flag
     * @return a collection of values
     * @see ApplicableRegionSet#queryAllValues(LocalPlayer, Flag)
     */
    public <V> Collection<V> queryAllValues(Location location, @Nullable Player player, Flag<V> flag) {
        LocalPlayer localPlayer = player != null ? plugin.wrapPlayer(player) : null;
        return getApplicableRegions(location).queryAllValues(localPlayer, flag);
    }

}
