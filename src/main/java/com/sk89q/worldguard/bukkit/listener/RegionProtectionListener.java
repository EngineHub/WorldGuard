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
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.bukkit.cause.Cause;
import com.sk89q.worldguard.bukkit.event.block.BreakBlockEvent;
import com.sk89q.worldguard.bukkit.event.block.PlaceBlockEvent;
import com.sk89q.worldguard.bukkit.event.block.UseBlockEvent;
import com.sk89q.worldguard.bukkit.event.entity.DestroyEntityEvent;
import com.sk89q.worldguard.bukkit.event.entity.SpawnEntityEvent;
import com.sk89q.worldguard.bukkit.event.entity.UseEntityEvent;
import com.sk89q.worldguard.bukkit.util.DelayedRegionOverlapAssociation;
import com.sk89q.worldguard.bukkit.util.Entities;
import com.sk89q.worldguard.bukkit.util.Materials;
import com.sk89q.worldguard.domains.Association;
import com.sk89q.worldguard.protection.association.Associables;
import com.sk89q.worldguard.protection.association.RegionAssociable;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

/**
 * Handle events that need to be processed by region protection.
 */
public class RegionProtectionListener extends AbstractListener {

    /**
     * Construct the listener.
     *
     * @param plugin an instance of WorldGuardPlugin
     */
    public RegionProtectionListener(WorldGuardPlugin plugin) {
        super(plugin);
    }

    /**
     * Tell a sender that s/he cannot do something 'here'.
     *
     * @param cause the cause
     * @param location the location
     */
    private void tellErrorMessage(Cause cause, Location location) {
        Object rootCause = cause.getRootCause();

        if (rootCause instanceof Player) {
            RegionQuery query = getPlugin().getRegionContainer().createQuery();
            Player player = (Player) rootCause;
            player.sendMessage(query.queryValue(location, player, DefaultFlag.DENY_MESSAGE));
        }
    }

    /**
     * Return whether the given cause is whitelist (should be ignored).
     *
     * @param cause the cause
     * @return true if whitelisted
     */
    private boolean isWhitelisted(Cause cause) {
        return false;
    }

