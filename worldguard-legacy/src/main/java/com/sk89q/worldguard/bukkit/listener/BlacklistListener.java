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

import static com.sk89q.worldguard.bukkit.BukkitUtil.createTarget;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.blacklist.event.BlockBreakBlacklistEvent;
import com.sk89q.worldguard.blacklist.event.BlockDispenseBlacklistEvent;
import com.sk89q.worldguard.blacklist.event.BlockInteractBlacklistEvent;
import com.sk89q.worldguard.blacklist.event.BlockPlaceBlacklistEvent;
import com.sk89q.worldguard.blacklist.event.ItemAcquireBlacklistEvent;
import com.sk89q.worldguard.blacklist.event.ItemDestroyWithBlacklistEvent;
import com.sk89q.worldguard.blacklist.event.ItemDropBlacklistEvent;
import com.sk89q.worldguard.blacklist.event.ItemUseBlacklistEvent;
import com.sk89q.worldguard.bukkit.BukkitWorldConfiguration;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.bukkit.event.block.BreakBlockEvent;
import com.sk89q.worldguard.bukkit.event.block.PlaceBlockEvent;
import com.sk89q.worldguard.bukkit.event.block.UseBlockEvent;
import com.sk89q.worldguard.bukkit.event.entity.DestroyEntityEvent;
import com.sk89q.worldguard.bukkit.event.entity.SpawnEntityEvent;
import com.sk89q.worldguard.bukkit.event.inventory.UseItemEvent;
import com.sk89q.worldguard.bukkit.util.Materials;
import com.sk89q.worldguard.config.ConfigurationManager;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

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
    public void onBreakBlock(final BreakBlockEvent event) {
        final Player player = event.getCause().getFirstPlayer();

        if (player == null) {
            return;
        }

        final LocalPlayer localPlayer = getPlugin().wrapPlayer(player);
        final BukkitWorldConfiguration wcfg = getWorldConfig(localPlayer);

        // Blacklist guard
        if (wcfg.getBlacklist() == null) {
            return;
        }

        event.filter(target -> {
            if (!wcfg.getBlacklist().check(
                    new BlockBreakBlacklistEvent(localPlayer, BukkitAdapter.asBlockVector(target), createTarget(target.getBlock())), false, false)) {
                return false;
            } else if (!wcfg.getBlacklist().check(
                    new ItemDestroyWithBlacklistEvent(localPlayer, BukkitAdapter.asBlockVector(target), createTarget(player.getItemInHand())), false, false)) {
                return false;
            }

            return true;
        });
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlaceBlock(final PlaceBlockEvent event) {
        Player player = event.getCause().getFirstPlayer();

        if (player == null) {
            return;
        }

        final LocalPlayer localPlayer = getPlugin().wrapPlayer(player);
        final BukkitWorldConfiguration wcfg = getWorldConfig(localPlayer);

        // Blacklist guard
        if (wcfg.getBlacklist() == null) {
            return;
        }

        event.filter(target -> wcfg.getBlacklist().check(new BlockPlaceBlacklistEvent(
                localPlayer, BukkitAdapter.asBlockVector(target), createTarget(target.getBlock())), false, false));
    }

    @EventHandler(ignoreCancelled = true)
    public void onUseBlock(final UseBlockEvent event) {
        Player player = event.getCause().getFirstPlayer();

        if (player == null) {
            return;
        }

        final LocalPlayer localPlayer = getPlugin().wrapPlayer(player);
        final BukkitWorldConfiguration wcfg = getWorldConfig(localPlayer);

        // Blacklist guard
        if (wcfg.getBlacklist() == null) {
            return;
        }

        event.filter(target -> wcfg.getBlacklist().check(new BlockInteractBlacklistEvent(
                localPlayer, BukkitAdapter.asBlockVector(target), createTarget(target.getBlock())), false, false));
    }

    @EventHandler(ignoreCancelled = true)
    public void onSpawnEntity(SpawnEntityEvent event) {
        Player player = event.getCause().getFirstPlayer();

        if (player == null) {
            return;
        }

        LocalPlayer localPlayer = getPlugin().wrapPlayer(player);
        BukkitWorldConfiguration wcfg = getWorldConfig(localPlayer);

        // Blacklist guard
        if (wcfg.getBlacklist() == null) {
            return;
        }

        Material material = Materials.getRelatedMaterial(event.getEffectiveType());
        if (material != null) {
            if (!wcfg.getBlacklist().check(new ItemUseBlacklistEvent(localPlayer, BukkitAdapter.asBlockVector(event.getTarget()), createTarget(material)), false, false)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onDestroyEntity(DestroyEntityEvent event) {
        Player player = event.getCause().getFirstPlayer();

        if (player == null) {
            return;
        }

        LocalPlayer localPlayer = getPlugin().wrapPlayer(player);
        Entity target = event.getEntity();
        BukkitWorldConfiguration wcfg = getWorldConfig(localPlayer);

        // Blacklist guard
        if (wcfg.getBlacklist() == null) {
            return;
        }

        if (target instanceof Item) {
            Item item = (Item) target;
            if (!wcfg.getBlacklist().check(
                    new ItemAcquireBlacklistEvent(localPlayer,
                            BukkitAdapter.asBlockVector(target.getLocation()), createTarget(item.getItemStack())), false, true)) {
                event.setCancelled(true);
                return;
            }
        }

        Material material = Materials.getRelatedMaterial(target.getType());
        if (material != null) {
            // Not really a block but we only have one on-break blacklist event
            if (!wcfg.getBlacklist().check(new BlockBreakBlacklistEvent(localPlayer, BukkitAdapter.asBlockVector(event.getTarget()), createTarget(material)), false, false)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onUseItem(UseItemEvent event) {
        Player player = event.getCause().getFirstPlayer();

        if (player == null) {
            return;
        }

        LocalPlayer localPlayer = getPlugin().wrapPlayer(player);
        ItemStack target = event.getItemStack();
        BukkitWorldConfiguration wcfg = getWorldConfig(localPlayer);

        // Blacklist guard
        if (wcfg.getBlacklist() == null) {
            return;
        }

        if (!wcfg.getBlacklist().check(new ItemUseBlacklistEvent(localPlayer, BukkitAdapter.asBlockVector(player.getLocation()), createTarget(target)), false, false)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        ConfigurationManager cfg = WorldGuard.getInstance().getPlatform().getGlobalStateManager();
        BukkitWorldConfiguration wcfg = (BukkitWorldConfiguration) cfg.get(BukkitAdapter.adapt(event.getPlayer().getWorld()));

        if (wcfg.getBlacklist() != null) {
            Item ci = event.getItemDrop();

            if (!wcfg.getBlacklist().check(
                    new ItemDropBlacklistEvent(getPlugin().wrapPlayer(event.getPlayer()),
                            BukkitAdapter.asBlockVector(ci.getLocation()), createTarget(ci.getItemStack())), false, false)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockDispense(BlockDispenseEvent event) {
        ConfigurationManager cfg = WorldGuard.getInstance().getPlatform().getGlobalStateManager();
        BukkitWorldConfiguration wcfg = (BukkitWorldConfiguration) cfg.get(BukkitAdapter.adapt(event.getBlock().getWorld()));

        if (wcfg.getBlacklist() != null) {
            if (!wcfg.getBlacklist().check(new BlockDispenseBlacklistEvent(null, BukkitAdapter.asBlockVector(event.getBlock().getLocation()),
                    createTarget(event.getItem())), false, false)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        HumanEntity entity = event.getWhoClicked();
        Inventory inventory = event.getInventory();
        ItemStack item = event.getCurrentItem();

        if (item != null && inventory != null && inventory.getHolder() != null && entity instanceof Player) {
            Player player = (Player) entity;
            ConfigurationManager cfg = WorldGuard.getInstance().getPlatform().getGlobalStateManager();
            BukkitWorldConfiguration wcfg = (BukkitWorldConfiguration) cfg.get(BukkitAdapter.adapt(entity.getWorld()));
            LocalPlayer localPlayer = getPlugin().wrapPlayer(player);

            if (wcfg.getBlacklist() != null && !wcfg.getBlacklist().check(
                    new ItemAcquireBlacklistEvent(localPlayer, BukkitAdapter.asBlockVector(entity.getLocation()), createTarget(item)), false, false)) {
                event.setCancelled(true);

                if (inventory.getHolder().equals(player)) {
                    event.setCurrentItem(null);
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryCreative(InventoryCreativeEvent event) {
        HumanEntity entity = event.getWhoClicked();
        ItemStack item = event.getCursor();

        if (item != null && entity instanceof Player) {
            Player player = (Player) entity;
            ConfigurationManager cfg = WorldGuard.getInstance().getPlatform().getGlobalStateManager();
            BukkitWorldConfiguration wcfg = (BukkitWorldConfiguration) cfg.get(BukkitAdapter.adapt(entity.getWorld()));
            LocalPlayer localPlayer = getPlugin().wrapPlayer(player);

            if (wcfg.getBlacklist() != null && !wcfg.getBlacklist().check(
                    new ItemAcquireBlacklistEvent(localPlayer, BukkitAdapter.asBlockVector(entity.getLocation()), createTarget(item)), false, false)) {
                event.setCancelled(true);
                event.setCursor(null);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        Inventory inventory = player.getInventory();
        ItemStack item = inventory.getItem(event.getNewSlot());

        if (item != null) {
            ConfigurationManager cfg = WorldGuard.getInstance().getPlatform().getGlobalStateManager();
            BukkitWorldConfiguration wcfg = (BukkitWorldConfiguration) cfg.get(BukkitAdapter.adapt(player.getWorld()));
            LocalPlayer localPlayer = getPlugin().wrapPlayer(player);

            if (wcfg.getBlacklist() != null && !wcfg.getBlacklist().check(
                    new ItemAcquireBlacklistEvent(localPlayer, BukkitAdapter.asBlockVector(player.getLocation()), createTarget(item)), false, false)) {
                inventory.setItem(event.getNewSlot(), null);
            }
        }
    }

}
