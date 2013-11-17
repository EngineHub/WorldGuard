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

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.entity.Wither;
import org.bukkit.entity.WitherSkull;
import org.bukkit.entity.Wolf;
import org.bukkit.entity.minecart.ExplosiveMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.CreeperPowerEvent;
import org.bukkit.event.entity.EntityBreakDoorEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityCreatePortalEvent;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.ExpBottleEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PigZapEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.blacklist.events.ItemUseBlacklistEvent;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.GlobalRegionManager;
import com.sk89q.worldguard.protection.events.DisallowedPVPEvent;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;

/**
 * Listener for entity related events.
 *
 * @author sk89q
 */
public class WorldGuardEntityListener implements Listener {

    private WorldGuardPlugin plugin;

    /**
     * Construct the object;
     *
     * @param plugin The plugin instance
     */
    public WorldGuardEntityListener(WorldGuardPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Register events.
     */
    public void registerEvents() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityInteract(EntityInteractEvent event) {
        Entity entity = event.getEntity();
        Block block = event.getBlock();

        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(entity.getWorld());

        if (block.getTypeId() == BlockID.SOIL) {
            if (/* entity instanceof Creature && // catch for any entity (not thrown for players) */
                wcfg.disableCreatureCropTrampling) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onExpBottle(ExpBottleEvent event) {
        WorldConfiguration wcfg = plugin.getGlobalStateManager().get(event.getEntity().getWorld());

        if (wcfg.disableExpDrops || !plugin.getGlobalRegionManager().allows(DefaultFlag.EXP_DROPS,
                event.getEntity().getLocation())) {
            event.setExperience(0);
            // event.setShowEffect(false); // don't want to cancel the bottle entirely I suppose, just the exp
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDeath(EntityDeathEvent event) {
        WorldConfiguration wcfg = plugin.getGlobalStateManager().get(event.getEntity().getWorld());

        if (wcfg.disableExpDrops || !plugin.getGlobalRegionManager().allows(DefaultFlag.EXP_DROPS,
                event.getEntity().getLocation())) {
            event.setDroppedExp(0);
        }

        if (event instanceof PlayerDeathEvent && wcfg.disableDeathMessages) {
            ((PlayerDeathEvent) event).setDeathMessage("");
        }
    }

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

            if (isInvincible(player)) {
                event.setCancelled(true);
                return;
            }

            if (wcfg.disableLavaDamage && type == DamageCause.LAVA) {
                event.setCancelled(true);
                player.setFireTicks(0);
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

            if (type == DamageCause.BLOCK_EXPLOSION
                    && (wcfg.disableExplosionDamage || wcfg.blockOtherExplosions
                            || !plugin.getGlobalRegionManager().allows(DefaultFlag.OTHER_EXPLOSION, player.getLocation()))) {
                event.setCancelled(true);
                return;
            }
        } else {

            // for whatever reason, plugin-caused explosions with a null entity count as block explosions and aren't
            // handled anywhere else
            if (type == DamageCause.BLOCK_EXPLOSION
                    && (wcfg.blockOtherExplosions
                            || !plugin.getGlobalRegionManager().allows(DefaultFlag.OTHER_EXPLOSION, defender.getLocation()))) {
                event.setCancelled(true);
                return;
            }
        }
    }

    private void onEntityDamageByEntity(EntityDamageByEntityEvent event) {

        if (event.getDamager() instanceof Projectile) {
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
            LocalPlayer localPlayer = plugin.wrapPlayer(player);

            ConfigurationManager cfg = plugin.getGlobalStateManager();
            WorldConfiguration wcfg = cfg.get(player.getWorld());

            if (isInvincible(player)) {
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

            if (wcfg.disableExplosionDamage) {
                switch (event.getCause()) {
                    case BLOCK_EXPLOSION:
                    case ENTITY_EXPLOSION:
                        event.setCancelled(true);
                        return;
                }
            }

            if (attacker != null) {
                if (attacker instanceof Player) {
                    if (wcfg.useRegions) {
                        Vector pt = toVector(defender.getLocation());
                        Vector pt2 = toVector(attacker.getLocation());
                        RegionManager mgr = plugin.getGlobalRegionManager().get(player.getWorld());

                        if (!mgr.getApplicableRegions(pt2).allows(DefaultFlag.PVP, plugin.wrapPlayer((Player) attacker))) {
                            tryCancelPVPEvent((Player) attacker, player, event, true);
                        } else if (!mgr.getApplicableRegions(pt).allows(DefaultFlag.PVP ,localPlayer)) {
                            tryCancelPVPEvent((Player) attacker, player, event, false);
                        }
                    }
                }

                if (attacker instanceof TNTPrimed || attacker instanceof ExplosiveMinecart) {

                    // The check for explosion damage should be handled already... But... What ever...
                    if (wcfg.blockTNTExplosions) {
                        event.setCancelled(true);
                        return;
                    }
                    if (wcfg.useRegions && wcfg.explosionFlagCancellation) {
                        Vector pt = toVector(defender.getLocation());
                        RegionManager mgr = plugin.getGlobalRegionManager().get(player.getWorld());
                        ApplicableRegionSet set = mgr.getApplicableRegions(pt);
                        if (!set.allows(DefaultFlag.TNT, localPlayer)) {
                            event.setCancelled(true);
                            return;
                        }
                    }
                }

                if (attacker instanceof Fireball) {
                    if (attacker instanceof WitherSkull) {
                        if (wcfg.blockWitherSkullExplosions) {
                            event.setCancelled(true);
                            return;
                        }
                    } else {
                        if (wcfg.blockFireballExplosions) {
                            event.setCancelled(true);
                            return;
                        }
                    }
                    if (wcfg.useRegions) {
                        Fireball fireball = (Fireball) attacker;
                        Vector pt = toVector(defender.getLocation());
                        RegionManager mgr = plugin.getGlobalRegionManager().get(player.getWorld());
                        ApplicableRegionSet set = mgr.getApplicableRegions(pt);
                        if (fireball.getShooter() instanceof Player) {
                            Vector pt2 = toVector(fireball.getShooter().getLocation());
                            if (!mgr.getApplicableRegions(pt2).allows(DefaultFlag.PVP, plugin.wrapPlayer((Player) fireball.getShooter()))) {
                                tryCancelPVPEvent((Player) fireball.getShooter(), player, event, true);
                            } else if (!set.allows(DefaultFlag.PVP, localPlayer)) {
                                tryCancelPVPEvent((Player) fireball.getShooter(), player, event, false);
                            }
                        } else {
                            if (!set.allows(DefaultFlag.GHAST_FIREBALL, localPlayer) && wcfg.explosionFlagCancellation) {
                                event.setCancelled(true);
                                return;
                            }
                        }

                    }
                }

                if (attacker instanceof LivingEntity && !(attacker instanceof Player)) {
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

                        if (!set.allows(DefaultFlag.MOB_DAMAGE, localPlayer) && !(attacker instanceof Tameable)) {
                            event.setCancelled(true);
                            return;
                        }

                        if (attacker instanceof Creeper) {
                            if (!set.allows(DefaultFlag.CREEPER_EXPLOSION, localPlayer) && wcfg.explosionFlagCancellation) {
                                event.setCancelled(true);
                                return;
                            }
                        }
                        if (attacker instanceof Tameable) {
                            if (((Tameable) attacker).getOwner() == null) {
                                if (!set.allows(DefaultFlag.MOB_DAMAGE, localPlayer)) {
                                    event.setCancelled(true);
                                    return;
                                }
                            }
                            if (!(((Tameable) attacker).getOwner() instanceof Player)) {
                                return;
                            }
                            Player beastMaster = (Player) ((Tameable) attacker).getOwner();
                            Vector pt2 = toVector(attacker.getLocation());
                            if (!mgr.getApplicableRegions(pt2).allows(DefaultFlag.PVP, plugin.wrapPlayer(beastMaster))) {
                                tryCancelPVPEvent(beastMaster, player, event, true);
                            } else if (!set.allows(DefaultFlag.PVP, localPlayer)) {
                                tryCancelPVPEvent(beastMaster, player, event, false);
                            }
                        }
                    }
                }
            }
        }
    }

    private void onEntityDamageByProjectile(EntityDamageByEntityEvent event) {
        Entity defender = event.getEntity();
        Entity attacker = ((Projectile) event.getDamager()).getShooter();

        if (defender instanceof Player) {
            Player player = (Player) defender;
            LocalPlayer localPlayer = plugin.wrapPlayer(player);

            ConfigurationManager cfg = plugin.getGlobalStateManager();
            WorldConfiguration wcfg = cfg.get(player.getWorld());

            // Check Invincible
            if (isInvincible(player)) {
                event.setCancelled(true);
                return;
            }

            // Check Mob
            if (attacker != null && !(attacker instanceof Player)) {
                if (wcfg.disableMobDamage) {
                    event.setCancelled(true);
                    return;
                }
                if (wcfg.useRegions) {
                    Vector pt = toVector(defender.getLocation());
                    RegionManager mgr = plugin.getGlobalRegionManager().get(player.getWorld());

                    if (!mgr.getApplicableRegions(pt).allows(DefaultFlag.MOB_DAMAGE, localPlayer)) {
                        event.setCancelled(true);
                        return;
                    }
                }
            }

            // Check Player
            // if (event.getDamager() instanceof EnderPearl || event.getDamager() instanceof Snowball) return;
            if (attacker != null && attacker instanceof Player) {
                if (event.getDamager() instanceof EnderPearl && attacker == player) return;
                if (wcfg.useRegions) {
                    Vector pt = toVector(defender.getLocation());
                    Vector pt2 = toVector(attacker.getLocation());
                    RegionManager mgr = plugin.getGlobalRegionManager().get(player.getWorld());

                    if (!mgr.getApplicableRegions(pt2).allows(DefaultFlag.PVP, plugin.wrapPlayer((Player) attacker))) {
                        tryCancelPVPEvent((Player) attacker, player, event, true);
                    } else if (!mgr.getApplicableRegions(pt).allows(DefaultFlag.PVP, localPlayer)) {
                        tryCancelPVPEvent((Player) attacker, player, event, false);
                    }
                }
            }
        }

    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {

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

            if (isInvincible(player)) {
                event.setCancelled(true);
                player.setFireTicks(0);
                return;
            }

            if (type == DamageCause.WITHER) {
                // wither boss DoT tick
                if (wcfg.disableMobDamage) {
                    event.setCancelled(true);
                    return;
                }

                if (wcfg.useRegions) {
                    Vector pt = toVector(defender.getLocation());
                    RegionManager mgr = plugin.getGlobalRegionManager().get(player.getWorld());
                    ApplicableRegionSet set = mgr.getApplicableRegions(pt);

                    if (!set.allows(DefaultFlag.MOB_DAMAGE, plugin.wrapPlayer(player))) {
                        event.setCancelled(true);
                        return;
                    }
                }
            }

            if (type == DamageCause.DROWNING && cfg.hasAmphibiousMode(player)) {
                player.setRemainingAir(player.getMaximumAir());
                event.setCancelled(true);
                return;
            }

            ItemStack helmet = player.getInventory().getHelmet();

            if (type == DamageCause.DROWNING && wcfg.pumpkinScuba
                    && helmet != null
                    && (helmet.getTypeId() == BlockID.PUMPKIN
                    || helmet.getTypeId() == BlockID.JACKOLANTERN)) {
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

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityCombust(EntityCombustEvent event) {
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

    /*
     * Called on entity explode.
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        ConfigurationManager cfg = plugin.getGlobalStateManager();
        Location l = event.getLocation();
        World world = l.getWorld();
        WorldConfiguration wcfg = cfg.get(world);
        Entity ent = event.getEntity();

        if (cfg.activityHaltToggle) {
            if (ent != null) {
                ent.remove();
            }
            event.setCancelled(true);
            return;
        }

        if (ent instanceof Creeper) {
            if (wcfg.blockCreeperExplosions) {
                event.setCancelled(true);
                return;
            }
            if (wcfg.blockCreeperBlockDamage) {
                event.blockList().clear();
                return;
            }

            if (wcfg.useRegions) {
                RegionManager mgr = plugin.getGlobalRegionManager().get(world);

                for (Block block : event.blockList()) {
                    if (!mgr.getApplicableRegions(toVector(block)).allows(DefaultFlag.CREEPER_EXPLOSION)) {
                        event.blockList().clear();
                        if (wcfg.explosionFlagCancellation) event.setCancelled(true);
                        return;
                    }
                }
            }
        } else if (ent instanceof EnderDragon) {
            if (wcfg.blockEnderDragonBlockDamage) {
                event.blockList().clear();
                return;
            }

            if (wcfg.useRegions) {
                RegionManager mgr = plugin.getGlobalRegionManager().get(world);

                for (Block block : event.blockList()) {
                    if (!mgr.getApplicableRegions(toVector(block)).allows(DefaultFlag.ENDERDRAGON_BLOCK_DAMAGE)) {
                        event.blockList().clear();
                        if (wcfg.explosionFlagCancellation) event.setCancelled(true);
                        return;
                    }
                }
            }
        } else if (ent instanceof TNTPrimed || ent instanceof ExplosiveMinecart) {
            if (wcfg.blockTNTExplosions) {
                event.setCancelled(true);
                return;
            }
            if (wcfg.blockTNTBlockDamage) {
                event.blockList().clear();
                return;
            }

            if (wcfg.useRegions) {
                RegionManager mgr = plugin.getGlobalRegionManager().get(world);

                for (Block block : event.blockList()) {
                    if (!mgr.getApplicableRegions(toVector(block)).allows(DefaultFlag.TNT)) {
                        event.blockList().clear();
                        if (wcfg.explosionFlagCancellation) event.setCancelled(true);
                        return;
                    }
                }
            }
        } else if (ent instanceof Fireball) {
            if (ent instanceof WitherSkull) {
                if (wcfg.blockWitherSkullExplosions) {
                    event.setCancelled(true);
                    return;
                }
                if (wcfg.blockWitherSkullBlockDamage) {
                    event.blockList().clear();
                    return;
                }
            } else {
                if (wcfg.blockFireballExplosions) {
                    event.setCancelled(true);
                    return;
                }
                if (wcfg.blockFireballBlockDamage) {
                    event.blockList().clear();
                    return;
                }
            }
            // allow wither skull blocking since there is no dedicated flag atm
            if (wcfg.useRegions) {
                RegionManager mgr = plugin.getGlobalRegionManager().get(world);

                for (Block block : event.blockList()) {
                    if (!mgr.getApplicableRegions(toVector(block)).allows(DefaultFlag.GHAST_FIREBALL)) {
                        event.blockList().clear();
                        if (wcfg.explosionFlagCancellation) event.setCancelled(true);
                        return;
                    }
                }
            }
        } else if (ent instanceof Wither) {
            if (wcfg.blockWitherExplosions) {
                event.setCancelled(true);
                return;
            }
            if (wcfg.blockWitherBlockDamage) {
                event.blockList().clear();
                return;
            }
        } else {
            // unhandled entity
            if (wcfg.blockOtherExplosions) {
                event.setCancelled(true);
                return;
            }
            if (wcfg.useRegions) {
                RegionManager mgr = plugin.getGlobalRegionManager().get(world);
                for (Block block : event.blockList()) {
                    if (!mgr.getApplicableRegions(toVector(block)).allows(DefaultFlag.OTHER_EXPLOSION)) {
                        event.blockList().clear();
                        if (wcfg.explosionFlagCancellation) event.setCancelled(true);
                        return;
                    }
                }
            }
        }


        if (wcfg.signChestProtection) {
            for (Block block : event.blockList()) {
                if (wcfg.isChestProtected(block)) {
                    event.blockList().clear();
                    return;
                }
            }
        }

    }

    /*
     * Called on explosion prime
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onExplosionPrime(ExplosionPrimeEvent event) {
        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(event.getEntity().getWorld());
        Entity ent = event.getEntity();

        if (cfg.activityHaltToggle) {
            ent.remove();
            event.setCancelled(true);
            return;
        }

        if (event.getEntityType() == EntityType.WITHER) {
            if (wcfg.blockWitherExplosions) {
                event.setCancelled(true);
                return;
            }
        } else if (event.getEntityType() == EntityType.WITHER_SKULL) {
            if (wcfg.blockWitherSkullExplosions) {
                event.setCancelled(true);
                return;
            }
        } else if (event.getEntityType() == EntityType.FIREBALL) {
            if (wcfg.blockFireballExplosions) {
                event.setCancelled(true);
                return;
            }
        } else if (event.getEntityType() == EntityType.CREEPER) {
            if (wcfg.blockCreeperExplosions) {
                event.setCancelled(true);
                return;
            }
        } else if (event.getEntityType() == EntityType.PRIMED_TNT
                || event.getEntityType() == EntityType.MINECART_TNT) {
            if (wcfg.blockTNTExplosions) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        ConfigurationManager cfg = plugin.getGlobalStateManager();

        if (cfg.activityHaltToggle) {
            event.setCancelled(true);
            return;
        }

        WorldConfiguration wcfg = cfg.get(event.getEntity().getWorld());

        // allow spawning of creatures from plugins
        if (!wcfg.blockPluginSpawning && event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.CUSTOM) {
            return;
        }

        if (wcfg.allowTamedSpawns
                && event.getEntity() instanceof Tameable // nullsafe check
                && ((Tameable) event.getEntity()).isTamed()) {
            return;
        }

        EntityType entityType = event.getEntityType();

        if (wcfg.blockCreatureSpawn.contains(entityType)) {
            event.setCancelled(true);
            return;
        }

        Location eventLoc = event.getLocation();

        if (wcfg.useRegions && cfg.useRegionsCreatureSpawnEvent) {
            Vector pt = toVector(eventLoc);
            RegionManager mgr = plugin.getGlobalRegionManager().get(eventLoc.getWorld());
            // @TODO get victims' stacktraces and find out why it's null anyway
            if (mgr == null) return;
            ApplicableRegionSet set = mgr.getApplicableRegions(pt);

            if (!set.allows(DefaultFlag.MOB_SPAWNING)) {
                event.setCancelled(true);
                return;
            }

            Set<EntityType> entityTypes = set.getFlag(DefaultFlag.DENY_SPAWN);
            if (entityTypes != null && entityTypes.contains(entityType)) {
                event.setCancelled(true);
                return;
            }
        }

        if (wcfg.blockGroundSlimes && entityType == EntityType.SLIME
                && eventLoc.getY() >= 60
                && event.getSpawnReason() == SpawnReason.NATURAL) {
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onCreatePortal(EntityCreatePortalEvent event) {
        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(event.getEntity().getWorld());

        switch (event.getEntityType()) {
            case ENDER_DRAGON:
                if (wcfg.blockEnderDragonPortalCreation) event.setCancelled(true);
                break;
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPigZap(PigZapEvent event) {
        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(event.getEntity().getWorld());

        if (wcfg.disablePigZap) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onCreeperPower(CreeperPowerEvent event) {
        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(event.getEntity().getWorld());

        if (wcfg.disableCreeperPower) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
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

    /**
     * Called when an entity changes a block somehow
     *
     * @param event Relevant event details
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        Entity ent = event.getEntity();
        Block block = event.getBlock();
        Location location = block.getLocation();

        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(ent.getWorld());
        if (ent instanceof Enderman) {
            if (wcfg.disableEndermanGriefing) {
                event.setCancelled(true);
                return;
            }

            if (wcfg.useRegions) {
                if (!plugin.getGlobalRegionManager().allows(DefaultFlag.ENDER_BUILD, location)) {
                    event.setCancelled(true);
                    return;
                }
            }
        } else if (ent.getType() == EntityType.WITHER) {
            if (wcfg.blockWitherBlockDamage || wcfg.blockWitherExplosions) {
                event.setCancelled(true);
                return;
            }
        } else if (/*ent instanceof Zombie && */event instanceof EntityBreakDoorEvent) {
            if (wcfg.blockZombieDoorDestruction) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (event.getFoodLevel() < player.getFoodLevel() && isInvincible(player)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPotionSplash(PotionSplashEvent event) {
        Entity entity = event.getEntity();
        ThrownPotion potion = event.getPotion();

        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(entity.getWorld());

        if (wcfg.blockPotionsAlways && wcfg.blockPotions.size() > 0) {
            boolean blocked = false;

            for (PotionEffect effect : potion.getEffects()) {
                if (wcfg.blockPotions.contains(effect.getType())) {
                    blocked = true;
                    break;
                }
            }

            if (blocked) {
                event.setCancelled(true);
                return;
            }
        }

        GlobalRegionManager regionMan = plugin.getGlobalRegionManager();

        int blockedEntities = 0;
        for (LivingEntity e : event.getAffectedEntities()) {
            if (!regionMan.allows(DefaultFlag.POTION_SPLASH, e.getLocation(),
                    e instanceof Player ? plugin.wrapPlayer((Player) e) : null)) {
                event.setIntensity(e, 0);
                ++blockedEntities;
            }
        }

        if (blockedEntities == event.getAffectedEntities().size()) {
            event.setCancelled(true);
        }
    }

    /**
     * Check if a player is invincible, via either god mode or region flag. If
     * the region denies invincibility, the player must have an extra permission
     * to override it. (worldguard.god.override-regions)
     *
     * @param player The player to check
     * @return Whether {@code player} is invincible
     */
    private boolean isInvincible(Player player) {
        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(player.getWorld());

        boolean god = cfg.hasGodMode(player);
        if (wcfg.useRegions) {
            Boolean flag = RegionQueryUtil.isAllowedInvinciblity(plugin, player);
            boolean allowed = flag == null || flag;
            boolean invincible = RegionQueryUtil.isInvincible(plugin, player);

            if (allowed) {
                return god || invincible;
            } else {
                return (god && plugin.hasPermission(player, "worldguard.god.override-regions"))
                    || invincible;
            }
        } else {
            return god;
        }
    }

    /**
     * Using a DisallowedPVPEvent, notifies other plugins that WorldGuard
     * wants to cancel a PvP damage event.<br />
     * If this event is not cancelled, the attacking player is notified that
     * PvP is disabled and WorldGuard cancels the damage event.
     *
     * @param attackingPlayer The attacker
     * @param defendingPlayer The defender
     * @param event The event that caused WorldGuard to act
     */
    public void tryCancelPVPEvent(final Player attackingPlayer, final Player defendingPlayer, EntityDamageByEntityEvent event, boolean aggressorTriggered) {
        final DisallowedPVPEvent disallowedPVPEvent = new DisallowedPVPEvent(attackingPlayer, defendingPlayer, event);
        plugin.getServer().getPluginManager().callEvent(disallowedPVPEvent);
        if (!disallowedPVPEvent.isCancelled()) {
            if (aggressorTriggered) attackingPlayer.sendMessage(ChatColor.DARK_RED + "You are in a no-PvP area.");
            else attackingPlayer.sendMessage(ChatColor.DARK_RED + "That player is in a no-PvP area.");
            event.setCancelled(true);
        }
    }
}
