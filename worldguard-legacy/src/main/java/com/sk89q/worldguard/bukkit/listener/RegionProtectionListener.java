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
import com.sk89q.worldguard.bukkit.internal.WGMetadata;
import com.sk89q.worldguard.bukkit.permission.RegionPermissionModel;
import com.sk89q.worldguard.bukkit.protection.DelayedRegionOverlapAssociation;
import com.sk89q.worldguard.bukkit.util.Entities;
import com.sk89q.worldguard.bukkit.util.Events;
import com.sk89q.worldguard.bukkit.util.Materials;
import com.sk89q.worldguard.bukkit.util.InteropUtils;
import com.sk89q.worldguard.domains.Association;
import com.sk89q.worldguard.protection.association.Associables;
import com.sk89q.worldguard.protection.association.RegionAssociable;
import com.sk89q.worldguard.protection.events.DisallowedPVPEvent;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.StateFlag.State;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.vehicle.VehicleExitEvent;

import java.util.Arrays;
import java.util.List;

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
        if (event.isSilent() || cause.isIndirect()) {
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
     * @param world the world
     * @param pvp whether the event in question is PvP combat
     * @return true if whitelisted
     */
    private boolean isWhitelisted(Cause cause, World world, boolean pvp) {
        Object rootCause = cause.getRootCause();

        if (rootCause instanceof Block) {
            Material type = ((Block) rootCause).getType();
            return type == Material.HOPPER || type == Material.DROPPER;
        } else if (rootCause instanceof Player) {
            Player player = (Player) rootCause;
            WorldConfiguration config = getWorldConfig(world);

            if (config.fakePlayerBuildOverride && InteropUtils.isFakePlayer(player)) {
                return true;
            }

            return !pvp && new RegionPermissionModel(getPlugin(), player).mayIgnoreRegionProtection(world);
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
        if (event.getResult() == Result.ALLOW) return; // Don't care about events that have been pre-allowed
        if (!isRegionSupportEnabled(event.getWorld())) return; // Region support disabled
        if (isWhitelisted(event.getCause(), event.getWorld(), false)) return; // Whitelisted cause

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
                    canPlace = query.testBuild(target, associable, combine(event, DefaultFlag.BLOCK_PLACE, DefaultFlag.LIGHTER));
                    what = "place fire";

                /* Everything else */
                } else {
                    canPlace = query.testBuild(target, associable, combine(event, DefaultFlag.BLOCK_PLACE));
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
        if (event.getResult() == Result.ALLOW) return; // Don't care about events that have been pre-allowed
        if (!isRegionSupportEnabled(event.getWorld())) return; // Region support disabled
        if (isWhitelisted(event.getCause(), event.getWorld(), false)) return; // Whitelisted cause

        final RegionQuery query = getPlugin().getRegionContainer().createQuery();

        if (!event.isCancelled()) {
            final RegionAssociable associable = createRegionAssociable(event.getCause());

            event.filter(new Predicate<Location>() {
                @Override
                public boolean apply(Location target) {
                    boolean canBreak;
                    String what;

                    /* TNT */
                    if (event.getCause().find(EntityType.PRIMED_TNT, EntityType.MINECART_TNT) != null) {
                        canBreak = query.testBuild(target, associable, combine(event, DefaultFlag.BLOCK_BREAK, DefaultFlag.TNT));
                        what = "use dynamite";

                    /* Everything else */
                    } else {
                        canBreak = query.testBuild(target, associable, combine(event, DefaultFlag.BLOCK_BREAK));
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
        if (event.getResult() == Result.ALLOW) return; // Don't care about events that have been pre-allowed
        if (!isRegionSupportEnabled(event.getWorld())) return; // Region support disabled
        if (isWhitelisted(event.getCause(), event.getWorld(), false)) return; // Whitelisted cause

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
                    canUse = query.testBuild(target, associable, combine(event));
                    what = "use that";

                /* Inventory */
                } else if (Materials.isInventoryBlock(type)) {
                    canUse = query.testBuild(target, associable, combine(event, DefaultFlag.INTERACT, DefaultFlag.CHEST_ACCESS));
                    what = "open that";

                /* Beds */
                } else if (type == Material.BED_BLOCK) {
                    canUse = query.testBuild(target, associable, combine(event, DefaultFlag.INTERACT, DefaultFlag.SLEEP));
                    what = "sleep";

                /* TNT */
                } else if (type == Material.TNT) {
                    canUse = query.testBuild(target, associable, combine(event, DefaultFlag.INTERACT, DefaultFlag.TNT));
                    what = "use explosives";

                /* Legacy USE flag */
                } else if (Materials.isUseFlagApplicable(type)) {
                    canUse = query.testBuild(target, associable, combine(event, DefaultFlag.INTERACT, DefaultFlag.USE));
                    what = "use that";

                /* Everything else */
                } else {
                    canUse = query.testBuild(target, associable, combine(event, DefaultFlag.INTERACT));
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
        if (event.getResult() == Result.ALLOW) return; // Don't care about events that have been pre-allowed
        if (!isRegionSupportEnabled(event.getWorld())) return; // Region support disabled
        if (isWhitelisted(event.getCause(), event.getWorld(), false)) return; // Whitelisted cause

        Location target = event.getTarget();
        EntityType type = event.getEffectiveType();

        RegionQuery query = getPlugin().getRegionContainer().createQuery();
        RegionAssociable associable = createRegionAssociable(event.getCause());

        boolean canSpawn;
        String what;

        /* Vehicles */
        if (Entities.isVehicle(type)) {
            canSpawn = query.testBuild(target, associable, combine(event, DefaultFlag.PLACE_VEHICLE));
            what = "place vehicles";

        /* Item pickup */
        } else if (event.getEntity() instanceof Item) {
            canSpawn = query.testBuild(target, associable, combine(event, DefaultFlag.ITEM_DROP));
            what = "drop items";

        /* XP drops */
        } else if (event.getEffectiveType() == EntityType.EXPERIENCE_ORB) {
            canSpawn = query.testBuild(target, associable, combine(event, DefaultFlag.EXP_DROPS));
            what = "drop XP";

        /* Everything else */
        } else {
            canSpawn = query.testBuild(target, associable, combine(event));

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
        if (event.getResult() == Result.ALLOW) return; // Don't care about events that have been pre-allowed
        if (!isRegionSupportEnabled(event.getWorld())) return; // Region support disabled
        if (isWhitelisted(event.getCause(), event.getWorld(), false)) return; // Whitelisted cause

        Location target = event.getTarget();
        EntityType type = event.getEntity().getType();
        RegionAssociable associable = createRegionAssociable(event.getCause());

        RegionQuery query = getPlugin().getRegionContainer().createQuery();
        boolean canDestroy;
        String what;

        /* Vehicles */
        if (Entities.isVehicle(type)) {
            canDestroy = query.testBuild(target, associable, combine(event, DefaultFlag.DESTROY_VEHICLE));
            what = "break vehicles";

        /* Item pickup */
        } else if (event.getEntity() instanceof Item || event.getEntity() instanceof ExperienceOrb) {
            canDestroy = query.testBuild(target, associable, combine(event, DefaultFlag.ITEM_PICKUP));
            what = "pick up items";

        /* Everything else */
        } else {
            canDestroy = query.testBuild(target, associable, combine(event));
            what = "break things";
        }

        if (!canDestroy) {
            tellErrorMessage(event, event.getCause(), target, what);
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onUseEntity(UseEntityEvent event) {
        if (event.getResult() == Result.ALLOW) return; // Don't care about events that have been pre-allowed
        if (!isRegionSupportEnabled(event.getWorld())) return; // Region support disabled
        if (isWhitelisted(event.getCause(), event.getWorld(), false)) return; // Whitelisted cause

        Location target = event.getTarget();
        RegionAssociable associable = createRegionAssociable(event.getCause());

        RegionQuery query = getPlugin().getRegionContainer().createQuery();
        boolean canUse;
        String what;

        /* Hostile / ambient mob override */
        if (Entities.isHostile(event.getEntity()) || Entities.isAmbient(event.getEntity())
                || Entities.isNPC(event.getEntity()) || Entities.isVehicle(event.getEntity().getType())) {
            canUse = event.getRelevantFlags().isEmpty() || query.queryState(target, associable, combine(event)) != State.DENY;
            what = "use that";

        /* Paintings, item frames, etc. */
        } else if (Entities.isConsideredBuildingIfUsed(event.getEntity())) {
            canUse = query.testBuild(target, associable, combine(event));
            what = "change that";

        /* Ridden on use */
        } else if (Entities.isRiddenOnUse(event.getEntity())) {
            canUse = query.testBuild(target, associable, combine(event, DefaultFlag.RIDE, DefaultFlag.INTERACT));
            what = "ride that";

        /* Everything else */
        } else {
            canUse = query.testBuild(target, associable, combine(event, DefaultFlag.INTERACT));
            what = "use that";
        }

        if (!canUse) {
            tellErrorMessage(event, event.getCause(), target, what);
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onDamageEntity(DamageEntityEvent event) {
        if (event.getResult() == Result.ALLOW) return; // Don't care about events that have been pre-allowed
        if (!isRegionSupportEnabled(event.getWorld())) return; // Region support disabled
        // Whitelist check is below

        Location target = event.getTarget();
        RegionAssociable associable = createRegionAssociable(event.getCause());

        RegionQuery query = getPlugin().getRegionContainer().createQuery();
        Player playerAttacker = event.getCause().getFirstPlayer();
        boolean canDamage;
        String what;

        // Block PvP like normal even if the player has an override permission
        // because (1) this is a frequent source of confusion and
        // (2) some users want to block PvP even with the bypass permission
        boolean pvp = event.getEntity() instanceof Player && playerAttacker != null && !playerAttacker.equals(event.getEntity());
        if (isWhitelisted(event.getCause(), event.getWorld(), pvp)) {
            return;
        }

        /* Hostile / ambient mob override */
        if (Entities.isHostile(event.getEntity()) || Entities.isAmbient(event.getEntity())
                || Entities.isVehicle(event.getEntity().getType())) {
            canDamage = event.getRelevantFlags().isEmpty() || query.queryState(target, associable, combine(event)) != State.DENY;
            what = "hit that";

        /* Paintings, item frames, etc. */
        } else if (Entities.isConsideredBuildingIfUsed(event.getEntity())) {
            canDamage = query.testBuild(target, associable, combine(event));
            what = "change that";

        /* PVP */
        } else if (pvp) {
            Player defender = (Player) event.getEntity();

            canDamage = query.testBuild(target, associable, combine(event, DefaultFlag.PVP))
                    && query.queryState(playerAttacker.getLocation(), playerAttacker, combine(event, DefaultFlag.PVP)) != State.DENY
                    && query.queryState(target, playerAttacker, combine(event, DefaultFlag.PVP)) != State.DENY;

            // Fire the disallow PVP event
            if (!canDamage && Events.fireAndTestCancel(new DisallowedPVPEvent(playerAttacker, defender, event.getOriginalEvent()))) {
                canDamage = true;
            }

            what = "PvP";

        /* Player damage not caused  by another player */
        } else if (event.getEntity() instanceof Player) {
            canDamage = event.getRelevantFlags().isEmpty() || query.queryState(target, associable, combine(event)) != State.DENY;
            what = "damage that";

        /* damage to non-hostile mobs (e.g. animals) */
        } else if (Entities.isNonHostile(event.getEntity())) {
            canDamage = query.testBuild(target, associable, combine(event, DefaultFlag.DAMAGE_ANIMALS));
            what = "harm that";

        /* Everything else */
        } else {
            canDamage = query.testBuild(target, associable, combine(event, DefaultFlag.INTERACT));
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
            if (!isWhitelisted(Cause.create(player), vehicle.getWorld(), false)) {
                RegionQuery query = getPlugin().getRegionContainer().createQuery();
                Location location = vehicle.getLocation();
                if (!query.testBuild(location, player, DefaultFlag.RIDE, DefaultFlag.INTERACT)) {
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

    /**
     * Combine the flags from a delegate event with an array of flags.
     *
     * <p>The delegate event's flags appear at the end.</p>
     *
     * @param event The event
     * @param flag An array of flags
     * @return An array of flags
     */
    private static StateFlag[] combine(DelegateEvent event, StateFlag... flag) {
        List<StateFlag> extra = event.getRelevantFlags();
        StateFlag[] flags = Arrays.copyOf(flag, flag.length + extra.size());
        for (int i = 0; i < extra.size(); i++) {
            flags[flag.length + i] = extra.get(i);
        }
        return flags;
    }

}
