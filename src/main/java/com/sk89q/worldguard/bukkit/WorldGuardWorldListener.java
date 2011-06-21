// $Id$
/*
 * Copyright (C) 2010 sk89q <http://www.sk89q.com>
 * All rights reserved.
*/
package com.sk89q.worldguard.bukkit;

import org.bukkit.entity.*;
import org.bukkit.event.Event;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.WorldListener;
import org.bukkit.plugin.PluginManager;

import java.util.logging.Logger;

public class WorldGuardWorldListener extends WorldListener {

    /**
     * Logger for messages.
     */
    private static final Logger logger = Logger.getLogger("Minecraft.WorldGuard");

    private WorldGuardPlugin plugin;

    /**
     * Construct the object;
     *
     * @param plugin
     */
    public WorldGuardWorldListener(WorldGuardPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Register events.
     */
    public void registerEvents() {
        PluginManager pm = plugin.getServer().getPluginManager();

        pm.registerEvent(Event.Type.CHUNK_LOAD, this, Event.Priority.Normal, plugin);
    }

    /**
     * Called when a chunk is loaded.
     */
    public void onChunkLoad(ChunkLoadEvent event) {
        ConfigurationManager cfg = plugin.getGlobalStateManager();

        if (cfg.activityHaltToggle) {
            int removed = 0;

            for (Entity entity : event.getChunk().getEntities()) {
                if (entity instanceof Item
                        || (entity instanceof LivingEntity && !(entity instanceof Tameable))) {
                    entity.remove();
                    removed++;
                }
            }

            if (removed > 50) {
                logger.info("WG Halt-Act: " + removed + " entities (>50) auto-removed from "
                        + event.getChunk().toString());
            }
        }
    }
}
