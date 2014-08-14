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

import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.protection.managers.RegionManager;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A region container creates {@link RegionManager}s for loaded worlds, which
 * allows access to the region data of a world. Generally, only data is
 * loaded for worlds that are loaded in the server.
 *
 * <p>This class is thread safe and its contents can be accessed from
 * multiple concurrent threads.</p>
 *
 * <p>An instance of this class can be retrieved using
 * {@link WorldGuardPlugin#getRegionContainer()}.</p>
 */
public class RegionContainer {

    private final Object lock = new Object();
    private final WorldGuardPlugin plugin;
    private final ManagerContainer container;

    /**
     * Create a new instance.
     *
     * @param plugin the plugin
     */
    RegionContainer(WorldGuardPlugin plugin) {
        this.plugin = plugin;

        ConfigurationManager config = plugin.getGlobalStateManager();
        container = new ManagerContainer(config);
    }

    /**
     * Initialize the region container.
     */
    void initialize() {
        container.initialize();

        loadWorlds();

        Bukkit.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onWorldLoad(WorldLoadEvent event) {
                load(event.getWorld());
            }

            @EventHandler
            public void onWorldUnload(WorldUnloadEvent event) {
                unload(event.getWorld());
            }

            @EventHandler
            public void onChunkLoad(ChunkLoadEvent event) {
                RegionManager manager = get(event.getWorld());
                if (manager != null) {
                    Chunk chunk = event.getChunk();
                    manager.loadChunk(new Vector2D(chunk.getX(), chunk.getZ()));
                }
            }

            @EventHandler
            public void onChunkUnload(ChunkUnloadEvent event) {
                RegionManager manager = get(event.getWorld());
                if (manager != null) {
                    Chunk chunk = event.getChunk();
                    manager.unloadChunk(new Vector2D(chunk.getX(), chunk.getZ()));
                }
            }
        }, plugin);
    }

    /**
     * Save data and unload.
     */
    void unload() {
        synchronized (lock) {
            container.unloadAll();
        }
    }

    /**
     * Try loading the region managers for all currently loaded worlds.
     */
    private void loadWorlds() {
        synchronized (lock) {
            for (World world : Bukkit.getServer().getWorlds()) {
                load(world);
            }
        }
    }

    /**
     * Reload the region container.
     *
     * <p>This method may block until the data for all loaded worlds has been
     * unloaded and new data has been loaded.</p>
     */
    public void reload() {
        synchronized (lock) {
            unload();
            loadWorlds();
        }
    }

    /**
     * Load the region data for a world if it has not been loaded already.
     *
     * @param world the world
     * @return a region manager, either returned from the cache or newly loaded
     */
    @Nullable
    private RegionManager load(World world) {
        checkNotNull(world);

        RegionManager manager;

        synchronized (lock) {
            manager = container.load(world.getName());

            if (manager != null) {
                // Bias the region data for loaded chunks
                List<Vector2D> positions = new ArrayList<Vector2D>();
                for (Chunk chunk : world.getLoadedChunks()) {
                    positions.add(new Vector2D(chunk.getX(), chunk.getZ()));
                }
                manager.loadChunks(positions);
            }
        }

        return manager;
    }

    /**
     * Unload the region data for a world.
     *
     * @param world a world
     */
    void unload(World world) {
        checkNotNull(world);

        synchronized (lock) {
            container.unload(world.getName());
        }
    }

    /**
     * Get the region manager for a world if one exists.
     *
     * <p>This method may return {@code null} if region data for the given
     * world has not been loaded, has failed to load, or support for regions
     * has been disabled. If you merely want to query flags or a list of
     * regions in a position, using {@link #createQuery(Player)} is much
     * simpler and it will handle the case of a {@code null}
     * {@code RegionManager}. That said, if you wish to make changes to
     * regions, then you will have to get a region manager.</p>
     *
     * @param world the world
     * @return a region manager, or {@code null} if one is not available
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
    public RegionQuery createAnonymousQuery() {
        return new RegionQuery(plugin, (Player) null);
    }

    /**
     * Create a new region query.
     *
     * @param player a player, or {@code null}
     * @return a new query
     */
    public RegionQuery createQuery(@Nullable Player player) {
        return new RegionQuery(plugin, player);
    }

    /**
     * Create a new region query.
     *
     * @param player a player, or {@code null}
     * @return a new query
     */
    public RegionQuery createQuery(@Nullable LocalPlayer player) {
        return new RegionQuery(plugin, player);
    }

}
