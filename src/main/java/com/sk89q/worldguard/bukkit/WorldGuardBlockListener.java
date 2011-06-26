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

import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event;
import org.bukkit.plugin.PluginManager;
import org.bukkit.block.Block;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.World;
import org.bukkit.event.block.*;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.inventory.ItemStack;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BlockType;
import com.sk89q.worldedit.blocks.ItemType;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.blacklist.events.*;
import com.sk89q.worldguard.protection.ApplicableRegionSet;

import java.util.logging.Logger;

import static com.sk89q.worldguard.bukkit.BukkitUtil.*;

/**
 * The listener for block events.
 * 
 * @author sk89q
 */
public class WorldGuardBlockListener extends BlockListener {
    /**
     * Logger for messages.
     */
    private static final Logger logger = Logger.getLogger("Minecraft.WorldGuard");

    private WorldGuardPlugin plugin;

    /**
     * Construct the object.
     * 
     * @param plugin
     */
    public WorldGuardBlockListener(WorldGuardPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Register events.
     */
    public void registerEvents() {
        PluginManager pm = plugin.getServer().getPluginManager();

        pm.registerEvent(Event.Type.BLOCK_DAMAGE, this, Priority.High, plugin);
        pm.registerEvent(Event.Type.BLOCK_BREAK, this, Priority.High, plugin);
        pm.registerEvent(Event.Type.BLOCK_FROMTO, this, Priority.Normal, plugin);
        pm.registerEvent(Event.Type.BLOCK_IGNITE, this, Priority.High, plugin);
        pm.registerEvent(Event.Type.BLOCK_PHYSICS, this, Priority.Normal, plugin);
        pm.registerEvent(Event.Type.BLOCK_PLACE, this, Priority.High, plugin);
        pm.registerEvent(Event.Type.BLOCK_BURN, this, Priority.High, plugin);
        pm.registerEvent(Event.Type.SIGN_CHANGE, this, Priority.High, plugin);
        pm.registerEvent(Event.Type.REDSTONE_CHANGE, this, Priority.High, plugin);
        pm.registerEvent(Event.Type.LEAVES_DECAY, this, Priority.High, plugin);
        registerEventSafe("SNOW_FORM", Priority.High);
        registerEventSafe("BLOCK_FORM", Priority.High);
        registerEventSafe("BLOCK_SPREAD", Priority.High);
    }

    private void registerEventSafe(String typeName, Priority priority) {
        try {
            Event.Type type = Event.Type.valueOf(typeName);
            PluginManager pm = plugin.getServer().getPluginManager();
            pm.registerEvent(type, this, priority, plugin);
        } catch (IllegalArgumentException e) {
            logger.info("WorldGuard: Unable to register missing event type " + typeName);
        }
    }
    
    /**
     * Get the world configuration given a world.
     * 
     * @param world
     * @return
     */
    protected WorldConfiguration getWorldConfig(World world) {
        return plugin.getGlobalStateManager().get(world);
    }
    
    /**
     * Get the world configuration given a player.
     * 
     * @param player
     * @return
     */
    protected WorldConfiguration getWorldConfig(Player player) {
        return plugin.getGlobalStateManager().get(player.getWorld());
    }
    
    /**
     * Called when a block is damaged.
     */
    @Override
    public void onBlockDamage(BlockDamageEvent event) {
        if (event.isCancelled()) {
            return;
        }

        Player player = event.getPlayer();
        Block blockDamaged = event.getBlock();

        // Cake are damaged and not broken when they are eaten, so we must
        // handle them a bit separately
        if (blockDamaged.getType() == Material.CAKE_BLOCK) {
            if (!plugin.getGlobalRegionManager().canBuild(player, blockDamaged)) {
                player.sendMessage(ChatColor.DARK_RED + "You're not invited to this tea party!");
                event.setCancelled(true);
                return;
            }
        }
    }
    
    /**
     * Called when a block is broken.
     */
    @Override
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled()) {
            return;
        }

        Player player = event.getPlayer();
        WorldConfiguration wcfg = getWorldConfig(player);

