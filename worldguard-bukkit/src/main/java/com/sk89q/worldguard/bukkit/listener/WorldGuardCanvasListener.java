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
import com.sk89q.worldguard.config.ConfigurationManager;
import com.sk89q.worldguard.config.WorldConfiguration;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import io.canvasmc.canvas.event.EntityTeleportAsyncEvent;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;

/**
 * Re-implements the ENDERPEARL/CHORUS_TELEPORT flag checks using Canvas's own
 * {@link EntityTeleportAsyncEvent} instead of the vanilla
 * {@link org.bukkit.event.player.PlayerTeleportEvent}.
 *
 * <p>Canvas (a Folia fork) documents that the vanilla event "doesn't function" for
 * entity-driven teleports under region threading, and added this event as a
 * replacement rather than fixing the old API. This listener is only registered when
 * {@link WorldGuardPlugin#isCanvas()} is true, so it has no effect on other platforms.</p>
 *
 * @see <a href="https://docs.canvasmc.io/canvas/developers/api/events/">Canvas events docs</a>
 */
public class WorldGuardCanvasListener extends AbstractListener {

    public WorldGuardCanvasListener(WorldGuardPlugin plugin) {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onEntityTeleportAsync(EntityTeleportAsyncEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        if (com.sk89q.worldguard.bukkit.util.Entities.isNPC(player)) {
            return;
        }

        TeleportCause cause = event.getCause();
        StateFlag flag;
        if (cause == TeleportCause.ENDER_PEARL) {
            flag = Flags.ENDERPEARL;
        } else if (cause == TeleportCause.CHORUS_FRUIT) {
            flag = Flags.CHORUS_TELEPORT;
        } else {
            return;
        }

        LocalPlayer localPlayer = getPlugin().wrapPlayer(player);
        ConfigurationManager cfg = getConfig();
        WorldConfiguration wcfg = getWorldConfig(player.getWorld());

        if (!wcfg.useRegions || !cfg.usePlayerTeleports) {
            return;
        }
        if (WorldGuard.getInstance().getPlatform().getSessionManager().hasBypass(localPlayer, localPlayer.getWorld())) {
            return;
        }

        RegionQuery query = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery();
        ApplicableRegionSet setFrom = query.getApplicableRegions(BukkitAdapter.adapt(event.getFrom()));
        ApplicableRegionSet setTo = query.getApplicableRegions(BukkitAdapter.adapt(event.getTo()));

        boolean cancel = false;
        String message = null;
        if (!setFrom.testState(localPlayer, flag)) {
            cancel = true;
            message = setFrom.queryValue(localPlayer, Flags.EXIT_DENY_MESSAGE);
        } else if (!setTo.testState(localPlayer, flag)) {
            cancel = true;
            message = setTo.queryValue(localPlayer, Flags.ENTRY_DENY_MESSAGE);
        }

        if (cancel) {
            if (message != null && !message.isEmpty()) {
                player.sendMessage(message);
            }
            event.setCancelled(true);
            // The pearl/fruit is consumed before this event fires, so give it back.
            if (player.getGameMode() != GameMode.CREATIVE) {
                Material refund = cause == TeleportCause.ENDER_PEARL ? Material.ENDER_PEARL : Material.CHORUS_FRUIT;
                player.getInventory().addItem(new ItemStack(refund, 1));
            }
        }
    }

}
