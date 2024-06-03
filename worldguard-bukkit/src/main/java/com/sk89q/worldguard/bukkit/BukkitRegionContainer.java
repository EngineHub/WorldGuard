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

import static com.google.common.base.Preconditions.checkNotNull;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.config.WorldConfiguration;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

public class BukkitRegionContainer extends RegionContainer {

    /**
     * Invalidation frequency in ticks.
     */
    private static final int CACHE_INVALIDATION_INTERVAL = 2;

    private final WorldGuardPlugin plugin;

    /**
     * Create a new instance.
     *
     * @param plugin the plugin
     */
    public BukkitRegionContainer(WorldGuardPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void initialize() {
        super.initialize();
        Bukkit.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onWorldLoad(WorldLoadEvent event) {
                load(BukkitAdapter.adapt(event.getWorld()));
            }

            @EventHandler
            public void onWorldUnload(WorldUnloadEvent event) {
                unload(BukkitAdapter.adapt(event.getWorld()));
            }

            @EventHandler
            public void onChunkLoad(ChunkLoadEvent event) {
                RegionManager manager = get(BukkitAdapter.adapt(event.getWorld()));
                if (manager != null) {
                    Chunk chunk = event.getChunk();
                    manager.loadChunk(BlockVector2.at(chunk.getX(), chunk.getZ()));
                }
            }

            @EventHandler
            public void onChunkUnload(ChunkUnloadEvent event) {
                RegionManager manager = get(BukkitAdapter.adapt(event.getWorld()));
                if (manager != null) {
                    Chunk chunk = event.getChunk();
                    manager.unloadChunk(BlockVector2.at(chunk.getX(), chunk.getZ()));
                }
            }
        }, plugin);

        plugin.getScheduler().runAsyncRate(cache::invalidateAll, CACHE_INVALIDATION_INTERVAL, CACHE_INVALIDATION_INTERVAL);
    }

    public void shutdown() {
        container.shutdown();
    }

    @Override
    @Nullable
    protected RegionManager load(World world) {
        checkNotNull(world);

        WorldConfiguration config = WorldGuard.getInstance().getPlatform().getGlobalStateManager().get(world);
        if (!config.useRegions) {
            return null;
        }

        RegionManager manager;

        synchronized (lock) {
            manager = container.load(world.getName());

            if (manager != null) {
                // Bias the region data for loaded chunks
                List<BlockVector2> positions = new ArrayList<>();
                for (Chunk chunk : ((BukkitWorld) world).getWorld().getLoadedChunks()) {
                    positions.add(BlockVector2.at(chunk.getX(), chunk.getZ()));
                }
                manager.loadChunks(positions);
            }
        }

        return manager;
    }

}