        if (!wcfg.itemDurability) {
            ItemStack held = player.getItemInHand();
            if (held.getTypeId() > 0
                    && !(ItemType.usesDamageValue(held.getTypeId())
                    || BlockType.usesData(held.getTypeId()))) {
                held.setDurability((short) -1);
                player.setItemInHand(held);
            }
        }

        if (!plugin.getGlobalRegionManager().canBuild(player, event.getBlock())) {
            player.sendMessage(ChatColor.DARK_RED + "You don't have permission for this area.");
            event.setCancelled(true);
            return;
        }

        if (wcfg.getBlacklist() != null) {
            if (!wcfg.getBlacklist().check(
                    new BlockBreakBlacklistEvent(plugin.wrapPlayer(player),
                    toVector(event.getBlock()),
                    event.getBlock().getTypeId()), false, false)) {
                event.setCancelled(true);
                return;
            }

            if (!wcfg.getBlacklist().check(
                    new DestroyWithBlacklistEvent(plugin.wrapPlayer(player),
                    toVector(event.getBlock()),
                    player.getItemInHand().getTypeId()), false, false)) {
                event.setCancelled(true);
                return;
            }
        }
        
        if (wcfg.isChestProtected(event.getBlock(), player)) {
            player.sendMessage(ChatColor.DARK_RED + "The chest is protected.");
            event.setCancelled(true);
            return;
        }
    }

    /**
     * Called when fluids flow.
     */
    @Override
    public void onBlockFromTo(BlockFromToEvent event) {
        if (event.isCancelled()) {
            return;
        }

        World world = event.getBlock().getWorld();
        Block blockFrom = event.getBlock();
        Block blockTo = event.getToBlock();

        boolean isWater = blockFrom.getTypeId() == 8 || blockFrom.getTypeId() == 9;
        boolean isLava = blockFrom.getTypeId() == 10 || blockFrom.getTypeId() == 11;

        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(event.getBlock().getWorld());

        if (cfg.activityHaltToggle) {
            event.setCancelled(true);
            return;
        }

        if (wcfg.simulateSponge && isWater) {
            int ox = blockTo.getX();
            int oy = blockTo.getY();
            int oz = blockTo.getZ();

            for (int cx = -wcfg.spongeRadius; cx <= wcfg.spongeRadius; cx++) {
                for (int cy = -wcfg.spongeRadius; cy <= wcfg.spongeRadius; cy++) {
                    for (int cz = -wcfg.spongeRadius; cz <= wcfg.spongeRadius; cz++) {
                        Block sponge = world.getBlockAt(ox + cx, oy + cy, oz + cz);
                        if (sponge.getTypeId() == 19
                                && (!wcfg.redstoneSponges || !sponge.isBlockIndirectlyPowered())) {
                            event.setCancelled(true);
                            return;
                        }
                    }
                }
            }
        }

        /*if (plugin.classicWater && isWater) {
        int blockBelow = blockFrom.getRelative(0, -1, 0).getTypeId();
        if (blockBelow != 0 && blockBelow != 8 && blockBelow != 9) {
        blockFrom.setTypeId(9);
        if (blockTo.getTypeId() == 0) {
        blockTo.setTypeId(9);
        }
        return;
        }
        }*/

        // Check the fluid block (from) whether it is air.
        // If so and the target block is protected, cancel the event
        if (wcfg.preventWaterDamage.size() > 0) {
            int targetId = blockTo.getTypeId();
            
            if ((blockFrom.getTypeId() == 0 || isWater) && 
                    wcfg.preventWaterDamage.contains(targetId)) {
                event.setCancelled(true);
                return;
            }
        }

        if (wcfg.allowedLavaSpreadOver.size() > 0 && isLava) {
            int targetId = blockTo.getRelative(0, -1, 0).getTypeId();
            
            if (!wcfg.allowedLavaSpreadOver.contains(targetId)) {
                event.setCancelled(true);
                return;
            }
        }

        if (wcfg.highFreqFlags && isWater
                && !plugin.getGlobalRegionManager().allows(DefaultFlag.WATER_FLOW,
                blockFrom.getLocation())) {
            event.setCancelled(true);
            return;
        }

        if (wcfg.highFreqFlags && isLava
                && !plugin.getGlobalRegionManager().allows(DefaultFlag.LAVA_FLOW,
                blockFrom.getLocation())) {
            event.setCancelled(true);
            return;
        }
    }

    /**
     * Called when a block gets ignited.
     */
    @Override
    public void onBlockIgnite(BlockIgniteEvent event) {
        if (event.isCancelled()) {
            return;
        }

        IgniteCause cause = event.getCause();
        Block block = event.getBlock();
        World world = block.getWorld();

        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(world);

        if (cfg.activityHaltToggle) {
            event.setCancelled(true);
            return;
        }

        boolean isFireSpread = cause == IgniteCause.SPREAD;

        if (wcfg.preventLightningFire && cause == IgniteCause.LIGHTNING) {
            event.setCancelled(true);
            return;
        }

        if (wcfg.preventLavaFire && cause == IgniteCause.LAVA) {
            event.setCancelled(true);
            return;
        }

        if (wcfg.disableFireSpread && isFireSpread) {
            event.setCancelled(true);
            return;
        }

        if (wcfg.blockLighter && cause == IgniteCause.FLINT_AND_STEEL
                && event.getPlayer() != null
                && !plugin.hasPermission(event.getPlayer(), "worldguard.override.lighter")) {
            event.setCancelled(true);
            return;
        }

        if (wcfg.fireSpreadDisableToggle && isFireSpread) {
            event.setCancelled(true);
            return;
        }

        if (wcfg.disableFireSpreadBlocks.size() > 0 && isFireSpread) {
            int x = block.getX();
            int y = block.getY();
            int z = block.getZ();

            if (wcfg.disableFireSpreadBlocks.contains(world.getBlockTypeIdAt(x, y - 1, z))
                    || wcfg.disableFireSpreadBlocks.contains(world.getBlockTypeIdAt(x + 1, y, z))
                    || wcfg.disableFireSpreadBlocks.contains(world.getBlockTypeIdAt(x - 1, y, z))
                    || wcfg.disableFireSpreadBlocks.contains(world.getBlockTypeIdAt(x, y, z - 1))
                    || wcfg.disableFireSpreadBlocks.contains(world.getBlockTypeIdAt(x, y, z + 1))) {
                event.setCancelled(true);
                return;
            }
        }

        if (wcfg.useRegions) {
            Vector pt = toVector(block);
            Player player = event.getPlayer();
            RegionManager mgr = plugin.getGlobalRegionManager().get(world);
            ApplicableRegionSet set = mgr.getApplicableRegions(pt);

            if (player != null && !plugin.getGlobalRegionManager().hasBypass(player, world)) {
                LocalPlayer localPlayer = plugin.wrapPlayer(player);

                if (cause == IgniteCause.FLINT_AND_STEEL) {
                    if (!set.canBuild(localPlayer)) {
                        event.setCancelled(true);
                        return;
                    }
                    if (!set.allows(DefaultFlag.LIGHTER)
                            && !plugin.hasPermission(player, "worldguard.override.lighter")) {
                        event.setCancelled(true);
                        return;
                    }
                }
            }

            if (wcfg.highFreqFlags && isFireSpread
                    && !set.allows(DefaultFlag.FIRE_SPREAD)) {
                event.setCancelled(true);
                return;
            }

            if (wcfg.highFreqFlags && cause == IgniteCause.LAVA
                    && !set.allows(DefaultFlag.LAVA_FIRE)) {
                event.setCancelled(true);
                return;
            }
        }

    }

    /**
     * Called when a block is destroyed from burning.
     */
    @Override
    public void onBlockBurn(BlockBurnEvent event) {

        if (event.isCancelled()) {
            return;
        }

        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(event.getBlock().getWorld());

        if (cfg.activityHaltToggle) {
            event.setCancelled(true);
            return;
        }

        if (wcfg.disableFireSpread) {
            event.setCancelled(true);
            return;
        }

        if (wcfg.fireSpreadDisableToggle) {
            event.setCancelled(true);
            return;
        }

        if (wcfg.disableFireSpreadBlocks.size() > 0) {
            Block block = event.getBlock();

            if (wcfg.disableFireSpreadBlocks.contains(block.getTypeId())) {
                event.setCancelled(true);
                return;
            }
        }

        if (wcfg.isChestProtected(event.getBlock())) {
            event.setCancelled(true);
            return;
        }
        
        if (wcfg.useRegions) {
            Block block = event.getBlock();
            Vector pt = toVector(block);
            RegionManager mgr = plugin.getGlobalRegionManager().get(block.getWorld());
            ApplicableRegionSet set = mgr.getApplicableRegions(pt);

            if (!set.allows(DefaultFlag.FIRE_SPREAD)) {
                event.setCancelled(true);
                return;
            }

        }
    }

    /**
     * Called when block physics occurs.
     */
    @Override
    public void onBlockPhysics(BlockPhysicsEvent event) {

        if (event.isCancelled()) {
            return;
        }

        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(event.getBlock().getWorld());

        if (cfg.activityHaltToggle) {
            event.setCancelled(true);
            return;
        }

        int id = event.getChangedTypeId();

        if (id == 13 && wcfg.noPhysicsGravel) {
            event.setCancelled(true);
            return;
        }

        if (id == 12 && wcfg.noPhysicsSand) {
            event.setCancelled(true);
            return;
        }

        if (id == 90 && wcfg.allowPortalAnywhere) {
            event.setCancelled(true);
            return;
        }
    }

    /**
     * Called when a player places a block.
     */
    @Override
    public void onBlockPlace(BlockPlaceEvent event) {

        if (event.isCancelled()) {
            return;
        }

        Block blockPlaced = event.getBlock();
        Player player = event.getPlayer();
        World world = blockPlaced.getWorld();

        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(world);

        if (wcfg.useRegions) {
            if (!plugin.getGlobalRegionManager().canBuild(player, blockPlaced.getLocation())) {
                player.sendMessage(ChatColor.DARK_RED + "You don't have permission for this area.");
                event.setCancelled(true);
                return;
            }
        }

        if (wcfg.getBlacklist() != null) {
            if (!wcfg.getBlacklist().check(
                    new BlockPlaceBlacklistEvent(plugin.wrapPlayer(player), toVector(blockPlaced),
                    blockPlaced.getTypeId()), false, false)) {
                event.setCancelled(true);
                return;
            }
        }
        
        if (wcfg.signChestProtection && wcfg.getChestProtection().isChest(blockPlaced.getType())) {
            if (wcfg.isAdjacentChestProtected(event.getBlock(), player)) {
                player.sendMessage(ChatColor.DARK_RED + "This spot is for a chest that you don't have permission for.");
                event.setCancelled(true);
                return;
            }
        }

        if (wcfg.simulateSponge && blockPlaced.getTypeId() == 19) {
            if (wcfg.redstoneSponges && blockPlaced.isBlockIndirectlyPowered()) {
                return;
            }

            int ox = blockPlaced.getX();
            int oy = blockPlaced.getY();
            int oz = blockPlaced.getZ();

            SpongeUtil.clearSpongeWater(plugin, world, ox, oy, oz);
        }
    }

    /**
     * Called when redstone changes.
     */
    @Override
    public void onBlockRedstoneChange(BlockRedstoneEvent event) {

        Block blockTo = event.getBlock();
        World world = blockTo.getWorld();

        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(world);

        if (wcfg.simulateSponge && wcfg.redstoneSponges) {
            int ox = blockTo.getX();
            int oy = blockTo.getY();
            int oz = blockTo.getZ();

            for (int cx = -1; cx <= 1; cx++) {
                for (int cy = -1; cy <= 1; cy++) {
                    for (int cz = -1; cz <= 1; cz++) {
                        Block sponge = world.getBlockAt(ox + cx, oy + cy, oz + cz);
                        if (sponge.getTypeId() == 19
                                && sponge.isBlockIndirectlyPowered()) {
                            SpongeUtil.clearSpongeWater(plugin, world, ox + cx, oy + cy, oz + cz);
                        } else if (sponge.getTypeId() == 19
                                && !sponge.isBlockIndirectlyPowered()) {
                            SpongeUtil.addSpongeWater(plugin, world, ox + cx, oy + cy, oz + cz);
                        }
                    }
                }
            }

            return;
        }
    }

    /**
     * Called when a sign is changed.
     */
    @Override
    public void onSignChange(SignChangeEvent event) {

        Player player = event.getPlayer();
        WorldConfiguration wcfg = getWorldConfig(player);
        
        if (wcfg.signChestProtection) {
            if (event.getLine(0).equalsIgnoreCase("[Lock]")) {
                if (wcfg.isChestProtectedPlacement(event.getBlock(), player)) {
                    player.sendMessage(ChatColor.DARK_RED + "You do not own the adjacent chest.");
                    dropSign(event.getBlock());
                    event.setCancelled(true);
                    return;
                }
                
                if (event.getBlock().getType() != Material.SIGN_POST) {
                    player.sendMessage(ChatColor.RED
                            + "The [Lock] sign must be a sign post, not a wall sign.");

                    dropSign(event.getBlock());
                    event.setCancelled(true);
                    return;
                }

                if (!event.getLine(1).equalsIgnoreCase(player.getName())) {
                    player.sendMessage(ChatColor.RED
                            + "The first owner line must be your name.");

                    dropSign(event.getBlock());
                    event.setCancelled(true);
                    return;
                }
                
                Material below = event.getBlock().getRelative(0, -1, 0).getType();

                if (below == Material.TNT || below == Material.SAND
                        || below == Material.GRAVEL || below == Material.SIGN_POST) {
                    player.sendMessage(ChatColor.RED
                            + "That is not a safe block that you're putting this sign on.");

                    dropSign(event.getBlock());
                    event.setCancelled(true);
                    return;
                }
                
                event.setLine(0, "[Lock]");
                player.sendMessage(ChatColor.YELLOW
                        + "A chest or double chest above is now protected.");
            }
        } else {
            if (event.getLine(0).equalsIgnoreCase("[Lock]")) {
                player.sendMessage(ChatColor.RED
                        + "WorldGuard's sign chest protection is disabled.");
                
                dropSign(event.getBlock());
                event.setCancelled(true);
                return;
            }
        }
        
        if(wcfg.useiConomy && iConomyManager.isloaded()){ //Economy Support 
        	Block block = event.getBlock();
	        RegionManager mgr = plugin.getGlobalRegionManager().get(block.getWorld());
	        LocalPlayer localPlayer = new BukkitPlayer(plugin,player);
	        
			if ((event.getLine(0).equalsIgnoreCase("[Buy Region]") || event.getLine(0).equalsIgnoreCase("#1[Buy Region]"))){
				String regionString = event.getLine(1);
				ProtectedRegion rgn = mgr.getRegion(regionString);
				

				event.setLine(0, "§1[Buy Region]");
				
				if (!mgr.hasRegion(regionString)){
					player.sendMessage(ChatColor.RED + "Region \""+regionString+"\" does not exist.");
				} else {
					boolean hasFlagCommand = false;
					
					if (rgn.isOwner(localPlayer))
						hasFlagCommand = localPlayer.hasPermission("worldguard.region.flag.own." + rgn.getId().toLowerCase());
			        else if (rgn.isMember(localPlayer))
			        	hasFlagCommand = localPlayer.hasPermission("worldguard.region.flag.member." + rgn.getId().toLowerCase());
			        else 
			        	hasFlagCommand = localPlayer.hasPermission("worldguard.region.flag." + rgn.getId().toLowerCase());

					
					boolean allowedToUseRequiredFlags = false;
					if (rgn.isOwner(localPlayer)) {
						allowedToUseRequiredFlags = 
							localPlayer.hasPermission("worldguard.region.flag.flags." + DefaultFlag.BUYABLE.getName() + 
								".owner." + rgn.getId().toLowerCase()) &&
								localPlayer.hasPermission("worldguard.region.flag.flags." + DefaultFlag.PRICE.getName() + 
								".owner." + rgn.getId().toLowerCase());
	                } else if (rgn.isMember(localPlayer)) {
	                	allowedToUseRequiredFlags = 
							localPlayer.hasPermission("worldguard.region.flag.flags." + DefaultFlag.BUYABLE.getName() + 
								".member." + rgn.getId().toLowerCase()) &&
								localPlayer.hasPermission("worldguard.region.flag.flags." + DefaultFlag.PRICE.getName() + 
								".member." + rgn.getId().toLowerCase());
	                } else {
	                	allowedToUseRequiredFlags = 
							localPlayer.hasPermission("worldguard.region.flag.flags." + DefaultFlag.BUYABLE.getName() + 
								"." + rgn.getId().toLowerCase()) &&
								localPlayer.hasPermission("worldguard.region.flag.flags." + DefaultFlag.PRICE.getName() + 
								"." + rgn.getId().toLowerCase());
	                } 
					
					if ( !(hasFlagCommand && allowedToUseRequiredFlags) ){
						player.sendMessage(ChatColor.DARK_RED + "You don't have permission.");
					} else {
						Vector signLocation = toVector(block.getLocation());
						iConomyManager econMgr = new iConomyManager();
		
						if (!rgn.contains(signLocation)){
							//To prevent confusion and scamming, region signs must be placed in specified region.
							//Use /region buy <id> to buy outside of region if need be.
							player.sendMessage(ChatColor.RED + "Sell sign must be placed in stated region.");
						} else {
							double price = -1;
							if (event.getLine(2) == null || event.getLine(2).equals("")){
								//Pull price from flag if possible, else set price from the sign
								if (rgn.getFlag(DefaultFlag.PRICE) != null && rgn.getFlag(DefaultFlag.PRICE) >= 0){
									price = rgn.getFlag(DefaultFlag.PRICE);
									rgn.setFlag(DefaultFlag.BUYABLE, true);
									event.setLine(2, econMgr.format(price)); 
									event.setLine(3, ChatColor.GRAY + player.getName()); //Affix player name
								}					
								else
									player.sendMessage(ChatColor.RED + "No price has been set previously or specified on the sign.");
							} else { //A value is set on the sign
								try{
									price = Double.parseDouble(event.getLine(2).replaceAll("[^0-9\\.]", ""));
									rgn.setFlag(DefaultFlag.PRICE, price);
									player.sendMessage(ChatColor.YELLOW+"Price of \""+regionString+"\" set to "+econMgr.format(price)+".");
									rgn.setFlag(DefaultFlag.BUYABLE, true);
									event.setLine(2, econMgr.format(price)); 
									event.setLine(3, ChatColor.GRAY + player.getName()); //Affix player name
								
								}catch(Exception e){
									player.sendMessage(ChatColor.RED + "Invalid price value.");
								}
							}
						}
					}
				}
				event.setLine(0, "§4[Buy Region]");//Set sign red and show proper formatting
				event.setLine(1, "<Region ID>");
				event.setLine(2, "$<Price>");
			}
		}
    }

    /**
     * Called when snow is formed.
     */
    @Override
    public void onSnowForm(SnowFormEvent event) {
        if (event.isCancelled()) {
            return;
        }

        ConfigurationManager cfg = plugin.getGlobalStateManager();

        if (cfg.activityHaltToggle) {
            event.setCancelled(true);
            return;
        }

        if (!plugin.getGlobalRegionManager().allows(DefaultFlag.SNOW_FALL,
                event.getBlock().getLocation())) {
            event.setCancelled(true);
        }
    }

    @Override
    public void onLeavesDecay(LeavesDecayEvent event) {
        if (event.isCancelled()) {
            return;
        }

        ConfigurationManager cfg = plugin.getGlobalStateManager();

        if (cfg.activityHaltToggle) {
            event.setCancelled(true);
            return;
        }

        if (!plugin.getGlobalRegionManager().allows(DefaultFlag.LEAF_DECAY, event.getBlock().getLocation())) {
            event.setCancelled(true);
        }
    }

    /**
     * Called when a block is formed based on world conditions.
     */
    public void onBlockForm(BlockFormEvent event) {
        ConfigurationManager cfg = plugin.getGlobalStateManager();

        if (cfg.activityHaltToggle) {
            event.setCancelled(true);
            return;
        }
    }

    /**
     * Called when a block spreads based on world conditions.
     */
    public void onBlockSpread(BlockSpreadEvent event) {
        ConfigurationManager cfg = plugin.getGlobalStateManager();

        if (cfg.activityHaltToggle) {
            event.setCancelled(true);
            return;
        }
    }
}
