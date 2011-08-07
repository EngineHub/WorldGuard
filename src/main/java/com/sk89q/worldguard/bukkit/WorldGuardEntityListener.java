// $Id$
/*
 * WorldGuard
 * Copyright (C) 2010 sk89q <http://www.sk89q.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.sk89q.worldguard.bukkit;

import static com.sk89q.worldguard.bukkit.BukkitUtil.toVector;

import java.util.Set;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Creature;
import org.bukkit.entity.CreatureType;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Painting;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.Wolf;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreeperPowerEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.entity.PigZapEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.painting.PaintingBreakByEntityEvent;
import org.bukkit.event.painting.PaintingBreakEvent;
import org.bukkit.event.painting.PaintingPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.blacklist.events.BlockBreakBlacklistEvent;
import com.sk89q.worldguard.blacklist.events.ItemUseBlacklistEvent;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;

/**
 * Listener for entity related events.
 * 
 * @author sk89q
 */
public class WorldGuardEntityListener extends EntityListener {
    /**
     * Logger for messages.
     */
    private static final Logger logger = Logger.getLogger("Minecraft.WorldGuard");

    private WorldGuardPlugin plugin;

    /**
     * Construct the object;
     * 
     * @param plugin
     */
    public WorldGuardEntityListener(WorldGuardPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Register events.
     */
    public void registerEvents() {
//        PluginManager pm = plugin.getServer().getPluginManager();

        registerEvent("ENTITY_DAMAGE", Priority.High);
        registerEvent("ENTITY_COMBUST", Priority.High);
        registerEvent("ENTITY_EXPLODE", Priority.High);
        registerEvent("EXPLOSION_PRIME", Priority.High);
        registerEvent("CREATURE_SPAWN", Priority.High);
        registerEvent("ENTITY_INTERACT", Priority.High);
        registerEvent("CREEPER_POWER", Priority.High);
        registerEvent("PIG_ZAP", Priority.High);
        registerEvent("PAINTING_BREAK", Priority.High);
        registerEvent("PAINTING_PLACE", Priority.High);
        registerEvent("ENTITY_REGAIN_HEALTH", Priority.High);
    }

    /**
     * Register an event, but not failing if the event is not implemented.
     *
     * @param typeName
     * @param priority
     */
    private void registerEvent(String typeName, Priority priority) {
        try {
            Event.Type type = Event.Type.valueOf(typeName);
            PluginManager pm = plugin.getServer().getPluginManager();
            pm.registerEvent(type, this, priority, plugin);
        } catch (IllegalArgumentException e) {
            logger.info("WorldGuard: Unable to register missing event type " + typeName);
        }
    }

    /**
     * Called when an entity interacts with another object.
     */
    @Override
    public void onEntityInteract(EntityInteractEvent event) {
        //bukkit doesn't actually throw this event yet, someone add a ticket to leaky
        Entity entity = event.getEntity();
        Block block = event.getBlock();

        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(entity.getWorld());

        if (block.getType() == Material.SOIL) {
            if (entity instanceof Creature && wcfg.disableCreatureCropTrampling) {
                event.setCancelled(true);
            }
        }
    }

    /**
     * Called on entity damage by a block.
     * 
     * @param event
     */
    private void onEntityDamageByBlock(EntityDamageByBlockEvent event) {
        Entity defender = event.getEntity();
        DamageCause type = event.getCause();

        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(defender.getWorld());

        if (defender instanceof Wolf && ((Wolf) defender).isTamed()) {
            if (wcfg.antiWolfDumbness && !(type == DamageCause.VOID)) {
                event.setCancelled(true);
                return;
            }
        } else if (defender instanceof Player) {
            Player player = (Player) defender;

            if (cfg.hasGodMode(player)
                    || (wcfg.useRegions && RegionQueryUtil.isInvincible(plugin, player))) {
                event.setCancelled(true);
                return;
            }

            if (wcfg.disableLavaDamage && type == DamageCause.LAVA) {
                event.setCancelled(true);
                if (cfg.hasGodMode(player)) player.setFireTicks(0);
                return;
            }

            if (wcfg.disableContactDamage && type == DamageCause.CONTACT) {
                event.setCancelled(true);
                return;
            }

            if (wcfg.teleportOnVoid && type == DamageCause.VOID) {
                BukkitUtil.findFreePosition(player);
                event.setCancelled(true);
                return;
            }

            if (wcfg.disableVoidDamage && type == DamageCause.VOID) {
                event.setCancelled(true);
                return;
            }

            if (wcfg.disableExplosionDamage && event.getCause() == DamageCause.BLOCK_EXPLOSION) {
                event.setCancelled(true);
                return;
            }
        }
    }

    /**
     * Called on entity damage by an entity.
     * 
     * @param event
     */
    private void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getCause() == DamageCause.PROJECTILE) {
            onEntityDamageByProjectile(event);
            return;
        }

