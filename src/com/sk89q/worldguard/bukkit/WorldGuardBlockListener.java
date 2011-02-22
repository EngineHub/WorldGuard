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

import java.util.Iterator;
import java.util.List;
import org.bukkit.block.Block;
import org.bukkit.block.BlockDamageLevel;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.World;
import org.bukkit.event.block.*;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.inventory.ItemStack;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.blacklist.events.*;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.AreaFlags;
import static com.sk89q.worldguard.bukkit.BukkitUtil.*;

public class WorldGuardBlockListener extends BlockListener {

    /**
     * Plugin.
     */
    private WorldGuardPlugin plugin;
    
    /**
     * Construct the object;
     * 
     * @param plugin
     */
    public WorldGuardBlockListener(WorldGuardPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Called when a block is damaged (or broken)
     *
     * @param event Relevant event details
     */
    @Override
    public void onBlockDamage(BlockDamageEvent event) {

        if(event.isCancelled())
        {
            return;
        }

        Player player = event.getPlayer();

        if (!plugin.itemDurability && event.getDamageLevel() == BlockDamageLevel.BROKEN) {
            ItemStack held = player.getItemInHand();
            if (held.getTypeId() > 0) {
                held.setDurability((short) -1);
                player.setItemInHand(held);
            }
        }
        
        if (plugin.useRegions && event.getDamageLevel() == BlockDamageLevel.BROKEN) {
            Vector pt = BukkitUtil.toVector(event.getBlock());
            LocalPlayer localPlayer = plugin.wrapPlayer(player);
            
            if (!plugin.hasPermission(player, "/regionbypass")
                    && !plugin.regionManager.getApplicableRegions(pt).canBuild(localPlayer)) {
                player.sendMessage(ChatColor.DARK_RED + "You don't have permission for this area.");
                event.setCancelled(true);
                return;
            }
        }
        
        if (plugin.blacklist != null && event.getDamageLevel() == BlockDamageLevel.BROKEN) {
            if (!plugin.blacklist.check(
                    new BlockBreakBlacklistEvent(plugin.wrapPlayer(player),
                            toVector(event.getBlock()),
                            event.getBlock().getTypeId()), false, false)) {
                event.setCancelled(true);
                return;
            }

            if (!plugin.blacklist.check(
                    new DestroyWithBlacklistEvent(plugin.wrapPlayer(player),
                            toVector(event.getBlock()),
                            player.getItemInHand().getTypeId()), false, false)) {
                event.setCancelled(true);
                return;
            }
        }

        Block blockDamaged = event.getBlock();
        if (plugin.useRegions && blockDamaged.getType() == Material.CAKE_BLOCK) {


            Vector pt = toVector(blockDamaged);
            LocalPlayer localPlayer = plugin.wrapPlayer(player);

            if (!plugin.hasPermission(player, "/regionbypass")
                    && !plugin.regionManager.getApplicableRegions(pt).canBuild(localPlayer)) {
                player.sendMessage(ChatColor.DARK_RED + "You don't have permission for this area.");

                event.setCancelled(true);
                return;
            }
        }
    }

    /**
     * Called when a block flows (water/lava)
     *
     * @param event Relevant event details
     */
    @Override
    public void onBlockFlow(BlockFromToEvent event) {

        if(event.isCancelled())
        {
            return;
        }

        World world = event.getBlock().getWorld();
        Block blockFrom = event.getBlock();
        Block blockTo = event.getToBlock();
        
        boolean isWater = blockFrom.getTypeId() == 8 || blockFrom.getTypeId() == 9;
        boolean isLava = blockFrom.getTypeId() == 10 || blockFrom.getTypeId() == 11;

        if (plugin.simulateSponge && isWater) {
            int ox = blockTo.getX();
            int oy = blockTo.getY();
            int oz = blockTo.getZ();

            for (int cx = -plugin.spongeRadius; cx <= plugin.spongeRadius; cx++) {
                for (int cy = -plugin.spongeRadius; cy <= plugin.spongeRadius; cy++) {
                    for (int cz = -plugin.spongeRadius; cz <= plugin.spongeRadius; cz++) {
                        Block sponge = world.getBlockAt(ox + cx, oy + cy, oz + cz);
                        if (sponge.getTypeId() == 19
                                && (!plugin.redstoneSponges || !sponge.isBlockIndirectlyPowered())) {
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

        // Check the fluid block (from) whether it is air. If so and the target block is protected, cancel the event
        if(plugin.preventWaterDamage.size() > 0 && blockFrom.getTypeId() == 0) {
            int targetId = world.getBlockTypeIdAt(
                    blockTo.getX(), blockTo.getY(), blockTo.getZ());
            if (plugin.preventWaterDamage.contains(targetId)) {
                event.setCancelled(true);
                return;
            }
        }
        
        if (plugin.preventWaterDamage.size() > 0 && isWater) {
            int targetId = world.getBlockTypeIdAt(
                    blockTo.getX(), blockTo.getY(), blockTo.getZ());
            if (plugin.preventWaterDamage.contains(targetId)) {
                event.setCancelled(true);
                return;
            }
        }

        if (plugin.allowedLavaSpreadOver.size() > 0 && isLava) {
            int targetId = world.getBlockTypeIdAt(
                    blockTo.getX(), blockTo.getY() - 1, blockTo.getZ());
            if (!plugin.allowedLavaSpreadOver.contains(targetId)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    /**
     * Called when a block gets ignited
     *
     * @param event Relevant event details
     */
    @Override
    public void onBlockIgnite(BlockIgniteEvent event) {

        if(event.isCancelled())
        {
            return;
        }

        IgniteCause cause = event.getCause();
        Block block = event.getBlock();
        //Player player = event.getPlayer();
        World world = block.getWorld();
        boolean isFireSpread = cause == IgniteCause.SPREAD;
        
        if (plugin.preventLavaFire && cause == IgniteCause.LAVA) {
            event.setCancelled(true);
            return;
        }

        if (plugin.disableFireSpread && isFireSpread) {
            event.setCancelled(true);
            return;
        }
        
        if (plugin.blockLighter && cause == IgniteCause.FLINT_AND_STEEL) {
            event.setCancelled(true);
            return;
        }

        if (plugin.fireSpreadDisableToggle && isFireSpread) {
            event.setCancelled(true);
            return;
        }

        if (plugin.disableFireSpreadBlocks.size() > 0 && isFireSpread) {
            int x = block.getX();
            int y = block.getY();
            int z = block.getZ();
            
            if (plugin.disableFireSpreadBlocks.contains(world.getBlockTypeIdAt(x, y - 1, z))
                    || plugin.disableFireSpreadBlocks.contains(world.getBlockTypeIdAt(x + 1, y, z))
                    || plugin.disableFireSpreadBlocks.contains(world.getBlockTypeIdAt(x - 1, y, z))
                    || plugin.disableFireSpreadBlocks.contains(world.getBlockTypeIdAt(x, y, z - 1))
                    || plugin.disableFireSpreadBlocks.contains(world.getBlockTypeIdAt(x, y, z + 1))) {
                event.setCancelled(true);
                return;
            }
        }
        
        /*if (plugin.useRegions) {
            Vector pt = toVector(block);
            
            if (player != null && !plugin.hasPermission(player, "/regionbypass")) {
                LocalPlayer localPlayer = plugin.wrapPlayer(player);
                
                if (cause == IgniteCause.FLINT_AND_STEEL
                        && !plugin.regionManager.getApplicableRegions(pt).canBuild(localPlayer)) {
                    event.setCancelled(true);
                    return;
                }
                
                if (cause == IgniteCause.FLINT_AND_STEEL
                        && !plugin.regionManager.getApplicableRegions(pt)
                        .allowsFlag(AreaFlags.FLAG_LIGHTER)) {
                    event.setCancelled(true);
                    return;
                }
            }
            
            f (isFireSpread && !plugin.regionManager.getApplicableRegions(pt)
                    .allowsFlag(AreaFlags.FLAG_FIRE_SPREAD)) {
                event.setCancelled(true);
                return;
            }
            
            if (cause == IgniteCause.LAVA && !plugin.regionManager.getApplicableRegions(pt)
                    .allowsFlag(AreaFlags.FLAG_LAVA_FIRE)) {
                event.setCancelled(true);
                return;
            }
        }*/
    }

    /**
     * Called when a block is destroyed from burning
     *
     * @param event Relevant event details
     */
    @Override
    public void onBlockBurn(BlockBurnEvent event) {

        if(event.isCancelled())
        {
            return;
        }

        if (plugin.disableFireSpread) {
            event.setCancelled(true);
            return;
        }

        if (plugin.fireSpreadDisableToggle) {
            event.setCancelled(true);
            return;
        }

        if (plugin.disableFireSpreadBlocks.size() > 0) {
            Block block = event.getBlock();

            if (plugin.disableFireSpreadBlocks.contains(block.getTypeId())) {
                event.setCancelled(true);
                return;
            }
        }
    }

    /**
     * Called when block physics occurs
     *
     * @param event Relevant event details
     */
    @Override
    public void onBlockPhysics(BlockPhysicsEvent event) {

        if(event.isCancelled())
        {
            return;
        }

        int id = event.getChangedTypeId();

        if (id == 13 && plugin.noPhysicsGravel) {
            event.setCancelled(true);
            return;
        }

        if (id == 12 && plugin.noPhysicsSand) {
            event.setCancelled(true);
            return;
        }

        if (id == 90 && plugin.allowPortalAnywhere) {
            event.setCancelled(true);
            return;
        }
    }
    
    /**
     * Called when a block is interacted with
     * 
     * @param event Relevant event details
     */
    @Override
    public void onBlockInteract(BlockInteractEvent event) {

        if(event.isCancelled())
        {
            return;
        }

        Block block = event.getBlock();
        LivingEntity entity = event.getEntity();
        
        if (entity instanceof Player
                && (block.getType() == Material.CHEST
                        || block.getType() == Material.DISPENSER
                        || block.getType() == Material.FURNACE
                        || block.getType() == Material.BURNING_FURNACE
                        || block.getType() == Material.NOTE_BLOCK)) {
            Player player = (Player)entity;
            if (plugin.useRegions) {
                Vector pt = toVector(block);
                LocalPlayer localPlayer = plugin.wrapPlayer(player);
    
                if (!plugin.hasPermission(player, "/regionbypass")
                        && !plugin.regionManager.getApplicableRegions(pt).allowsFlag(AreaFlags.FLAG_CHEST_ACCESS)
                        && !plugin.regionManager.getApplicableRegions(pt).canBuild(localPlayer)) {
                    player.sendMessage(ChatColor.DARK_RED + "You don't have permission for this area.");
                    event.setCancelled(true);
                    return;
                }
            }
        }
        
        if (plugin.blacklist != null && entity instanceof Player) {
            Player player = (Player)entity;
            
            if (!plugin.blacklist.check(
                    new BlockInteractBlacklistEvent(plugin.wrapPlayer(player), toVector(block),
                            block.getTypeId()), false, false)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    /**
     * Called when a player places a block
     *
     * @param event Relevant event details
     */
    @Override
    public void onBlockPlace(BlockPlaceEvent event) {

        if(event.isCancelled())
        {
            return;
        }

        Block blockPlaced = event.getBlock();
        Player player = event.getPlayer();
        World world = blockPlaced.getWorld();
        
        if (plugin.useRegions) {
            Vector pt = toVector(blockPlaced);
            LocalPlayer localPlayer = plugin.wrapPlayer(player);

            if (!plugin.hasPermission(player, "/regionbypass")
                    && !plugin.regionManager.getApplicableRegions(pt).canBuild(localPlayer)) {
                player.sendMessage(ChatColor.DARK_RED + "You don't have permission for this area.");
                event.setCancelled(true);
                return;
            }
        }
        
        if (plugin.blacklist != null) {
            if (!plugin.blacklist.check(
                    new BlockPlaceBlacklistEvent(plugin.wrapPlayer(player), toVector(blockPlaced),
                            blockPlaced.getTypeId()), false, false)) {
                event.setCancelled(true);
                return;
            }
        }

        if (plugin.simulateSponge && blockPlaced.getTypeId() == 19) {
            if (plugin.redstoneSponges && blockPlaced.isBlockIndirectlyPowered()) {
                return;
            }
            
            int ox = blockPlaced.getX();
            int oy = blockPlaced.getY();
            int oz = blockPlaced.getZ();

            clearSpongeWater(world, ox, oy, oz);
        }
    }

    /**
     * Called when a player right clicks a block
     *
     * @param event Relevant event details
     */
    @Override
    public void onBlockRightClick(BlockRightClickEvent event) {

        Player player = event.getPlayer();
        Block blockClicked = event.getBlock();
        
        if (plugin.useRegions && event.getItemInHand().getTypeId() == plugin.regionWand) {
            Vector pt = toVector(blockClicked);
            ApplicableRegionSet app = plugin.regionManager.getApplicableRegions(pt);
            List<String> regions = plugin.regionManager.getApplicableRegionsIDs(pt);
            
            if (regions.size() > 0) {
                player.sendMessage(ChatColor.YELLOW + "Can you build? "
                        + (app.canBuild(plugin.wrapPlayer(player)) ? "Yes" : "No"));
                
                StringBuilder str = new StringBuilder();
                for (Iterator<String> it = regions.iterator(); it.hasNext(); ) {
                    str.append(it.next());
                    if (it.hasNext()) {
                        str.append(", ");
                    }
                }
                
                player.sendMessage(ChatColor.YELLOW + "Applicable regions: " + str.toString());
            } else {
                player.sendMessage(ChatColor.YELLOW + "WorldGuard: No defined regions here!");
            }
        }

        Material type = blockClicked.getType();

        if (plugin.useRegions && type == Material.CAKE_BLOCK) {

            Vector pt = toVector(blockClicked);
            LocalPlayer localPlayer = plugin.wrapPlayer(player);

            if (!plugin.hasPermission(player, "/regionbypass")
                    && !plugin.regionManager.getApplicableRegions(pt).canBuild(localPlayer)) {
                player.sendMessage(ChatColor.DARK_RED + "You don't have permission for this area.");

                byte newData = (byte) (blockClicked.getData() - 1);
                newData = newData < 0 ? 0 : newData;

                blockClicked.setData(newData);
                player.setHealth(player.getHealth() - 3);

                return;
            }
        }
    }


    /**
     * Called when redstone changes
     * From: the source of the redstone change
     * To: The redstone dust that changed
     *
     * @param event Relevant event details
     */
    @Override
    public void onBlockRedstoneChange(BlockRedstoneEvent event) {

        World world = event.getBlock().getWorld();
        Block blockTo = event.getBlock();

        if (plugin.simulateSponge && plugin.redstoneSponges) {
            int ox = blockTo.getX();
            int oy = blockTo.getY();
            int oz = blockTo.getZ();
            
            for (int cx = -1; cx <= 1; cx++) {
                for (int cy = -1; cy <= 1; cy++) {
                    for (int cz = -1; cz <= 1; cz++) {
                        Block sponge = world.getBlockAt(ox + cx, oy + cy, oz + cz);
                        if (sponge.getTypeId() == 19
                                && sponge.isBlockIndirectlyPowered()) {
                            clearSpongeWater(world, ox + cx, oy + cy, oz + cz);
                        } else if (sponge.getTypeId() == 19
                                && ! sponge.isBlockIndirectlyPowered()) {
                            addSpongeWater(world, ox + cx, oy + cy, oz + cz);
                        }
                    }
                }
            }
            
            return;
        }
    }
    
    /**
     * Remove water around a sponge.
     * 
     * @param world
     * @param ox
     * @param oy
     * @param oz
     */
    private void clearSpongeWater(World world, int ox, int oy, int oz) {
        for (int cx = -plugin.spongeRadius; cx <= plugin.spongeRadius; cx++) {
            for (int cy = -plugin.spongeRadius; cy <= plugin.spongeRadius; cy++) {
                for (int cz = -plugin.spongeRadius; cz <= plugin.spongeRadius; cz++) {
                    if (isBlockWater(world, ox + cx, oy + cy, oz + cz)) {
                        world.getBlockAt(ox + cx, oy + cy, oz + cz)
                                .setTypeId(0);
                    }
                }
            }
        }
    }
    
    /**
     * Add water around a sponge.
     * 
     * @param world
     * @param ox
     * @param oy
     * @param oz
     */
    private void addSpongeWater(World world, int ox, int oy, int oz) {
        // The negative x edge
        int cx = ox - plugin.spongeRadius - 1;
        for (int cy = oy - plugin.spongeRadius - 1; cy <= oy + plugin.spongeRadius + 1; cy++) {
            for (int cz = oz - plugin.spongeRadius - 1; cz <= oz + plugin.spongeRadius + 1; cz++) {
                if (isBlockWater(world, cx, cy, cz)) {
                    setBlockToWater(world, cx + 1, cy, cz);
                }
            }
        }
        
        // The positive x edge
        cx = ox + plugin.spongeRadius + 1;
        for (int cy = oy - plugin.spongeRadius - 1; cy <= oy + plugin.spongeRadius + 1; cy++) {
            for (int cz = oz - plugin.spongeRadius - 1; cz <= oz + plugin.spongeRadius + 1; cz++) {
                if (isBlockWater(world, cx, cy, cz)) {
                    setBlockToWater(world, cx - 1, cy, cz);
                }
            }
        }

        // The negative y edge
        int cy = oy - plugin.spongeRadius - 1;
        for (cx = ox - plugin.spongeRadius - 1; cx <= ox + plugin.spongeRadius + 1; cx++) {
            for (int cz = oz - plugin.spongeRadius - 1; cz <= oz + plugin.spongeRadius + 1; cz++) {
                if (isBlockWater(world, cx, cy, cz)) {
                    setBlockToWater(world, cx, cy + 1, cz);
                }
            }
        }
        
        // The positive y edge
        cy = oy + plugin.spongeRadius + 1;
        for (cx = ox - plugin.spongeRadius - 1; cx <= ox + plugin.spongeRadius + 1; cx++) {
            for (int cz = oz - plugin.spongeRadius - 1; cz <= oz + plugin.spongeRadius + 1; cz++) {
                if (isBlockWater(world, cx, cy, cz)) {
                    setBlockToWater(world, cx, cy - 1, cz);
                }
            }
        }
        
        // The negative z edge
        int cz = oz - plugin.spongeRadius - 1;
        for (cx = ox - plugin.spongeRadius - 1; cx <= ox + plugin.spongeRadius + 1; cx++) {
            for (cy = oy - plugin.spongeRadius - 1; cy <= oy + plugin.spongeRadius + 1; cy++) {
                if (isBlockWater(world, cx, cy, cz)) {
                    setBlockToWater(world, cx, cy, cz + 1);
                }
            }
        }
        
        // The positive z edge
        cz = oz + plugin.spongeRadius + 1;
        for (cx = ox - plugin.spongeRadius - 1; cx <= ox + plugin.spongeRadius + 1; cx++) {
            for (cy = oy - plugin.spongeRadius - 1; cy <= oy + plugin.spongeRadius + 1; cy++) {
                if (isBlockWater(world, cx, cy, cz)) {
                    setBlockToWater(world, cx, cy, cz - 1);
                }
            }
        }
    }
    
    /**
     * Sets the given block to fluid water.
     * Used by addSpongeWater()
     * 
     * @see addSpongeWater()
     * 
     * @param world
     * @param ox
     * @param oy
     * @param oz
     */
    private void setBlockToWater(World world, int ox, int oy, int oz) {
        world.getBlockAt(ox, oy, oz)
        .setTypeId( 8 );
    }
    
    /**
     * Checks if the given block is water
     * 
     * @param world
     * @param ox
     * @param oy
     * @param oz
     */
    private boolean isBlockWater(World world, int ox, int oy, int oz) {
        Block block = world.getBlockAt(ox, oy, oz);
        int id = block.getTypeId();
        if (id == 8 || id == 9) {
            return true;
        } else {
            return false;
        }
    }
}
