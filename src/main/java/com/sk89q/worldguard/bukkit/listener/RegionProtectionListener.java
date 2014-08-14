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

import com.sk89q.worldguard.bukkit.RegionQuery;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.bukkit.event.block.BreakBlockEvent;
import com.sk89q.worldguard.bukkit.event.block.PlaceBlockEvent;
import com.sk89q.worldguard.bukkit.event.block.UseBlockEvent;
import com.sk89q.worldguard.bukkit.event.entity.DestroyEntityEvent;
import com.sk89q.worldguard.bukkit.event.entity.SpawnEntityEvent;
import com.sk89q.worldguard.bukkit.event.entity.UseEntityEvent;
import com.sk89q.worldguard.bukkit.util.Entities;
import com.sk89q.worldguard.bukkit.util.Materials;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

/**
 * Handle events that need to be processed by region protection.
 */
public class RegionProtectionListener extends AbstractListener {

    /**
     * Construct the listener.
     *
     * @param plugin an instance of WorldGuardPlugin
     */
    public RegionProtectionListener(WorldGuardPlugin plugin) {
        super(plugin);
    }

    private void tellErrorMessage(CommandSender sender, Object subject) {
        sender.sendMessage(ChatColor.DARK_RED + "Sorry, but you are not allowed to do that here.");
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlaceBlock(PlaceBlockEvent event) {
        Player player = event.getCause().getPlayerRootCause();
        Location target = event.getTarget();
        Material type = event.getEffectiveMaterial();

        if (player != null) {
            RegionQuery query = getPlugin().getRegionContainer().createQuery(player);
            boolean canPlace;

            // Flint and steel, fire charge
            if (type == Material.FIRE) {
                canPlace = query.testPermission(target, DefaultFlag.LIGHTER);

            } else {
                canPlace = query.testPermission(target);
            }

            if (!canPlace) {
                tellErrorMessage(player, target);
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBreakBlock(BreakBlockEvent event) {
        Player player = event.getCause().getPlayerRootCause();
        Location target = event.getTarget();

        if (player != null) {
            RegionQuery query = getPlugin().getRegionContainer().createQuery(player);
            boolean canBreak = query.testPermission(target);

            if (!canBreak) {
                tellErrorMessage(player, target);
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onUseBlock(UseBlockEvent event) {
        Player player = event.getCause().getPlayerRootCause();
        Location target = event.getTarget();
        Material type = event.getEffectiveMaterial();

        if (player != null) {
            RegionQuery query = getPlugin().getRegionContainer().createQuery(player);
            boolean canUse;

            // Inventory blocks (CHEST_ACCESS)
            if (Materials.isInventoryBlock(type)) {
                canUse = query.testPermission(target, DefaultFlag.USE, DefaultFlag.CHEST_ACCESS);

            // Beds (SLEEP)
            } else if (type == Material.BED) {
                canUse = query.testPermission(target, DefaultFlag.USE, DefaultFlag.SLEEP);

            // TNT (TNT)
            } else if (type == Material.TNT) {
                canUse = query.testPermission(target, DefaultFlag.TNT);

            // Everything else
            } else {
                canUse = query.testPermission(target, DefaultFlag.USE);
            }

            if (!canUse) {
                tellErrorMessage(player, target);
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onSpawnEntity(SpawnEntityEvent event) {
        Player player = event.getCause().getPlayerRootCause();
        Location target = event.getTarget();
        EntityType type = event.getEffectiveType();

        if (player != null) {
            RegionQuery query = getPlugin().getRegionContainer().createQuery(player);
            boolean canSpawn;

            if (Entities.isVehicle(type)) {
                canSpawn = query.testPermission(target, DefaultFlag.PLACE_VEHICLE);
            } else {
                canSpawn = query.testPermission(target);
            }

            if (!canSpawn) {
                tellErrorMessage(player, target);
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onDestroyEntity(DestroyEntityEvent event) {
        Player player = event.getCause().getPlayerRootCause();
        Location target = event.getTarget();
        EntityType type = event.getEntity().getType();

        if (player != null) {
            RegionQuery query = getPlugin().getRegionContainer().createQuery(player);
            boolean canDestroy;

            if (Entities.isVehicle(type)) {
                canDestroy = query.testPermission(target, DefaultFlag.DESTROY_VEHICLE);
            } else {
                canDestroy = query.testPermission(target);
            }

            if (!canDestroy) {
                tellErrorMessage(player, target);
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onUseEntity(UseEntityEvent event) {
        Player player = event.getCause().getPlayerRootCause();
        Location target = event.getTarget();

        if (player != null) {
            RegionQuery query = getPlugin().getRegionContainer().createQuery(player);
            boolean canUse = query.testPermission(target, DefaultFlag.USE);

            if (!canUse) {
                tellErrorMessage(player, target);
                event.setCancelled(true);
            }
        }
    }

}
