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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.*;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Result;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.blacklist.events.*;
import com.sk89q.worldguard.bukkit.FlagStateManager.PlayerFlagState;
import com.sk89q.worldguard.bukkit.iConomyManager.EcoAccount;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.RegionGroupFlag.RegionGroup;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;


/**
 * Handles all events thrown in relation to a player.
 */
public class WorldGuardPlayerListener extends PlayerListener {

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
    public WorldGuardPlayerListener(WorldGuardPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Register events.
     */
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
        pm.registerEvent(Event.Type.PLAYER_ITEM_HELD, this, Priority.High, plugin);
        pm.registerEvent(Event.Type.PLAYER_BED_ENTER, this, Priority.High, plugin);
        pm.registerEvent(Event.Type.PLAYER_MOVE, this, Priority.High, plugin);
    }

    /**
     * Called when a player attempts to log in to the server.
     */
    @Override
    public void onPlayerLogin(PlayerLoginEvent event) {
        Player player = event.getPlayer();

        ConfigurationManager cfg = plugin.getGlobalStateManager();
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
     * Called when a player joins a server.
     */
    @Override
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(player.getWorld());

        if (cfg.activityHaltToggle) {
            player.sendMessage(ChatColor.YELLOW
                    + "Intensive server activity has been HALTED.");

            int removed = 0;

            for (Entity entity : player.getWorld().getEntities()) {
                if (entity instanceof Item
                        || (entity instanceof LivingEntity
                        && !(entity instanceof Tameable)
                        && !(entity instanceof Player))) {
                    entity.remove();
                    removed++;
                }
            }

            if (removed > 10) {
                logger.info("WG Halt-Act: " + removed + " entities (>10) auto-removed from "
                        + player.getWorld().toString());
            }
        }

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
     * Called when a player leaves a server.
     */
    @Override
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        ConfigurationManager cfg = plugin.getGlobalStateManager();
        cfg.forgetPlayer(plugin.wrapPlayer(player));
        plugin.forgetPlayer(player);
    }

