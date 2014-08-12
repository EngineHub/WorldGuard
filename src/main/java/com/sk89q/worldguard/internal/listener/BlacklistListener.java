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

package com.sk89q.worldguard.internal.listener;

import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.blacklist.event.BlockBreakBlacklistEvent;
import com.sk89q.worldguard.blacklist.event.BlockDispenseBlacklistEvent;
import com.sk89q.worldguard.blacklist.event.BlockInteractBlacklistEvent;
import com.sk89q.worldguard.blacklist.event.BlockPlaceBlacklistEvent;
import com.sk89q.worldguard.blacklist.event.ItemAcquireBlacklistEvent;
import com.sk89q.worldguard.blacklist.event.ItemDestroyWithBlacklistEvent;
import com.sk89q.worldguard.blacklist.event.ItemDropBlacklistEvent;
import com.sk89q.worldguard.blacklist.event.ItemUseBlacklistEvent;
import com.sk89q.worldguard.bukkit.ConfigurationManager;
import com.sk89q.worldguard.bukkit.WorldConfiguration;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.bukkit.util.Materials;
import com.sk89q.worldguard.internal.cause.Causes;
import com.sk89q.worldguard.internal.event.block.BreakBlockEvent;
import com.sk89q.worldguard.internal.event.block.PlaceBlockEvent;
import com.sk89q.worldguard.internal.event.block.UseBlockEvent;
import com.sk89q.worldguard.internal.event.entity.DestroyEntityEvent;
import com.sk89q.worldguard.internal.event.entity.SpawnEntityEvent;
import com.sk89q.worldguard.internal.event.inventory.UseItemEvent;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;

import static com.sk89q.worldguard.bukkit.BukkitUtil.createTarget;
import static com.sk89q.worldguard.bukkit.BukkitUtil.toVector;

/**
 * Handle events that need to be processed by the blacklist.
 */
public class BlacklistListener extends AbstractListener {

    /**
     * Construct the listener.
     *
     * @param plugin an instance of WorldGuardPlugin
     */
    public BlacklistListener(WorldGuardPlugin plugin) {
        super(plugin);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBreakBlock(BreakBlockEvent event) {
        Player player = Causes.getInvolvedPlayer(event.getCauses());

        if (player == null) {
            return;
        }

        LocalPlayer localPlayer = getPlugin().wrapPlayer(player);
        Block target = event.getBlock();
        WorldConfiguration wcfg = getWorldConfig(player);

        // Blacklist guard
        if (wcfg.getBlacklist() == null) {
            return;
        }

        if (!wcfg.getBlacklist().check(
                new BlockBreakBlacklistEvent(localPlayer, toVector(event.getTarget()), createTarget(target, event.getEffectiveMaterial())), false, false)) {
            event.setCancelled(true);
        } else if (!wcfg.getBlacklist().check(
                new ItemDestroyWithBlacklistEvent(localPlayer, toVector(event.getTarget()), createTarget(player.getItemInHand())), false, false)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlaceBlock(PlaceBlockEvent event) {
        Player player = Causes.getInvolvedPlayer(event.getCauses());

        if (player == null) {
            return;
        }

        LocalPlayer localPlayer = getPlugin().wrapPlayer(player);
        Block target = event.getBlock();
        WorldConfiguration wcfg = getWorldConfig(player);

        // Blacklist guard
        if (wcfg.getBlacklist() == null) {
            return;
        }

        if (!wcfg.getBlacklist().check(new BlockPlaceBlacklistEvent(
                localPlayer, toVector(event.getTarget()), createTarget(target, event.getEffectiveMaterial())), false, false)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onUseBlock(UseBlockEvent event) {
        Player player = Causes.getInvolvedPlayer(event.getCauses());

        if (player == null) {
            return;
        }

        LocalPlayer localPlayer = getPlugin().wrapPlayer(player);
        Block target = event.getBlock();
        WorldConfiguration wcfg = getWorldConfig(player);

        // Blacklist guard
        if (wcfg.getBlacklist() == null) {
            return;
        }

        if (!wcfg.getBlacklist().check(new BlockInteractBlacklistEvent(
                localPlayer, toVector(event.getTarget()), createTarget(target, event.getEffectiveMaterial())), false, false)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onSpawnEntity(SpawnEntityEvent event) {
        Player player = Causes.getInvolvedPlayer(event.getCauses());

        if (player == null) {
            return;
        }

        LocalPlayer localPlayer = getPlugin().wrapPlayer(player);
        WorldConfiguration wcfg = getWorldConfig(player);

        // Blacklist guard
        if (wcfg.getBlacklist() == null) {
            return;
        }

        Material material = Materials.getRelatedMaterial(event.getEffectiveType());
        if (material != null) {
            if (!wcfg.getBlacklist().check(new ItemUseBlacklistEvent(localPlayer, toVector(event.getTarget()), createTarget(material)), false, false)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onDestroyEntity(DestroyEntityEvent event) {
        Player player = Causes.getInvolvedPlayer(event.getCauses());

        if (player == null) {
            return;
        }

        LocalPlayer localPlayer = getPlugin().wrapPlayer(player);
        Entity target = event.getEntity();
        WorldConfiguration wcfg = getWorldConfig(player);

        // Blacklist guard
        if (wcfg.getBlacklist() == null) {
            return;
        }

        Material material = Materials.getRelatedMaterial(target.getType());
        if (material != null) {
            // Not really a block but we only have one on-break blacklist event
            if (!wcfg.getBlacklist().check(new BlockBreakBlacklistEvent(localPlayer, toVector(event.getTarget()), createTarget(material)), false, false)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onUseItem(UseItemEvent event) {
        Player player = Causes.getInvolvedPlayer(event.getCauses());

        if (player == null) {
            return;
        }

        LocalPlayer localPlayer = getPlugin().wrapPlayer(player);
        ItemStack target = event.getItemStack();
        WorldConfiguration wcfg = getWorldConfig(player);

        // Blacklist guard
        if (wcfg.getBlacklist() == null) {
            return;
        }

        if (!wcfg.getBlacklist().check(new ItemUseBlacklistEvent(localPlayer, toVector(player.getLocation()), createTarget(target)), false, false)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        ConfigurationManager cfg = getPlugin().getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(event.getPlayer().getWorld());

        if (wcfg.getBlacklist() != null) {
            Item ci = event.getItemDrop();

            if (!wcfg.getBlacklist().check(
                    new ItemDropBlacklistEvent(getPlugin().wrapPlayer(event.getPlayer()),
                            toVector(ci.getLocation()), createTarget(ci.getItemStack())), false, false)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        ConfigurationManager cfg = getPlugin().getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(event.getPlayer().getWorld());

        if (wcfg.getBlacklist() != null) {
            Item ci = event.getItem();

            if (!wcfg.getBlacklist().check(
                    new ItemAcquireBlacklistEvent(getPlugin().wrapPlayer(event.getPlayer()),
                            toVector(ci.getLocation()), createTarget(ci.getItemStack())), false, true)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockDispense(BlockDispenseEvent event) {
        ConfigurationManager cfg = getPlugin().getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(event.getBlock().getWorld());

        if (wcfg.getBlacklist() != null) {
            if (!wcfg.getBlacklist().check(new BlockDispenseBlacklistEvent(null, toVector(event.getBlock()), createTarget(event.getItem())), false, false)) {
                event.setCancelled(true);
            }
        }
    }

}