    private RegionAssociable createRegionAssociable(Cause cause) {
        Object rootCause = cause.getRootCause();

        if (rootCause instanceof Player) {
            return getPlugin().wrapPlayer((Player) rootCause);
        } else if (rootCause instanceof Entity) {
            RegionQuery query = getPlugin().getRegionContainer().createQuery();
            return new DelayedRegionOverlapAssociation(query, ((Entity) rootCause).getLocation());
        } else if (rootCause instanceof Block) {
            RegionQuery query = getPlugin().getRegionContainer().createQuery();
            return new DelayedRegionOverlapAssociation(query, ((Block) rootCause).getLocation());
        } else {
            return Associables.constant(Association.NON_MEMBER);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlaceBlock(final PlaceBlockEvent event) {
        if (!isRegionSupportEnabled(event.getWorld())) return; // Region support disabled
        if (isWhitelisted(event.getCause())) return; // Whitelisted cause

        final Material type = event.getEffectiveMaterial();
        final RegionQuery query = getPlugin().getRegionContainer().createQuery();
        final RegionAssociable associable = createRegionAssociable(event.getCause());

        event.filter(new Predicate<Location>() {
            @Override
            public boolean apply(Location target) {
                boolean canPlace;

                /* Flint and steel, fire charge, etc. */
                if (type == Material.FIRE) {
                    canPlace = query.testBuild(target, associable, DefaultFlag.LIGHTER);

                /* Everything else */
                } else {
                    canPlace = query.testBuild(target, associable);
                }

                if (!canPlace) {
                    tellErrorMessage(event.getCause(), target);
                    return false;
                }

                return true;
            }
        });
    }

    @EventHandler(ignoreCancelled = true)
    public void onBreakBlock(final BreakBlockEvent event) {
        if (!isRegionSupportEnabled(event.getWorld())) return; // Region support disabled
        if (isWhitelisted(event.getCause())) return; // Whitelisted cause

        final RegionQuery query = getPlugin().getRegionContainer().createQuery();

        if (!event.isCancelled()) {
            final RegionAssociable associable = createRegionAssociable(event.getCause());

            event.filter(new Predicate<Location>() {
                @Override
                public boolean apply(Location target) {
                    boolean canBreak;

                    /* TNT */
                    if (event.getCause().find(EntityType.PRIMED_TNT, EntityType.PRIMED_TNT) != null) {
                        canBreak = query.testBuild(target, associable, DefaultFlag.TNT);

                    /* Everything else */
                    } else {
                        canBreak = query.testBuild(target, associable);
                    }

                    if (!canBreak) {
                        tellErrorMessage(event.getCause(), target);
                        return false;
                    }

                    return true;
                }
            });
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onUseBlock(final UseBlockEvent event) {
        if (!isRegionSupportEnabled(event.getWorld())) return; // Region support disabled
        if (isWhitelisted(event.getCause())) return; // Whitelisted cause

        final Material type = event.getEffectiveMaterial();
        final RegionQuery query = getPlugin().getRegionContainer().createQuery();
        final RegionAssociable associable = createRegionAssociable(event.getCause());

        event.filter(new Predicate<Location>() {
            @Override
            public boolean apply(Location target) {
                boolean canUse;

                /* Inventory */
                if (Materials.isInventoryBlock(type)) {
                    canUse = query.testBuild(target, associable, DefaultFlag.USE, DefaultFlag.CHEST_ACCESS);

                /* Beds */
                } else if (type == Material.BED) {
                    canUse = query.testBuild(target, associable, DefaultFlag.USE, DefaultFlag.SLEEP);

                /* TNT */
                } else if (type == Material.TNT) {
                    canUse = query.testBuild(target, associable, DefaultFlag.TNT);

                /* Everything else */
                } else {
                    canUse = query.testBuild(target, associable, DefaultFlag.USE);
                }

                if (!canUse) {
                    tellErrorMessage(event.getCause(), target);
                    return false;
                }

                return true;
            }
        });
    }

    @EventHandler(ignoreCancelled = true)
    public void onSpawnEntity(SpawnEntityEvent event) {
        if (!isRegionSupportEnabled(event.getWorld())) return; // Region support disabled
        if (isWhitelisted(event.getCause())) return; // Whitelisted cause

        Location target = event.getTarget();
        EntityType type = event.getEffectiveType();

        RegionQuery query = getPlugin().getRegionContainer().createQuery();
        RegionAssociable associable = createRegionAssociable(event.getCause());

        boolean canSpawn;

        /* Vehicles */
        if (Entities.isVehicle(type)) {
            canSpawn = query.testBuild(target, associable, DefaultFlag.PLACE_VEHICLE);

        /* Everything else */
        } else {
            canSpawn = query.testBuild(target, associable);
        }

        if (!canSpawn) {
            tellErrorMessage(event.getCause(), target);
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onDestroyEntity(DestroyEntityEvent event) {
        if (!isRegionSupportEnabled(event.getWorld())) return; // Region support disabled
        if (isWhitelisted(event.getCause())) return; // Whitelisted cause

        Location target = event.getTarget();
        EntityType type = event.getEntity().getType();
        RegionAssociable associable = createRegionAssociable(event.getCause());

        RegionQuery query = getPlugin().getRegionContainer().createQuery();
        boolean canDestroy;

        /* Vehicles */
        if (Entities.isVehicle(type)) {
            canDestroy = query.testBuild(target, associable, DefaultFlag.DESTROY_VEHICLE);

        /* Everything else */
        } else {
            canDestroy = query.testBuild(target, associable);
        }

        if (!canDestroy) {
            tellErrorMessage(event.getCause(), target);
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onUseEntity(UseEntityEvent event) {
        if (!isRegionSupportEnabled(event.getWorld())) return; // Region support disabled
        if (isWhitelisted(event.getCause())) return; // Whitelisted cause

        Location target = event.getTarget();
        RegionAssociable associable = createRegionAssociable(event.getCause());

        RegionQuery query = getPlugin().getRegionContainer().createQuery();
        boolean canUse = query.testBuild(target, associable, DefaultFlag.USE);

        if (!canUse) {
            tellErrorMessage(event.getCause(), target);
            event.setCancelled(true);
        }
    }

}
