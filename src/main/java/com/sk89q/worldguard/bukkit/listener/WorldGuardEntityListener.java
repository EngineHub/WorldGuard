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

import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.*;
import com.sk89q.worldguard.bukkit.util.Entities;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.entity.minecart.ExplosiveMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;

import java.util.Set;

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

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDeath(EntityDeathEvent event) {
        WorldConfiguration wcfg = plugin.getGlobalStateManager().get(event.getEntity().getWorld());

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
                            || (wcfg.explosionFlagCancellation
                                && !plugin.getGlobalRegionManager().allows(DefaultFlag.OTHER_EXPLOSION, player.getLocation())))) {
                event.setCancelled(true);
                return;
            }
        } else {

            // for whatever reason, plugin-caused explosions with a null entity count as block explosions and aren't
            // handled anywhere else
            if (type == DamageCause.BLOCK_EXPLOSION
                    && (wcfg.blockOtherExplosions
                            || (wcfg.explosionFlagCancellation
                                && !plugin.getGlobalRegionManager().allows(DefaultFlag.OTHER_EXPLOSION, defender.getLocation())))) {
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

        if (defender instanceof ItemFrame) {
            if (checkItemFrameProtection(attacker, (ItemFrame) defender)) {
                event.setCancelled(true);
                return;
            }
        }

        if (defender instanceof Player) {
            Player player = (Player) defender;
            LocalPlayer localPlayer = plugin.wrapPlayer(player);

            ConfigurationManager cfg = plugin.getGlobalStateManager();
            WorldConfiguration wcfg = cfg.get(player.getWorld());

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
                if (attacker instanceof TNTPrimed || attacker instanceof ExplosiveMinecart) {
                    // The check for explosion damage should be handled already... But... What ever...
                    if (wcfg.blockTNTExplosions) {
                        event.setCancelled(true);
                        return;
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
                        RegionQuery query = plugin.getRegionContainer().createQuery();
                        if (!query.testState(defender.getLocation(), (Player) defender, DefaultFlag.GHAST_FIREBALL) && wcfg.explosionFlagCancellation) {
                            event.setCancelled(true);
                            return;
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
                        RegionManager mgr = plugin.getGlobalRegionManager().get(player.getWorld());
                        ApplicableRegionSet set = plugin.getRegionContainer().createQuery().getApplicableRegions(defender.getLocation());

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
                    }
                }
            }
        }
    }

    private void onEntityDamageByProjectile(EntityDamageByEntityEvent event) {
        Entity defender = event.getEntity();
        Entity attacker;
        ProjectileSource source = ((Projectile) event.getDamager()).getShooter();
        if (source instanceof LivingEntity) {
            attacker = (LivingEntity) source;
        } else {
            return;
        }

        if (defender instanceof Player) {
            Player player = (Player) defender;
            LocalPlayer localPlayer = plugin.wrapPlayer(player);

            ConfigurationManager cfg = plugin.getGlobalStateManager();
            WorldConfiguration wcfg = cfg.get(player.getWorld());

            // Check Mob
            if (attacker != null && !(attacker instanceof Player)) {
                if (wcfg.disableMobDamage) {
                    event.setCancelled(true);
                    return;
                }
                if (wcfg.useRegions) {
                    if (!plugin.getRegionContainer().createQuery().getApplicableRegions(defender.getLocation()).allows(DefaultFlag.MOB_DAMAGE, localPlayer)) {
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        } else if (defender instanceof ItemFrame) {
            if (checkItemFrameProtection(attacker, (ItemFrame) defender)) {
                event.setCancelled(true);
                return;
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

            if (type == DamageCause.WITHER) {
                // wither boss DoT tick
                if (wcfg.disableMobDamage) {
                    event.setCancelled(true);
                    return;
                }

                if (wcfg.useRegions) {
                    ApplicableRegionSet set = plugin.getRegionContainer().createQuery().getApplicableRegions(defender.getLocation());

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
        } else if (ent instanceof EnderDragon) {
            if (wcfg.blockEnderDragonBlockDamage) {
                event.blockList().clear();
                return;
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
                    if (!plugin.getRegionContainer().createQuery().getApplicableRegions(block.getLocation()).allows(DefaultFlag.GHAST_FIREBALL)) {
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
                    if (!plugin.getRegionContainer().createQuery().getApplicableRegions(block.getLocation()).allows(DefaultFlag.OTHER_EXPLOSION)) {
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

        // armor stands are living entities, but we check them as blocks/non-living entities, so ignore them here
        if (Entities.isConsideredBuildingIfUsed(event.getEntity())) {
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
            ApplicableRegionSet set = plugin.getRegionContainer().createQuery().getApplicableRegions(eventLoc);

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

    /**
     * Checks regions and config settings to protect items from being knocked
     * out of item frames.
     * @param attacker attacking entity
     * @param defender item frame being damaged
     * @return true if the event should be cancelled
     */
    private boolean checkItemFrameProtection(Entity attacker, ItemFrame defender) {
        World world = attacker.getWorld();
        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(world);
        if (wcfg.useRegions) {
        // bukkit throws this event when a player attempts to remove an item from a frame
            RegionManager mgr = plugin.getGlobalRegionManager().get(world);
            if (!(attacker instanceof Player)) {
                if (!plugin.getGlobalRegionManager().allows(
                        DefaultFlag.ENTITY_ITEM_FRAME_DESTROY, defender.getLocation())) {
                    return true;
                }
            }
        }
        if (wcfg.blockEntityItemFrameDestroy && !(attacker instanceof Player)) {
            return true;
        }
        return false;
    }

}
