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
import com.sk89q.worldguard.bukkit.cause.Cause;
import com.sk89q.worldguard.bukkit.event.DelegateEvent;
import com.sk89q.worldguard.bukkit.event.block.BreakBlockEvent;
import com.sk89q.worldguard.bukkit.event.block.PlaceBlockEvent;
import com.sk89q.worldguard.bukkit.event.block.UseBlockEvent;
import com.sk89q.worldguard.bukkit.event.entity.DamageEntityEvent;
import com.sk89q.worldguard.bukkit.event.entity.DestroyEntityEvent;
import com.sk89q.worldguard.bukkit.event.entity.SpawnEntityEvent;
import com.sk89q.worldguard.bukkit.event.entity.UseEntityEvent;
import com.sk89q.worldguard.bukkit.permission.RegionPermissionModel;
import com.sk89q.worldguard.bukkit.util.DelayedRegionOverlapAssociation;
import com.sk89q.worldguard.bukkit.util.Entities;
import com.sk89q.worldguard.bukkit.util.Events;
import com.sk89q.worldguard.bukkit.util.Materials;
import com.sk89q.worldguard.bukkit.util.WGMetadata;
import com.sk89q.worldguard.domains.Association;
import com.sk89q.worldguard.protection.association.Associables;
import com.sk89q.worldguard.protection.association.RegionAssociable;
import com.sk89q.worldguard.protection.events.DisallowedPVPEvent;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.StateFlag.State;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.vehicle.VehicleExitEvent;

/**
 * Handle events that need to be processed by region protection.
 */
public class RegionProtectionListener extends AbstractListener {

    private static final String DENY_MESSAGE_KEY = "worldguard.region.lastMessage";
    private static final String DISEMBARK_MESSAGE_KEY = "worldguard.region.disembarkMessage";
    private static final int LAST_MESSAGE_DELAY = 500;

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
     * @param event the event
     * @param cause the cause
     * @param location the location
     * @param what what was done
     */
    private void tellErrorMessage(DelegateEvent event, Cause cause, Location location, String what) {
        if (event.isSilent()) {
            return;
        }

        Object rootCause = cause.getRootCause();

        if (rootCause instanceof Player) {
            Player player = (Player) rootCause;

            long now = System.currentTimeMillis();
            Long lastTime = WGMetadata.getIfPresent(player, DENY_MESSAGE_KEY, Long.class);
            if (lastTime == null || now - lastTime >= LAST_MESSAGE_DELAY) {
                RegionQuery query = getPlugin().getRegionContainer().createQuery();
                String message = query.queryValue(location, player, DefaultFlag.DENY_MESSAGE);
                if (message != null && !message.isEmpty()) {
                    player.sendMessage(message.replace("%what%", what));
                }
                WGMetadata.put(player, DENY_MESSAGE_KEY, now);
            }
        }
    }

    /**
     * Return whether the given cause is whitelist (should be ignored).
     *
     * @param cause the cause
     * @return true if whitelisted
     */
    private boolean isWhitelisted(Cause cause, World world) {
        Object rootCause = cause.getRootCause();

        if (rootCause instanceof Player) {
            Player player = (Player) rootCause;
            WorldConfiguration config = getWorldConfig(world);

            if (config.fakePlayerBuildOverride && Entities.isFakePlayer(player)) {
                return true;
            }

            return new RegionPermissionModel(getPlugin(), player).mayIgnoreRegionProtection(world);
        } else {
            return false;
        }
    }

