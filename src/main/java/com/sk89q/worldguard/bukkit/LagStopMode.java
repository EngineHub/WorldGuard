// $Id$
/*
 * This file is a part of WorldGuard.
 * Copyright (c) sk89q <http://www.sk89q.com>
 * Copyright (c) the WorldGuard team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.sk89q.worldguard.bukkit;

import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.Tameable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
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
import org.bukkit.plugin.PluginManager;

import com.sk89q.rebar.util.LoggerUtils;

/**
 * Handles the "stop lag" mode.
 */
public class LagStopMode implements Listener {

    private static final Logger logger = LoggerUtils.getLogger(LagStopMode.class,
            "[WorldGuard] Lag Stop Mode: ");

    private final WorldGuardPlugin plugin;
    private boolean enabled = false;
    private boolean removeAnimals = true;

    LagStopMode(WorldGuardPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Register this class's events.
     */
    void registerEvents() {
        PluginManager pm = plugin.getServer().getPluginManager();
        pm.registerEvents(this, plugin);
    }

    /**
     * Get whether the lag stop mode is enabled.
     *
     * @return true if it's enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Enable the lag stop mode. This will start the process and emit messages.
     *
     * @param unloadChunks true to unload chunks
     */
    public void enable(boolean unloadChunks) {
        if (!enabled) {
            Server server = plugin.getServer();

            server.broadcastMessage(ChatColor.GRAY
                    + "(WorldGuard's 'lag stop' mode has been enabled. Some parts of the game may be 'frozen'. Keep calm and carry on.)");

            // Remove entities
            server.broadcastMessage(ChatColor.GRAY + "(Please wait... removing entities...)");
            for (World world : server.getWorlds()) {
                freeUp(world);
            }

            // Unloading chunks
            if (unloadChunks) {
                server.broadcastMessage(ChatColor.GRAY + "(Please wait... garbage collecting chunks...)");
                for (World world : server.getWorlds()) {
                    Chunk[] chunks = world.getLoadedChunks();

                    for (Chunk chunk : chunks) {
                        chunk.unload(true, true);
                    }
                }
            }

            server.broadcastMessage(ChatColor.GRAY + "(Completed. Lag stop mode is still enabled.)");
        }

        this.enabled = true;

    }

    /**
     * Disable the lag stop mode.
     */
    public void disable() {
        if (enabled) {
            Server server = plugin.getServer();
            server.broadcastMessage(ChatColor.GRAY
                    + "(WorldGuard's 'lag stop' mode is now off. Everything should proceed to start working again.)");
        }

        this.enabled = false;

    }

    /**
     * Returns whether animals are being removed.
     *
     * @return true if animals are being removed
     */
    public boolean isRemovingAnimals() {
        return removeAnimals;
    }

    /**
     * Set whether animals should be removed.
     *
     * @param removeAnimals true to remove animals
     */
    public void setRemoveAnimals(boolean removeAnimals) {
        this.removeAnimals = removeAnimals;
    }

    /**
     * Remove items and other things within the chunk.
     *
     * @param plugin the plugin
     * @param chunk the chunk to halt
     */
    private void freeUp(Chunk chunk) {
        int removed = 0;

        for (Entity entity : chunk.getEntities()) {
            if (shouldRemove(entity)) {
                entity.remove();
                removed++;
            }
        }

        if (removed > 50) {
            logger.info(removed + " entities (>50) auto-removed from " + chunk.toString());
        }
    }

    /**
     * Remove items and other things within the world.
     *
     * @param plugin the plugin
     * @param world the world to halt
     */
    private void freeUp(World world) {
        int removed = 0;

        for (Entity entity : world.getEntities()) {
            if (shouldRemove(entity)) {
                entity.remove();
                removed++;
            }
        }

        if (removed > 50) {
            logger.info(removed + " entities (>50) auto-removed from " + world.toString());
        }
    }

    /**
     * Returns whether an entity should be removed.
     *
     * @param entity the entity to check
     * @return true if it should be removed
     */
    private boolean shouldRemove(Entity entity) {
        return entity instanceof Item
                || entity instanceof TNTPrimed
                || entity instanceof ExperienceOrb
                || entity instanceof FallingBlock
                || (entity instanceof LivingEntity
                    && !(entity instanceof Tameable)
                    && !(entity instanceof Player)
                    && (removeAnimals || !(entity instanceof Animals)));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        World world = player.getWorld();

        if (enabled) {
            player.sendMessage(ChatColor.GRAY
                    + "WorldGuard's 'lag stop' mode has been enabled. Some parts of the game may be 'frozen'.");
            freeUp(world);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockFromTo(BlockFromToEvent event) {
        if (enabled) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockIgnite(BlockIgniteEvent event) {
        if (enabled) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockBurn(BlockBurnEvent event) {
        if (enabled) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockPhysics(BlockPhysicsEvent event) {
        if (enabled) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockForm(BlockFormEvent event) {
        if (enabled) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onLeavesDecay(LeavesDecayEvent event) {
        if (enabled) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockSpread(BlockSpreadEvent event) {
        if (enabled) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityExplode(EntityExplodeEvent event) {
        if (enabled) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onExplosionPrime(ExplosionPrimeEvent event) {
        if (enabled) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (enabled) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChunkLoad(ChunkLoadEvent event) {
        if (enabled) {
            freeUp(event.getChunk());
        }
    }

}
