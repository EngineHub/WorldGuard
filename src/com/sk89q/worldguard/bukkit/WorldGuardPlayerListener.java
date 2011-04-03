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
import java.util.Iterator;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.blacklist.events.BlockInteractBlacklistEvent;
import com.sk89q.worldguard.blacklist.events.ItemAcquireBlacklistEvent;
import com.sk89q.worldguard.blacklist.events.ItemDropBlacklistEvent;
import com.sk89q.worldguard.blacklist.events.ItemUseBlacklistEvent;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.RegionGroupFlag.RegionGroup;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

/**
 * Handles all events thrown in relation to a Player
 */
public class WorldGuardPlayerListener extends PlayerListener {

    /**
     * Plugin.
     */
    private WorldGuardPlugin plugin;

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

        pm.registerEvent(Event.Type.PLAYER_INTERACT, this, Priority.High, plugin);
        pm.registerEvent(Event.Type.PLAYER_DROP_ITEM, this, Priority.High, plugin);
        pm.registerEvent(Event.Type.PLAYER_PICKUP_ITEM, this, Priority.High, plugin);
        pm.registerEvent(Event.Type.PLAYER_JOIN, this, Priority.Normal, plugin);
        pm.registerEvent(Event.Type.PLAYER_LOGIN, this, Priority.Normal, plugin);
        pm.registerEvent(Event.Type.PLAYER_QUIT, this, Priority.Normal, plugin);
        pm.registerEvent(Event.Type.PLAYER_BUCKET_FILL, this, Priority.High, plugin);
        pm.registerEvent(Event.Type.PLAYER_BUCKET_EMPTY, this, Priority.High, plugin);
        pm.registerEvent(Event.Type.PLAYER_RESPAWN, this, Priority.High, plugin);
    }

    /**
     * Called when a player interacts with an item.
     * 
     * @param event Relevant event details
     */
    @Override
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            handleBlockRightClick(event);
        } else if (event.getAction() == Action.RIGHT_CLICK_AIR) {
            handleAirRightClick(event);
        }
    }
    
    /**
     * Called when a block is damaged.
     * 
     * @param event 
     */
    public void handleAirRightClick(PlayerInteractEvent event) {
        /*if (event.isCancelled()) {
            return;
        }*/

        Player player = event.getPlayer();
        World world = player.getWorld();
        ItemStack item = player.getItemInHand();

        ConfigurationManager cfg = plugin.getGlobalConfiguration();
        WorldConfiguration wcfg = cfg.get(world);

        if (wcfg.getBlacklist() != null) {
            if (!wcfg.getBlacklist().check(
                    new ItemUseBlacklistEvent(plugin.wrapPlayer(player),
                            toVector(player.getLocation()),
                    item.getTypeId()), false, false)) {
                event.setCancelled(true);
                return;
            }
        }
    }
    
    /**
     * Called when a block is damaged.
     * 
     * @param event 
     */
    public void handleBlockRightClick(PlayerInteractEvent event) {
        if (event.isCancelled()) {
            return;
        }

        Block block = event.getClickedBlock();
        World world = block.getWorld();
        Material type = block.getType();
        Player player = event.getPlayer();
        ItemStack item = player.getItemInHand();

        ConfigurationManager cfg = plugin.getGlobalConfiguration();
        WorldConfiguration wcfg = cfg.get(world);
        
        if (wcfg.useRegions) {
            Vector pt = toVector(block);
            RegionManager mgr = plugin.getGlobalRegionManager().get(world);
            ApplicableRegionSet set = mgr.getApplicableRegions(pt);
            LocalPlayer localPlayer = plugin.wrapPlayer(player);

            if (item.getTypeId() == wcfg.regionWand) {
                if (set.size() > 0) {
                    player.sendMessage(ChatColor.YELLOW + "Can you build? "
                            + (set.canBuild(localPlayer) ? "Yes" : "No"));

                    StringBuilder str = new StringBuilder();
                    for (Iterator<ProtectedRegion> it = set.iterator(); it.hasNext();) {
                        str.append(it.next().getId());
                        if (it.hasNext()) {
                            str.append(", ");
                        }
                    }

                    player.sendMessage(ChatColor.YELLOW + "Applicable regions: " + str.toString());
                } else {
                    player.sendMessage(ChatColor.YELLOW + "WorldGuard: No defined regions here!");
                }

                event.setCancelled(true);
                return;
            }

            if (item.getType() == Material.FLINT_AND_STEEL) {
                if (!set.allows(DefaultFlag.LIGHTER)) {
                    event.setCancelled(true);
                    return;
                }
            }

            if ((block.getType() == Material.CHEST
                    || block.getType() == Material.DISPENSER
                    || block.getType() == Material.FURNACE
                    || block.getType() == Material.BURNING_FURNACE
                    || block.getType() == Material.NOTE_BLOCK)) {
                
                if (!set.allows(DefaultFlag.CHEST_ACCESS)
                        && !set.canBuild(localPlayer)) {
                    player.sendMessage(ChatColor.DARK_RED + "You don't have permission for this area.");
                    event.setCancelled(true);
                    return;
                }
            }
    
            if (type == Material.LEVER || type == Material.STONE_BUTTON) {
                if (!set.allows(DefaultFlag.USE)
                        && !set.canBuild(localPlayer)) {
                    player.sendMessage(ChatColor.DARK_RED + "You don't have permission for this area.");
                    event.setCancelled(true);
                    return;
                }
            }

            if (type == Material.CAKE_BLOCK) {
                if (!set.canBuild(localPlayer)) {
                    player.sendMessage(ChatColor.DARK_RED + "You're not invited to this tea party!");
                    event.setCancelled(true);
                    return;
                }
            }
            
            if (type == Material.RAILS && item.getType() == Material.MINECART) {
                if (!set.canBuild(localPlayer)
                        && !set.allows(DefaultFlag.PLACE_VEHICLE)) {
                    player.sendMessage(ChatColor.DARK_RED + "You don't have permission to place vehicles here.");
                    event.setCancelled(true);
                    return;
                }
            }
            
            if (item.getType() == Material.BOAT) {
                if (!set.canBuild(localPlayer)
                        && !set.allows(DefaultFlag.PLACE_VEHICLE)) {
                    player.sendMessage(ChatColor.DARK_RED + "You don't have permission to place vehicles here.");
                    event.setCancelled(true);
                    return;
                }
            }
        }

        if (wcfg.getBlacklist() != null) {
            if (!wcfg.getBlacklist().check(
                    new ItemUseBlacklistEvent(plugin.wrapPlayer(player), toVector(block),
                    item.getTypeId()), false, false)) {
                event.setCancelled(true);
                return;
            }
            
            if (!wcfg.getBlacklist().check(
                    new BlockInteractBlacklistEvent(plugin.wrapPlayer(player), toVector(block),
                    block.getTypeId()), false, false)) {
                event.setCancelled(true);
                return;
            }
        }

        /*if (wcfg.useRegions && wcfg.useiConomy && cfg.getiConomy() != null
                    && (type == Material.SIGN_POST || type == Material.SIGN || type == Material.WALL_SIGN)) {
            BlockState block = blockClicked.getState();

            if (((Sign)block).getLine(0).equalsIgnoreCase("[WorldGuard]")
                    && ((Sign)block).getLine(1).equalsIgnoreCase("For sale")) {
                String regionId = ((Sign)block).getLine(2);
                //String regionComment = ((Sign)block).getLine(3);

                if (regionId != null && regionId != "") {
                    RegionManager mgr = cfg.getWorldGuardPlugin().getGlobalRegionManager().get(player.getWorld().getName());
                    ProtectedRegion region = mgr.getRegion(regionId);

                    if (region != null) {
                        RegionFlags flags = region.getFlags();

                        if (flags.getBooleanFlag(DefaultFlag.BUYABLE).getValue(false)) {
                            if (iConomy.getBank().hasAccount(player.getName())) {
                                Account account = iConomy.getBank().getAccount(player.getName());
                                double balance = account.getBalance();
                                double regionPrice = flags.getDoubleFlag(DefaultFlag.PRICE).getValue();

                                if (balance >= regionPrice) {
                                    account.subtract(regionPrice);
                                    player.sendMessage(ChatColor.YELLOW + "You have bought the region " + regionId + " for " +
                                            iConomy.getBank().format(regionPrice));
                                    DefaultDomain owners = region.getOwners();
                                    owners.addPlayer(player.getName());
                                    region.setOwners(owners);
                                    flags.getBooleanFlag(DefaultFlag.BUYABLE).setValue(false);
                                    account.save();
                                } else {
                                    player.sendMessage(ChatColor.YELLOW + "You have not enough money.");
                                }
                            } else {
                                player.sendMessage(ChatColor.YELLOW + "You have not enough money.");
                            }
                        } else {
                            player.sendMessage(ChatColor.RED + "Region: " + regionId + " is not buyable");
                        } 
                    } else {
                        player.sendMessage(ChatColor.DARK_RED + "The region " + regionId + " does not exist.");
                    }
                } else {
                    player.sendMessage(ChatColor.DARK_RED + "No region specified.");
                }
            }
        }*/
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
                    + "Fire spread is currently globally disabled for this world.");
        }
        
        if (plugin.inGroup(player, "wg-invincible")) {
            cfg.enableGodMode(player);
        }
        
        if (plugin.inGroup(player, "wg-amphibious")) {
            cfg.enableAmphibiousMode(player);
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
                    new ItemDropBlacklistEvent(plugin.wrapPlayer(event.getPlayer()),
                            toVector(ci.getLocation()), ci.getItemStack().getTypeId()), false, false)) {
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
                    new ItemAcquireBlacklistEvent(plugin.wrapPlayer(event.getPlayer()),
                            toVector(ci.getLocation()), ci.getItemStack().getTypeId()), false, true)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    /**
     * Called when a bucket is filled.
     */
    @Override
    public void onPlayerBucketFill(PlayerBucketFillEvent event) {
        Player player = event.getPlayer();
        World world = player.getWorld();
        
        ConfigurationManager cfg = plugin.getGlobalConfiguration();
        WorldConfiguration wcfg = cfg.get(world);
        
        if (!plugin.getGlobalRegionManager().canBuild(player, event.getBlockClicked())) {
            player.sendMessage(ChatColor.DARK_RED + "You don't have permission for this area.");
            event.setCancelled(true);
            return;
        }
        
        if (wcfg.getBlacklist() != null) {
            if (!wcfg.getBlacklist().check(
                    new ItemUseBlacklistEvent(plugin.wrapPlayer(player),
                            toVector(player.getLocation()), event.getBucket().getId()), false, false)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    /**
     * Called when a bucket is empty.
     */
    @Override
    public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
        Player player = event.getPlayer();
        World world = player.getWorld();
        
        ConfigurationManager cfg = plugin.getGlobalConfiguration();
        WorldConfiguration wcfg = cfg.get(world);
        
        if (!plugin.getGlobalRegionManager().canBuild(player, event.getBlockClicked())) {
            player.sendMessage(ChatColor.DARK_RED + "You don't have permission for this area.");
            event.setCancelled(true);
            return;
        }
        
        if (wcfg.getBlacklist() != null) {
            if (!wcfg.getBlacklist().check(
                    new ItemUseBlacklistEvent(plugin.wrapPlayer(player),
                            toVector(player.getLocation()), event.getBucket().getId()), false, false)) {
                event.setCancelled(true);
                return;
            }
        }
    }
    
    @Override
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        Location location = player.getLocation();
        
        ConfigurationManager cfg = plugin.getGlobalConfiguration();
        WorldConfiguration wcfg = cfg.get(player.getWorld());
        
        if (wcfg.useRegions) {
            Vector pt = toVector(location);
            RegionManager mgr = plugin.getGlobalRegionManager().get(player.getWorld());
            ApplicableRegionSet set = mgr.getApplicableRegions(pt);
    
            Vector spawn = set.getFlag(DefaultFlag.SPAWN_LOC);
    
            if (spawn != null) {
                RegionGroup group = set.getFlag(DefaultFlag.SPAWN_PERM);
                Location spawnLoc = BukkitUtil.toLocation(player.getWorld(), spawn);
                
                if (group != null) {
                    LocalPlayer localPlayer = plugin.wrapPlayer(player);
                    
                    if (group == RegionGroup.OWNERS) {
                        if (set.isOwnerOfAll(localPlayer)) {
                            event.setRespawnLocation(spawnLoc);
                        }
                    } else if (group == RegionGroup.MEMBERS) {
                        if (set.isMemberOfAll(localPlayer)) {
                            event.setRespawnLocation(spawnLoc);
                        }
                    }
                } else {
                    event.setRespawnLocation(spawnLoc);
                }
            }
        }
    }
}