    private RegionAssociable createRegionAssociable(Cause cause) {
        Object rootCause = cause.getRootCause();

        if (!cause.isKnown()) {
            return Associables.constant(Association.NON_MEMBER);
        } else if (rootCause instanceof Player) {
            return getPlugin().wrapPlayer((Player) rootCause);
        } else if (rootCause instanceof OfflinePlayer) {
            return getPlugin().wrapOfflinePlayer((OfflinePlayer) rootCause);
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
        if (isWhitelisted(event.getCause(), event.getWorld())) return; // Whitelisted cause

        final Material type = event.getEffectiveMaterial();
        final RegionQuery query = getPlugin().getRegionContainer().createQuery();
        final RegionAssociable associable = createRegionAssociable(event.getCause());

        // Don't check liquid flow unless it's enabled
        if (event.getCause().getRootCause() instanceof Block
                && Materials.isLiquid(type)
                && !getWorldConfig(event.getWorld()).checkLiquidFlow) {
            return;
        }

        event.filter(new Predicate<Location>() {
            @Override
            public boolean apply(Location target) {
                boolean canPlace;
                String what;

                /* Flint and steel, fire charge, etc. */
                if (type == Material.FIRE) {
                    canPlace = query.testBuild(target, associable, DefaultFlag.BLOCK_PLACE, DefaultFlag.LIGHTER);
                    what = "place fire";

                /* Everything else */
                } else {
                    canPlace = query.testBuild(target, associable, DefaultFlag.BLOCK_PLACE);
                    what = "place that block";
                }

                if (!canPlace) {
                    tellErrorMessage(event, event.getCause(), target, what);
                    return false;
                }

                return true;
            }
        });
    }

    @EventHandler(ignoreCancelled = true)
    public void onBreakBlock(final BreakBlockEvent event) {
        if (!isRegionSupportEnabled(event.getWorld())) return; // Region support disabled
        if (isWhitelisted(event.getCause(), event.getWorld())) return; // Whitelisted cause

        final RegionQuery query = getPlugin().getRegionContainer().createQuery();

        if (!event.isCancelled()) {
            final RegionAssociable associable = createRegionAssociable(event.getCause());

            event.filter(new Predicate<Location>() {
                @Override
                public boolean apply(Location target) {
                    boolean canBreak;
                    String what;

                    /* TNT */
                    if (event.getCause().find(EntityType.PRIMED_TNT, EntityType.PRIMED_TNT) != null) {
                        canBreak = query.testBuild(target, associable, DefaultFlag.BLOCK_BREAK, DefaultFlag.TNT);
                        what = "dynamite blocks";

                    /* Everything else */
                    } else {
                        canBreak = query.testBuild(target, associable, DefaultFlag.BLOCK_BREAK);
                        what = "break that block";
                    }

                    if (!canBreak) {
                        tellErrorMessage(event, event.getCause(), target, what);
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
        if (isWhitelisted(event.getCause(), event.getWorld())) return; // Whitelisted cause

        final Material type = event.getEffectiveMaterial();
        final RegionQuery query = getPlugin().getRegionContainer().createQuery();
        final RegionAssociable associable = createRegionAssociable(event.getCause());

        event.filter(new Predicate<Location>() {
            @Override
            public boolean apply(Location target) {
                boolean canUse;
                String what;

                /* Saplings, etc. */
                if (Materials.isConsideredBuildingIfUsed(type)) {
                    canUse = query.testBuild(target, associable);
                    what = "use that";

                /* Inventory */
                } else if (Materials.isInventoryBlock(type)) {
                    canUse = query.testBuild(target, associable, DefaultFlag.USE, DefaultFlag.CHEST_ACCESS);
                    what = "open that";

                /* Beds */
                } else if (type == Material.BED_BLOCK) {
                    canUse = query.testBuild(target, associable, DefaultFlag.USE, DefaultFlag.SLEEP);
                    what = "sleep";

                /* TNT */
                } else if (type == Material.TNT) {
                    canUse = query.testBuild(target, associable, DefaultFlag.TNT);
                    what = "use explosives";

                /* Everything else */
                } else {
                    canUse = query.testBuild(target, associable, DefaultFlag.USE);
                    what = "use that";
                }

                if (!canUse) {
                    tellErrorMessage(event, event.getCause(), target, what);
                    return false;
                }

                return true;
            }
        });
    }

    @EventHandler(ignoreCancelled = true)
    public void onSpawnEntity(SpawnEntityEvent event) {
        if (!isRegionSupportEnabled(event.getWorld())) return; // Region support disabled
        if (isWhitelisted(event.getCause(), event.getWorld())) return; // Whitelisted cause

        Location target = event.getTarget();
        EntityType type = event.getEffectiveType();

        RegionQuery query = getPlugin().getRegionContainer().createQuery();
        RegionAssociable associable = createRegionAssociable(event.getCause());

        boolean canSpawn;
        String what;

        /* Vehicles */
        if (Entities.isVehicle(type)) {
            canSpawn = query.testBuild(target, associable, DefaultFlag.PLACE_VEHICLE);
            what = "place vehicles";

        /* Item pickup */
        } else if (event.getEntity() instanceof Item) {
            canSpawn = query.testBuild(target, associable, DefaultFlag.ITEM_DROP);
            what = "drop items";

        /* Everything else */
        } else {
            canSpawn = query.testBuild(target, associable);

            if (event.getEntity() instanceof Item) {
                what = "drop items";
            } else {
                what = "place things";
            }
        }

        if (!canSpawn) {
            tellErrorMessage(event, event.getCause(), target, what);
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onDestroyEntity(DestroyEntityEvent event) {
        if (!isRegionSupportEnabled(event.getWorld())) return; // Region support disabled
        if (isWhitelisted(event.getCause(), event.getWorld())) return; // Whitelisted cause

        Location target = event.getTarget();
        EntityType type = event.getEntity().getType();
        RegionAssociable associable = createRegionAssociable(event.getCause());

        RegionQuery query = getPlugin().getRegionContainer().createQuery();
        boolean canDestroy;
        String what;

        /* Vehicles */
        if (Entities.isVehicle(type)) {
            canDestroy = query.testBuild(target, associable, DefaultFlag.DESTROY_VEHICLE);
            what = "break vehicles";

        /* Item pickup */
        } else if (event.getEntity() instanceof Item || event.getEntity() instanceof ExperienceOrb) {
            canDestroy = query.testBuild(target, associable, DefaultFlag.ITEM_PICKUP);
            what = "pick up items";

        /* Everything else */
        } else {
            canDestroy = query.testBuild(target, associable);
            what = "break things";
        }

        if (!canDestroy) {
            tellErrorMessage(event, event.getCause(), target, what);
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onUseEntity(UseEntityEvent event) {
        if (!isRegionSupportEnabled(event.getWorld())) return; // Region support disabled
        if (isWhitelisted(event.getCause(), event.getWorld())) return; // Whitelisted cause

        Location target = event.getTarget();
        RegionAssociable associable = createRegionAssociable(event.getCause());

        RegionQuery query = getPlugin().getRegionContainer().createQuery();
        boolean canUse;
        String what;

        /* Hostile / ambient mob override */
        if (Entities.isHostile(event.getEntity()) || Entities.isAmbient(event.getEntity()) || Entities.isNPC(event.getEntity())) {
            canUse = true;
            what = "use that";

        /* Paintings, item frames, etc. */
        } else if (Entities.isConsideredBuildingIfUsed(event.getEntity())) {
            canUse = query.testBuild(target, associable);
            what = "change that";

        /* Everything else */
        } else {
            canUse = query.testBuild(target, associable, DefaultFlag.USE);
            what = "use that";
        }

        if (!canUse) {
            tellErrorMessage(event, event.getCause(), target, what);
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onDamageEntity(DamageEntityEvent event) {
        if (!isRegionSupportEnabled(event.getWorld())) return; // Region support disabled
        if (isWhitelisted(event.getCause(), event.getWorld())) return; // Whitelisted cause

        Location target = event.getTarget();
        RegionAssociable associable = createRegionAssociable(event.getCause());

        RegionQuery query = getPlugin().getRegionContainer().createQuery();
        Object rootCause = event.getCause().getRootCause();
        Player attacker;
        boolean canDamage;
        String what;

        /* Hostile / ambient mob override */
        if (isWhitelistedEntity(event.getEntity()) || (rootCause instanceof Entity && isWhitelistedEntity((Entity) rootCause))) {
            canDamage = true;
            what = "hit that";

        /* Paintings, item frames, etc. */
        } else if (Entities.isConsideredBuildingIfUsed(event.getEntity())) {
            canDamage = query.testBuild(target, associable);
            what = "change that";

        /* PVP */
        } else if (event.getEntity() instanceof Player && (attacker = event.getCause().getFirstPlayer()) != null && !attacker.equals(event.getEntity())) {
            Player defender = (Player) event.getEntity();

            canDamage = query.testBuild(target, associable, DefaultFlag.PVP)
                    && query.queryState(attacker.getLocation(), attacker, DefaultFlag.PVP) != State.DENY;

            // Fire the disallow PVP event
            if (!canDamage && Events.fireAndTestCancel(new DisallowedPVPEvent(attacker, defender, event.getOriginalEvent()))) {
                canDamage = true;
            }

            what = "PvP";

        /* Player damage not caused  by another player */
        } else if (event.getEntity() instanceof Player) {
            canDamage = true;
            what = "damage that";

        /* Everything else */
        } else {
            canDamage = query.testBuild(target, associable, DefaultFlag.USE);
            what = "hit that";
        }

        if (!canDamage) {
            tellErrorMessage(event, event.getCause(), target, what);
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onVehicleExit(VehicleExitEvent event) {
        Entity vehicle = event.getVehicle();
        Entity exited = event.getExited();

        if (vehicle instanceof Tameable && exited instanceof Player) {
            Player player = (Player) exited;
            if (!isWhitelisted(Cause.create(player), vehicle.getWorld())) {
                RegionQuery query = getPlugin().getRegionContainer().createQuery();
                Location location = vehicle.getLocation();
                if (!query.testBuild(location, player, DefaultFlag.USE)) {
                    long now = System.currentTimeMillis();
                    Long lastTime = WGMetadata.getIfPresent(player, DISEMBARK_MESSAGE_KEY, Long.class);
                    if (lastTime == null || now - lastTime >= LAST_MESSAGE_DELAY) {
                        player.sendMessage("" + ChatColor.GOLD + "Don't disembark here!" + ChatColor.GRAY + " You can't get back on.");
                        WGMetadata.put(player, DISEMBARK_MESSAGE_KEY, now);
                    }

                    event.setCancelled(true);
                }
            }
        }
    }

    private boolean isWhitelistedEntity(Entity entity) {
        return Entities.isNonPlayerCreature(entity);
    }

}
