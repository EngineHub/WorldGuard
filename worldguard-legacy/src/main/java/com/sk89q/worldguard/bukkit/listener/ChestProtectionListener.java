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

import com.google.common.base.Predicate;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldguard.bukkit.WorldConfiguration;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.bukkit.event.DelegateEvent;
import com.sk89q.worldguard.bukkit.event.block.BreakBlockEvent;
import com.sk89q.worldguard.bukkit.event.block.PlaceBlockEvent;
import com.sk89q.worldguard.bukkit.event.block.UseBlockEvent;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.SignChangeEvent;

/**
 * Handle events that need to be processed by the chest protection.
 */
public class ChestProtectionListener extends AbstractListener {

    /**
     * Construct the listener.
     *
     * @param plugin an instance of WorldGuardPlugin
     */
    public ChestProtectionListener(WorldGuardPlugin plugin) {
        super(plugin);
    }

    private void sendMessage(DelegateEvent event, Player player, String message) {
        if (!event.isSilent()) {
            player.sendMessage(message);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlaceBlock(final PlaceBlockEvent event) {
        final Player player = event.getCause().getFirstPlayer();

        if (player != null) {
            final WorldConfiguration wcfg = getWorldConfig(player);

            // Early guard
            if (!wcfg.signChestProtection) {
                return;
            }

            event.filter(new Predicate<Location>() {
                @Override
                public boolean apply(Location target) {
                    if (wcfg.getChestProtection().isChest(event.getEffectiveMaterial().getId()) && wcfg.isChestProtected(target.getBlock(), player)) {
                        sendMessage(event, player, ChatColor.DARK_RED + "Это место для сундука, для которого у Вас нет разрешения.");
                        return false;
                    }

                    return true;
                }
            }, true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBreakBlock(final BreakBlockEvent event) {
        final Player player = event.getCause().getFirstPlayer();

        final WorldConfiguration wcfg = getWorldConfig(event.getWorld());

        // Early guard
        if (!wcfg.signChestProtection) {
            return;
        }

        if (player != null) {
            event.filter(new Predicate<Location>() {
                @Override
                public boolean apply(Location target) {
                    if (wcfg.isChestProtected(target.getBlock(), player)) {
                        sendMessage(event, player, ChatColor.DARK_RED + "Этот сундук защищен.");
                        return false;
                    }

                    return true;
                }
            }, true);
        } else {
            event.filter(new Predicate<Location>() {
                @Override
                public boolean apply(Location target) {
                    return !wcfg.isChestProtected(target.getBlock());

                }
            });
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onUseBlock(final UseBlockEvent event) {
        final Player player = event.getCause().getFirstPlayer();

        final WorldConfiguration wcfg = getWorldConfig(event.getWorld());

        // Early guard
        if (!wcfg.signChestProtection) {
            return;
        }

        if (player != null) {
            event.filter(new Predicate<Location>() {
                @Override
                public boolean apply(Location target) {
                    if (wcfg.isChestProtected(target.getBlock(), player)) {
                        sendMessage(event, player, ChatColor.DARK_RED + "Этот сундук защищен.");
                        return false;
                    }

                    return true;
                }
            }, true);
        } else {
            event.filter(new Predicate<Location>() {
                @Override
                public boolean apply(Location target) {
                    return !wcfg.isChestProtected(target.getBlock());

                }
            });
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onSignChange(SignChangeEvent event) {
        Player player = event.getPlayer();
        WorldConfiguration wcfg = getWorldConfig(player);

        if (wcfg.signChestProtection) {
            if (event.getLine(0).equalsIgnoreCase("[Lock]")) {
                if (wcfg.isChestProtectedPlacement(event.getBlock(), player)) {
                    player.sendMessage(ChatColor.DARK_RED + "Вы не являетесь владельцем сундука.");
                    event.getBlock().breakNaturally();
                    event.setCancelled(true);
                    return;
                }

                if (event.getBlock().getTypeId() != BlockID.SIGN_POST) {
                    player.sendMessage(ChatColor.RED
                            + "Табличка [Lock] далжна стоять на земле, а не висеть.");

                    event.getBlock().breakNaturally();
                    event.setCancelled(true);
                    return;
                }

                if (!event.getLine(1).equalsIgnoreCase(player.getName())) {
                    player.sendMessage(ChatColor.RED
                            + "В первой строке должен быть ваш ник.");

                    event.getBlock().breakNaturally();
                    event.setCancelled(true);
                    return;
                }

                int below = event.getBlock().getRelative(0, -1, 0).getTypeId();

                if (below == BlockID.TNT || below == BlockID.SAND
                        || below == BlockID.GRAVEL || below == BlockID.SIGN_POST) {
                    player.sendMessage(ChatColor.RED
                            + "На этот тип блока лучше не ставить табличку.");

                    event.getBlock().breakNaturally();
                    event.setCancelled(true);
                    return;
                }

                event.setLine(0, "[Lock]");
                player.sendMessage(ChatColor.YELLOW
                        + "Сундук или двойной сундук теперь защищены.");
            }
        } else if (!wcfg.disableSignChestProtectionCheck) {
            if (event.getLine(0).equalsIgnoreCase("[Lock]")) {
                player.sendMessage(ChatColor.RED
                        + "WorldGuard: Защита сундуков с помощью табличек отключена.");

                event.getBlock().breakNaturally();
                event.setCancelled(true);
            }
        }
    }

}