    /**
     * Called when a player interacts with an item.
     */
    @Override
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        World world = player.getWorld();
        
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            handleBlockRightClick(event);
        } else if (event.getAction() == Action.RIGHT_CLICK_AIR) {
            handleAirRightClick(event);
        } else if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            handleBlockLeftClick(event);
        } else if (event.getAction() == Action.LEFT_CLICK_AIR) {
            handleAirLeftClick(event);
        } else if (event.getAction() == Action.PHYSICAL) {
            handlePhysicalInteract(event);
        }
        
        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(world);
        
        if (wcfg.removeInfiniteStacks
                && !plugin.hasPermission(player, "worldguard.override.infinite-stack")) {
            int slot = player.getInventory().getHeldItemSlot();
            ItemStack heldItem = player.getInventory().getItem(slot);
            if (heldItem.getAmount() < 0) {
                player.getInventory().setItem(slot, null);
                player.sendMessage(ChatColor.RED + "Infinite stack removed.");
            }
        }
    }

    /**
     * Called when a player attempts to move.
     *
     * @param event
     */
    @Override
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        World world = player.getWorld();
        
        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(world);
        
        if (wcfg.useRegions) {
            // Did we move a block?
            if (event.getFrom().getBlockX() != event.getTo().getBlockX()
                    || event.getFrom().getBlockY() != event.getTo().getBlockY()
                    || event.getFrom().getBlockZ() != event.getTo().getBlockZ()) {
                PlayerFlagState state = plugin.getFlagStateManager().getState(player);

                RegionManager mgr = plugin.getGlobalRegionManager().get(world);
                Vector pt = toVector(event.getTo());
                ApplicableRegionSet set = mgr.getApplicableRegions(pt);
                String greeting = set.getFlag(DefaultFlag.GREET_MESSAGE);
                String farewell = set.getFlag(DefaultFlag.FAREWELL_MESSAGE);
                Boolean notifyEnter = set.getFlag(DefaultFlag.NOTIFY_ENTER);
                Boolean notifyLeave = set.getFlag(DefaultFlag.NOTIFY_LEAVE);
                
                if (state.lastFarewell != null && (farewell == null 
                        || !state.lastFarewell.equals(farewell))) {
                    player.sendMessage(ChatColor.AQUA + " ** " + state.lastFarewell);
                }
                
                if (greeting != null && (state.lastGreeting == null
                        || !state.lastGreeting.equals(greeting))) {
                    player.sendMessage(ChatColor.AQUA + " ** " + greeting);
                }
                
                if ((notifyLeave == null || !notifyLeave)
                        && state.notifiedForLeave != null && state.notifiedForLeave) {
                    plugin.broadcastNotification(ChatColor.GRAY + "WG: " 
                            + ChatColor.LIGHT_PURPLE + player.getName()
                            + ChatColor.GOLD + " left NOTIFY region");
                }
                
                if (notifyEnter != null && notifyEnter && (state.notifiedForEnter == null
                        || !state.notifiedForEnter)) {
                    StringBuilder regionList = new StringBuilder();
                    
                    for (ProtectedRegion region : set) {
                        if (regionList.length() != 0) {
                            regionList.append(", ");
                        }
                        regionList.append(region.getId());
                    }
                    
                    plugin.broadcastNotification(ChatColor.GRAY + "WG: " 
                            + ChatColor.LIGHT_PURPLE + player.getName()
                            + ChatColor.GOLD + " entered NOTIFY region: "
                            + ChatColor.WHITE
                            + regionList);
                }

                state.lastGreeting = greeting;
                state.lastFarewell = farewell;
                state.notifiedForEnter = notifyEnter;
                state.notifiedForLeave = notifyLeave;
            }
        }
    }
    
    /**
     * Called when a player left clicks air.
     *
     * @param event
     */
    private void handleAirLeftClick(PlayerInteractEvent event) {
         // I don't think we have to do anything here yet.
         return;
    }

    /**
     * Called when a player left clicks a block.
     *
     * @param event
     */
    private void handleBlockLeftClick(PlayerInteractEvent event) {
        if (event.isCancelled()) return;

        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        Material type = block.getType();
        World world = player.getWorld();

        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(world);

        if (wcfg.useRegions) {
            Vector pt = toVector(block);
            RegionManager mgr = plugin.getGlobalRegionManager().get(world);
            ApplicableRegionSet set = mgr.getApplicableRegions(pt);
            LocalPlayer localPlayer = plugin.wrapPlayer(player);

            if (type == Material.STONE_BUTTON
                  || type == Material.LEVER
                  || type == Material.WOODEN_DOOR
                  || type == Material.TRAP_DOOR
                  || type == Material.NOTE_BLOCK) {
                if (!plugin.getGlobalRegionManager().hasBypass(player, world)
                        && !set.allows(DefaultFlag.USE)
                        && !set.canBuild(localPlayer)) {
                    player.sendMessage(ChatColor.DARK_RED + "You don't have permission to use that in this area.");
                    event.setUseInteractedBlock(Result.DENY);
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    /**
     * Called when a player right clicks air.
     * 
     * @param event 
     */
    private void handleAirRightClick(PlayerInteractEvent event) {
        if (event.isCancelled()) {
            return;
        }

        Player player = event.getPlayer();
        World world = player.getWorld();
        ItemStack item = player.getItemInHand();

        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(world);

        if (wcfg.getBlacklist() != null) {
            if (!wcfg.getBlacklist().check(
                    new ItemUseBlacklistEvent(plugin.wrapPlayer(player),
                            toVector(player.getLocation()),
                    item.getTypeId()), false, false)) {
                event.setCancelled(true);
                event.setUseItemInHand(Result.DENY);
                return;
            }
        }
    }
    
    /**
     * Called when a player right clicks a block.
     * 
     * @param event 
     */
    private void handleBlockRightClick(PlayerInteractEvent event) {
        if (event.isCancelled()) {
            return;
        }

        Block block = event.getClickedBlock();
        World world = block.getWorld();
        Material type = block.getType();
        Player player = event.getPlayer();
        ItemStack item = player.getItemInHand();

        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(world);

        // Infinite stack removal
        if ((type == Material.CHEST
                || type == Material.JUKEBOX
                || type == Material.DISPENSER
                || type == Material.FURNACE
                || type == Material.BURNING_FURNACE)
                && wcfg.removeInfiniteStacks
                && !plugin.hasPermission(player, "worldguard.override.infinite-stack")) {
            for (int slot = 0; slot < 40; slot++) {
                ItemStack heldItem = player.getInventory().getItem(slot);
                if (heldItem != null && heldItem.getAmount() < 0) {
                    player.getInventory().setItem(slot, null);
                    player.sendMessage(ChatColor.RED + "Infinite stack in slot #" + slot + " removed.");
                }
            }
        }

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
            if (item.getType() == Material.INK_SACK
                    && item.getData() != null
                    && item.getData().getData() == 15 // bonemeal
                    && type == Material.GRASS) {
                if (!plugin.getGlobalRegionManager().hasBypass(player, world)
                        && !set.canBuild(localPlayer)) {
                    event.setCancelled(true);
                    event.setUseInteractedBlock(Result.DENY);
                    event.setUseItemInHand(Result.DENY);
                }
            }

            if (type == Material.CHEST
                    || type == Material.JUKEBOX //stores the (arguably) most valuable item
                    || type == Material.DISPENSER
                    || type == Material.FURNACE
                    || type == Material.BURNING_FURNACE) {
                if (!plugin.getGlobalRegionManager().hasBypass(player, world)
                        && !set.allows(DefaultFlag.CHEST_ACCESS)
                        && !set.canBuild(localPlayer)) {
                    player.sendMessage(ChatColor.DARK_RED + "You don't have permission to open that in this area.");
                    event.setCancelled(true);
                    return;
                }
            }
    
            if (type == Material.LEVER
                   || type == Material.STONE_BUTTON
                   || type == Material.NOTE_BLOCK
                   || type == Material.DIODE_BLOCK_OFF
                   || type == Material.DIODE_BLOCK_ON
                   || type == Material.WOODEN_DOOR
                   || type == Material.TRAP_DOOR
                   || type == Material.WORKBENCH) {
                if (!plugin.getGlobalRegionManager().hasBypass(player, world)
                        && !set.allows(DefaultFlag.USE)
                        && !set.canBuild(localPlayer)) {
                    player.sendMessage(ChatColor.DARK_RED + "You don't have permission to use that in this area.");
                    event.setUseInteractedBlock(Result.DENY);
                    event.setCancelled(true);
                    return;
                }
            }

            if (type == Material.CAKE_BLOCK) {
                if (!plugin.getGlobalRegionManager().hasBypass(player, world)
                        && !set.canBuild(localPlayer)) {
                    player.sendMessage(ChatColor.DARK_RED + "You're not invited to this tea party!");
                    event.setCancelled(true);
                    return;
                }
            }
            
            if ((type == Material.RAILS || type == Material.POWERED_RAIL || type == Material.DETECTOR_RAIL)
                   && item.getType() == Material.MINECART) {
                if (!plugin.getGlobalRegionManager().hasBypass(player, world)
                        && !set.canBuild(localPlayer)
                        && !set.allows(DefaultFlag.PLACE_VEHICLE)) {
                    player.sendMessage(ChatColor.DARK_RED + "You don't have permission to place vehicles here.");
                    event.setCancelled(true);
                    return;
                }
            }
            
            if (item.getType() == Material.BOAT) {
                if (!plugin.getGlobalRegionManager().hasBypass(player, world)
                        && !set.canBuild(localPlayer)
                        && !set.allows(DefaultFlag.PLACE_VEHICLE)) {
                    player.sendMessage(ChatColor.DARK_RED + "You don't have permission to place vehicles here.");
                    event.setCancelled(true);
                    return;
                }
            }
            
            if (wcfg.useiConomy && iConomyManager.isloaded()
                    && (type == Material.SIGN_POST || type == Material.SIGN || type == Material.WALL_SIGN)) {
            	if (plugin.hasPermission(player, "worldguard.region.buy.sign")){
            		if (((Sign)block.getState()).getLine(0).trim().equals("§1[Buy Region]")) {
    	                String regionId = ((Sign)block.getState()).getLine(1);
    	                
    	                if (regionId != null && regionId != "") {
    	                    ProtectedRegion region = mgr.getRegion(regionId);
    	                    
    	                    if (region != null) {
    	                    	
    	                    	iConomyManager ico = new iConomyManager();
    	                    	if (region.getFlag(DefaultFlag.PRICE) == Double.parseDouble(((Sign)block.getState()).getLine(2).split(" ")[0]) ){
    	                    		
	     	                    	if(! ((Sign)block.getState()).getLine(3).equals(ChatColor.GRAY + player.getName()) ){
	    	                    		
	    	                    		
	    		                        if (region.getFlag(DefaultFlag.BUYABLE)) {
	    		                        	
	    		                            if (ico.hasAccount(player.getName())) {
	    		                            	
	    		                                EcoAccount account = ico.getAccount(player.getName());
	    		                                double balance = account.balance();
	    		                                
	    		                                //Note: Currently a single owner, if there are multiple, CANNOT list a region as sellable
	    		                                //This would be a GREAT configuration toggle, thus the code below...
	    		                                Set<String> ownersSet = region.getOwners().getPlayers(); 
	    		                                List<EcoAccount> ownerAccounts = new ArrayList<EcoAccount>();
	    		                                for (String owner:ownersSet){
	    		                                	if (!ico.hasAccount(owner))
	    		                                		ico.createAccount(owner);
	    		                                	ownerAccounts.add(ico.getAccount(owner));
	    		                                }
	    		                                
	    		                                double regionPrice = region.getFlag(DefaultFlag.PRICE);
	    		
	    		                                if (balance >= regionPrice) {
	    		                                    account.subtract(regionPrice);
	    		                                    ico.dividAndDistribute(regionPrice,ownerAccounts);
	    		                                    player.sendMessage(ChatColor.YELLOW + "You have bought the region \"" + regionId + "\" for " +
	    		                                            ico.format(regionPrice));
	    		                                    DefaultDomain owners = new DefaultDomain();
	    		                                    owners.addPlayer(player.getName());
	    		                                    region.setOwners(owners);
	    		                                    region.setFlag(DefaultFlag.BUYABLE, false);
	    		                                    block.setTypeId(0); //Set sign to air
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
	    	                    		player.sendMessage(ChatColor.RED + "You already own \""+regionId+".\"");
	    	                    	}   	                    		
	                    		} else {//Sign price is wrong...  Fix and report to user.
	                    			((Sign)block.getState()).setLine(2,ico.format(region.getFlag(DefaultFlag.PRICE)));
	                    			((Sign) block.getState()).update(); //So the client sees the update
	                    			player.sendMessage(ChatColor.YELLOW + "The price on this sign was out of date.  It has been updated.");
	                    		}
    	                    } else {
    	                        player.sendMessage(ChatColor.DARK_RED + "The region " + regionId + " does not exist.");
    	                    }
    	                } else {
    	                    player.sendMessage(ChatColor.DARK_RED + "No region specified.");
    	                }
    	            }
            	} else {
            		player.sendMessage(ChatColor.RED + "You don't have permission for that command!");
            	}
            }
        }

        if (wcfg.getBlacklist() != null) {
            if((block.getType() != Material.CHEST
                && block.getType() != Material.DISPENSER
                && block.getType() != Material.FURNACE
                && block.getType() != Material.BURNING_FURNACE)) {
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

        }

        if ((block.getType() == Material.CHEST
                || block.getType() == Material.DISPENSER
                || block.getType() == Material.FURNACE
                || block.getType() == Material.BURNING_FURNACE)) {
            
            if (wcfg.isChestProtected(block, player)) {
                player.sendMessage(ChatColor.DARK_RED + "The chest is protected.");
                event.setCancelled(true);
                return;
            }
        }
    }

    /**
     * Called when a player steps on a pressure plate or tramples crops.
     *
     * @param event
     */
    private void handlePhysicalInteract(PlayerInteractEvent event) {
        if (event.isCancelled() == true) return;

        Player player = event.getPlayer();
        Block block = event.getClickedBlock(); //not actually clicked but whatever
        Material type = block.getType();
        World world = player.getWorld();

        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(world);

        if (block.getType() == Material.SOIL && wcfg.disablePlayerCropTrampling) {
            event.setCancelled(true);
            return;
        }

        if (wcfg.useRegions) {
            Vector pt = toVector(block);
            RegionManager mgr = plugin.getGlobalRegionManager().get(world);
            ApplicableRegionSet set = mgr.getApplicableRegions(pt);
            LocalPlayer localPlayer = plugin.wrapPlayer(player);

            if (type == Material.STONE_PLATE || type == Material.WOOD_PLATE) {
               if (!plugin.getGlobalRegionManager().hasBypass(player, world)
                       && !set.allows(DefaultFlag.USE)
                       && !set.canBuild(localPlayer)) {
                   event.setUseInteractedBlock(Result.DENY);
                   event.setCancelled(true);
                   return;
               }
            }
        }
    }

    /**
     * Called when a player uses an item.
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
     * Called when a player attempts to drop an item.
     */
    @Override
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (event.isCancelled()) {
            return;
        }

        ConfigurationManager cfg = plugin.getGlobalStateManager();
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
     * Called when a player attempts to pickup an item.
     */
    @Override
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        if (event.isCancelled()) {
            return;
        }

        ConfigurationManager cfg = plugin.getGlobalStateManager();
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
        
        ConfigurationManager cfg = plugin.getGlobalStateManager();
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
        
        ConfigurationManager cfg = plugin.getGlobalStateManager();
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
     * Called when a player is respawned.
     */
    @Override
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        Location location = player.getLocation();
        
        ConfigurationManager cfg = plugin.getGlobalStateManager();
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

    /**
     * Called when a player changes their held item.
     */
    @Override
    public void onItemHeldChange(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        
        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(player.getWorld());
        
        if (wcfg.removeInfiniteStacks
                && !plugin.hasPermission(player, "worldguard.override.infinite-stack")) {
            int newSlot = event.getNewSlot();
            ItemStack heldItem = player.getInventory().getItem(newSlot);
            if (heldItem.getAmount() < 0) {
                player.getInventory().setItem(newSlot, null);
                player.sendMessage(ChatColor.RED + "Infinite stack removed.");
            }
        }
    }

    /**
     * Called when a player enters a bed.
     */
    @Override
    public void onPlayerBedEnter(PlayerBedEnterEvent event) {
        if (event.isCancelled()) {
            return;
        }

        Player player = event.getPlayer();
        Location location = player.getLocation();

        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(player.getWorld());

        if (wcfg.useRegions) {
            Vector pt = toVector(location);
            RegionManager mgr = plugin.getGlobalRegionManager().get(player.getWorld());
            ApplicableRegionSet set = mgr.getApplicableRegions(pt);

            if (!plugin.getGlobalRegionManager().hasBypass(player, player.getWorld())
                && !set.allows(DefaultFlag.SLEEP)) {
                    event.setCancelled(true);
                    player.sendMessage("This bed doesn't belong to you!");
                    return;
            }
        }
    }
}
