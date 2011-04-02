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
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.*;
import org.bukkit.plugin.PluginManager;
import com.sk89q.worldguard.blacklist.events.ItemAcquireBlacklistEvent;
import com.sk89q.worldguard.blacklist.events.ItemDropBlacklistEvent;

/**
 * Handles all events thrown in relation to a Player
 */
public class WorldGuardPlayerListener extends PlayerListener {

    /**
     * Plugin.
     */
    private WorldGuardPlugin plugin;

    /**
     * Interact Handler
     */
    private WorldGuardInteractHandler interactHandler;

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

        //pm.registerEvent(Event.Type.PLAYER_ITEM, this, Priority.High, plugin);
        pm.registerEvent(Event.Type.PLAYER_INTERACT, this, Priority.High, plugin);
        pm.registerEvent(Event.Type.PLAYER_DROP_ITEM, this, Priority.High, plugin);
        pm.registerEvent(Event.Type.PLAYER_PICKUP_ITEM, this, Priority.High, plugin);
        pm.registerEvent(Event.Type.PLAYER_JOIN, this, Priority.Normal, plugin);
        pm.registerEvent(Event.Type.PLAYER_LOGIN, this, Priority.Normal, plugin);
        pm.registerEvent(Event.Type.PLAYER_QUIT, this, Priority.Normal, plugin);
        pm.registerEvent(Event.Type.PLAYER_RESPAWN, this, Priority.High, plugin);
    }

    /**
     * Called when a player interacts with an item.
     * 
     * @param event Relevant event details
     */
    @Override
    public void onPlayerInteract(PlayerInteractEvent event) {
        ConfigurationManager cfg = plugin.getGlobalConfiguration();
        WorldConfiguration wcfg = cfg.get(event.getPlayer().getWorld());

        if(event.getAction() == Action.LEFT_CLICK_AIR
                || event.getAction() == Action.LEFT_CLICK_BLOCK) {
            this.interactHandler.onBlockClick(event, cfg, wcfg,
                    event.getAction(), event.getPlayer(), event.getClickedBlock(), event.getItem(), event.getItem().getTypeId());
        }
        if(event.getAction() == Action.RIGHT_CLICK_AIR
                || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if(!this.interactHandler.itemInHand(event, wcfg, event.getAction(),
                    event.getPlayer(), event.getClickedBlock(), event.getItem(), event.getItem().getTypeId())) {
                this.interactHandler.onBlockRightclick(event, cfg, wcfg,
                        event.getAction(), event.getPlayer(), event.getClickedBlock(), event.getItem(), event.getItem().getTypeId());
            }
        }
    }

    /**
     * Called when a player joins a server
     *
     * @param event Relevant event details
     */
    @Override
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        ConfigurationManager cfg = plugin.getGlobalConfiguration();
        WorldConfiguration wcfg = cfg.get(player.getWorld());

        if (wcfg.fireSpreadDisableToggle) {
            player.sendMessage(ChatColor.YELLOW
                    + "Fire spread is currently globally disabled.");
        }
    }

    /**
     * Called when a player leaves a server
     *
     * @param event Relevant event details
     */
    @Override
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        ConfigurationManager cfg = plugin.getGlobalConfiguration();
        cfg.forgetPlayer(plugin.wrapPlayer(player));
    }

    /**
     * Called when a player uses an item
     * 
     * @param event Relevant event details
     *//*
    @Override
    public void onPlayerItem(PlayerItemEvent event) {

        if (event.isCancelled()) {
            return;
        }

        Player player = event.getPlayer();
        Block block = event.getBlockClicked();
        ItemStack item = event.getItem();
        int itemId = item.getTypeId();

        GlobalConfiguration cfg = plugin.getGlobalConfiguration();
        WorldConfiguration wcfg = cfg.getWorldConfig(player.getWorld().getName());

        if (wcfg.useRegions
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
                    new ItemUseBlacklistEvent(plugin.wrapPlayer(player),
                    toVector(block.getRelative(event.getBlockFace())),
                    item.getTypeId()), false, false)) {
                event.setCancelled(true);
                return;
            }
        }

        if (wcfg.useRegions && item != null && block != null && item.getTypeId() == 259) {
            Vector pt = toVector(block.getRelative(event.getBlockFace()));
            RegionManager mgr = plugin.getGlobalRegionManager().get(player.getWorld().getName());

            if (!mgr.getApplicableRegions(pt).isStateFlagAllowed(DefaultFlag.LIGHTER)) {
                event.setCancelled(true);
                return;
            }
        }
    }*/

    /**
     * Called when a player attempts to log in to the server
     *
     * @param event Relevant event details
     */
    @Override
    public void onPlayerLogin(PlayerLoginEvent event) {
        Player player = event.getPlayer();

        ConfigurationManager cfg = plugin.getGlobalConfiguration();
        WorldConfiguration wcfg = cfg.get(player.getWorld());

        if (wcfg.enforceOneSession) {
            String name = player.getName();

            for (Player pl : plugin.getServer().getOnlinePlayers()) {
                if (pl.getName().equalsIgnoreCase(name)) {
                    pl.kickPlayer("Logged in from another location.");
                }
            }
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

        ConfigurationManager cfg = plugin.getGlobalConfiguration();
        WorldConfiguration wcfg = cfg.get(event.getPlayer().getWorld());

        if (wcfg.getBlacklist() != null) {
            Item ci = event.getItemDrop();

            if (!wcfg.getBlacklist().check(
                    new ItemDropBlacklistEvent(plugin.wrapPlayer(event.getPlayer()), toVector(ci.getLocation()), ci.getItemStack().getTypeId()), false, false)) {
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

        ConfigurationManager cfg = plugin.getGlobalConfiguration();
        WorldConfiguration wcfg = cfg.get(event.getPlayer().getWorld());

        if (wcfg.getBlacklist() != null) {
            Item ci = event.getItem();

            if (!wcfg.getBlacklist().check(
                    new ItemAcquireBlacklistEvent(plugin.wrapPlayer(event.getPlayer()), toVector(ci.getLocation()), ci.getItemStack().getTypeId()), false, false)) {
                event.setCancelled(true);
                return;
            }
        }
    }
/*
    @Override
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        Location location = player.getLocation();

        ApplicableRegionSet regions = plugin.getGlobalRegionManager().get(
                player.getWorld().getName()).getApplicableRegions(
                BukkitUtil.toVector(location));

        Location spawn = regions.getLocationFlag(DefaultFlag.SPAWN_LOC, true).getValue(player.getServer());

        if (spawn != null) {
            RegionGroup spawnconfig = regions.getRegionGroupFlag(DefaultFlag.SPAWN_PERM, true).getValue();
            if (spawnconfig != null) {
                LocalPlayer localPlayer = plugin.wrapPlayer(player);
                
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
    }*/
}
