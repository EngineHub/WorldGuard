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

import com.sk89q.worldguard.bukkit.ConfigurationManager;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.bukkit.event.player.ProcessPlayerEvent;
import com.sk89q.worldguard.session.Session;
import com.sk89q.worldguard.session.handler.GodMode;
import com.sk89q.worldguard.session.handler.WaterBreathing;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import java.util.logging.Level;
import java.util.logging.Logger;

public class PlayerModesListener extends AbstractListener {

    private static final Logger log = Logger.getLogger(PlayerModesListener.class.getCanonicalName());

    private static final String INVINCIBLE_PERMISSION = "worldguard.auto-invincible";
    private static final String INVINCIBLE_GROUP = "wg-invincible";
    private static final String AMPHIBIOUS_GROUP = "wg-amphibious";

    /**
     * Construct the listener.
     *
     * @param plugin an instance of WorldGuardPlugin
     */
    public PlayerModesListener(WorldGuardPlugin plugin) {
        super(plugin);
    }

    private boolean hasGodModeGroup(Player player) {
        return getConfig().useGodGroup && getPlugin().inGroup(player, INVINCIBLE_GROUP);
    }

    private boolean hasGodModePermission(Player player) {
        return getConfig().useGodPermission && getPlugin().hasPermission(player, INVINCIBLE_PERMISSION);
    }

    private boolean hasAmphibiousGroup(Player player) {
        return getConfig().useAmphibiousGroup && getPlugin().inGroup(player, AMPHIBIOUS_GROUP);
    }

    @EventHandler
    public void onProcessPlayer(ProcessPlayerEvent event) {
        ConfigurationManager config = getConfig();
        Player player = event.getPlayer();
        Session session = getPlugin().getSessionManager().get(player);

        if (hasGodModeGroup(player) || hasGodModePermission(player)) {
            if (GodMode.set(player, session, true)) {
                log.log(Level.INFO, "Enabled auto-god mode for " + player.getName());
            }
        }

        if (hasAmphibiousGroup(player)) {
            if (WaterBreathing.set(player, session, true)) {
                log.log(Level.INFO, "Enabled water breathing mode for " + player.getName() + " (player is in group 'wg-amphibious')");
            }
        }
    }

}
