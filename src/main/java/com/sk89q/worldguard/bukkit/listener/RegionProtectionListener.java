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
import com.sk89q.worldguard.bukkit.util.Entities;
import com.sk89q.worldguard.bukkit.util.Materials;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
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
     * @param sender the sender
     * @param subject the subject that the sender was blocked from touching
     */
    private void tellErrorMessage(CommandSender sender, Object subject) {
        sender.sendMessage(ChatColor.DARK_RED + "Sorry, but you are not allowed to do that here.");
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
    private Predicate<Location> createStateFlagPredicate(final RegionQuery query, final StateFlag flag) {
        return new Predicate<Location>() {
            @Override
            public boolean apply(@Nullable Location location) {
                return query.testState(location, null, flag);
            }
        };
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlaceBlock(final PlaceBlockEvent event) {
        if (isWhitelisted(event.getCause())) {
            return; // Whitelisted cause
        }

        final Material type = event.getEffectiveMaterial();

        final Player player;

        if ((player = event.getCause().getPlayerRootCause()) != null) {
            final RegionQuery query = getPlugin().getRegionContainer().createQuery();

            event.filterBlocks(new Predicate<Location>() {
                @Override
                public boolean apply(Location target) {
                    boolean canPlace;

                    // Flint and steel, fire charge
                    if (type == Material.FIRE) {
                        canPlace = query.testPermission(target, player, DefaultFlag.LIGHTER);

                    } else {
                        canPlace = query.testPermission(target, player);
                    }

                    if (!canPlace) {
                        tellErrorMessage(player, target);
                        return false;
                    }

                    return true;
                }
            });
        } else {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBreakBlock(final BreakBlockEvent event) {
        if (isWhitelisted(event.getCause())) {
            return; // Whitelisted cause
        }

        final RegionQuery query = getPlugin().getRegionContainer().createQuery();
        ConfigurationManager globalConfig = getPlugin().getGlobalStateManager();
        WorldConfiguration config = globalConfig.get(event.getWorld());

        final Player player;
        final Entity entity;

        // ====================================================================
        // Player caused
        // ====================================================================

        if ((player = event.getCause().getPlayerRootCause()) != null) {
            event.filterBlocks(new Predicate<Location>() {
                @Override
                public boolean apply(Location target) {
                    boolean canBreak = query.testPermission(target, player);

                    if (!canBreak) {
                        tellErrorMessage(player, target);
                        return false;
                    }

                    return true;
                }
            });

        // ====================================================================
        // Entity caused
        // ====================================================================

        } else if ((entity = event.getCause().getEntityRootCause()) != null) {
            // Creeper
            if (entity instanceof Creeper) {
                event.filterBlocks(createStateFlagPredicate(query, DefaultFlag.CREEPER_EXPLOSION), config.explosionFlagCancellation);

            // Enderdragon
            } else if (entity instanceof EnderDragon) {
                event.filterBlocks(createStateFlagPredicate(query, DefaultFlag.ENDERDRAGON_BLOCK_DAMAGE), config.explosionFlagCancellation);

            // TNT + explosive TNT carts
            } else if (Entities.isTNTBased(entity)) {
                event.filterBlocks(createStateFlagPredicate(query, DefaultFlag.TNT), config.explosionFlagCancellation);

            }
        } else {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onUseBlock(final UseBlockEvent event) {
        if (isWhitelisted(event.getCause())) {
            return; // Whitelisted cause
        }

        final Material type = event.getEffectiveMaterial();

        final Player player;

        if ((player = event.getCause().getPlayerRootCause()) != null) {
            final RegionQuery query = getPlugin().getRegionContainer().createQuery();

            event.filterBlocks(new Predicate<Location>() {
                @Override
                public boolean apply(Location target) {
                    boolean canUse;

                    // Inventory blocks (CHEST_ACCESS)
                    if (Materials.isInventoryBlock(type)) {
                        canUse = query.testPermission(target, player, DefaultFlag.USE, DefaultFlag.CHEST_ACCESS);

                    // Beds (SLEEP)
                    } else if (type == Material.BED) {
                        canUse = query.testPermission(target, player, DefaultFlag.USE, DefaultFlag.SLEEP);

                    // TNT (TNT)
                    } else if (type == Material.TNT) {
                        canUse = query.testPermission(target, player, DefaultFlag.TNT);

                    // Everything else
                    } else {
                        canUse = query.testPermission(target, player, DefaultFlag.USE);
                    }

                    if (!canUse) {
                        tellErrorMessage(player, target);
                        return false;
                    }

                    return true;
                }
            });
        } else {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onSpawnEntity(SpawnEntityEvent event) {
        if (isWhitelisted(event.getCause())) {
            return; // Whitelisted cause
        }

        Location target = event.getTarget();
        EntityType type = event.getEffectiveType();

        Player player;

        if ((player = event.getCause().getPlayerRootCause()) != null) {
            RegionQuery query = getPlugin().getRegionContainer().createQuery();
            boolean canSpawn;

            if (Entities.isVehicle(type)) {
                canSpawn = query.testPermission(target, player, DefaultFlag.PLACE_VEHICLE);
            } else {
                canSpawn = query.testPermission(target, player);
            }

            if (!canSpawn) {
                tellErrorMessage(player, target);
                event.setCancelled(true);
            }
        } else {
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

        Player player;

        if ((player = event.getCause().getPlayerRootCause()) != null) {
            RegionQuery query = getPlugin().getRegionContainer().createQuery();
            boolean canDestroy;

            if (Entities.isVehicle(type)) {
                canDestroy = query.testPermission(target, player, DefaultFlag.DESTROY_VEHICLE);
            } else {
                canDestroy = query.testPermission(target, player);
            }

            if (!canDestroy) {
                tellErrorMessage(player, target);
                event.setCancelled(true);
            }
        } else {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onUseEntity(UseEntityEvent event) {
        if (isWhitelisted(event.getCause())) {
            return; // Whitelisted cause
        }

        Location target = event.getTarget();

        Player player;

        if ((player = event.getCause().getPlayerRootCause()) != null) {
            RegionQuery query = getPlugin().getRegionContainer().createQuery();
            boolean canUse = query.testPermission(target, player, DefaultFlag.USE);

            if (!canUse) {
                tellErrorMessage(player, target);
                event.setCancelled(true);
            }
        } else {
            event.setCancelled(true);
        }
    }

}
