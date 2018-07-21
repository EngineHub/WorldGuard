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
import com.sk89q.worldguard.bukkit.BukkitWorldConfiguration;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.bukkit.event.block.BreakBlockEvent;
import com.sk89q.worldguard.bukkit.event.block.PlaceBlockEvent;
import com.sk89q.worldguard.bukkit.util.Entities;
import com.sk89q.worldguard.bukkit.util.Materials;
import com.sk89q.worldguard.protection.association.RegionAssociable;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import java.util.function.Predicate;

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
        com.sk89q.worldedit.world.World weWorld = BukkitAdapter.adapt(event.getWorld());
        if (!isRegionSupportEnabled(weWorld)) return; // Region support disabled

        RegionQuery query = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery();

        Block block;
        if ((block = event.getCause().getFirstBlock()) != null) {
            if (Materials.isPistonBlock(block.getType())) {
                event.filter(testState(query, Flags.PISTONS), false);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBreakBlock(final BreakBlockEvent event) {
        com.sk89q.worldedit.world.World weWorld = BukkitAdapter.adapt(event.getWorld());
        if (!isRegionSupportEnabled(weWorld)) return; // Region support disabled

        BukkitWorldConfiguration config = getWorldConfig(weWorld);
        RegionQuery query = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery();

        Block block;
        if ((block = event.getCause().getFirstBlock()) != null) {
            if (Materials.isPistonBlock(block.getType())) {
                event.filter(testState(query, Flags.PISTONS), false);
            }
        }

        if (event.getCause().find(EntityType.CREEPER) != null) { // Creeper
            event.filter(testState(query, Flags.CREEPER_EXPLOSION), config.explosionFlagCancellation);
        }

        if (event.getCause().find(EntityType.ENDER_DRAGON) != null) { // Enderdragon
            event.filter(testState(query, Flags.ENDERDRAGON_BLOCK_DAMAGE), config.explosionFlagCancellation);
        }

        if (event.getCause().find(Entities.enderCrystalType) != null) { // should be nullsafe even if enderCrystalType field is null
            event.filter(testState(query, Flags.OTHER_EXPLOSION), config.explosionFlagCancellation);
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        Entity entity = event.getEntity();
        World world = entity.getWorld();

        if (!isRegionSupportEnabled(BukkitAdapter.adapt(world))) return; // Region support disabled
        RegionQuery query = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery();

        if (entity instanceof Player && event.getCause() == DamageCause.FALL) {
            LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer((Player) entity);
            if (!query.testState(BukkitAdapter.adapt(entity.getLocation()), localPlayer, Flags.FALL_DAMAGE)) {
                event.setCancelled(true);
                return;
            }
        } else {
            try {
                if (entity instanceof Player && event.getCause() == DamageCause.FLY_INTO_WALL) {
                    LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer((Player) entity);
                    if (!query.testState(BukkitAdapter.adapt(entity.getLocation()), localPlayer, Flags.FALL_DAMAGE)) {
                        event.setCancelled(true);
                        return;
                    }
                }
            } catch (NoSuchFieldError ignored) {
            }
        }

        if (event instanceof EntityDamageByEntityEvent) {
            Entity damager = (((EntityDamageByEntityEvent) event)).getDamager();
            if (damager != null && damager.getType() == EntityType.FIREWORK) {
                if (!query.testState(BukkitAdapter.adapt(entity.getLocation()), (RegionAssociable) null, Flags.FIREWORK_DAMAGE)) {
                    event.setCancelled(true);
                    return;
                }
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
        return location -> query.testState(BukkitAdapter.adapt(location), (RegionAssociable) null, flag);
    }


}
