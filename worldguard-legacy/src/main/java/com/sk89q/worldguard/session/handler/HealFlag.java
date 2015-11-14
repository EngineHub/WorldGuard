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

package com.sk89q.worldguard.session.handler;

import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.session.Session;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

public class HealFlag extends Handler {

    private long lastHeal = 0;

    public HealFlag(Session session) {
        super(session);
    }

    @Override
    public void tick(Player player, ApplicableRegionSet set) {
        LocalPlayer localPlayer = getPlugin().wrapPlayer(player);

        if (player.getHealth() <= 0) {
            return;
        }

        long now = System.currentTimeMillis();

        Integer healAmount = set.queryValue(localPlayer, DefaultFlag.HEAL_AMOUNT);
        Integer healDelay = set.queryValue(localPlayer, DefaultFlag.HEAL_DELAY);
        Double minHealth = set.queryValue(localPlayer, DefaultFlag.MIN_HEAL);
        Double maxHealth = set.queryValue(localPlayer, DefaultFlag.MAX_HEAL);

        if (healAmount == null || healDelay == null || healAmount == 0 || healDelay < 0) {
            return;
        }
        if (healAmount < 0
                && (getSession().isInvincible(player)
                || (player.getGameMode() != GameMode.SURVIVAL && player.getGameMode() != GameMode.ADVENTURE))) {
            // don't damage invincible players
            return;
        }
        if (minHealth == null) {
            minHealth = 0.0;
        }
        if (maxHealth == null) {
            maxHealth = player.getMaxHealth();
        }

        // Apply a cap to prevent possible exceptions
        minHealth = Math.min(player.getMaxHealth(), minHealth);
        maxHealth = Math.min(player.getMaxHealth(), maxHealth);

        if (player.getHealth() >= maxHealth && healAmount > 0) {
            return;
        }

        if (healDelay <= 0) {
            player.setHealth(healAmount > 0 ? maxHealth : minHealth); // this will insta-kill if the flag is unset
            lastHeal = now;
        } else if (now - lastHeal > healDelay * 1000) {
            // clamp health between minimum and maximum
            player.setHealth(Math.min(maxHealth, Math.max(minHealth, player.getHealth() + healAmount)));
            lastHeal = now;
        }
    }

}
