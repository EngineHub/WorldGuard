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

import com.sk89q.worldguard.bukkit.ConfigurationManager;
import com.sk89q.worldguard.bukkit.WorldConfiguration;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import org.bukkit.World;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Painting;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingBreakEvent.RemoveCause;
import org.bukkit.projectiles.ProjectileSource;

/**
 * Listener for painting related events.
 *
 * @author BangL <henno.rickowski@gmail.com>
 */
public class WorldGuardHangingListener implements Listener {

    private WorldGuardPlugin plugin;

    /**
     * Construct the object;
     *
     * @param plugin The plugin instance
     */
    public WorldGuardHangingListener(WorldGuardPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Register events.
     */
    public void registerEvents() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onHangingBreak(HangingBreakEvent event) {
        Hanging hanging = event.getEntity();
        World world = hanging.getWorld();
        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(world);

        if (event instanceof HangingBreakByEntityEvent) {
            HangingBreakByEntityEvent entityEvent = (HangingBreakByEntityEvent) event;
            Entity removerEntity = entityEvent.getRemover();
            if (removerEntity instanceof Projectile) {
                Projectile projectile = (Projectile) removerEntity;
                ProjectileSource remover = projectile.getShooter(); 
                removerEntity = (remover instanceof LivingEntity ? (LivingEntity) remover : null);
            }

            if (!(removerEntity instanceof Player)) {
                if (removerEntity instanceof Creeper) {
                    if (wcfg.blockCreeperBlockDamage || wcfg.blockCreeperExplosions) {
                        event.setCancelled(true);
                        return;
                    }
                    if (wcfg.useRegions && !plugin.getGlobalRegionManager().allows(DefaultFlag.CREEPER_EXPLOSION, hanging.getLocation())) {
                        event.setCancelled(true);
                        return;
                    }
                }

                // this now covers dispensers as well, if removerEntity is null above,
                // due to a non-LivingEntity ProjectileSource
                if (hanging instanceof Painting
                        && (wcfg.blockEntityPaintingDestroy
                        || (wcfg.useRegions
                        && !plugin.getGlobalRegionManager().allows(DefaultFlag.ENTITY_PAINTING_DESTROY, hanging.getLocation())))) {
                    event.setCancelled(true);
                } else if (hanging instanceof ItemFrame
                        && (wcfg.blockEntityItemFrameDestroy
                        || (wcfg.useRegions
                        && !plugin.getGlobalRegionManager().allows(DefaultFlag.ENTITY_ITEM_FRAME_DESTROY, hanging.getLocation())))) {
                    event.setCancelled(true);
                }
            }
        } else {
            // Explosions from mobs are not covered by HangingBreakByEntity
            if (hanging instanceof Painting && wcfg.blockEntityPaintingDestroy
                    && event.getCause() == RemoveCause.EXPLOSION) {
                event.setCancelled(true);
            } else if (hanging instanceof ItemFrame && wcfg.blockEntityItemFrameDestroy
                    && event.getCause() == RemoveCause.EXPLOSION) {
                event.setCancelled(true);
            }
        }
    }

}
