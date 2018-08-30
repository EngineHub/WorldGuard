/*
 * WorldGuard, a suite of tools for Minecraft
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldGuard team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.worldguard.bukkit.listener;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.BukkitWorldConfiguration;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.bukkit.util.Materials;
import com.sk89q.worldguard.config.ConfigurationManager;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.association.RegionAssociable;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowman;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.EntityBlockFormEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.inventory.ItemStack;

/**
 * The listener for block events.
 *
 * @author sk89q
 */
public class WorldGuardBlockListener implements Listener {

    private WorldGuardPlugin plugin;

    /**
     * Construct the object.
     *
     * @param plugin The plugin instance
     */
    public WorldGuardBlockListener(WorldGuardPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Register events.
     */
    public void registerEvents() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Get the world configuration given a world.
     *
     * @param world The world to get the configuration for.
     * @return The configuration for {@code world}
     */
    protected BukkitWorldConfiguration getWorldConfig(World world) {
        return (BukkitWorldConfiguration) WorldGuard.getInstance().getPlatform().getGlobalStateManager().get(BukkitAdapter.adapt(world));
    }

    /**
     * Get the world configuration given a player.
     *
     * @param player The player to get the wold from
     * @return The {@link BukkitWorldConfiguration} for the player's world
     */
    protected BukkitWorldConfiguration getWorldConfig(Player player) {
        return getWorldConfig(player.getWorld());
    }

    /*
     * Called when a block is broken.
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block target = event.getBlock();
        BukkitWorldConfiguration wcfg = getWorldConfig(player);

        if (!wcfg.itemDurability) {
            ItemStack held = player.getItemInHand();
            if (held.getType() != Material.AIR) {
                held.setDurability((short) 0);
                player.setItemInHand(held);
            }
        }
    }

    /*
     * Called when fluids flow.
     */
    @EventHandler(ignoreCancelled = true)
    public void onBlockFromTo(BlockFromToEvent event) {
        World world = event.getBlock().getWorld();
        Block blockFrom = event.getBlock();
        Block blockTo = event.getToBlock();

        boolean isWater = blockFrom.getType() == Material.WATER;
        boolean isLava = blockFrom.getType() == Material.LAVA;
        boolean isAir = blockFrom.getType() == Material.AIR;

        ConfigurationManager cfg = WorldGuard.getInstance().getPlatform().getGlobalStateManager();
        BukkitWorldConfiguration wcfg = getWorldConfig(event.getBlock().getWorld());

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
                        if (sponge.getType() == Material.SPONGE
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
            Material targetId = blockTo.getType();

            if ((isAir || isWater) &&
                    wcfg.preventWaterDamage.contains(BukkitAdapter.asBlockType(targetId).getId())) {
                event.setCancelled(true);
                return;
            }
        }

        if (wcfg.allowedLavaSpreadOver.size() > 0 && isLava) {
            Material targetId = blockTo.getRelative(0, -1, 0).getType();

            if (!wcfg.allowedLavaSpreadOver.contains(BukkitAdapter.asBlockType(targetId).getId())) {
                event.setCancelled(true);
                return;
            }
        }

        if (wcfg.highFreqFlags && isWater
                && WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery().queryState(BukkitAdapter.adapt(blockFrom.getLocation()), (RegionAssociable) null, Flags.WATER_FLOW) == StateFlag.State.DENY) {
            event.setCancelled(true);
            return;
        }

        if (wcfg.highFreqFlags && isLava
                && !StateFlag.test(WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery().queryState(BukkitAdapter.adapt(blockFrom.getLocation()), (RegionAssociable) null, Flags.LAVA_FLOW))) {
            event.setCancelled(true);
            return;
        }

        if (wcfg.disableObsidianGenerators && (isAir || isLava)
                && (blockTo.getType() == Material.REDSTONE_WIRE
                    || blockTo.getType() == Material.TRIPWIRE)) {
            blockTo.setType(Material.AIR);
            return;
        }
    }

    /*
     * Called when a block gets ignited.
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockIgnite(BlockIgniteEvent event) {
        IgniteCause cause = event.getCause();
        Block block = event.getBlock();
        World world = block.getWorld();

        ConfigurationManager cfg = WorldGuard.getInstance().getPlatform().getGlobalStateManager();
        BukkitWorldConfiguration wcfg = getWorldConfig(world);

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

        if (wcfg.blockLighter && (cause == IgniteCause.FLINT_AND_STEEL || cause == IgniteCause.FIREBALL)
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

            if (wcfg.disableFireSpreadBlocks.contains(BukkitAdapter.asBlockType(world.getBlockAt(x, y - 1, z).getType()).getId())
                    || wcfg.disableFireSpreadBlocks.contains(BukkitAdapter.asBlockType(world.getBlockAt(x + 1, y, z).getType()).getId())
                    || wcfg.disableFireSpreadBlocks.contains(BukkitAdapter.asBlockType(world.getBlockAt(x - 1, y, z).getType()).getId())
                    || wcfg.disableFireSpreadBlocks.contains(BukkitAdapter.asBlockType(world.getBlockAt(x, y, z - 1).getType()).getId())
                    || wcfg.disableFireSpreadBlocks.contains(BukkitAdapter.asBlockType(world.getBlockAt(x, y, z + 1).getType()).getId())) {
                event.setCancelled(true);
                return;
            }
        }

        if (wcfg.useRegions) {
            ApplicableRegionSet set =
                    WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery().getApplicableRegions(BukkitAdapter.adapt(block.getLocation()));

            if (wcfg.highFreqFlags && isFireSpread
                    && !set.testState(null, Flags.FIRE_SPREAD)) {
                event.setCancelled(true);
                return;
            }

            if (wcfg.highFreqFlags && cause == IgniteCause.LAVA
                    && !set.testState(null, Flags.LAVA_FIRE)) {
                event.setCancelled(true);
                return;
            }

            if (cause == IgniteCause.FIREBALL && event.getPlayer() == null) {
                // wtf bukkit, FIREBALL is supposed to be reserved to players
                if (!set.testState(null, Flags.GHAST_FIREBALL)) {
                    event.setCancelled(true);
                    return;
                }
            }

            if (cause == IgniteCause.LIGHTNING && !set.testState(null, Flags.LIGHTNING)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    /*
     * Called when a block is destroyed from burning.
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBurn(BlockBurnEvent event) {
        ConfigurationManager cfg = WorldGuard.getInstance().getPlatform().getGlobalStateManager();
        BukkitWorldConfiguration wcfg = getWorldConfig(event.getBlock().getWorld());

        if (cfg.activityHaltToggle) {
            event.setCancelled(true);
            return;
        }

        if (wcfg.disableFireSpread) {
            event.setCancelled(true);
            return;
        }

        if (wcfg.fireSpreadDisableToggle) {
            Block block = event.getBlock();
            event.setCancelled(true);
            checkAndDestroyAround(block.getWorld(), block.getX(), block.getY(), block.getZ(), Material.FIRE);
            return;
        }

        if (wcfg.disableFireSpreadBlocks.size() > 0) {
            Block block = event.getBlock();

            if (wcfg.disableFireSpreadBlocks.contains(BukkitAdapter.asBlockType(block.getType()).getId())) {
                event.setCancelled(true);
                checkAndDestroyAround(block.getWorld(), block.getX(), block.getY(), block.getZ(), Material.FIRE);
                return;
            }
        }

        if (wcfg.isChestProtected(BukkitAdapter.adapt(event.getBlock().getLocation()))) {
            event.setCancelled(true);
            return;
        }

        if (wcfg.useRegions) {
            Block block = event.getBlock();
            int x = block.getX();
            int y = block.getY();
            int z = block.getZ();
            ApplicableRegionSet set =
                    WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery().getApplicableRegions(BukkitAdapter.adapt(block.getLocation()));

            if (!set.testState(null, Flags.FIRE_SPREAD)) {
                checkAndDestroyAround(block.getWorld(), x, y, z, Material.FIRE);
                event.setCancelled(true);
            }

        }
    }

    private void checkAndDestroyAround(World world, int x, int y, int z, Material required) {
        checkAndDestroy(world, x, y, z + 1, required);
        checkAndDestroy(world, x, y, z - 1, required);
        checkAndDestroy(world, x, y + 1, z, required);
        checkAndDestroy(world, x, y - 1, z, required);
        checkAndDestroy(world, x + 1, y, z, required);
        checkAndDestroy(world, x - 1, y, z, required);
    }

    private void checkAndDestroy(World world, int x, int y, int z, Material required) {
        if (world.getBlockAt(x, y, z).getType() == required) {
            world.getBlockAt(x, y, z).setType(Material.AIR);
        }
    }

    /*
     * Called when block physics occurs.
     */
    @EventHandler(ignoreCancelled = true)
    public void onBlockPhysics(BlockPhysicsEvent event) {
        ConfigurationManager cfg = WorldGuard.getInstance().getPlatform().getGlobalStateManager();
        BukkitWorldConfiguration wcfg = getWorldConfig(event.getBlock().getWorld());

        if (cfg.activityHaltToggle) {
            event.setCancelled(true);
            return;
        }

        Material id = event.getBlock().getType();

        if (id == Material.GRAVEL && wcfg.noPhysicsGravel) {
            event.setCancelled(true);
            return;
        }

        if (id == Material.SAND && wcfg.noPhysicsSand) {
            event.setCancelled(true);
            return;
        }

        if (id == Material.NETHER_PORTAL && wcfg.allowPortalAnywhere) {
            event.setCancelled(true);
            return;
        }

        if (wcfg.ropeLadders && event.getBlock().getType() == Material.LADDER) {
            if (event.getBlock().getRelative(0, 1, 0).getType() == Material.LADDER) {
                event.setCancelled(true);
                return;
            }
        }
    }

    /*
     * Called when a player places a block.
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Block target = event.getBlock();
        World world = target.getWorld();

        BukkitWorldConfiguration wcfg = getWorldConfig(world);

        if (wcfg.simulateSponge && target.getType() == Material.SPONGE) {
            if (wcfg.redstoneSponges && target.isBlockIndirectlyPowered()) {
                return;
            }

            int ox = target.getX();
            int oy = target.getY();
            int oz = target.getZ();

            SpongeUtil.clearSpongeWater(BukkitAdapter.adapt(world), ox, oy, oz);
        }
    }

    /*
     * Called when redstone changes.
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockRedstoneChange(BlockRedstoneEvent event) {
        Block blockTo = event.getBlock();
        World world = blockTo.getWorld();

        BukkitWorldConfiguration wcfg = getWorldConfig(world);

        if (wcfg.simulateSponge && wcfg.redstoneSponges) {
            int ox = blockTo.getX();
            int oy = blockTo.getY();
            int oz = blockTo.getZ();

            for (int cx = -1; cx <= 1; cx++) {
                for (int cy = -1; cy <= 1; cy++) {
                    for (int cz = -1; cz <= 1; cz++) {
                        Block sponge = world.getBlockAt(ox + cx, oy + cy, oz + cz);
                        if (sponge.getType() == Material.SPONGE
                                && sponge.isBlockIndirectlyPowered()) {
                            SpongeUtil.clearSpongeWater(BukkitAdapter.adapt(world), ox + cx, oy + cy, oz + cz);
                        } else if (sponge.getType() == Material.SPONGE
                                && !sponge.isBlockIndirectlyPowered()) {
                            SpongeUtil.addSpongeWater(BukkitAdapter.adapt(world), ox + cx, oy + cy, oz + cz);
                        }
                    }
                }
            }

            return;
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onLeavesDecay(LeavesDecayEvent event) {
        ConfigurationManager cfg = WorldGuard.getInstance().getPlatform().getGlobalStateManager();
        BukkitWorldConfiguration wcfg = getWorldConfig(event.getBlock().getWorld());

        if (cfg.activityHaltToggle) {
            event.setCancelled(true);
            return;
        }

        if (wcfg.disableLeafDecay) {
            event.setCancelled(true);
            return;
        }

        if (wcfg.useRegions) {
            if (!StateFlag.test(WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery().queryState(BukkitAdapter.adapt(event.getBlock().getLocation()), (RegionAssociable) null, Flags.LEAF_DECAY))) {
                event.setCancelled(true);
            }
        }
    }

    /*
     * Called when a block is formed based on world conditions.
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockForm(BlockFormEvent event) {
        ConfigurationManager cfg = WorldGuard.getInstance().getPlatform().getGlobalStateManager();
        BukkitWorldConfiguration wcfg = getWorldConfig(event.getBlock().getWorld());

        if (cfg.activityHaltToggle) {
            event.setCancelled(true);
            return;
        }

        Material type = event.getNewState().getType();

        if (event instanceof EntityBlockFormEvent) {
            if (((EntityBlockFormEvent) event).getEntity() instanceof Snowman) {
                if (wcfg.disableSnowmanTrails) {
                    event.setCancelled(true);
                    return;
                }
            }
            return;
        }

        if (Tag.ICE.isTagged(type)) {
            if (wcfg.disableIceFormation) {
                event.setCancelled(true);
                return;
            }
            if (wcfg.useRegions && !StateFlag.test(WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery()
                    .queryState(BukkitAdapter.adapt(event.getBlock().getLocation()), (RegionAssociable) null, Flags.ICE_FORM))) {
                event.setCancelled(true);
                return;
            }
        }

        if (type == Material.SNOW) {
            if (wcfg.disableSnowFormation) {
                event.setCancelled(true);
                return;
            }
            if (wcfg.allowedSnowFallOver.size() > 0) {
                Material targetId = event.getBlock().getRelative(0, -1, 0).getType();

                if (!wcfg.allowedSnowFallOver.contains(BukkitAdapter.asBlockType(targetId).getId())) {
                    event.setCancelled(true);
                    return;
                }
            }
            if (wcfg.useRegions && !StateFlag.test(WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery()
                    .queryState(BukkitAdapter.adapt(event.getBlock().getLocation()), (RegionAssociable) null, Flags.SNOW_FALL))) {
                event.setCancelled(true);
                return;
            }
        }
    }

    /*
     * Called when a block spreads based on world conditions.
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockSpread(BlockSpreadEvent event) {
        ConfigurationManager cfg = WorldGuard.getInstance().getPlatform().getGlobalStateManager();
        BukkitWorldConfiguration wcfg = getWorldConfig(event.getBlock().getWorld());

        if (cfg.activityHaltToggle) {
            event.setCancelled(true);
            return;
        }

        Material fromType = event.getSource().getType();

        if (Materials.isMushroom(fromType)) {
            if (wcfg.disableMushroomSpread) {
                event.setCancelled(true);
                return;
            }
            if (wcfg.useRegions && !StateFlag.test(WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery()
                    .queryState(BukkitAdapter.adapt(event.getBlock().getLocation()), (RegionAssociable) null, Flags.MUSHROOMS))) {
                event.setCancelled(true);
                return;
            }
        }

        if (fromType == Material.GRASS_BLOCK) {
            if (wcfg.disableGrassGrowth) {
                event.setCancelled(true);
                return;
            }
            if (wcfg.useRegions && !StateFlag.test(WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery()
                    .queryState(BukkitAdapter.adapt(event.getBlock().getLocation()), (RegionAssociable) null, Flags.GRASS_SPREAD))) {
                event.setCancelled(true);
                return;
            }
        }

        if (fromType == Material.MYCELIUM) {
            if (wcfg.disableMyceliumSpread) {
                event.setCancelled(true);
                return;
            }

            if (wcfg.useRegions && !StateFlag.test(WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery()
                    .queryState(BukkitAdapter.adapt(event.getBlock().getLocation()), (RegionAssociable) null, Flags.MYCELIUM_SPREAD))) {
                event.setCancelled(true);
                return;
            }
        }

        if (fromType == Material.VINE) {
            if (wcfg.disableVineGrowth) {
                event.setCancelled(true);
                return;
            }

            if (wcfg.useRegions && !StateFlag.test(WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery()
                    .queryState(BukkitAdapter.adapt(event.getBlock().getLocation()), (RegionAssociable) null, Flags.VINE_GROWTH))) {
                event.setCancelled(true);
                return;
            }
        }
    }

    /*
     * Called when a block fades.
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockFade(BlockFadeEvent event) {

        BukkitWorldConfiguration wcfg = getWorldConfig(event.getBlock().getWorld());

        if (Tag.ICE.isTagged(event.getBlock().getType())) {
            if (wcfg.disableIceMelting) {
                event.setCancelled(true);
                return;
            }

            if (wcfg.useRegions && !StateFlag.test(WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery()
                    .queryState(BukkitAdapter.adapt(event.getBlock().getLocation()), (RegionAssociable) null, Flags.ICE_MELT))) {
                event.setCancelled(true);
                return;
            }
        } else if (event.getBlock().getType() == Material.SNOW) {
            if (wcfg.disableSnowMelting) {
                event.setCancelled(true);
                return;
            }

            if (wcfg.useRegions && !StateFlag.test(WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery()
                    .queryState(BukkitAdapter.adapt(event.getBlock().getLocation()), (RegionAssociable) null, Flags.SNOW_MELT))) {
                event.setCancelled(true);
                return;
            }
        } else if (event.getBlock().getType() == Material.FARMLAND) {
            if (wcfg.disableSoilDehydration) {
                event.setCancelled(true);
                return;
            }
            if (wcfg.useRegions && !StateFlag.test(WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery()
                    .queryState(BukkitAdapter.adapt(event.getBlock().getLocation()), (RegionAssociable) null, Flags.SOIL_DRY))) {
                event.setCancelled(true);
                return;
            }
        }
    }

}
