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

package com.sk89q.worldguard.bukkit.listener;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.config.ConfigurationManager;
import com.sk89q.worldguard.config.WorldConfiguration;
import com.sk89q.worldguard.util.Entities;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.WorldLoadEvent;

import java.util.logging.Logger;

public class WorldGuardWorldListener extends AbstractListener {

    private static final Logger log = Logger.getLogger(WorldGuardWorldListener.class.getCanonicalName());

    public WorldGuardWorldListener(WorldGuardPlugin plugin) {
        super(plugin);
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        ConfigurationManager cfg = getConfig();

        if (cfg.activityHaltToggle) {
            int removed = 0;

            for (Entity entity : event.getChunk().getEntities()) {
                if (Entities.isIntensiveEntity(BukkitAdapter.adapt(entity))) {
                    entity.remove();
                    removed++;
                }
            }

            if (removed > 50) {
                log.info("Интенсивная активность: " + removed + " энтити (>50) автоматически удалены из " + event.getChunk().toString());
            }
        }
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        initWorld(event.getWorld());
    }

    /**
     * Initialize the settings for the specified world
     * @see WorldConfiguration#alwaysRaining
     * @see WorldConfiguration#disableWeather
     * @see WorldConfiguration#alwaysThundering
     * @see WorldConfiguration#disableThunder
     * @param world The specified world
     */
    public void initWorld(World world) {
        WorldConfiguration wcfg = getWorldConfig(world);
        if (wcfg.alwaysRaining && !wcfg.disableWeather) {
            world.setStorm(true);
        } else if (wcfg.disableWeather && !wcfg.alwaysRaining) {
            world.setStorm(false);
        }
        if (wcfg.alwaysThundering && !wcfg.disableThunder) {
            world.setThundering(true);
        } else if (wcfg.disableThunder && !wcfg.alwaysThundering) {
            world.setStorm(false);
        }
    }
}
