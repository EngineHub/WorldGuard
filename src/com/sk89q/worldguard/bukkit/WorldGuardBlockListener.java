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
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.blacklist.events.*;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
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

        pm.registerEvent(Event.Type.BLOCK_DAMAGE, this, Priority.High, plugin);
        pm.registerEvent(Event.Type.BLOCK_BREAK, this, Priority.High, plugin);
        pm.registerEvent(Event.Type.BLOCK_FROMTO, this, Priority.Normal, plugin);
        pm.registerEvent(Event.Type.BLOCK_IGNITE, this, Priority.High, plugin);
        pm.registerEvent(Event.Type.BLOCK_PHYSICS, this, Priority.Normal, plugin);
        //pm.registerEvent(Event.Type.BLOCK_INTERACT, this, Priority.High, plugin);
        pm.registerEvent(Event.Type.BLOCK_PLACE, this, Priority.High, plugin);
        //pm.registerEvent(Event.Type.BLOCK_RIGHTCLICK, this, Priority.High, plugin);
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

        ConfigurationManager cfg = plugin.getGlobalConfiguration();
        WorldConfiguration wcfg = cfg.forWorld(player.getWorld().getName());

        if (wcfg.useRegions && blockDamaged.getType() == Material.CAKE_BLOCK) {
            if (!plugin.canBuild(player, blockDamaged.getLocation())) {
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
        ConfigurationManager cfg = plugin.getGlobalConfiguration();
        WorldConfiguration wcfg = cfg.forWorld(player.getWorld().getName());

        if (!wcfg.itemDurability) {
            ItemStack held = player.getItemInHand();
            if (held.getTypeId() > 0) {
                held.setDurability((short) -1);
                player.setItemInHand(held);
            }
        }

        if (wcfg.useRegions) {
            if (!plugin.canBuild(player, event.getBlock().getLocation())) {
                player.sendMessage(ChatColor.DARK_RED + "You don't have permission for this area.");
                event.setCancelled(true);
                return;
            }
        }

        if (wcfg.getBlacklist() != null) {
            if (!wcfg.getBlacklist().check(
                    new BlockBreakBlacklistEvent(plugin.wrapPlayer(player),
                    toVector(event.getBlock()),
                    event.getBlock().getTypeId()), false, false)) {
                event.setCancelled(true);
                return;
            }

            if (wcfg.getBlacklist().check(
                    new DestroyWithBlacklistEvent(plugin.wrapPlayer(player),
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

        ConfigurationManager cfg = plugin.getGlobalConfiguration();
        WorldConfiguration wcfg = cfg.forWorld(event.getBlock().getWorld().getName());

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
            RegionManager mgr = plugin.getGlobalRegionManager().get(world.getName());

            if (!mgr.getApplicableRegions(pt).allows(DefaultFlag.WATER_FLOW)) {
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

        ConfigurationManager cfg = plugin.getGlobalConfiguration();
        WorldConfiguration wcfg = cfg.forWorld(world.getName());

        boolean isFireSpread = cause == IgniteCause.SPREAD;

        if (wcfg.useRegions) {
            Vector pt = toVector(block);
            Player player = event.getPlayer();
            RegionManager mgr = plugin.getGlobalRegionManager().get(world.getName());

            ApplicableRegionSet set = mgr.getApplicableRegions(pt);

            if (player != null && !plugin.hasPermission(player, "region.bypass")) {
                LocalPlayer localPlayer = plugin.wrapPlayer(player);

                if (cause == IgniteCause.FLINT_AND_STEEL
                        && !set.canBuild(localPlayer)) {
                    event.setCancelled(true);
                    return;
                }

                if (cause == IgniteCause.FLINT_AND_STEEL
                        && !set.allows(DefaultFlag.LIGHTER)) {
                    event.setCancelled(true);
                    return;
                }
            }

            if (isFireSpread && set.allows(DefaultFlag.FIRE_SPREAD)) {
                event.setCancelled(true);
                return;
            }

            if (cause == IgniteCause.LAVA && !set.allows(DefaultFlag.LAVA_FIRE)) {
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

        ConfigurationManager cfg = plugin.getGlobalConfiguration();
        WorldConfiguration wcfg = cfg.forWorld(event.getBlock().getWorld().getName());

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

        ConfigurationManager cfg = plugin.getGlobalConfiguration();
        WorldConfiguration wcfg = cfg.forWorld(event.getBlock().getWorld().getName());

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
     *//*
    @Override
    public void onBlockInteract(BlockInteractEvent event) {

        if (event.isCancelled()) {
            return;
        }

        Block block = event.getBlock();
        Material type = block.getType();
        LivingEntity entity = event.getEntity();

        GlobalConfiguration cfg = plugin.getGlobalConfiguration();
        WorldConfiguration wcfg = cfg.getWorldConfig(event.getBlock().getWorld().getName());

        if (entity instanceof Player
                && (block.getType() == Material.CHEST
                || block.getType() == Material.DISPENSER
                || block.getType() == Material.FURNACE
                || block.getType() == Material.BURNING_FURNACE
                || block.getType() == Material.NOTE_BLOCK)) {
            Player player = (Player) entity;
            if (wcfg.useRegions) {
                Vector pt = toVector(block);
                LocalPlayer localPlayer = plugin.wrapPlayer(player);
                RegionManager mgr = plugin.getGlobalRegionManager().get(player.getWorld().getName());

                if (!plugin.hasPermission(player, "region.bypass")) {
                    ApplicableRegionSet set = mgr.getApplicableRegions(pt);
                    if (!set.allows(DefaultFlag.CHEST_ACCESS) && !set.canBuild(localPlayer)) {
                        player.sendMessage(ChatColor.DARK_RED + "You don't have permission for this area.");
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        }

        if (wcfg.useRegions && (type == Material.LEVER || type == Material.STONE_BUTTON) && entity instanceof Player) {
            Vector pt = toVector(block);
            RegionManager mgr = cfg.getWorldGuardPlugin().getGlobalRegionManager().get(((Player)entity).getWorld().getName());
            ApplicableRegionSet applicableRegions = mgr.getApplicableRegions(pt);
            LocalPlayer localPlayer = plugin.wrapPlayer((Player)entity);

            if (!applicableRegions.canUse(localPlayer)) {
                ((Player)entity).sendMessage(ChatColor.DARK_RED + "You don't have permission for this area.");
                event.setCancelled(true);
                return;
            }
        }

        if (wcfg.getBlacklist() != null && entity instanceof Player) {
            Player player = (Player) entity;

            if (!wcfg.getBlacklist().check(
                    new BlockInteractBlacklistEvent(plugin.wrapPlayer(player), toVector(block),
                    block.getTypeId()), false, false)) {
                event.setCancelled(true);
                return;
            }
        }
    }*/

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

        ConfigurationManager cfg = plugin.getGlobalConfiguration();
        WorldConfiguration wcfg = cfg.forWorld(world.getName());

        if (wcfg.useRegions) {
            if (!plugin.canBuild(player, blockPlaced.getLocation())) {
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
    /*
    @Override
    public void onBlockRightClick(BlockRightClickEvent event) {
        Player player = event.getPlayer();
        Block blockClicked = event.getBlock();

        GlobalConfiguration cfg = plugin.getGlobalConfiguration();
        WorldConfiguration wcfg = cfg.getWorldConfig(blockClicked.getWorld().getName());

        if (wcfg.useRegions && event.getItemInHand().getTypeId() == wcfg.regionWand) {
            Vector pt = toVector(blockClicked);

            RegionManager mgr = plugin.getGlobalRegionManager().get(player.getWorld().getName());
            ApplicableRegionSet app = mgr.getApplicableRegions(pt);
            List<String> regions = mgr.getApplicableRegionsIDs(pt);

            if (regions.size() > 0) {
                player.sendMessage(ChatColor.YELLOW + "Can you build? "
                        + (app.canBuild(plugin.wrapPlayer(player)) ? "Yes" : "No"));

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

        if (wcfg.useRegions && wcfg.useiConomy && cfg.getiConomy() != null
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
        }
    }*/

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

        ConfigurationManager cfg = plugin.getGlobalConfiguration();
        WorldConfiguration wcfg = cfg.forWorld(world.getName());

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

        ConfigurationManager cfg = plugin.getGlobalConfiguration();
        WorldConfiguration wcfg = cfg.forWorld(world.getName());

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

        ConfigurationManager cfg = plugin.getGlobalConfiguration();
        WorldConfiguration wcfg = cfg.forWorld(world.getName());

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
