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

package com.sk89q.worldguard.bukkit;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.blacklist.event.BlockDispenseBlacklistEvent;
import com.sk89q.worldguard.internal.Events;
import com.sk89q.worldguard.internal.cause.Causes;
import com.sk89q.worldguard.internal.event.BlockInteractEvent;
import com.sk89q.worldguard.internal.event.Interaction;
import com.sk89q.worldguard.internal.event.ItemInteractEvent;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowman;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockExpEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.EntityBlockFormEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.block.SignChangeEvent;

import static com.sk89q.worldguard.bukkit.BukkitUtil.createTarget;
import static com.sk89q.worldguard.bukkit.BukkitUtil.toVector;

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
    protected WorldConfiguration getWorldConfig(World world) {
        return plugin.getGlobalStateManager().get(world);
    }

    /**
     * Get the world configuration given a player.
     *
     * @param player The player to get the wold from
     * @return The {@link WorldConfiguration} for the player's world
     */
    protected WorldConfiguration getWorldConfig(Player player) {
        return getWorldConfig(player.getWorld());
    }

    /*
     * Called when a block is damaged.
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockDamage(BlockDamageEvent event) {
        Block target = event.getBlock();

        // Cake are damaged and not broken when they are eaten, so we must
        // handle them a bit separately
        if (target.getType() == Material.CAKE_BLOCK) {
            Events.fireToCancel(event, new BlockInteractEvent(event, Causes.create(event.getPlayer()), Interaction.INTERACT, target));
        }
    }

    /*
     * Called when a block is broken.
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Block target = event.getBlock();

        Events.fireToCancel(event, new BlockInteractEvent(event, Causes.create(event.getPlayer()), Interaction.BREAK, target));
    }

    /*
     * Called when a block gets ignited.
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockIgnite(BlockIgniteEvent event) {
        IgniteCause cause = event.getCause();
        Block block = event.getBlock();
        World world = block.getWorld();

        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(world);

        boolean isFireSpread = cause == IgniteCause.SPREAD;

        if (wcfg.blockLighter && (cause == IgniteCause.FLINT_AND_STEEL || cause == IgniteCause.FIREBALL)
                && event.getPlayer() != null
                && !plugin.hasPermission(event.getPlayer(), "worldguard.override.lighter")) {
            event.setCancelled(true);
            return;
        }

        if (wcfg.useRegions) {
            Vector pt = toVector(block);
            Player player = event.getPlayer();
            RegionManager mgr = plugin.getGlobalRegionManager().get(world);
            ApplicableRegionSet set = mgr.getApplicableRegions(pt);

            if (player != null && !plugin.getGlobalRegionManager().hasBypass(player, world)) {
                LocalPlayer localPlayer = plugin.wrapPlayer(player);

                // this is preliminarily handled in the player listener under handleBlockRightClick
                // why it's handled here too, no one knows
                if (cause == IgniteCause.FLINT_AND_STEEL || cause == IgniteCause.FIREBALL) {
                    if (!set.allows(DefaultFlag.LIGHTER)
                            && !set.canBuild(localPlayer)
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

            if (cause == IgniteCause.FIREBALL && event.getPlayer() == null) {
                // wtf bukkit, FIREBALL is supposed to be reserved to players
                if (!set.allows(DefaultFlag.GHAST_FIREBALL)) {
                    event.setCancelled(true);
                    return;
                }
            }

            if (cause == IgniteCause.LIGHTNING && !set.allows(DefaultFlag.LIGHTNING)) {
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
        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(event.getBlock().getWorld());

        if (wcfg.isChestProtected(event.getBlock())) {
            event.setCancelled(true);
            return;
        }

        if (wcfg.useRegions) {
            Block block = event.getBlock();
            int x = block.getX();
            int y = block.getY();
            int z = block.getZ();
            Vector pt = toVector(block);
            RegionManager mgr = plugin.getGlobalRegionManager().get(block.getWorld());
            ApplicableRegionSet set = mgr.getApplicableRegions(pt);

            if (!set.allows(DefaultFlag.FIRE_SPREAD)) {
                checkAndDestroyAround(block.getWorld(), x, y, z, BlockID.FIRE);
                event.setCancelled(true);
                return;
            }

        }
    }

    private void checkAndDestroyAround(World world, int x, int y, int z, int required) {
        checkAndDestroy(world, x, y, z + 1, required);
        checkAndDestroy(world, x, y, z - 1, required);
        checkAndDestroy(world, x, y + 1, z, required);
        checkAndDestroy(world, x, y - 1, z, required);
        checkAndDestroy(world, x + 1, y, z, required);
        checkAndDestroy(world, x - 1, y, z, required);
    }

    private void checkAndDestroy(World world, int x, int y, int z, int required) {
        if (world.getBlockTypeIdAt(x, y, z) == required) {
            world.getBlockAt(x, y, z).setTypeId(BlockID.AIR);
        }
    }

    /*
     * Called when block physics occurs.
     */
    @EventHandler(ignoreCancelled = true)
    public void onBlockPhysics(BlockPhysicsEvent event) {
        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(event.getBlock().getWorld());

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

        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(world);

        Events.fireToCancel(event, new BlockInteractEvent(event, Causes.create(event.getPlayer()), Interaction.PLACE, target));
    }

    /*
     * Called when a sign is changed.
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onSignChange(SignChangeEvent event) {
        Player player = event.getPlayer();
        WorldConfiguration wcfg = getWorldConfig(player);

        if (wcfg.signChestProtection) {
            if (event.getLine(0).equalsIgnoreCase("[Lock]")) {
                if (wcfg.isChestProtectedPlacement(event.getBlock(), player)) {
                    player.sendMessage(ChatColor.DARK_RED + "You do not own the adjacent chest.");
                    event.getBlock().breakNaturally();
                    event.setCancelled(true);
                    return;
                }

                if (event.getBlock().getTypeId() != BlockID.SIGN_POST) {
                    player.sendMessage(ChatColor.RED
                            + "The [Lock] sign must be a sign post, not a wall sign.");

                    event.getBlock().breakNaturally();
                    event.setCancelled(true);
                    return;
                }

                if (!event.getLine(1).equalsIgnoreCase(player.getName())) {
                    player.sendMessage(ChatColor.RED
                            + "The first owner line must be your name.");

                    event.getBlock().breakNaturally();
                    event.setCancelled(true);
                    return;
                }

                int below = event.getBlock().getRelative(0, -1, 0).getTypeId();

                if (below == BlockID.TNT || below == BlockID.SAND
                        || below == BlockID.GRAVEL || below == BlockID.SIGN_POST) {
                    player.sendMessage(ChatColor.RED
                            + "That is not a safe block that you're putting this sign on.");

                    event.getBlock().breakNaturally();
                    event.setCancelled(true);
                    return;
                }

                event.setLine(0, "[Lock]");
                player.sendMessage(ChatColor.YELLOW
                        + "A chest or double chest above is now protected.");
            }
        } else if (!wcfg.disableSignChestProtectionCheck) {
            if (event.getLine(0).equalsIgnoreCase("[Lock]")) {
                player.sendMessage(ChatColor.RED
                        + "WorldGuard's sign chest protection is disabled.");

                event.getBlock().breakNaturally();
                event.setCancelled(true);
                return;
            }
        }

        if (!plugin.getGlobalRegionManager().canBuild(player, event.getBlock())) {
            player.sendMessage(ChatColor.DARK_RED + "You don't have permission for this area.");
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onLeavesDecay(LeavesDecayEvent event) {
        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(event.getBlock().getWorld());

        if (wcfg.disableLeafDecay) {
            event.setCancelled(true);
            return;
        }

        if (wcfg.useRegions) {
            if (!plugin.getGlobalRegionManager().allows(DefaultFlag.LEAF_DECAY,
                    event.getBlock().getLocation())) {
                event.setCancelled(true);
            }
        }
    }

    /*
     * Called when a block is formed based on world conditions.
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockForm(BlockFormEvent event) {
        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(event.getBlock().getWorld());

        int type = event.getNewState().getTypeId();

        if (event instanceof EntityBlockFormEvent) {
            if (((EntityBlockFormEvent) event).getEntity() instanceof Snowman) {
                if (wcfg.disableSnowmanTrails) {
                    event.setCancelled(true);
                    return;
                }
            }
            return;
        }

        if (type == BlockID.ICE) {
            if (wcfg.disableIceFormation) {
                event.setCancelled(true);
                return;
            }
            if (wcfg.useRegions && !plugin.getGlobalRegionManager().allows(
                    DefaultFlag.ICE_FORM, event.getBlock().getLocation())) {
                event.setCancelled(true);
                return;
            }
        }

        if (type == BlockID.SNOW) {
            if (wcfg.disableSnowFormation) {
                event.setCancelled(true);
                return;
            }
            if (wcfg.allowedSnowFallOver.size() > 0) {
                int targetId = event.getBlock().getRelative(0, -1, 0).getTypeId();

                if (!wcfg.allowedSnowFallOver.contains(targetId)) {
                    event.setCancelled(true);
                    return;
                }
            }
            if (wcfg.useRegions && !plugin.getGlobalRegionManager().allows(
                    DefaultFlag.SNOW_FALL, event.getBlock().getLocation())) {
                event.setCancelled(true);
                return;
            }
        }
    }

    /*
     * Called when a piston extends
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPistonExtend(BlockPistonExtendEvent event) {
        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(event.getBlock().getWorld());

        if (wcfg.useRegions) {
            if (!plugin.getGlobalRegionManager().allows(DefaultFlag.PISTONS, event.getBlock().getLocation())) {
                event.setCancelled(true);
                return;
            }
            for (Block block : event.getBlocks()) {
                if (!plugin.getGlobalRegionManager().allows(DefaultFlag.PISTONS, block.getLocation())) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    /*
     * Called when a piston retracts
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPistonRetract(BlockPistonRetractEvent event) {
        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(event.getBlock().getWorld());

        if (wcfg.useRegions && event.isSticky()) {
            if (!(plugin.getGlobalRegionManager().allows(DefaultFlag.PISTONS, event.getRetractLocation()))
                    || !(plugin.getGlobalRegionManager().allows(DefaultFlag.PISTONS, event.getBlock().getLocation()))) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockDispense(BlockDispenseEvent event) {
        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(event.getBlock().getWorld());

        if (wcfg.getBlacklist() != null) {
            if (!wcfg.getBlacklist().check(new BlockDispenseBlacklistEvent(null, toVector(event.getBlock()), createTarget(event.getItem())), false, false)) {
                event.setCancelled(true);
                return;
            }
        }

        Events.fireToCancel(event, new ItemInteractEvent(event, Causes.create(event.getBlock()), Interaction.INTERACT, event.getBlock().getWorld(), event.getItem()));
    }

    /*
     * Called when a block yields exp
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockExp(BlockExpEvent event) {
        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(event.getBlock().getWorld());
        if (wcfg.disableExpDrops || !plugin.getGlobalRegionManager().allows(DefaultFlag.EXP_DROPS,
                event.getBlock().getLocation())) {
            event.setExpToDrop(0);
        }
    }

}
