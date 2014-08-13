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

import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.ConfigurationManager;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.bukkit.util.ProtectedRegionQuery;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A global region manager loads, saves and caches region data for zero or
 * more worlds at a time.
 *
 * <p>This class is thread safe and its contents can be accessed from
 * multiple concurrent threads.</p>
 */
public class GlobalRegionManager {

    private final WorldGuardPlugin plugin;
    private final ManagerContainer container;

    /**
     * Create a new instance.
     *
     * @param plugin the plugin
     */
    public GlobalRegionManager(WorldGuardPlugin plugin) {
        this.plugin = plugin;

        ConfigurationManager config = plugin.getGlobalStateManager();
        container = new ManagerContainer(config);
    }

    /**
     * Load the region data for a world if it has not been loaded already.
     *
     * <p>This method is called by WorldGuard and should not be called
     * by other plugins.</p>
     *
     * @param world the world
     * @return a region manager, either returned from the cache or newly loaded
     */
    @Nullable
    public RegionManager load(World world) {
        checkNotNull(world);

        RegionManager manager = container.load(world.getName());

        if (manager != null) {
            // Bias the region data for loaded chunks
            List<Vector2D> positions = new ArrayList<Vector2D>();
            for (Chunk chunk : world.getLoadedChunks()) {
                positions.add(new Vector2D(chunk.getX(), chunk.getZ()));
            }
            manager.loadChunks(positions);
        }

        return manager;
    }

    /**
     * Load the region data for a list of worlds.
     *
     * <p>This method is called by WorldGuard and should not be called
     * by other plugins.</p>
     *
     * @param worlds a list of worlds
     */
    public void loadAll(Collection<? extends World> worlds) {
        checkNotNull(worlds);

        for (World world : worlds) {
            load(world);
        }
    }

    /**
     * Unload the region data for a world.
     *
     * @param world a world
     */
    public void unload(World world) {
        checkNotNull(world);

        container.unload(world.getName());
    }

    /**
     * Unload all region data for all worlds that region data has
     * been loaded for.
     */
    public void unloadAll() {
        container.unloadAll();
    }

    /**
     * Get the region manager for a world if one exists.
     *
     * <p>This method may return {@code null} if region data for the given
     * world has not been loaded, has failed to load, or support for regions
     * has been disabled.</p>
     *
     * @param world the world
     * @return a region manager, or {@code null} if one is not availale
     */
    @Nullable
    public RegionManager get(World world) {
        return container.get(world.getName());
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
     * Create a new region query with no player.
     *
     * @return a new query
     */
    public ProtectedRegionQuery createAnonymousQuery() {
        return new ProtectedRegionQuery(plugin, (Player) null);
    }

    /**
     * Create a new region query.
     *
     * @param player a player, or {@code null}
     * @return a new query
     */
    public ProtectedRegionQuery createQuery(@Nullable Player player) {
        return new ProtectedRegionQuery(plugin, player);
    }

    /**
     * Create a new region query.
     *
     * @param player a player, or {@code null}
     * @return a new query
     */
    private ProtectedRegionQuery createQuery(@Nullable LocalPlayer player) {
        return new ProtectedRegionQuery(plugin, player);
    }

    /**
     * Test whether the given player has region protection bypass permission.
     *
     * @param player the player
     * @param world the world
     * @return true if a bypass is permitted
     * @deprecated use {@link #createQuery(Player)}
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
     * @deprecated use {@link #createQuery(Player)}
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
     * @deprecated use {@link #createQuery(Player)}
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
     * @deprecated use {@link #createQuery(Player)}
     */
    @Deprecated
    public boolean canBuild(Player player, Location location) {
        return createQuery(player).canBuild(location);
    }

    /**
     * Test whether the player can place blocks at the given position.
     *
     * @param player the player
     * @param block the block
     * @return true if permitted
     * @deprecated use {@link #createQuery(Player)}
     */
    @Deprecated
    @SuppressWarnings("deprecation")
    public boolean canConstruct(Player player, Block block) {
        return canConstruct(player, block.getLocation());
    }

    /**
     * Test whether the player can place blocks at the given position.
     *
     * @param player the player
     * @param location the location
     * @return true if permitted
     * @deprecated use {@link #createQuery(Player)}
     */
    @Deprecated
    public boolean canConstruct(Player player, Location location) {
        return createQuery(player).canConstruct(location);
    }

    /**
     * Test the value of a state flag at a location.
     *
     * @param flag the flag
     * @param location the location
     * @return true if set to true
     * @deprecated use {@link #createQuery(Player)}
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
     * @deprecated use {@link #createQuery(Player)}
     */
    @Deprecated
    public boolean allows(StateFlag flag, Location location, @Nullable LocalPlayer player) {
        return createQuery(player).allows(flag, location);
    }

}
