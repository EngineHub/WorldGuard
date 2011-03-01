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

import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event;
import org.bukkit.plugin.PluginManager;
import com.sk89q.worldguard.protection.regions.flags.RegionFlagContainer;
import com.sk89q.worldguard.protection.regions.flags.FlagDatabase.FlagType;
import com.nijiko.coelho.iConomy.iConomy;
import com.nijiko.coelho.iConomy.system.Account;
import com.sk89q.worldguard.protection.regionmanager.RegionManager;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.block.Block;
import org.bukkit.block.Sign;
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
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

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

    public void registerEvents() {

        PluginManager pm = plugin.getServer().getPluginManager();

        pm.registerEvent(Event.Type.BLOCK_DAMAGED, this, Priority.High, plugin);
        pm.registerEvent(Event.Type.BLOCK_BREAK, this, Priority.High, plugin);
        pm.registerEvent(Event.Type.BLOCK_FLOW, this, Priority.Normal, plugin);
        pm.registerEvent(Event.Type.BLOCK_IGNITE, this, Priority.High, plugin);
        pm.registerEvent(Event.Type.BLOCK_PHYSICS, this, Priority.Normal, plugin);
        pm.registerEvent(Event.Type.BLOCK_INTERACT, this, Priority.High, plugin);
        pm.registerEvent(Event.Type.BLOCK_PLACED, this, Priority.High, plugin);
        pm.registerEvent(Event.Type.BLOCK_RIGHTCLICKED, this, Priority.High, plugin);
        pm.registerEvent(Event.Type.BLOCK_BURN, this, Priority.High, plugin);
        pm.registerEvent(Event.Type.REDSTONE_CHANGE, this, Priority.High, plugin);
    }

    /**
     * Called when a block is damaged (or broken)
     *
     * @param event Relevant event details
     */
    @Override
    public void onBlockDamage(BlockDamageEvent event) {
        if (event.isCancelled()) {
            return;
        }

        Player player = event.getPlayer();
        Block blockDamaged = event.getBlock();

        WorldGuardConfiguration cfg = plugin.getWgConfiguration();
        WorldGuardWorldConfiguration wcfg = cfg.getWorldConfig(player.getWorld().getName());

        if (wcfg.useRegions && blockDamaged.getType() == Material.CAKE_BLOCK) {
            Vector pt = toVector(blockDamaged);

            if (!cfg.canBuild(player, pt)) {
                player.sendMessage(ChatColor.DARK_RED + "You're not invited to this tea party!");

                event.setCancelled(true);
                return;
            }
        }


    }

    /**
     * Called when a block is destroyed by a player.
     *
     * @param event Relevant event details
     */
    @Override
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled()) {
            return;
        }

        Player player = event.getPlayer();
        WorldGuardConfiguration cfg = plugin.getWgConfiguration();
        WorldGuardWorldConfiguration wcfg = cfg.getWorldConfig(player.getWorld().getName());

        if (!wcfg.itemDurability) {
            ItemStack held = player.getItemInHand();
            if (held.getTypeId() > 0) {
                held.setDurability((short) -1);
                player.setItemInHand(held);
            }
        }

        if (wcfg.useRegions) {
            Vector pt = BukkitUtil.toVector(event.getBlock());

            if (!cfg.canBuild(player, pt)) {
                player.sendMessage(ChatColor.DARK_RED + "You don't have permission for this area.");
                event.setCancelled(true);
                return;
            }
        }

        if (wcfg.getBlacklist() != null) {
            if (!wcfg.getBlacklist().check(
                    new BlockBreakBlacklistEvent(BukkitPlayer.wrapPlayer(cfg, player),
                    toVector(event.getBlock()),
                    event.getBlock().getTypeId()), false, false)) {
                event.setCancelled(true);
                return;
            }

            if (wcfg.getBlacklist().check(
                    new DestroyWithBlacklistEvent(BukkitPlayer.wrapPlayer(cfg, player),
                    toVector(event.getBlock()),
                    player.getItemInHand().getTypeId()), false, false)) {
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

        if (event.isCancelled()) {
            return;
        }

        World world = event.getBlock().getWorld();
        Block blockFrom = event.getBlock();
        Block blockTo = event.getToBlock();

        boolean isWater = blockFrom.getTypeId() == 8 || blockFrom.getTypeId() == 9;
        boolean isLava = blockFrom.getTypeId() == 10 || blockFrom.getTypeId() == 11;

        WorldGuardConfiguration cfg = plugin.getWgConfiguration();
        WorldGuardWorldConfiguration wcfg = cfg.getWorldConfig(event.getBlock().getWorld().getName());

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

        // Check the fluid block (from) whether it is air. If so and the target block is protected, cancel the event
        if (wcfg.preventWaterDamage.size() > 0 && blockFrom.getTypeId() == 0) {
            int targetId = world.getBlockTypeIdAt(
                    blockTo.getX(), blockTo.getY(), blockTo.getZ());
            if (wcfg.preventWaterDamage.contains(targetId)) {
                event.setCancelled(true);
                return;
            }
        }

        if (wcfg.preventWaterDamage.size() > 0 && isWater) {
            int targetId = world.getBlockTypeIdAt(
                    blockTo.getX(), blockTo.getY(), blockTo.getZ());
            if (wcfg.preventWaterDamage.contains(targetId)) {
                event.setCancelled(true);
                return;
            }
        }

        if (wcfg.allowedLavaSpreadOver.size() > 0 && isLava) {
            int targetId = world.getBlockTypeIdAt(
                    blockTo.getX(), blockTo.getY() - 1, blockTo.getZ());
            if (!wcfg.allowedLavaSpreadOver.contains(targetId)) {
                event.setCancelled(true);
                return;
            }
        }

        if (wcfg.useRegions) {
            Vector pt = toVector(blockFrom.getLocation());
            RegionManager mgr = plugin.getGlobalRegionManager().getRegionManager(world.getName());

            if (!mgr.getApplicableRegions(pt).isStateFlagAllowed(FlagType.WATER_FLOW)) {
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

        if (event.isCancelled()) {
            return;
        }


        IgniteCause cause = event.getCause();
        Block block = event.getBlock();
        World world = block.getWorld();

        WorldGuardConfiguration cfg = plugin.getWgConfiguration();
        WorldGuardWorldConfiguration wcfg = cfg.getWorldConfig(world.getName());

        boolean isFireSpread = cause == IgniteCause.SPREAD;

        if (wcfg.useRegions) {
            Vector pt = toVector(block);
            Player player = event.getPlayer();
            RegionManager mgr = plugin.getGlobalRegionManager().getRegionManager(world.getName());

            ApplicableRegionSet set = mgr.getApplicableRegions(pt);

            if (player != null && !cfg.hasPermission(player, "region.bypass")) {
                LocalPlayer localPlayer = BukkitPlayer.wrapPlayer(cfg, player);

                if (cause == IgniteCause.FLINT_AND_STEEL
                        && !set.canBuild(localPlayer)) {
                    event.setCancelled(true);
                    return;
                }

                if (cause == IgniteCause.FLINT_AND_STEEL
                        && !set.isStateFlagAllowed(FlagType.LIGHTER)) {
                    event.setCancelled(true);
                    return;
                }
            }

            if (isFireSpread && set.isStateFlagAllowed(FlagType.FIRE_SPREAD)) {
                event.setCancelled(true);
                return;
            }

            if (cause == IgniteCause.LAVA && !set.isStateFlagAllowed(FlagType.LAVA_FIRE)) {
                event.setCancelled(true);
                return;
            }
        }

        if (wcfg.preventLavaFire && cause == IgniteCause.LAVA) {
            event.setCancelled(true);
            return;
        }

        if (wcfg.disableFireSpread && isFireSpread) {
            event.setCancelled(true);
            return;
        }

        if (wcfg.blockLighter && cause == IgniteCause.FLINT_AND_STEEL) {
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

    }

    /**
     * Called when a block is destroyed from burning
     *
     * @param event Relevant event details
     */
    @Override
    public void onBlockBurn(BlockBurnEvent event) {

        if (event.isCancelled()) {
            return;
        }

        WorldGuardConfiguration cfg = plugin.getWgConfiguration();
        WorldGuardWorldConfiguration wcfg = cfg.getWorldConfig(event.getBlock().getWorld().getName());

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
    }

    /**
     * Called when block physics occurs
     *
     * @param event Relevant event details
     */
    @Override
    public void onBlockPhysics(BlockPhysicsEvent event) {

        if (event.isCancelled()) {
            return;
        }

        WorldGuardConfiguration cfg = plugin.getWgConfiguration();
        WorldGuardWorldConfiguration wcfg = cfg.getWorldConfig(event.getBlock().getWorld().getName());

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
     * Called when a block is interacted with
     * 
     * @param event Relevant event details
     */
    @Override
    public void onBlockInteract(BlockInteractEvent event) {

        if (event.isCancelled()) {
            return;
        }

        Block block = event.getBlock();
        Material type = block.getType();
        LivingEntity entity = event.getEntity();

        WorldGuardConfiguration cfg = plugin.getWgConfiguration();
        WorldGuardWorldConfiguration wcfg = cfg.getWorldConfig(event.getBlock().getWorld().getName());

        if (entity instanceof Player
                && (block.getType() == Material.CHEST
                || block.getType() == Material.DISPENSER
                || block.getType() == Material.FURNACE
                || block.getType() == Material.BURNING_FURNACE
                || block.getType() == Material.NOTE_BLOCK)) {
            Player player = (Player) entity;
            if (wcfg.useRegions) {
                Vector pt = toVector(block);
                LocalPlayer localPlayer = BukkitPlayer.wrapPlayer(cfg, player);
                RegionManager mgr = plugin.getGlobalRegionManager().getRegionManager(player.getWorld().getName());

                if (!cfg.hasPermission(player, "region.bypass")) {
                    ApplicableRegionSet set = mgr.getApplicableRegions(pt);
                    if (!set.isStateFlagAllowed(FlagType.CHEST_ACCESS) && !set.canBuild(localPlayer)) {
                        player.sendMessage(ChatColor.DARK_RED + "You don't have permission for this area.");
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        }

        if (wcfg.useRegions && (type == Material.LEVER || type == Material.STONE_BUTTON) && entity instanceof Player) {
            Vector pt = toVector(block);
            RegionManager mgr = cfg.getWorldGuardPlugin().getGlobalRegionManager().getRegionManager(((Player)entity).getWorld().getName());
            ApplicableRegionSet applicableRegions = mgr.getApplicableRegions(pt);
            LocalPlayer localPlayer = BukkitPlayer.wrapPlayer(cfg, (Player)entity);

            if (!applicableRegions.isStateFlagAllowed(FlagType.LEVER_AND_BUTTON, localPlayer)) {
                ((Player)entity).sendMessage(ChatColor.DARK_RED + "You don't have permission for this area.");
                event.setCancelled(true);
                return;
            }
        }

        if (wcfg.getBlacklist() != null && entity instanceof Player) {
            Player player = (Player) entity;

            if (!wcfg.getBlacklist().check(
                    new BlockInteractBlacklistEvent(BukkitPlayer.wrapPlayer(cfg, player), toVector(block),
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

        if (event.isCancelled()) {
            return;
        }

        Block blockPlaced = event.getBlock();
        Player player = event.getPlayer();
        World world = blockPlaced.getWorld();

        WorldGuardConfiguration cfg = plugin.getWgConfiguration();
        WorldGuardWorldConfiguration wcfg = cfg.getWorldConfig(world.getName());

        if (wcfg.useRegions) {
            Vector pt = toVector(blockPlaced);

            if (!cfg.canBuild(player, pt)) {
                player.sendMessage(ChatColor.DARK_RED + "You don't have permission for this area.");
                event.setCancelled(true);
                return;
            }
        }

        if (wcfg.getBlacklist() != null) {
            if (!wcfg.getBlacklist().check(
                    new BlockPlaceBlacklistEvent(BukkitPlayer.wrapPlayer(cfg, player), toVector(blockPlaced),
                    blockPlaced.getTypeId()), false, false)) {
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

        WorldGuardConfiguration cfg = plugin.getWgConfiguration();
        WorldGuardWorldConfiguration wcfg = cfg.getWorldConfig(blockClicked.getWorld().getName());

        if (wcfg.useRegions && event.getItemInHand().getTypeId() == wcfg.regionWand) {
            Vector pt = toVector(blockClicked);

            RegionManager mgr = plugin.getGlobalRegionManager().getRegionManager(player.getWorld().getName());
            ApplicableRegionSet app = mgr.getApplicableRegions(pt);
            List<String> regions = mgr.getApplicableRegionsIDs(pt);

            if (regions.size() > 0) {
                player.sendMessage(ChatColor.YELLOW + "Can you build? "
                        + (app.canBuild(BukkitPlayer.wrapPlayer(cfg, player)) ? "Yes" : "No"));

                StringBuilder str = new StringBuilder();
                for (Iterator<String> it = regions.iterator(); it.hasNext();) {
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

        if (wcfg.useRegions && type == Material.CAKE_BLOCK) {

            Vector pt = toVector(blockClicked);

            if (!cfg.canBuild(player, pt)) {
                player.sendMessage(ChatColor.DARK_RED + "You don't have permission for this area.");

                byte newData = (byte) (blockClicked.getData() - 1);
                newData = newData < 0 ? 0 : newData;

                blockClicked.setData(newData);
                player.setHealth(player.getHealth() - 3);

                return;
            }
        }

        if (wcfg.useRegions && wcfg.useiConomy && cfg.getiConomy() != null && (type == Material.SIGN || type == Material.WALL_SIGN)) {
            if (((Sign)blockClicked).getLine(0) == "[WorldGuard]" && ((Sign)blockClicked).getLine(1) == "For sale") {
                String regionId = ((Sign)blockClicked).getLine(2);
                String regionComment = ((Sign)blockClicked).getLine(3);

                if (regionId != null && regionId != "") {
                    RegionManager mgr = cfg.getWorldGuardPlugin().getGlobalRegionManager().getRegionManager(player.getWorld().getName());
                    ProtectedRegion region = mgr.getRegion(regionId);

                    if (region != null) {
                        RegionFlagContainer flags = region.getFlags();

                        if (flags.getBooleanFlag(FlagType.BUYABLE).getValue(false)) {
                            if (iConomy.getBank().hasAccount(player.getName())) {
                                Account account = iConomy.getBank().getAccount(player.getName());
                                double balance = account.getBalance();
                                double regionPrice = flags.getIntegerFlag(FlagType.PRICE).getValue();

                                if (balance >= regionPrice) {
                                    account.subtract(regionPrice);
                                    player.sendMessage(ChatColor.YELLOW + "You have bought the region " + regionId + " for " +
                                            iConomy.getBank().format(regionPrice));
                                    DefaultDomain owners = region.getOwners();
                                    owners.addPlayer(player.getName());
                                    region.setOwners(owners);
                                    flags.getBooleanFlag(FlagType.BUYABLE).setValue(false);
                                    account.save();
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

        Block blockTo = event.getBlock();
        World world = blockTo.getWorld();

        WorldGuardConfiguration cfg = plugin.getWgConfiguration();
        WorldGuardWorldConfiguration wcfg = cfg.getWorldConfig(world.getName());

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
                            clearSpongeWater(world, ox + cx, oy + cy, oz + cz);
                        } else if (sponge.getTypeId() == 19
                                && !sponge.isBlockIndirectlyPowered()) {
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

        WorldGuardConfiguration cfg = plugin.getWgConfiguration();
        WorldGuardWorldConfiguration wcfg = cfg.getWorldConfig(world.getName());

        for (int cx = -wcfg.spongeRadius; cx <= wcfg.spongeRadius; cx++) {
            for (int cy = -wcfg.spongeRadius; cy <= wcfg.spongeRadius; cy++) {
                for (int cz = -wcfg.spongeRadius; cz <= wcfg.spongeRadius; cz++) {
                    if (isBlockWater(world, ox + cx, oy + cy, oz + cz)) {
                        world.getBlockAt(ox + cx, oy + cy, oz + cz).setTypeId(0);
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

        WorldGuardConfiguration cfg = plugin.getWgConfiguration();
        WorldGuardWorldConfiguration wcfg = cfg.getWorldConfig(world.getName());

        // The negative x edge
        int cx = ox - wcfg.spongeRadius - 1;
        for (int cy = oy - wcfg.spongeRadius - 1; cy <= oy + wcfg.spongeRadius + 1; cy++) {
            for (int cz = oz - wcfg.spongeRadius - 1; cz <= oz + wcfg.spongeRadius + 1; cz++) {
                if (isBlockWater(world, cx, cy, cz)) {
                    setBlockToWater(world, cx + 1, cy, cz);
                }
            }
        }

        // The positive x edge
        cx = ox + wcfg.spongeRadius + 1;
        for (int cy = oy - wcfg.spongeRadius - 1; cy <= oy + wcfg.spongeRadius + 1; cy++) {
            for (int cz = oz - wcfg.spongeRadius - 1; cz <= oz + wcfg.spongeRadius + 1; cz++) {
                if (isBlockWater(world, cx, cy, cz)) {
                    setBlockToWater(world, cx - 1, cy, cz);
                }
            }
        }

        // The negative y edge
        int cy = oy - wcfg.spongeRadius - 1;
        for (cx = ox - wcfg.spongeRadius - 1; cx <= ox + wcfg.spongeRadius + 1; cx++) {
            for (int cz = oz - wcfg.spongeRadius - 1; cz <= oz + wcfg.spongeRadius + 1; cz++) {
                if (isBlockWater(world, cx, cy, cz)) {
                    setBlockToWater(world, cx, cy + 1, cz);
                }
            }
        }

        // The positive y edge
        cy = oy + wcfg.spongeRadius + 1;
        for (cx = ox - wcfg.spongeRadius - 1; cx <= ox + wcfg.spongeRadius + 1; cx++) {
            for (int cz = oz - wcfg.spongeRadius - 1; cz <= oz + wcfg.spongeRadius + 1; cz++) {
                if (isBlockWater(world, cx, cy, cz)) {
                    setBlockToWater(world, cx, cy - 1, cz);
                }
            }
        }

        // The negative z edge
        int cz = oz - wcfg.spongeRadius - 1;
        for (cx = ox - wcfg.spongeRadius - 1; cx <= ox + wcfg.spongeRadius + 1; cx++) {
            for (cy = oy - wcfg.spongeRadius - 1; cy <= oy + wcfg.spongeRadius + 1; cy++) {
                if (isBlockWater(world, cx, cy, cz)) {
                    setBlockToWater(world, cx, cy, cz + 1);
                }
            }
        }

        // The positive z edge
        cz = oz + wcfg.spongeRadius + 1;
        for (cx = ox - wcfg.spongeRadius - 1; cx <= ox + wcfg.spongeRadius + 1; cx++) {
            for (cy = oy - wcfg.spongeRadius - 1; cy <= oy + wcfg.spongeRadius + 1; cy++) {
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
        Block block = world.getBlockAt(ox, oy, oz);
        int id = block.getTypeId();
        if (id == 0) {
            block.setTypeId(8);
        }
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
