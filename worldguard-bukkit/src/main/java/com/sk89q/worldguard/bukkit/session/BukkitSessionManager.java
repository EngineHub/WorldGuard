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

package com.sk89q.worldguard.bukkit.session;

import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.BukkitPlayer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.bukkit.event.player.ProcessPlayerEvent;
import com.sk89q.worldguard.bukkit.util.Entities;
import com.sk89q.worldguard.session.AbstractSessionManager;
import com.sk89q.worldguard.session.Session;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Collection;

/**
 * Keeps tracks of sessions and also does session-related handling
 * (flags, etc.).
 */
public class BukkitSessionManager extends AbstractSessionManager implements Runnable, Listener {

    /**
     * Re-initialize handlers and clear "last position," "last state," etc.
     * information for all players.
     */
    @Override
    public void resetAllStates() {
        Collection<? extends Player> players = Bukkit.getServer().getOnlinePlayers();
        for (Player player : players) {
            BukkitPlayer bukkitPlayer = new BukkitPlayer(WorldGuardPlugin.inst(), player);
            Session session = getIfPresent(bukkitPlayer);
            if (session != null) {
                session.resetState(bukkitPlayer);
            }
        }
    }

    @EventHandler
    public void onPlayerProcess(ProcessPlayerEvent event) {
        // Pre-load a session
        LocalPlayer player = WorldGuardPlugin.inst().wrapPlayer(event.getPlayer());
        get(player).initialize(player);
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);
            get(localPlayer).tick(localPlayer);
        }
    }

    @Override
    public boolean hasBypass(LocalPlayer player, World world) {
        if (player instanceof BukkitPlayer bukkitPlayer) {
            if (Entities.isNPC(bukkitPlayer.getPlayer())
                    && WorldGuard.getInstance().getPlatform().getGlobalStateManager().get(world).fakePlayerBuildOverride) {
                return true;
            }
            if (!((BukkitPlayer) player).getPlayer().isOnline()) {
                return false;
            }
        }
        return super.hasBypass(player, world);
    }

    public void shutdown() {
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);
            get(localPlayer).uninitialize(localPlayer);
        }
    }
}
