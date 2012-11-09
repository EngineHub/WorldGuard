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

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.block.SignChangeEvent;

import com.sk89q.rulelists.KnownAttachment;
import com.sk89q.rulelists.RuleSet;
import com.sk89q.worldedit.blocks.BlockID;

/**
 * The listener for block events.
 */
class WorldGuardBlockListener implements Listener {

    private WorldGuardPlugin plugin;

    /**
     * Construct the listener.
     *
     * @param plugin WorldGuard plugin
     */
    WorldGuardBlockListener(WorldGuardPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Register the events.
     */
    void registerEvents() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockDamage(BlockDamageEvent event) {
        Player player = event.getPlayer();
        Block blockDamaged = event.getBlock();

        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(event.getBlock().getWorld());

        // Cake are damaged and not broken when they are eaten, so we must
        // handle them a bit separately
        if (blockDamaged.getTypeId() == BlockID.CAKE_BLOCK) {
            // RuleLists
            RuleSet rules = wcfg.getRuleList().get(KnownAttachment.BLOCK_INTERACT);
            BukkitContext context = new BukkitContext(event);
            context.setSourceEntity(player);
            context.setTargetBlock(event.getBlock().getState());
            if (rules.process(context)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(event.getBlock().getWorld());

        // RuleLists
        RuleSet rules = wcfg.getRuleList().get(KnownAttachment.BLOCK_BREAK);
        BukkitContext context = new BukkitContext(event);
        context.setSourceEntity(player);
        context.setTargetBlock(event.getBlock().getState());
        if (rules.process(context)) {
            event.setCancelled(true);
            return;
        }

        /* --- No short-circuit returns below this line --- */

        // Sponges
        SpongeApplicator spongeAppl = wcfg.getSpongeApplicator();
        if (spongeAppl != null && block.getType() == Material.SPONGE) {
            if (spongeAppl.isActiveSponge(block)) {
                spongeAppl.placeWater(block);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockFromTo(BlockFromToEvent event) {
        Block blockFrom = event.getBlock();
        Block blockTo = event.getToBlock();

        boolean isWater = blockFrom.getTypeId() == 8 || blockFrom.getTypeId() == 9;

        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(event.getBlock().getWorld());

        // Sponges
        SpongeApplicator spongeAppl = wcfg.getSpongeApplicator();
        if (spongeAppl != null && isWater) {
            if (spongeAppl.isNearSponge(blockTo)) {
                event.setCancelled(true);
                return;
            }
        }

        // RuleLists
        RuleSet rules = wcfg.getRuleList().get(KnownAttachment.BLOCK_SPREAD);
        BukkitContext context = new BukkitContext(event);
        context.setSourceBlock(event.getBlock().getState());
        context.setTargetBlock(event.getToBlock().getState());
        if (rules.process(context)) {
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockIgnite(BlockIgniteEvent event) {
        IgniteCause cause = event.getCause();
        Block block = event.getBlock();
        World world = block.getWorld();
        boolean isFireSpread = cause == IgniteCause.SPREAD;

        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(world);

        // Fire stop toggle
        if (wcfg.fireSpreadDisableToggle && isFireSpread) {
            event.setCancelled(true);
            return;
        }

        // RuleLists
        RuleSet rules;
        BukkitContext context;
        BlockState placedState;

        switch (event.getCause()) {
        case FLINT_AND_STEEL:
            // Consider flint and steel as an item use
            rules = wcfg.getRuleList().get(KnownAttachment.ITEM_USE);
            context = new BukkitContext(event);
            context.setSourceEntity(event.getPlayer());
            context.setTargetBlock(event.getBlock().getState());
            context.setItem(event.getPlayer().getItemInHand()); // Should be flint and steel

            // Make a virtual new state
            placedState = event.getBlock().getState();
            placedState.setType(Material.FIRE);
            context.setPlacedBlock(placedState);

            if (rules.process(context)) {
                event.setCancelled(true);
                return;
            }
            break;
        case LAVA:
        case SPREAD:
            // Consider everything else as a block spread
            rules = wcfg.getRuleList().get(KnownAttachment.BLOCK_SPREAD);
            context = new BukkitContext(event);
            context.setTargetBlock(event.getBlock().getState());

            // Make a virtual source state
            BlockState sourceState = event.getBlock().getState();
            sourceState.setType(event.getCause() == IgniteCause.LAVA ? Material.LAVA : Material.FIRE);
            context.setSourceBlock(sourceState);

            // Make a virtual new state
            placedState = event.getBlock().getState();
            placedState.setType(Material.FIRE);
            context.setPlacedBlock(placedState);

            if (rules.process(context)) {
                event.setCancelled(true);
                return;
            }
            break;
        case FIREBALL:
        case LIGHTNING:
            // Consider everything else as a block spread
            rules = wcfg.getRuleList().get(KnownAttachment.BLOCK_PLACE);
            context = new BukkitContext(event);
            context.setTargetBlock(event.getBlock().getState());

            // Make a virtual new state
            placedState = event.getBlock().getState();
            placedState.setType(Material.FIRE);
            context.setPlacedBlock(placedState);

            if (rules.process(context)) {
                event.setCancelled(true);
                return;
            }
            break;
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBurn(BlockBurnEvent event) {
        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(event.getBlock().getWorld());

        // Fire stop toggle
        if (wcfg.fireSpreadDisableToggle) {
            Block block = event.getBlock();
            event.setCancelled(true);
            BukkitUtil.checkAndDestroyAround(block.getWorld(), block.getX(),
                    block.getY(), block.getZ(), BlockID.FIRE);
            return;
        }

        // RuleLists
        RuleSet rules = wcfg.getRuleList().get(KnownAttachment.BLOCK_BREAK);
        BukkitContext context = new BukkitContext(event);
        BlockState virtualFireState = event.getBlock().getState();
        virtualFireState.setType(Material.FIRE);
        context.setSourceBlock(virtualFireState);
        context.setTargetBlock(event.getBlock().getState());
        if (rules.process(context)) {
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPhysics(BlockPhysicsEvent event) {
        Block block = event.getBlock();

        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(event.getBlock().getWorld());

        // RuleLists
        RuleSet rules = wcfg.getRuleList().get(KnownAttachment.BLOCK_PHYSICS);
        BukkitContext context = new BukkitContext(event);
        context.setTargetBlock(event.getBlock().getState());
        if (rules.process(context)) {
            event.setCancelled(true);
            return;
        }

        /* --- No short-circuit returns below this line --- */

        // Sponges
        SpongeApplicator spongeAppl = wcfg.getSpongeApplicator();
        if (spongeAppl != null && block.getType() == Material.SPONGE) {
            if (spongeAppl.isActiveSponge(block)) {
                spongeAppl.placeWater(block);
            } else {
                spongeAppl.clearWater(block);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Block blockPlaced = event.getBlock();
        World world = blockPlaced.getWorld();

        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(world);

        // Sponges
        SpongeApplicator spongeAppl = wcfg.getSpongeApplicator();
        if (spongeAppl != null && spongeAppl.isActiveSponge(blockPlaced)) {
            spongeAppl.clearWater(blockPlaced);
        }

        // RuleLists
        RuleSet rules = wcfg.getRuleList().get(KnownAttachment.BLOCK_PLACE);
        BukkitContext context = new BukkitContext(event);
        context.setTargetBlock(event.getBlock().getState());
        context.setPlacedBlock(event.getBlockReplacedState());
        if (rules.process(context)) {
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onSignChange(SignChangeEvent event) {
        Player player = event.getPlayer();

        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(player.getWorld());

        // Chest protection
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

        // RuleLists
        RuleSet rules = wcfg.getRuleList().get(KnownAttachment.BLOCK_INTERACT);
        BukkitContext context = new BukkitContext(event);
        context.setTargetBlock(event.getBlock().getState());
        if (rules.process(context)) {
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onLeavesDecay(LeavesDecayEvent event) {
        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(event.getBlock().getWorld());

        // RuleLists
        RuleSet rules = wcfg.getRuleList().get(KnownAttachment.BLOCK_FADE);
        BukkitContext context = new BukkitContext(event);
        context.setTargetBlock(event.getBlock().getState());
        if (rules.process(context)) {
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockForm(BlockFormEvent event) {
        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(event.getBlock().getWorld());

        // RuleLists
        RuleSet rules = wcfg.getRuleList().get(KnownAttachment.BLOCK_FORM);
        BukkitContext context = new BukkitContext(event);
        context.setTargetBlock(event.getBlock().getState());
        context.setPlacedBlock(event.getNewState());
        if (rules.process(context)) {
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockSpread(BlockSpreadEvent event) {
        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(event.getBlock().getWorld());

        // RuleLists
        RuleSet rules = wcfg.getRuleList().get(KnownAttachment.BLOCK_SPREAD);
        BukkitContext context = new BukkitContext(event);
        context.setSourceBlock(event.getSource().getState());
        context.setTargetBlock(event.getBlock().getState());
        context.setPlacedBlock(event.getNewState());
        if (rules.process(context)) {
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockFade(BlockFadeEvent event) {
        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(event.getBlock().getWorld());

        // RuleLists
        RuleSet rules = wcfg.getRuleList().get(KnownAttachment.BLOCK_FADE);
        BukkitContext context = new BukkitContext(event);
        context.setTargetBlock(event.getBlock().getState());
        context.setPlacedBlock(event.getNewState());
        if (rules.process(context)) {
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockDispense(BlockDispenseEvent event) {
        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(event.getBlock().getWorld());

        // RuleLists
        RuleSet rules = wcfg.getRuleList().get(KnownAttachment.ITEM_DROP);
        BukkitContext context = new BukkitContext(event);
        context.setSourceBlock(event.getBlock().getState());
        context.setItem(event.getItem());
        if (rules.process(context)) {
            event.setCancelled(true);
            return;
        }
    }
}
