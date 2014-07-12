// $Id$
/*
 * WorldGuard
 * Copyright (C) 2010 sk89q <http://www.sk89q.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.sk89q.worldguard.bukkit;

import static com.sk89q.worldguard.bukkit.BukkitUtil.toVector;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.block.Block;
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
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.projectiles.ProjectileSource;

import com.sk89q.worldedit.blocks.ItemID;
import com.sk89q.worldguard.blacklist.events.BlockBreakBlacklistEvent;
import com.sk89q.worldguard.blacklist.events.ItemUseBlacklistEvent;
import com.sk89q.worldguard.protection.flags.DefaultFlag;

/**
 * Listener for painting related events.
 *
 * @author BangL <henno.rickowski@gmail.com>
 */
public class WorldGuardHangingListener implements Listener {

    private WorldGuardPlugin plugin;
    private final LanguageManager lang;

    /**
     * Construct the object;
     *
     * @param plugin The plugin instance
     */
    public WorldGuardHangingListener(WorldGuardPlugin plugin) {
        this.plugin = plugin;
        lang = plugin.getLang();
    }

    /**
     * Register events.
     */
    public void registerEvents() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onHangingingBreak(HangingBreakEvent event) {
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

            if (removerEntity instanceof Player) {
                Player player = (Player) removerEntity;

                if (wcfg.getBlacklist() != null) {
                    if (hanging instanceof Painting
                            && !wcfg.getBlacklist().check(
                                new BlockBreakBlacklistEvent(plugin.wrapPlayer(player),
                                        toVector(player.getLocation()), ItemID.PAINTING), false, false)) {
                        event.setCancelled(true);
                        return;
                    } else if (hanging instanceof ItemFrame
                            && !wcfg.getBlacklist().check(
                                new BlockBreakBlacklistEvent(plugin.wrapPlayer(player),
                                        toVector(player.getLocation()), ItemID.ITEM_FRAME), false, false)) {
                        event.setCancelled(true);
                        return;
                    }
                }

                if (wcfg.useRegions) {
                    if (!plugin.getGlobalRegionManager().canBuild(player, hanging.getLocation())) {
                        player.sendMessage(lang.getText("you-dont-have-permission-area"));
                        event.setCancelled(true);
                        return;
                    }
                }
            } else {
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

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onHangingPlace(HangingPlaceEvent event) {
        Block placedOn = event.getBlock();
        Player player = event.getPlayer();
        World world = placedOn.getWorld();

        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(world);

        if (wcfg.getBlacklist() != null) {

            if (event.getEntity() instanceof Painting
                    && !wcfg.getBlacklist().check(
                        new ItemUseBlacklistEvent(plugin.wrapPlayer(player),
                                toVector(player.getLocation()), ItemID.PAINTING), false, false)) {
                event.setCancelled(true);
                return;
            } else if (event.getEntity() instanceof ItemFrame
                    && !wcfg.getBlacklist().check(
                        new ItemUseBlacklistEvent(plugin.wrapPlayer(player),
                                toVector(player.getLocation()), ItemID.ITEM_FRAME), false, false)) {
                event.setCancelled(true);
                return;
            }
        }

        if (wcfg.useRegions) {
            if (!plugin.getGlobalRegionManager().canBuild(player, placedOn.getRelative(event.getBlockFace()))) {
                player.sendMessage(lang.getText("you-dont-have-permission-area"));
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityInteract(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        Entity entity = event.getRightClicked();

        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(entity.getWorld());

        if (wcfg.useRegions && (entity instanceof ItemFrame || entity instanceof Painting)) {
            if (!plugin.getGlobalRegionManager().canBuild(player, entity.getLocation())) {
                player.sendMessage(lang.getText("you-dont-have-permission-area"));
                event.setCancelled(true);
                return;
            }

//            if (entity instanceof ItemFrame
//                    && ((!plugin.getGlobalRegionManager().allows(
//                            DefaultFlag.ENTITY_ITEM_FRAME_DESTROY, entity.getLocation())))) {
//                event.setCancelled(true);
//            } else if (entity instanceof Painting
//                    && ((!plugin.getGlobalRegionManager().allows(
//                            DefaultFlag.ENTITY_PAINTING_DESTROY, entity.getLocation())))) {
//                event.setCancelled(true);
//            }
        }
    }
}
