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

import java.util.logging.Logger;

import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event;
import org.bukkit.plugin.PluginManager;
import com.sk89q.worldguard.protection.regions.flags.RegionFlag.RegionGroup;
import com.sk89q.worldguard.protection.regions.flags.FlagDatabase.FlagType;
import com.nijiko.coelho.iConomy.iConomy;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regionmanager.RegionManager;
import com.sk89q.worldguard.blacklist.events.ItemAcquireBlacklistEvent;
import org.bukkit.entity.Item;
import com.sk89q.worldguard.blacklist.events.ItemDropBlacklistEvent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.blacklist.events.ItemUseBlacklistEvent;

import static com.sk89q.worldguard.bukkit.BukkitUtil.*;

/**
 * Handles all events thrown in relation to a Player
 */
public class WorldGuardPlayerListener extends PlayerListener {

    /**
     * Plugin.
     */
    private WorldGuardPlugin plugin;
    private boolean checkediConomy = false;

    /**
     * Construct the object;
     * 
     * @param plugin
     */
    public WorldGuardPlayerListener(WorldGuardPlugin plugin) {
        this.plugin = plugin;
    }


    public void registerEvents() {

        PluginManager pm = plugin.getServer().getPluginManager();

        pm.registerEvent(Event.Type.PLAYER_ITEM, this, Priority.High, plugin);
        pm.registerEvent(Event.Type.PLAYER_DROP_ITEM, this, Priority.High, plugin);
        pm.registerEvent(Event.Type.PLAYER_PICKUP_ITEM, this, Priority.High, plugin);
        pm.registerEvent(Event.Type.PLAYER_JOIN, this, Priority.Normal, plugin);
        pm.registerEvent(Event.Type.PLAYER_LOGIN, this, Priority.Normal, plugin);
        pm.registerEvent(Event.Type.PLAYER_QUIT, this, Priority.Normal, plugin);
        pm.registerEvent(Event.Type.PLAYER_RESPAWN, this, Priority.High, plugin);
    }


    /**
     * Called when a player joins a server
     *
     * @param event Relevant event details
     */
    @Override
    public void onPlayerJoin(PlayerEvent event) {
        Player player = event.getPlayer();

        WorldGuardConfiguration cfg = plugin.getWgConfiguration();
        WorldGuardWorldConfiguration wcfg = cfg.getWorldConfig(player.getWorld().getName());

        if (wcfg.fireSpreadDisableToggle) {
            player.sendMessage(ChatColor.YELLOW
                    + "Fire spread is currently globally disabled.");
        }

        if (cfg.inGroup(player, "wg-invincible")) {
            cfg.addInvinciblePlayer(player.getName());
        }

        if (cfg.inGroup(player, "wg-amphibious")) {
            cfg.addAmphibiousPlayer(player.getName());
        }
    }

    /**
     * Called when a player leaves a server
     *
     * @param event Relevant event details
     */
    @Override
    public void onPlayerQuit(PlayerEvent event) {
        Player player = event.getPlayer();

        WorldGuardConfiguration cfg = plugin.getWgConfiguration();
        WorldGuardWorldConfiguration wcfg = cfg.getWorldConfig(player.getWorld().getName());

        cfg.removeInvinciblePlayer(player.getName());
        cfg.removeAmphibiousPlayer(player.getName());

        cfg.forgetPlayerAllBlacklists(BukkitPlayer.wrapPlayer(cfg, player));
    }

