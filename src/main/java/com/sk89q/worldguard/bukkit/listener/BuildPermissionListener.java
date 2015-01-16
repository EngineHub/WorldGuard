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

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.bukkit.event.block.BreakBlockEvent;
import com.sk89q.worldguard.bukkit.event.block.PlaceBlockEvent;
import com.sk89q.worldguard.bukkit.event.block.UseBlockEvent;
import com.sk89q.worldguard.bukkit.event.entity.DamageEntityEvent;
import com.sk89q.worldguard.bukkit.event.entity.DestroyEntityEvent;
import com.sk89q.worldguard.bukkit.event.entity.SpawnEntityEvent;
import com.sk89q.worldguard.bukkit.event.entity.UseEntityEvent;
import com.sk89q.worldguard.bukkit.event.inventory.UseItemEvent;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

public class BuildPermissionListener extends AbstractListener {

    /**
     * Construct the listener.
     *
     * @param plugin an instance of WorldGuardPlugin
     */
    public BuildPermissionListener(WorldGuardPlugin plugin) {
        super(plugin);
    }

    private boolean hasBuildPermission(CommandSender sender, String perm) {
        return getPlugin().hasPermission(sender, "worldguard.build." + perm);
    }

    private void tellErrorMessage(CommandSender sender, World world) {
        String message = getWorldConfig(world).buildPermissionDenyMessage;
        if (!message.isEmpty()) {
            sender.sendMessage(message);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlaceBlock(final PlaceBlockEvent event) {
        if (!getWorldConfig(event.getWorld()).buildPermissions) return;

        Object rootCause = event.getCause().getRootCause();

        if (rootCause instanceof Player) {
            final Player player = (Player) rootCause;
            final Material material = event.getEffectiveMaterial();

            if (!hasBuildPermission(player, "block." + material.name().toLowerCase() + ".place")
                    && !hasBuildPermission(player, "block.place." + material.name().toLowerCase())) {
                tellErrorMessage(player, event.getWorld());
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBreakBlock(final BreakBlockEvent event) {
        if (!getWorldConfig(event.getWorld()).buildPermissions) return;

        Object rootCause = event.getCause().getRootCause();

        if (rootCause instanceof Player) {
            final Player player = (Player) rootCause;
            final Material material = event.getEffectiveMaterial();

            if (!hasBuildPermission(player, "block." + material.name().toLowerCase() + ".remove")
                    && !hasBuildPermission(player, "block.remove." + material.name().toLowerCase())) {
                tellErrorMessage(player, event.getWorld());
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onUseBlock(final UseBlockEvent event) {
        if (!getWorldConfig(event.getWorld()).buildPermissions) return;

        Object rootCause = event.getCause().getRootCause();

        if (rootCause instanceof Player) {
            final Player player = (Player) rootCause;
            final Material material = event.getEffectiveMaterial();

            if (!hasBuildPermission(player, "block." + material.name().toLowerCase() + ".interact")
                    && !hasBuildPermission(player, "block.interact." + material.name().toLowerCase())) {
                tellErrorMessage(player, event.getWorld());
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onSpawnEntity(SpawnEntityEvent event) {
        if (!getWorldConfig(event.getWorld()).buildPermissions) return;

        Object rootCause = event.getCause().getRootCause();

        if (rootCause instanceof Player) {
            final Player player = (Player) rootCause;
            final EntityType type = event.getEffectiveType();

            if (!hasBuildPermission(player, "entity." + type.name().toLowerCase() + ".place")
                    && !hasBuildPermission(player, "entity.place." + type.name().toLowerCase())) {
                tellErrorMessage(player, event.getWorld());
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onDestroyEntity(DestroyEntityEvent event) {
        if (!getWorldConfig(event.getWorld()).buildPermissions) return;

        Object rootCause = event.getCause().getRootCause();

        if (rootCause instanceof Player) {
            final Player player = (Player) rootCause;
            final EntityType type = event.getEntity().getType();

            if (!hasBuildPermission(player, "entity." + type.name().toLowerCase() + ".remove")
                    && !hasBuildPermission(player, "entity.remove." + type.name().toLowerCase())) {
                tellErrorMessage(player, event.getWorld());
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onUseEntity(UseEntityEvent event) {
        if (!getWorldConfig(event.getWorld()).buildPermissions) return;

        Object rootCause = event.getCause().getRootCause();

        if (rootCause instanceof Player) {
            final Player player = (Player) rootCause;
            final EntityType type = event.getEntity().getType();

            if (!hasBuildPermission(player, "entity." + type.name().toLowerCase() + ".interact")
                    && !hasBuildPermission(player, "entity.interact." + type.name().toLowerCase())) {
                tellErrorMessage(player, event.getWorld());
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onDamageEntity(DamageEntityEvent event) {
        if (!getWorldConfig(event.getWorld()).buildPermissions) return;

        Object rootCause = event.getCause().getRootCause();

        if (rootCause instanceof Player) {
            final Player player = (Player) rootCause;
            final EntityType type = event.getEntity().getType();

            if (!hasBuildPermission(player, "entity." + type.name().toLowerCase() + ".damage")
                    && !hasBuildPermission(player, "entity.damage." + type.name().toLowerCase())) {
                tellErrorMessage(player, event.getWorld());
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onUseItem(UseItemEvent event) {
        if (!getWorldConfig(event.getWorld()).buildPermissions) return;

        Object rootCause = event.getCause().getRootCause();

        if (rootCause instanceof Player) {
            Player player = (Player) rootCause;
            Material material = event.getItemStack().getType();

            if (material.isBlock()) {
                return;
            }

            if (!hasBuildPermission(player, "item." + material.name().toLowerCase() + ".use")
                    && !hasBuildPermission(player, "item.use." + material.name().toLowerCase())) {
                tellErrorMessage(player, event.getWorld());
                event.setCancelled(true);
            }
        }
    }

}
