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
import com.sk89q.worldguard.bukkit.RegionQuery;
import com.sk89q.worldguard.bukkit.WorldConfiguration;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.bukkit.event.block.BreakBlockEvent;
import com.sk89q.worldguard.bukkit.event.entity.SpawnEntityEvent;
import com.sk89q.worldguard.bukkit.util.Entities;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import org.bukkit.Location;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import javax.annotation.Nullable;

public class RegionFlagsListener extends AbstractListener {

    /**
     * Construct the listener.
     *
     * @param plugin an instance of WorldGuardPlugin
     */
    public RegionFlagsListener(WorldGuardPlugin plugin) {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBreakBlock(final BreakBlockEvent event) {
        WorldConfiguration config = getWorldConfig(event.getWorld());
        RegionQuery query = getPlugin().getRegionContainer().createQuery();

        Entity entity;
        if ((entity = event.getCause().getFirstEntity()) != null) {
            if (entity instanceof Creeper) { // Creeper
                event.filter(testState(query, DefaultFlag.CREEPER_EXPLOSION), config.explosionFlagCancellation);

            } else if (entity instanceof EnderDragon) { // Enderdragon
                event.filter(testState(query, DefaultFlag.ENDERDRAGON_BLOCK_DAMAGE), config.explosionFlagCancellation);

            } else if (Entities.isTNTBased(entity)) { // TNT + explosive TNT carts
                event.filter(testState(query, DefaultFlag.TNT), config.explosionFlagCancellation);

            }
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onSpawnEntity(final SpawnEntityEvent event) {
        RegionQuery query = getPlugin().getRegionContainer().createQuery();

        if (event.getEffectiveType() == EntityType.EXPERIENCE_ORB) {
            event.filter(testState(query, DefaultFlag.EXP_DROPS), false);
        }
    }

    /**
     * Create a new predicate to test a state flag for each location.
     *
     * @param query the query
     * @param flag the flag
     * @return a predicate
     */
    private Predicate<Location> testState(final RegionQuery query, final StateFlag flag) {
        return new Predicate<Location>() {
            @Override
            public boolean apply(@Nullable Location location) {
                return query.testState(location, null, flag);
            }
        };
    }


}
