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

package com.sk89q.worldguard.bukkit.listener.module;

import com.sk89q.worldguard.bukkit.WorldConfiguration;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExpEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ExpBottleEvent;

import java.util.function.BiPredicate;

public class XPDropListener implements Listener {

    private final BiPredicate<Location, Integer> predicate;

    public XPDropListener(BiPredicate<Location, Integer> predicate) {
        this.predicate = predicate;
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockExp(BlockExpEvent event) {
        if (predicate.test(event.getBlock().getLocation(), event.getExpToDrop())) {
            event.setExpToDrop(0);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onExpBottle(ExpBottleEvent event) {
        if (predicate.test(event.getEntity().getLocation(), event.getExperience())) {
            event.setExperience(0);
            // event.setShowEffect(false); // don't want to cancel the bottle entirely I suppose, just the exp
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        if (predicate.test(event.getEntity().getLocation(), event.getDroppedExp())) {
            event.setDroppedExp(0);
        }
    }

}
