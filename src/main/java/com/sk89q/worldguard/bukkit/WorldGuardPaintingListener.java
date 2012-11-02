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
import org.bukkit.entity.Painting;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.painting.PaintingBreakByEntityEvent;
import org.bukkit.event.painting.PaintingBreakEvent;
import org.bukkit.event.painting.PaintingPlaceEvent;

import com.sk89q.worldedit.blocks.ItemID;
import com.sk89q.worldguard.blacklist.events.BlockBreakBlacklistEvent;
import com.sk89q.worldguard.blacklist.events.ItemUseBlacklistEvent;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import org.bukkit.Warning;

/**
 * Listener for painting related events.
 *
 * @author sk89q
 * @deprecated Use {@link com.sk89q.worldguard.bukkit.WorldGuardHangingListener} instead.
 */
@Deprecated
@Warning(reason="This listener has been replaced by WorldGuardHangingListener")
public class WorldGuardPaintingListener implements Listener {

    private WorldGuardPlugin plugin;

    /**
     * Construct the object;
     *
     * @param plugin The plugin instance
     */
    public WorldGuardPaintingListener(WorldGuardPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Register events.
     */
    public void registerEvents() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPaintingBreak(PaintingBreakEvent breakEvent) {
        if (!(breakEvent instanceof PaintingBreakByEntityEvent)) {
            return;
        }

        PaintingBreakByEntityEvent event = (PaintingBreakByEntityEvent) breakEvent;
        Painting painting = event.getPainting();
        World world = painting.getWorld();
        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(world);

        if (event.getRemover() instanceof Player) {
            Player player = (Player) event.getRemover();

            if (wcfg.getBlacklist() != null) {
                if (!wcfg.getBlacklist().check(
                            new BlockBreakBlacklistEvent(plugin.wrapPlayer(player),
                                    toVector(player.getLocation()), ItemID.PAINTING), false, false)) {
                    event.setCancelled(true);
                    return;
                }
            }

            if (wcfg.useRegions) {
                if (!plugin.getGlobalRegionManager().canBuild(player, painting.getLocation())) {
                    player.sendMessage(ChatColor.DARK_RED + "You don't have permission for this area.");
                    event.setCancelled(true);
                    return;
                }
            }
        } else {
            if (event.getRemover() instanceof Creeper) {
                if (wcfg.blockCreeperBlockDamage || wcfg.blockCreeperExplosions) {
                    event.setCancelled(true);
                    return;
                }
                if (wcfg.useRegions && !plugin.getGlobalRegionManager().allows(DefaultFlag.CREEPER_EXPLOSION, painting.getLocation())) {
                    event.setCancelled(true);
                    return;
                }
            }

            if (wcfg.blockEntityPaintingDestroy
                    || (wcfg.useRegions
                    && !plugin.getGlobalRegionManager().allows(DefaultFlag.ENTITY_PAINTING_DESTROY, painting.getLocation()))) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPaintingPlace(PaintingPlaceEvent event) {
        Block placedOn = event.getBlock();
        Player player = event.getPlayer();
        World world = placedOn.getWorld();

        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(world);

        if (wcfg.getBlacklist() != null) {

            if (!wcfg.getBlacklist().check(
                    new ItemUseBlacklistEvent(plugin.wrapPlayer(player),
                            toVector(player.getLocation()), ItemID.PAINTING), false, false)) {
                event.setCancelled(true);
                return;
            }
        }

        if (wcfg.useRegions) {
            if (!plugin.getGlobalRegionManager().canBuild(player, placedOn.getLocation())) {
                player.sendMessage(ChatColor.DARK_RED + "You don't have permission for this area.");
                event.setCancelled(true);
                return;
            }
        }
    }
}
