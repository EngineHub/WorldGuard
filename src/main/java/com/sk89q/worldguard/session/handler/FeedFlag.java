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

import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.session.Session;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

public class FeedFlag extends Handler {

    private long lastFeed = 0;

    public FeedFlag(Session session) {
        super(session);
    }

    @Override
    public void tick(Player player, ApplicableRegionSet set) {
        if (!getSession().isInvincible(player) && player.getGameMode() != GameMode.CREATIVE) {
            long now = System.currentTimeMillis();

            Integer feedAmount = set.getFlag(DefaultFlag.FEED_AMOUNT);
            Integer feedDelay = set.getFlag(DefaultFlag.FEED_DELAY);
            Integer minHunger = set.getFlag(DefaultFlag.MIN_FOOD);
            Integer maxHunger = set.getFlag(DefaultFlag.MAX_FOOD);

            if (feedAmount == null || feedDelay == null || feedAmount == 0 || feedDelay < 0) {
                return;
            }
            if (minHunger == null) {
                minHunger = 0;
            }
            if (maxHunger == null) {
                maxHunger = 20;
            }

            // Apply a cap to prevent possible exceptions
            minHunger = Math.min(20, minHunger);
            maxHunger = Math.min(20, maxHunger);

            if (player.getFoodLevel() >= maxHunger && feedAmount > 0) {
                return;
            }

            if (feedDelay <= 0) {
                player.setFoodLevel(feedAmount > 0 ? maxHunger : minHunger);
                player.setSaturation(player.getFoodLevel());
                lastFeed = now;
            } else if (now - lastFeed > feedDelay * 1000) {
                // clamp health between minimum and maximum
                player.setFoodLevel(Math.min(maxHunger, Math.max(minHunger, player.getFoodLevel() + feedAmount)));
                player.setSaturation(player.getFoodLevel());
                lastFeed = now;
            }
        }
    }

}
