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
import com.sk89q.worldguard.bukkit.event.block.PlaceBlockEvent;
import com.sk89q.worldguard.bukkit.util.Materials;
import com.sk89q.worldguard.protection.association.RegionAssociable;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

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
    public void onPlaceBlock(final PlaceBlockEvent event) {
        if (!isRegionSupportEnabled(event.getWorld())) return; // Region support disabled

        RegionQuery query = getPlugin().getRegionContainer().createQuery();

        Block block;
        if ((block = event.getCause().getFirstBlock()) != null) {
            // ================================================================
            // PISTONS flag
            // ================================================================

            if (Materials.isPistonBlock(block.getType())) {
                event.filter(testState(query, DefaultFlag.PISTONS), false);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBreakBlock(final BreakBlockEvent event) {
        if (!isRegionSupportEnabled(event.getWorld())) return; // Region support disabled

        WorldConfiguration config = getWorldConfig(event.getWorld());
        RegionQuery query = getPlugin().getRegionContainer().createQuery();

        Block block;
        if ((block = event.getCause().getFirstBlock()) != null) {
            // ================================================================
            // PISTONS flag
            // ================================================================

            if (Materials.isPistonBlock(block.getType())) {
                event.filter(testState(query, DefaultFlag.PISTONS), false);
            }
        }

        Entity entity;
        if ((entity = event.getCause().getFirstNonPlayerEntity()) != null) {
            // ================================================================
            // CREEPER_EXPLOSION flag
            // ================================================================

            if (entity instanceof Creeper) { // Creeper
                event.filter(testState(query, DefaultFlag.CREEPER_EXPLOSION), config.explosionFlagCancellation);
            }

            // ================================================================
            // ENDERDRAGON_BLOCK_DAMAGE flag
            // ================================================================

            if (entity instanceof EnderDragon) { // Enderdragon
                event.filter(testState(query, DefaultFlag.ENDERDRAGON_BLOCK_DAMAGE), config.explosionFlagCancellation);
            }

        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        Entity entity = event.getEntity();
        World world = entity.getWorld();

        if (!isRegionSupportEnabled(world)) return; // Region support disabled
        RegionQuery query = getPlugin().getRegionContainer().createQuery();

        if (entity instanceof Player && event.getCause() == DamageCause.FALL) {
            if (!query.testState(entity.getLocation(), (Player) entity, DefaultFlag.FALL_DAMAGE)) {
                event.setCancelled(true);
            }
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
                return query.testState(location, (RegionAssociable) null, flag);
            }
        };
    }


}
