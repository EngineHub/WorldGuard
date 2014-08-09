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

package com.sk89q.worldguard.bukkit.listener.module;

import com.google.common.base.Predicate;
import com.sk89q.worldguard.bukkit.BukkitUtil;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.world.ChunkLoadEvent;

import java.util.logging.Logger;

/**
 * Halts ticking of the world to a certain degree when enabled.
 */
public class TickHaltingListener implements Listener {

    private static final Logger log = Logger.getLogger(TickHaltingListener.class.getCanonicalName());
    private final Predicate<Chunk> predicate;

    public TickHaltingListener(Predicate<Chunk> predicate) {
        this.predicate = predicate;
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBurn(BlockBurnEvent event) {
        if (predicate.apply(event.getBlock().getLocation().getChunk())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockForm(BlockFormEvent event) {
        if (predicate.apply(event.getBlock().getLocation().getChunk())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockFromTo(BlockFromToEvent event) {
        if (predicate.apply(event.getBlock().getLocation().getChunk())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockIgnite(BlockIgniteEvent event) {
        if (predicate.apply(event.getBlock().getLocation().getChunk())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPhysics(BlockPhysicsEvent event) {
        if (predicate.apply(event.getBlock().getLocation().getChunk())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockSpread(BlockSpreadEvent event) {
        if (predicate.apply(event.getBlock().getLocation().getChunk())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onLeavesDecay(LeavesDecayEvent event) {
        if (predicate.apply(event.getBlock().getLocation().getChunk())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (predicate.apply(event.getEntity().getLocation().getChunk())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        Entity entity = event.getEntity();

        if (predicate.apply(event.getLocation().getChunk())) {
            if (entity != null) {
                entity.remove();
            }
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onExplosionPrime(ExplosionPrimeEvent event) {
        Entity entity = event.getEntity();

        if (predicate.apply(entity.getLocation().getChunk())) {
            entity.remove();
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        World world = player.getWorld();

        if (predicate.apply(player.getLocation().getChunk())) {
            player.sendMessage(ChatColor.YELLOW + "Note: WorldGuard's /stoplag is currently active.");

            int removed = 0;

            for (Entity entity : world.getEntities()) {
                if (BukkitUtil.isIntensiveEntity(entity)) {
                    entity.remove();
                    removed++;
                }
            }

            if (removed > 10) {
                log.info("Lag Stop Mode: " + removed + " entities (>10) auto-removed from " + player.getWorld());
            }
        }
    }


    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        if (predicate.apply(event.getChunk())) {
            int removed = 0;

            for (Entity entity : event.getChunk().getEntities()) {
                if (BukkitUtil.isIntensiveEntity(entity)) {
                    entity.remove();
                    removed++;
                }
            }

            if (removed > 50) {
                log.info("Lag Stop Mode: " + removed + " entities (>50) auto-removed from " + event.getChunk());
            }
        }
    }

}
