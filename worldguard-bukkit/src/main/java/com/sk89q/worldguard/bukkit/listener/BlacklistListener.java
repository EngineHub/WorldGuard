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
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.blacklist.event.BlockBreakBlacklistEvent;
import com.sk89q.worldguard.blacklist.event.BlockDispenseBlacklistEvent;
import com.sk89q.worldguard.blacklist.event.BlockInteractBlacklistEvent;
import com.sk89q.worldguard.blacklist.event.BlockPlaceBlacklistEvent;
import com.sk89q.worldguard.blacklist.event.ItemAcquireBlacklistEvent;
import com.sk89q.worldguard.blacklist.event.ItemDestroyWithBlacklistEvent;
import com.sk89q.worldguard.blacklist.event.ItemDropBlacklistEvent;
import com.sk89q.worldguard.blacklist.event.ItemEquipBlacklistEvent;
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
import com.sk89q.worldguard.config.WorldConfiguration;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockDispenseArmorEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import static com.sk89q.worldguard.bukkit.BukkitUtil.createTarget;

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
        final WorldConfiguration wcfg = getWorldConfig(event.getWorld());

        // Blacklist guard
        if (wcfg.getBlacklist() == null) {
            return;
        }
        Player player = event.getCause().getFirstPlayer();

        if (player == null) {
            return;
        }

        final LocalPlayer localPlayer = getPlugin().wrapPlayer(player);
        event.filter(target -> {
            if (!wcfg.getBlacklist().check(
                    new BlockBreakBlacklistEvent(localPlayer, BukkitAdapter.asBlockVector(target),
                            createTarget(target.getBlock())), false, false)) {
                return false;
            } else if (!wcfg.getBlacklist().check(
                    new ItemDestroyWithBlacklistEvent(localPlayer, BukkitAdapter.asBlockVector(target),
                            createTarget(player.getInventory().getItemInMainHand())), false, false)) {
                return false;
            }

            return true;
        });
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlaceBlock(final PlaceBlockEvent event) {
        final WorldConfiguration wcfg = getWorldConfig(event.getWorld());

        // Blacklist guard
        if (wcfg.getBlacklist() == null) {
            return;
        }
        Player player = event.getCause().getFirstPlayer();

        if (player == null) {
            return;
        }

        final LocalPlayer localPlayer = getPlugin().wrapPlayer(player);
        event.filter(target -> wcfg.getBlacklist().check(new BlockPlaceBlacklistEvent(
                localPlayer, BukkitAdapter.asBlockVector(target), createTarget(target.getBlock())), false, false));
    }

    @EventHandler(ignoreCancelled = true)
    public void onUseBlock(final UseBlockEvent event) {
        final WorldConfiguration wcfg = getWorldConfig(event.getWorld());

        // Blacklist guard
        if (wcfg.getBlacklist() == null) {
            return;
        }
        Player player = event.getCause().getFirstPlayer();

        if (player == null) {
            return;
        }

        final LocalPlayer localPlayer = getPlugin().wrapPlayer(player);
        event.filter(target -> wcfg.getBlacklist().check(new BlockInteractBlacklistEvent(
                localPlayer, BukkitAdapter.asBlockVector(target), createTarget(target.getBlock())), false, false));
    }

    @EventHandler(ignoreCancelled = true)
    public void onSpawnEntity(SpawnEntityEvent event) {
        final WorldConfiguration wcfg = getWorldConfig(event.getWorld());

        // Blacklist guard
        if (wcfg.getBlacklist() == null) {
            return;
        }
        Player player = event.getCause().getFirstPlayer();

        if (player == null) {
            return;
        }

        final LocalPlayer localPlayer = getPlugin().wrapPlayer(player);
        Material material = Materials.getRelatedMaterial(event.getEffectiveType());
        if (material != null) {
            if (!wcfg.getBlacklist().check(new ItemUseBlacklistEvent(
                    localPlayer, BukkitAdapter.asBlockVector(event.getTarget()), createTarget(material)), false, false)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onDestroyEntity(DestroyEntityEvent event) {
        final WorldConfiguration wcfg = getWorldConfig(event.getWorld());

        // Blacklist guard
        if (wcfg.getBlacklist() == null) {
            return;
        }
        Player player = event.getCause().getFirstPlayer();

        if (player == null) {
            return;
        }

        final LocalPlayer localPlayer = getPlugin().wrapPlayer(player);
        Entity target = event.getEntity();
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
            if (!wcfg.getBlacklist().check(new BlockBreakBlacklistEvent(
                    localPlayer, BukkitAdapter.asBlockVector(event.getTarget()), createTarget(material)), false, false)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onUseItem(UseItemEvent event) {
        final WorldConfiguration wcfg = getWorldConfig(event.getWorld());

        // Blacklist guard
        if (wcfg.getBlacklist() == null) {
            return;
        }
        Player player = event.getCause().getFirstPlayer();

        if (player == null) {
            return;
        }

        final LocalPlayer localPlayer = getPlugin().wrapPlayer(player);
        ItemStack target = event.getItemStack();
        if (!wcfg.getBlacklist().check(new ItemUseBlacklistEvent(
                localPlayer, BukkitAdapter.asBlockVector(player.getLocation()), createTarget(target)), false, false)) {
            event.setCancelled(true);
            return;
        }

        if (Materials.isArmor(target.getType()) && !wcfg.getBlacklist().check(new ItemEquipBlacklistEvent(
                localPlayer, BukkitAdapter.asBlockVector(player.getLocation()), createTarget(target)), false, false)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        WorldConfiguration wcfg = getWorldConfig(event.getPlayer().getWorld());

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
        BukkitWorldConfiguration wcfg = getWorldConfig(event.getBlock().getWorld());

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
        if (!(entity instanceof Player)) return;
        Inventory inventory = event.getInventory();
        ItemStack item = event.getCurrentItem();

        if (item != null && inventory.getHolder() != null) {
            Player player = (Player) entity;
            WorldConfiguration wcfg = getWorldConfig(player.getWorld());
            LocalPlayer localPlayer = getPlugin().wrapPlayer(player);

            if (wcfg.getBlacklist() != null && !wcfg.getBlacklist().check(
                    new ItemAcquireBlacklistEvent(localPlayer, BukkitAdapter.asBlockVector(entity.getLocation()), createTarget(item)), false, false)) {
                event.setCancelled(true);

                if (inventory.getHolder().equals(player)) {
                    event.setCurrentItem(null);
                }
            }


            ItemStack equipped = checkEquipped(event);
            if (equipped != null) {
                if (wcfg.getBlacklist() != null && !wcfg.getBlacklist().check(new ItemEquipBlacklistEvent(localPlayer,
                        BukkitAdapter.asBlockVector(player.getLocation()), createTarget(equipped)), false, false)) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent event) {
        HumanEntity entity = event.getWhoClicked();
        if (!(entity instanceof Player)) return;
        if (event.getInventory().getType() != InventoryType.PLAYER
                && event.getInventory().getType() != InventoryType.CRAFTING) return;
        if (event.getRawSlots().stream().anyMatch(i -> i >= 5 && i <= 8)) { // dropped on armor slots
            Player player = (Player) entity;
            ConfigurationManager cfg = WorldGuard.getInstance().getPlatform().getGlobalStateManager();
            WorldConfiguration wcfg = cfg.get(BukkitAdapter.adapt(entity.getWorld()));
            LocalPlayer localPlayer = getPlugin().wrapPlayer(player);
            if (wcfg.getBlacklist() != null && !wcfg.getBlacklist().check(new ItemEquipBlacklistEvent(localPlayer,
                    BukkitAdapter.asBlockVector(player.getLocation()), createTarget(event.getOldCursor())), false, false)) {
                event.setCancelled(true);
            }
        }
    }

    private ItemStack checkEquipped(InventoryClickEvent event) {
        final Inventory clickedInventory = event.getClickedInventory();
        if (event.getSlotType() == InventoryType.SlotType.ARMOR) {
            switch (event.getAction()) {
                case PLACE_ONE:
                case PLACE_SOME:
                case PLACE_ALL:
                case SWAP_WITH_CURSOR:
                    final ItemStack cursor = event.getCursor();
                    if (cursor != null) {
                        return cursor;
                    }
                case HOTBAR_SWAP:
                    if (event.getClick() == ClickType.SWAP_OFFHAND) {
                        return clickedInventory == null ? null : ((PlayerInventory) clickedInventory).getItemInOffHand();
                    }
                    return clickedInventory == null ? null : clickedInventory.getItem(event.getHotbarButton());
                default:
                    break;
            }
        } else if (clickedInventory != null && clickedInventory.getType() == InventoryType.PLAYER
                && (event.getView().getTopInventory().getType() == InventoryType.PLAYER
                    || event.getView().getTopInventory().getType() == InventoryType.CRAFTING)
                && event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
            return event.getCurrentItem();
        }
        return null;
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryCreative(InventoryCreativeEvent event) {
        HumanEntity entity = event.getWhoClicked();
        ItemStack item = event.getCursor();

        if (item.getType() != Material.AIR && entity instanceof Player) {
            Player player = (Player) entity;
            WorldConfiguration wcfg = getWorldConfig(player.getWorld());
            LocalPlayer localPlayer = getPlugin().wrapPlayer(player);

            if (wcfg.getBlacklist() != null && !wcfg.getBlacklist().check(
                    new ItemAcquireBlacklistEvent(localPlayer, BukkitAdapter.asBlockVector(entity.getLocation()), createTarget(item)), false, false)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        Inventory inventory = player.getInventory();
        ItemStack item = inventory.getItem(event.getNewSlot());

        if (item != null) {
            WorldConfiguration wcfg = getWorldConfig(player.getWorld());
            LocalPlayer localPlayer = getPlugin().wrapPlayer(player);

            if (wcfg.getBlacklist() != null && !wcfg.getBlacklist().check(
                    new ItemAcquireBlacklistEvent(localPlayer, BukkitAdapter.asBlockVector(player.getLocation()), createTarget(item)), false, false)) {
                inventory.setItem(event.getNewSlot(), null);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockDispenseArmor(BlockDispenseArmorEvent event) {
        if (!(event.getTargetEntity() instanceof Player)) return;
        Player player = ((Player) event.getTargetEntity());
        ItemStack stack = event.getItem();

        WorldConfiguration wcfg = getWorldConfig(player.getWorld());
        LocalPlayer localPlayer = getPlugin().wrapPlayer(player);
        if (wcfg.getBlacklist() != null && !wcfg.getBlacklist().check(
                new ItemEquipBlacklistEvent(localPlayer, BukkitAdapter.asBlockVector(player.getLocation()), createTarget(stack)), false, true)) {
            event.setCancelled(true);
        }
    }
}