    /**
     * Called when a player uses an item
     * 
     * @param event Relevant event details
     */
    @Override
    public void onPlayerItem(PlayerItemEvent event) {

        if (event.isCancelled()) {
            return;
        }

        Player player = event.getPlayer();
        Block block = event.getBlockClicked();
        ItemStack item = event.getItem();
        int itemId = item.getTypeId();

        WorldGuardConfiguration cfg = plugin.getWgConfiguration();
        WorldGuardWorldConfiguration wcfg = cfg.getWorldConfig(player.getWorld().getName());


        Material type = block.getType();

        if (wcfg.useRegions && type != Material.LEVER && type != Material.STONE_BUTTON && type != Material.DIODE
                && (itemId == 322 || itemId == 320 || itemId == 319 || itemId == 297 || itemId == 260
                        || itemId == 350 || itemId == 349 || itemId == 354) ) {
            return;
        }

        if (!wcfg.itemDurability) {
            // Hoes
            if (item.getTypeId() >= 290 && item.getTypeId() <= 294) {
                item.setDurability((byte) -1);
                player.setItemInHand(item);
            }
        }

        if (wcfg.useRegions && !event.isBlock() && block != null) {
            Vector pt = toVector(block.getRelative(event.getBlockFace()));
            if (block.getType() == Material.WALL_SIGN) {
                pt = pt.subtract(0, 1, 0);
            }

            if (!cfg.canBuild(player, pt)) {
                player.sendMessage(ChatColor.DARK_RED
                        + "You don't have permission for this area.");
                event.setCancelled(true);
                return;
            }
        }

        if (wcfg.getBlacklist() != null && item != null && block != null) {
            if (!wcfg.getBlacklist().check(
                    new ItemUseBlacklistEvent(BukkitPlayer.wrapPlayer(cfg, player),
                    toVector(block.getRelative(event.getBlockFace())),
                    item.getTypeId()), false, false)) {
                event.setCancelled(true);
                return;
            }
        }

        if (wcfg.useRegions && item != null && block != null && item.getTypeId() == 259) {
            Vector pt = toVector(block.getRelative(event.getBlockFace()));
            RegionManager mgr = plugin.getGlobalRegionManager().getRegionManager(player.getWorld().getName());

            if (!mgr.getApplicableRegions(pt).isStateFlagAllowed(FlagType.LIGHTER)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    /**
     * Called when a player attempts to log in to the server
     *
     * @param event Relevant event details
     */
    @Override
    public void onPlayerLogin(PlayerLoginEvent event) {
        Player player = event.getPlayer();

        WorldGuardConfiguration cfg = plugin.getWgConfiguration();
        WorldGuardWorldConfiguration wcfg = cfg.getWorldConfig(player.getWorld().getName());

        if (wcfg.enforceOneSession) {
            String name = player.getName();

            for (Player pl : plugin.getServer().getOnlinePlayers()) {
                if (pl.getName().equalsIgnoreCase(name)) {
                    pl.kickPlayer("Logged in from another location.");
                }
            }
        }

        if (!checkediConomy) {
            iConomy iconomy = (iConomy) plugin.getServer().getPluginManager().getPlugin("iConomy");
            if (iconomy != null) {
                plugin.getWgConfiguration().setiConomy(iconomy);
            }

            checkediConomy = true;
        }
    }

    /**
     * Called when a player attempts to drop an item
     *
     * @param event Relevant event details
     */
    @Override
    public void onPlayerDropItem(PlayerDropItemEvent event) {

        if (event.isCancelled()) {
            return;
        }

        WorldGuardConfiguration cfg = plugin.getWgConfiguration();
        WorldGuardWorldConfiguration wcfg = cfg.getWorldConfig(event.getPlayer().getWorld().getName());

        if (wcfg.getBlacklist() != null) {
            Item ci = event.getItemDrop();

            if (!wcfg.getBlacklist().check(
                    new ItemDropBlacklistEvent(BukkitPlayer.wrapPlayer(cfg, event.getPlayer()), toVector(ci.getLocation()), ci.getItemStack().getTypeId()), false, false)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    /**
     * Called when a player attempts to pickup an item
     * 
     * @param event
     *            Relevant event details
     */
    @Override
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {

        if (event.isCancelled()) {
            return;
        }

        WorldGuardConfiguration cfg = plugin.getWgConfiguration();
        WorldGuardWorldConfiguration wcfg = cfg.getWorldConfig(event.getPlayer().getWorld().getName());

        if (wcfg.getBlacklist() != null) {
            Item ci = event.getItem();

            if (!wcfg.getBlacklist().check(
                    new ItemAcquireBlacklistEvent(BukkitPlayer.wrapPlayer(cfg, event.getPlayer()), toVector(ci.getLocation()), ci.getItemStack().getTypeId()), false, false)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @Override
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        Location location = player.getLocation();

        WorldGuardConfiguration cfg = plugin.getWgConfiguration();

        ApplicableRegionSet regions = plugin.getGlobalRegionManager().getRegionManager(
                player.getWorld().getName()).getApplicableRegions(
                BukkitUtil.toVector(location));

        Location spawn = regions.getLocationFlag(FlagType.SPAWN_LOC, true).getValue(player.getServer());

        if (spawn != null) {
            RegionGroup spawnconfig = regions.getRegionGroupFlag(FlagType.SPAWN_PERM, true).getValue();
            if (spawnconfig != null) {
                BukkitPlayer localPlayer = BukkitPlayer.wrapPlayer(cfg, player);
                if (spawnconfig == RegionGroup.OWNER) {
                    if (regions.isOwner(localPlayer)) {
                        event.setRespawnLocation(spawn);
                    }
                } else if (spawnconfig == RegionGroup.MEMBER) {
                    if (regions.isMember(localPlayer)) {
                        event.setRespawnLocation(spawn);
                    }
                } else {
                    event.setRespawnLocation(spawn);
                }
            } else {
                event.setRespawnLocation(spawn);
            }
        }
    }
}
