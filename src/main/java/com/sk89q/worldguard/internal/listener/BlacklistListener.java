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

import com.sk89q.worldguard.blacklist.events.BlockBreakBlacklistEvent;
import com.sk89q.worldguard.blacklist.events.BlockPlaceBlacklistEvent;
import com.sk89q.worldguard.blacklist.events.DestroyWithBlacklistEvent;
import com.sk89q.worldguard.bukkit.WorldConfiguration;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.internal.cause.Causes;
import com.sk89q.worldguard.internal.event.BlockInteractEvent;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

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
    public void handleBlockInteract(BlockInteractEvent event) {
        Player player = Causes.getInvolvedPlayer(event.getCauses());
        Block target = event.getTarget();
        WorldConfiguration wcfg = getWorldConfig(player);

        // Blacklist guard
        if (wcfg.getBlacklist() == null) {
            return;
        }

        if (player != null) {
            switch (event.getAction()) {
                case BREAK:
                    if (!wcfg.getBlacklist().check(
                            new BlockBreakBlacklistEvent(getPlugin().wrapPlayer(player), toVector(target), target.getTypeId()), false, false)) {
                        event.setCancelled(true);
                        return;
                    }

                    if (!wcfg.getBlacklist().check(
                            new DestroyWithBlacklistEvent(getPlugin().wrapPlayer(player), toVector(target), player.getItemInHand().getTypeId()), false, false)) {
                        event.setCancelled(true);
                        return;
                    }

                    break;

                case PLACE:
                    if (!wcfg.getBlacklist().check(
                            new BlockPlaceBlacklistEvent(getPlugin().wrapPlayer(player), toVector(target), target.getTypeId()), false, false)) {
                        event.setCancelled(true);
                        return;
                    }

                    break;
            }
        }
    }

}
