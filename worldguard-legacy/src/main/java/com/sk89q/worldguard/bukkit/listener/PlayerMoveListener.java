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
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.session.MoveType;
import com.sk89q.worldguard.session.Session;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.util.Vector;

public class PlayerMoveListener implements Listener {

    private final WorldGuardPlugin plugin;

    public PlayerMoveListener(WorldGuardPlugin plugin) {
        this.plugin = plugin;
    }

    public void registerEvents() {
        if (WorldGuard.getInstance().getPlatform().getGlobalStateManager().usePlayerMove) {
            PluginManager pm = plugin.getServer().getPluginManager();
            pm.registerEvents(this, plugin);
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        LocalPlayer player = plugin.wrapPlayer(event.getPlayer());

        Session session = WorldGuard.getInstance().getPlatform().getSessionManager().get(player);
        session.testMoveTo(player, BukkitAdapter.adapt(event.getRespawnLocation()), MoveType.RESPAWN, true);
    }

    @EventHandler
    public void onVehicleEnter(VehicleEnterEvent event) {
        Entity entity = event.getEntered();
        if (entity instanceof Player) {
            LocalPlayer player = plugin.wrapPlayer((Player) entity);
            Session session = WorldGuard.getInstance().getPlatform().getSessionManager().get(player);
            if (null != session.testMoveTo(player, BukkitAdapter.adapt(event.getVehicle().getLocation()), MoveType.EMBARK, true)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerMove(PlayerMoveEvent event) {
        final Player player = event.getPlayer();
        LocalPlayer localPlayer = plugin.wrapPlayer(player);

        Session session = WorldGuard.getInstance().getPlatform().getSessionManager().get(localPlayer);
        com.sk89q.worldedit.util.Location weLocation = session.testMoveTo(localPlayer, BukkitAdapter.adapt(event.getTo()), MoveType.MOVE);

        if (weLocation != null) {
            final Location override = BukkitAdapter.adapt(weLocation);
            override.setX(override.getBlockX() + 0.5);
            override.setY(override.getBlockY());
            override.setZ(override.getBlockZ() + 0.5);
            override.setPitch(event.getTo().getPitch());
            override.setYaw(event.getTo().getYaw());

            event.setTo(override.clone());

            Entity vehicle = player.getVehicle();
            if (vehicle != null) {
                vehicle.eject();

                Entity current = vehicle;
                while (current != null) {
                    current.eject();
                    vehicle.setVelocity(new Vector());
                    if (vehicle instanceof LivingEntity) {
                        vehicle.teleport(override.clone());
                    } else {
                        vehicle.teleport(override.clone().add(0, 1, 0));
                    }
                    current = current.getVehicle();
                }

                player.teleport(override.clone().add(0, 1, 0));

                Bukkit.getScheduler().runTaskLater(plugin, () -> player.teleport(override.clone().add(0, 1, 0)), 1);
            }
        }
    }

}
