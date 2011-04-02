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

import static com.sk89q.worldguard.bukkit.BukkitUtil.toVector;

import java.util.Iterator;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.blacklist.events.BlockInteractBlacklistEvent;
import com.sk89q.worldguard.blacklist.events.ItemUseBlacklistEvent;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;


/**
 * 
 * @author DarkLiKally
 */
public class WorldGuardInteractHandler {
    /**
     * Plugin
     */
    WorldGuardPlugin plugin;

    public WorldGuardInteractHandler(WorldGuardPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean onBlockRightclick(PlayerInteractEvent event, ConfigurationManager cfg, WorldConfiguration wcfg,
            Action action, Player player, Block block, ItemStack item, int itemId) {
        if (wcfg.useRegions && !event.hasBlock() && block != null) {
            Vector pt = toVector(block.getRelative(event.getBlockFace()));
            RegionManager mgr = plugin.getGlobalRegionManager().get(player.getWorld());

            if (block.getType() == Material.WALL_SIGN) {
                pt = pt.subtract(0, 1, 0);
            }

            if (!mgr.getApplicableRegions(pt).canBuild(new BukkitPlayer(plugin, player))) {
                player.sendMessage(ChatColor.DARK_RED
                        + "You don't have permission for this area.");
                event.setCancelled(true);
                return false;
            }
        }

        if (wcfg.getBlacklist() != null && item != null && block != null) {
            if (!wcfg.getBlacklist().check(
                    new ItemUseBlacklistEvent(new BukkitPlayer(plugin, player),
                    toVector(block.getRelative(event.getBlockFace())),
                    item.getTypeId()), false, false)) {
                event.setCancelled(true);
                return false;
            }
        }

        if (wcfg.useRegions && item != null && block != null && item.getTypeId() == 259) {
            Vector pt = toVector(block.getRelative(event.getBlockFace()));
            RegionManager mgr = plugin.getGlobalRegionManager().get(player.getWorld());

            if (!mgr.getApplicableRegions(pt).allows(DefaultFlag.LIGHTER)) {
                event.setCancelled(true);
                return false;
            }
        }

        
        // onBlockInteract
        if ((block.getType() == Material.CHEST
                || block.getType() == Material.DISPENSER
                || block.getType() == Material.FURNACE
                || block.getType() == Material.BURNING_FURNACE
                || block.getType() == Material.NOTE_BLOCK)) {
            if (wcfg.useRegions) {
                Vector pt = toVector(block);
                LocalPlayer localPlayer = new BukkitPlayer(plugin, player);
                RegionManager mgr = plugin.getGlobalRegionManager().get(player.getWorld());

                if (!plugin.hasPermission(player, "region.bypass")) {
                    ApplicableRegionSet set = mgr.getApplicableRegions(pt);
                    if (!set.allows(DefaultFlag.CHEST_ACCESS) && !set.canBuild(localPlayer)) {
                        player.sendMessage(ChatColor.DARK_RED + "You don't have permission for this area.");
                        event.setCancelled(true);
                        return false;
                    }
                }
            }
        }

        if (wcfg.useRegions && (block.getType() == Material.LEVER
                || block.getType() == Material.STONE_BUTTON)) {
            Vector pt = toVector(block);
            RegionManager mgr = plugin.getGlobalRegionManager().get(player.getWorld());
            ApplicableRegionSet set = mgr.getApplicableRegions(pt);

            if (!set.allows(DefaultFlag.USE)) {
                player.sendMessage(ChatColor.DARK_RED + "You don't have permission for this area.");
                event.setCancelled(true);
                return false;
            }
        }

        if (wcfg.getBlacklist() != null) {
            if (!wcfg.getBlacklist().check(
                    new BlockInteractBlacklistEvent(new BukkitPlayer(plugin, player), toVector(block),
                    block.getTypeId()), false, false)) {
                event.setCancelled(true);
                return false;
            }
        }

        // onBlockRightClick
        if (wcfg.useRegions && itemId == wcfg.regionWand) {
            Vector pt = toVector(block);

            RegionManager mgr = plugin.getGlobalRegionManager().get(player.getWorld());
            ApplicableRegionSet set = mgr.getApplicableRegions(pt);
            List<String> regions = mgr.getApplicableRegionsIDs(pt);

            if (regions.size() > 0) {
                player.sendMessage(ChatColor.YELLOW + "Can you build? "
                        + (set.canBuild(new BukkitPlayer(plugin, player)) ? "Yes" : "No"));

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

        Material type = block.getType();

        if (wcfg.useRegions && type == Material.CAKE_BLOCK) {
            Vector pt = toVector(block);

            RegionManager mgr = plugin.getGlobalRegionManager().get(player.getWorld());
            ApplicableRegionSet set = mgr.getApplicableRegions(pt);

            if (!set.canBuild(new BukkitPlayer(plugin, player))) {
                player.sendMessage(ChatColor.DARK_RED + "You don't have permission for this area.");

                byte newData = (byte) (block.getData() - 1);
                newData = newData < 0 ? 0 : newData;

                block.setData(newData);
                player.setHealth(player.getHealth() - 3);

                return true;
            }
        }

        /*if (wcfg.useRegions && wcfg.useiConomy && cfg.getiConomy() != null
                    && (type == Material.SIGN_POST || type == Material.SIGN || type == Material.WALL_SIGN)) {
            BlockState blockState = block.getState();

            if (((Sign)blockState).getLine(0).equalsIgnoreCase("[WorldGuard]")
                    && ((Sign)blockState).getLine(1).equalsIgnoreCase("For sale")) {
                String regionId = ((Sign)blockState).getLine(2);
                //String regionComment = ((Sign)block).getLine(3);

                if (regionId != null && regionId != "") {
                    RegionManager mgr = cfg.getWorldGuardPlugin().getGlobalRegionManager().getRegionManager(player.getWorld().getName());
                    ProtectedRegion region = mgr.getRegion(regionId);

                    if (region != null) {
                        RegionFlagContainer flags = region.getFlags();

                        if (flags.getBooleanFlag(Flags.BUYABLE).getValue(false)) {
                            if (iConomy.getBank().hasAccount(player.getName())) {
                                Account account = iConomy.getBank().getAccount(player.getName());
                                double balance = account.getBalance();
                                double regionPrice = flags.getDoubleFlag(Flags.PRICE).getValue();

                                if (balance >= regionPrice) {
                                    account.subtract(regionPrice);
                                    player.sendMessage(ChatColor.YELLOW + "You have bought the region " + regionId + " for " +
                                            iConomy.getBank().format(regionPrice));
                                    DefaultDomain owners = region.getOwners();
                                    owners.addPlayer(player.getName());
                                    region.setOwners(owners);
                                    flags.getBooleanFlag(Flags.BUYABLE).setValue(false);
                                    try{
                                        account.save();
                                    } catch(Exception e) {}
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
        }*/

        return true;
    }

    public boolean onBlockClick(PlayerInteractEvent event, ConfigurationManager cfg, WorldConfiguration wcfg,
            Action action, Player player, Block block, ItemStack item, int itemId) {
        if (wcfg.useRegions && !event.hasBlock() && block != null) {
            Vector pt = toVector(block.getRelative(event.getBlockFace()));

            RegionManager mgr = plugin.getGlobalRegionManager().get(player.getWorld());
            ApplicableRegionSet set = mgr.getApplicableRegions(pt);

            if (block.getType() == Material.WALL_SIGN) {
                pt = pt.subtract(0, 1, 0);
            }

            if (!set.canBuild(new BukkitPlayer(plugin, player))) {
                player.sendMessage(ChatColor.DARK_RED
                        + "You don't have permission for this area.");
                event.setCancelled(true);
                return false;
            }
        }

        if (wcfg.getBlacklist() != null && item != null && block != null) {
            if (!wcfg.getBlacklist().check(
                    new ItemUseBlacklistEvent(new BukkitPlayer(plugin, player),
                    toVector(block.getRelative(event.getBlockFace())),
                    item.getTypeId()), false, false)) {
                event.setCancelled(true);
                return false;
            }
        }

        if (wcfg.useRegions && item != null && block != null && item.getTypeId() == 259) {
            Vector pt = toVector(block.getRelative(event.getBlockFace()));
            RegionManager mgr = plugin.getGlobalRegionManager().get(player.getWorld());

            if (!mgr.getApplicableRegions(pt).allows(DefaultFlag.LIGHTER)) {
                event.setCancelled(true);
                return false;
            }
        }

        // onBlockInteract
        if (wcfg.useRegions && (block.getType() == Material.LEVER
                || block.getType() == Material.STONE_BUTTON)) {
            Vector pt = toVector(block);
            RegionManager mgr = plugin.getGlobalRegionManager().get(player.getWorld());
            ApplicableRegionSet applicableRegions = mgr.getApplicableRegions(pt);

            if (!applicableRegions.allows(DefaultFlag.USE)) {
                player.sendMessage(ChatColor.DARK_RED + "You don't have permission for this area.");
                event.setCancelled(true);
                return false;
            }
        }

        if (wcfg.getBlacklist() != null) {
            if (!wcfg.getBlacklist().check(
                    new BlockInteractBlacklistEvent(new BukkitPlayer(plugin, player), toVector(block),
                    block.getTypeId()), false, false)) {
                event.setCancelled(true);
                return false;
            }
        }

        return true;
    }

    public boolean onBlockPlace(PlayerInteractEvent event, ConfigurationManager cfg, WorldConfiguration wcfg,
            Action action, Player player, Block block, ItemStack item, int itemId) {
        return true;
    }

    public boolean itemInHand(PlayerInteractEvent event, WorldConfiguration wcfg,
            Action action, Player player, Block block, ItemStack item, int itemId) {
        if (wcfg.useRegions
                && (itemId == 322 || itemId == 320 || itemId == 319 || itemId == 297 || itemId == 260
                        || itemId == 350 || itemId == 349 || itemId == 354) ) {
            return true;
        }

        if (!wcfg.itemDurability) {
            // Hoes
            if (item.getTypeId() >= 290 && item.getTypeId() <= 294) {
                item.setDurability((byte) -1);
                player.setItemInHand(item);
            }
        }

        return false;
    }
}