        Entity attacker = event.getDamager();
        Entity defender = event.getEntity();
        
        if (attacker instanceof Player) {
            Player player = (Player) attacker;

            ConfigurationManager cfg = plugin.getGlobalStateManager();
            WorldConfiguration wcfg = cfg.get(player.getWorld());

            ItemStack held = player.getInventory().getItemInHand();
            
            if (held != null) {
                if (wcfg.getBlacklist() != null) {
                    if (!wcfg.getBlacklist().check(
                            new ItemUseBlacklistEvent(plugin.wrapPlayer(player),
                                    toVector(player.getLocation()), held.getTypeId()), false, false)) {
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        }

        if (defender instanceof Player) {
            Player player = (Player) defender;

            ConfigurationManager cfg = plugin.getGlobalStateManager();
            WorldConfiguration wcfg = cfg.get(player.getWorld());
            
            if (cfg.hasGodMode(player)) {
                event.setCancelled(true);
                return;
            }

            if (wcfg.useRegions && RegionQueryUtil.isInvincible(plugin, player)) {
                if (wcfg.regionInvinciblityRemovesMobs
                        && attacker instanceof LivingEntity && !(attacker instanceof Player)
                        && !(attacker instanceof Tameable && ((Tameable) attacker).isTamed())) {
                    attacker.remove();
                }

                event.setCancelled(true);
                return;
            }

            if (wcfg.disableLightningDamage && event.getCause() == DamageCause.LIGHTNING) {
                event.setCancelled(true);
                return;
            }

            if (wcfg.disableExplosionDamage && event.getCause() == DamageCause.ENTITY_EXPLOSION) {
                event.setCancelled(true);
                return;
            }

            if (attacker != null && attacker instanceof Player) {
                if (wcfg.useRegions) {
                    Vector pt = toVector(defender.getLocation());
                    RegionManager mgr = plugin.getGlobalRegionManager().get(player.getWorld());

                    if (!mgr.getApplicableRegions(pt).allows(DefaultFlag.PVP)) {
                        ((Player) attacker).sendMessage(ChatColor.DARK_RED + "You are in a no-PvP area.");
                        event.setCancelled(true);
                        return;
                    }
                }
            }

            if (attacker != null && attacker instanceof TNTPrimed && wcfg.disableTNTDamage) {
                event.setCancelled(true);
                return;
            }

            if (attacker != null && attacker instanceof LivingEntity
                    && !(attacker instanceof Player)) {
                if (attacker instanceof Creeper && wcfg.blockCreeperExplosions) {
                    event.setCancelled(true);
                    return;
                }

                if (wcfg.disableMobDamage) {
                    event.setCancelled(true);
                    return;
                }

                if (wcfg.useRegions) {
                    Vector pt = toVector(defender.getLocation());
                    RegionManager mgr = plugin.getGlobalRegionManager().get(player.getWorld());
                    ApplicableRegionSet set = mgr.getApplicableRegions(pt);

                    if (!set.allows(DefaultFlag.MOB_DAMAGE)) {
                        event.setCancelled(true);
                        return;
                    }

                    if (attacker instanceof Creeper) {
                        if (!set.allows(DefaultFlag.CREEPER_EXPLOSION)) {
                            event.setCancelled(true);
                            return;
                        }
                    }
                }

            }
        }
    }

    /**
     * Called on entity damage by a projectile.
     * 
     * @param event
     */
    private void onEntityDamageByProjectile(EntityDamageByEntityEvent event) {
        Entity defender = event.getEntity();
        Entity attacker = ((Projectile) event.getDamager()).getShooter();

        if (defender instanceof Player) {
            Player player = (Player) defender;

            ConfigurationManager cfg = plugin.getGlobalStateManager();
            WorldConfiguration wcfg = cfg.get(player.getWorld());
            
            if (cfg.hasGodMode(player) || (wcfg.useRegions && RegionQueryUtil.isInvincible(plugin, player))) {
                event.setCancelled(true);
                return;
            }

            if (attacker != null && attacker instanceof Player) {
                if (wcfg.useRegions) {
                    Vector pt = toVector(defender.getLocation());
                    RegionManager mgr = plugin.getGlobalRegionManager().get(player.getWorld());

                    if (!mgr.getApplicableRegions(pt).allows(DefaultFlag.PVP)) {
                        ((Player) attacker).sendMessage(ChatColor.DARK_RED + "You are in a no-PvP area.");
                        event.setCancelled(true);
                        return;
                    }
                }
            }
            if (attacker != null && attacker instanceof Skeleton) {
                if (wcfg.disableMobDamage) {
                    event.setCancelled(true);
                    return;
                }
                if (wcfg.useRegions) {
                    Vector pt = toVector(defender.getLocation());
                    RegionManager mgr = plugin.getGlobalRegionManager().get(player.getWorld());

                    if (!mgr.getApplicableRegions(pt).allows(DefaultFlag.MOB_DAMAGE)) {
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        }

    }

    /**
     * Called on entity damage.
     */
    @Override
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if (event instanceof EntityDamageByEntityEvent) {
            this.onEntityDamageByEntity((EntityDamageByEntityEvent) event);
            return;
        } else if (event instanceof EntityDamageByBlockEvent) {
            this.onEntityDamageByBlock((EntityDamageByBlockEvent) event);
            return;
        }

        Entity defender = event.getEntity();
        DamageCause type = event.getCause();

        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(defender.getWorld());

        if (defender instanceof Wolf && ((Wolf) defender).isTamed()) {
            if (wcfg.antiWolfDumbness) {
                event.setCancelled(true);
                return;
            }
        } else if (defender instanceof Player) {
            Player player = (Player) defender;

            if (cfg.hasGodMode(player)
                    || (wcfg.useRegions && RegionQueryUtil.isInvincible(plugin, player))) {
                event.setCancelled(true);
                player.setFireTicks(0);
                return;
            }

            if (type == DamageCause.DROWNING && cfg.hasAmphibiousMode(player)) {
                player.setRemainingAir(player.getMaximumAir());
                event.setCancelled(true);
                return;
            }

            if (type == DamageCause.DROWNING && wcfg.pumpkinScuba
                    && (player.getInventory().getHelmet().getType() == Material.PUMPKIN
                    || player.getInventory().getHelmet().getType() == Material.JACK_O_LANTERN)) {
                player.setRemainingAir(player.getMaximumAir());
                event.setCancelled(true);
                return;
            }

            if (wcfg.disableFallDamage && type == DamageCause.FALL) {
                event.setCancelled(true);
                return;
            }

            if (wcfg.disableFireDamage && (type == DamageCause.FIRE
                    || type == DamageCause.FIRE_TICK)) {
                event.setCancelled(true);
                return;
            }

            if (wcfg.disableDrowningDamage && type == DamageCause.DROWNING) {
                player.setRemainingAir(player.getMaximumAir());
                event.setCancelled(true);
                return;
            }

           if (wcfg.teleportOnSuffocation && type == DamageCause.SUFFOCATION) {
                BukkitUtil.findFreePosition(player);
                event.setCancelled(true);
                return;
            }

            if (wcfg.disableSuffocationDamage && type == DamageCause.SUFFOCATION) {
                event.setCancelled(true);
                return;
            }
        }
    }

    /**
     * Called on entity combust.
     */
    @Override
    public void onEntityCombust(EntityCombustEvent event) {
        if (event.isCancelled()) {
            return;
        }

        Entity entity = event.getEntity();

        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(entity.getWorld());

        if (entity instanceof Player) {
            Player player = (Player) entity;

            if (cfg.hasGodMode(player) || (wcfg.useRegions && RegionQueryUtil.isInvincible(plugin, player))) {
                event.setCancelled(true);
                return;
            }
        }
    }

    /**
     * Called on entity explode.
     */
    @Override
    public void onEntityExplode(EntityExplodeEvent event) {
        if (event.isCancelled()) {
            return;
        }

        ConfigurationManager cfg = plugin.getGlobalStateManager();
        Location l = event.getLocation();
        World world = l.getWorld();
        WorldConfiguration wcfg = cfg.get(world);
        Entity ent = event.getEntity();

        if (cfg.activityHaltToggle) {
            ent.remove();
            event.setCancelled(true);
            return;
        }

        if (ent instanceof LivingEntity) {
            if (wcfg.blockCreeperBlockDamage) {
                event.setCancelled(true);
                return;
            }

            if (wcfg.blockCreeperExplosions) {
                event.setCancelled(true);
                return;
            }

            if (wcfg.useRegions) {
                Vector pt = toVector(l);
                RegionManager mgr = plugin.getGlobalRegionManager().get(world);

                if (!mgr.getApplicableRegions(pt).allows(DefaultFlag.CREEPER_EXPLOSION)) {
                    event.setCancelled(true);
                    return;
                }
            }
        } else if (ent instanceof TNTPrimed) {
            if (wcfg.blockTNT) {
                event.setCancelled(true);
                return;
            }

            if (wcfg.useRegions) {
                Vector pt = toVector(l);
                RegionManager mgr = plugin.getGlobalRegionManager().get(world);

                if (!mgr.getApplicableRegions(pt).allows(DefaultFlag.TNT)) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
        
        if (wcfg.signChestProtection) {
            for (Block block : event.blockList()) {
                if (wcfg.isChestProtected(block)) {
                    event.setCancelled(true);
                    return;
                }
            }
        }

        if (wcfg.useRegions) {
            RegionManager mgr = plugin.getGlobalRegionManager().get(world);

            // Whoo, for each block
            for (Block block : event.blockList()) {
                Vector pt = toVector(block);

                if (!mgr.getApplicableRegions(pt).allows(DefaultFlag.TNT)) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    /**
     * Called on explosion prime
     */
    @Override
    public void onExplosionPrime(ExplosionPrimeEvent event) {
        if (event.isCancelled()) {
            return;
        }

        ConfigurationManager cfg = plugin.getGlobalStateManager();
        Location l = event.getEntity().getLocation();
        World world = l.getWorld();
        WorldConfiguration wcfg = cfg.get(world);
        Entity ent = event.getEntity();

        if (cfg.activityHaltToggle) {
            ent.remove();
            event.setCancelled(true);
            return;
        }

        if (ent instanceof Fireball) {
            if (wcfg.blockFireballBlockDamage) {
                event.setCancelled(true);
                return;
            }

            if (wcfg.useRegions) {
                Vector pt = toVector(l);
                RegionManager mgr = plugin.getGlobalRegionManager().get(world);

                if (!mgr.getApplicableRegions(pt).allows(DefaultFlag.GHAST_FIREBALL)) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    /**
     * Called on creature spawn.
     */
    @Override
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (event.isCancelled()) {
            return;
        }

        ConfigurationManager cfg = plugin.getGlobalStateManager();

        if (cfg.activityHaltToggle) {
            event.setCancelled(true);
            return;
        }

        WorldConfiguration wcfg = cfg.get(event.getEntity().getWorld());
        CreatureType creaType = event.getCreatureType();

        if (wcfg.blockCreatureSpawn.contains(creaType)) {
            event.setCancelled(true);
            return;
        }
        
        Location eventLoc = event.getLocation();
        
        if (wcfg.useRegions) {
            Vector pt = toVector(eventLoc);
            RegionManager mgr = plugin.getGlobalRegionManager().get(eventLoc.getWorld());
            ApplicableRegionSet set = mgr.getApplicableRegions(pt);

            if (!set.allows(DefaultFlag.MOB_SPAWNING)) {
                event.setCancelled(true);
                return;
            }

            Set<CreatureType> blockTypes = set.getFlag(DefaultFlag.DENY_SPAWN);
            if (blockTypes != null && blockTypes.contains(creaType)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    /**
     * Called on pig zap.
     */
    @Override
    public void onPigZap(PigZapEvent event) {
        if (event.isCancelled()) {
           return;
        }

        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(event.getEntity().getWorld());

        if (wcfg.disablePigZap) {
            event.setCancelled(true);
        }
    }

    /**
     * Called on creeper power.
     */
    @Override
    public void onCreeperPower(CreeperPowerEvent event) {
        if (event.isCancelled()) {
           return;
        }

        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(event.getEntity().getWorld());

        if (wcfg.disableCreeperPower) {
            event.setCancelled(true);
        }
    }

    /**
     * Called when a painting is removed.
     */
    @Override
    public void onPaintingBreak(PaintingBreakEvent breakEvent) {
        if (breakEvent.isCancelled()) {
            return;
        }

        if (!(breakEvent instanceof PaintingBreakByEntityEvent)) {
            return;
        }
        
        PaintingBreakByEntityEvent event = (PaintingBreakByEntityEvent) breakEvent;
        if (!(event.getRemover() instanceof Player)) {
            return;
        }
        
        Painting painting= event.getPainting();
        Player player = (Player) event.getRemover();
        World world = painting.getWorld();

        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(world);

        if (wcfg.getBlacklist() != null) {
            if (!wcfg.getBlacklist().check(
                    new BlockBreakBlacklistEvent(plugin.wrapPlayer(player),
                            toVector(player.getLocation()), 321), false, false)) {
                event.setCancelled(true);
                return;
            }
        }

        if (wcfg.useRegions) {
            if (!plugin.getGlobalRegionManager().canBuild(player, painting.getLocation())) {
                player.sendMessage(ChatColor.DARK_RED + "You don't have permission for this area.");
                event.setCancelled(true);
                return;
            }
        }
    }

    /**
     * Called on painting place.
     */
    @Override
    public void onPaintingPlace(PaintingPlaceEvent event) {
        Block placedOn = event.getBlock();
        Player player = event.getPlayer();
        World world = placedOn.getWorld();

        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(world);

        if (wcfg.getBlacklist() != null) {
            if (!wcfg.getBlacklist().check(
                    new ItemUseBlacklistEvent(plugin.wrapPlayer(player),
                            toVector(player.getLocation()), 321), false, false)) {
                event.setCancelled(true);
                return;
            }
        }

        if (wcfg.useRegions) {
            if (!plugin.getGlobalRegionManager().canBuild(player, placedOn.getLocation())) {
                player.sendMessage(ChatColor.DARK_RED + "You don't have permission for this area.");
                event.setCancelled(true);
                return;
            }
        }
    }

    /**
     * Called on entity health regain.
     */
    public void onEntityRegainHealth(EntityRegainHealthEvent event) {
        Entity ent = event.getEntity();
        World world = ent.getWorld();

        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(world);

        if (wcfg.disableHealthRegain) {
            event.setCancelled(true);
            return;
        }
    }

}
