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
import com.sk89q.worldguard.bukkit.ConfigurationManager;
import com.sk89q.worldguard.bukkit.RegionQuery;
import com.sk89q.worldguard.bukkit.WorldConfiguration;
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
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.association.Associables;
import com.sk89q.worldguard.protection.association.RegionAssociable;
import com.sk89q.worldguard.protection.association.RegionOverlapAssociation;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import javax.annotation.Nullable;

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
     * @param subject the subject that the sender was blocked from touching
     */
    private void tellErrorMessage(Cause cause, Object subject) {
        Object rootCause = cause.getRootCause();

        if (rootCause instanceof Player) {
            ((Player) rootCause).sendMessage(ChatColor.DARK_RED + "Sorry, but you are not allowed to do that here.");
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

    /**
     * Create a new predicate to test a state flag for each location.
     *
     * @param query the query
     * @param flag the flag
     * @return a predicate
     */
    private Predicate<Location> createStateTest(final RegionQuery query, final StateFlag flag) {
        return new Predicate<Location>() {
            @Override
            public boolean apply(@Nullable Location location) {
                return query.testState(location, null, flag);
            }
        };
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
        if (isWhitelisted(event.getCause())) {
            return; // Whitelisted cause
        }

        final Material type = event.getEffectiveMaterial();
        final RegionQuery query = getPlugin().getRegionContainer().createQuery();
        final RegionAssociable associable = createRegionAssociable(event.getCause());

        event.filterBlocks(new Predicate<Location>() {
            @Override
            public boolean apply(Location target) {
                boolean canPlace;

                // Flint and steel, fire charge
                if (type == Material.FIRE) {
                    canPlace = query.testBuild(target, associable, DefaultFlag.LIGHTER);

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
        if (isWhitelisted(event.getCause())) {
            return; // Whitelisted cause
        }

        ConfigurationManager globalConfig = getPlugin().getGlobalStateManager();
        WorldConfiguration config = globalConfig.get(event.getWorld());
        final RegionQuery query = getPlugin().getRegionContainer().createQuery();

        // TODO: Move this to another event handler
        Entity entity;
        if ((entity = event.getCause().getEntityRootCause()) != null) {
            // Creeper
            if (entity instanceof Creeper) {
                event.filterBlocks(createStateTest(query, DefaultFlag.CREEPER_EXPLOSION), config.explosionFlagCancellation);

            // Enderdragon
            } else if (entity instanceof EnderDragon) {
                event.filterBlocks(createStateTest(query, DefaultFlag.ENDERDRAGON_BLOCK_DAMAGE), config.explosionFlagCancellation);

            // TNT + explosive TNT carts
            } else if (Entities.isTNTBased(entity)) {
                event.filterBlocks(createStateTest(query, DefaultFlag.TNT), config.explosionFlagCancellation);

            }
        }

        if (!event.isCancelled()) {
            final RegionAssociable associable = createRegionAssociable(event.getCause());

            event.filterBlocks(new Predicate<Location>() {
                @Override
                public boolean apply(Location target) {
                    boolean canBreak = query.testBuild(target, associable);

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
        if (isWhitelisted(event.getCause())) {
            return; // Whitelisted cause
        }

        final Material type = event.getEffectiveMaterial();
        final RegionQuery query = getPlugin().getRegionContainer().createQuery();
        final RegionAssociable associable = createRegionAssociable(event.getCause());

        event.filterBlocks(new Predicate<Location>() {
            @Override
            public boolean apply(Location target) {
                boolean canUse;

                // Inventory blocks (CHEST_ACCESS)
                if (Materials.isInventoryBlock(type)) {
                    canUse = query.testBuild(target, associable, DefaultFlag.USE, DefaultFlag.CHEST_ACCESS);

                // Beds (SLEEP)
                } else if (type == Material.BED) {
                    canUse = query.testBuild(target, associable, DefaultFlag.USE, DefaultFlag.SLEEP);

                // TNT (TNT)
                } else if (type == Material.TNT) {
                    canUse = query.testBuild(target, associable, DefaultFlag.TNT);

                // Everything else
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
        if (isWhitelisted(event.getCause())) {
            return; // Whitelisted cause
        }

        Location target = event.getTarget();
        EntityType type = event.getEffectiveType();

        RegionQuery query = getPlugin().getRegionContainer().createQuery();
        RegionAssociable associable = createRegionAssociable(event.getCause());

        boolean canSpawn;

        if (Entities.isVehicle(type)) {
            canSpawn = query.testBuild(target, associable, DefaultFlag.PLACE_VEHICLE);
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
        if (isWhitelisted(event.getCause())) {
            return; // Whitelisted cause
        }

        Location target = event.getTarget();
        EntityType type = event.getEntity().getType();
        RegionAssociable associable = createRegionAssociable(event.getCause());

        RegionQuery query = getPlugin().getRegionContainer().createQuery();
        boolean canDestroy;

        if (Entities.isVehicle(type)) {
            canDestroy = query.testBuild(target, associable, DefaultFlag.DESTROY_VEHICLE);
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
        if (isWhitelisted(event.getCause())) {
            return; // Whitelisted cause
        }

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
