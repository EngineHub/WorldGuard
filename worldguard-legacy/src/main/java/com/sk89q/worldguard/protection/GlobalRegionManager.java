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

package com.sk89q.worldguard.protection;

import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.BukkitPlayer;
import com.sk89q.worldguard.bukkit.RegionContainer;
import com.sk89q.worldguard.bukkit.RegionQuery;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.association.RegionAssociable;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This is the legacy class for accessing region data.
 *
 * @deprecated use {@link WorldGuardPlugin#getRegionContainer()}
 */
@Deprecated
public class GlobalRegionManager {

    private final WorldGuardPlugin plugin;
    private final RegionContainer container;

    /**
     * Create a new instance.
     *
     * @param plugin the plugin
     * @param container the container
     */
    public GlobalRegionManager(WorldGuardPlugin plugin, RegionContainer container) {
        checkNotNull(plugin);
        checkNotNull(container);
        this.plugin = plugin;
        this.container = container;
    }

    /**
     * Get the region manager for a world if one exists.
     *
     * <p>This method may return {@code null} if region data for the given
     * world has not been loaded, has failed to load, or support for regions
     * has been disabled.</p>
     *
     * @param world the world
     * @return a region manager, or {@code null} if one is not available
     */
    @Nullable
    public RegionManager get(World world) {
        return container.get(world);
    }

    /**
     * Get an immutable list of loaded {@link RegionManager}s.
     *
     * @return a list of managers
     */
    public List<RegionManager> getLoaded() {
        return Collections.unmodifiableList(container.getLoaded());
    }

    /**
     * Create a new region query.
     *
     * @return a new query
     */
    private RegionQuery createQuery() {
        return container.createQuery();
    }

    /**
     * Test whether the given player has region protection bypass permission.
     *
     * @param player the player
     * @param world the world
     * @return true if a bypass is permitted
     * @deprecated use {@link RegionContainer#createQuery()}
     */
    @Deprecated
    public boolean hasBypass(LocalPlayer player, World world) {
        return player.hasPermission("worldguard.region.bypass." + world.getName());
    }

    /**
     * Test whether the given player has region protection bypass permission.
     *
     * @param player the player
     * @param world the world
     * @return true if a bypass is permitted
     * @deprecated use {@link RegionContainer#createQuery()}
     */
    @Deprecated
    public boolean hasBypass(Player player, World world) {
        return plugin.hasPermission(player, "worldguard.region.bypass." + world.getName());
    }

    /**
     * Test whether the player can build (place, use, destroy blocks and
     * entities) at the given position, considering only the build flag
     * and the region's members.
     *
     * <p>This method is not an absolute test as to whether WorldGuard
     * would allow or block an event because this method doesn't
     * consider flags (i.e. chest-access flags when concerning a chest) or
     * other modules in WorldGuard (i.e chest protection).</p>
     *
     * @param player the player
     * @param block the block
     * @return true if a bypass is permitted
     * @deprecated use {@link RegionContainer#createQuery()}
     */
    @SuppressWarnings("deprecation")
    @Deprecated
    public boolean canBuild(Player player, Block block) {
        return canBuild(player, block.getLocation());
    }

    /**
     * Test whether the player can build (place, use, destroy blocks and
     * entities) at the given position, considering only the build flag
     * and the region's members.
     *
     * <p>This method is not an absolute test as to whether WorldGuard
     * would allow or block an event because this method doesn't
     * consider flags (i.e. chest-access flags when concerning a chest) or
     * other modules in WorldGuard (i.e chest protection).</p>
     *
     * @param player the player
     * @param location the location
     * @return true if a bypass is permitted
     * @deprecated use {@link RegionContainer#createQuery()}
     */
    @Deprecated
    public boolean canBuild(Player player, Location location) {
        return hasBypass(player, location.getWorld()) || createQuery().testState(location, player, DefaultFlag.BUILD);

    }

    /**
     * Test whether the player can place blocks at the given position.
     *
     * @param player the player
     * @param block the block
     * @return true if permitted
     * @deprecated the construct flag is being removed
     */
    @Deprecated
    @SuppressWarnings("deprecation")
    public boolean canConstruct(Player player, Block block) {
        return canBuild(player, block.getLocation());
    }

    /**
     * Test whether the player can place blocks at the given position.
     *
     * @param player the player
     * @param location the location
     * @return true if permitted
     * @deprecated the construct flag is being removed
     */
    @Deprecated
    public boolean canConstruct(Player player, Location location) {
        return canBuild(player, location);
    }

    /**
     * Test the value of a state flag at a location.
     *
     * @param flag the flag
     * @param location the location
     * @return true if set to true
     * @deprecated use {@link RegionContainer#createQuery()}
     */
    @Deprecated
    @SuppressWarnings("deprecation")
    public boolean allows(StateFlag flag, Location location) {
        return allows(flag, location, null);
    }

    /**
     * Test the value of a state flag at a location, using the player as the
     * relevant actor.
     *
     * @param flag the flag
     * @param location the location
     * @param player the actor
     * @return true if set to true
     * @deprecated use {@link RegionContainer#createQuery()}
     */
    @Deprecated
    public boolean allows(StateFlag flag, Location location, @Nullable LocalPlayer player) {
        if (player == null) {
            return StateFlag.test(createQuery().queryState(location, (RegionAssociable) null, flag));
        } else if (player instanceof BukkitPlayer) {
            Player p = ((BukkitPlayer) player).getPlayer();
            return StateFlag.test(createQuery().queryState(location, p, flag));
        } else {
            throw new IllegalArgumentException("Can't take a non-Bukkit player");
        }
    }

}
