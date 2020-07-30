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
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.BukkitWorldConfiguration;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.bukkit.util.Entities;
import com.sk89q.worldguard.config.ConfigurationManager;
import com.sk89q.worldguard.config.WorldConfiguration;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.association.RegionAssociable;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.Tameable;
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
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.event.entity.EntityTransformEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PigZapEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.world.PortalCreateEvent;
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

        ConfigurationManager cfg = WorldGuard.getInstance().getPlatform().getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(BukkitAdapter.adapt(entity.getWorld()));

        if (block.getType() == Material.FARMLAND && wcfg.disableCreatureCropTrampling) {
            event.setCancelled(true);
            return;
        }
        if (block.getType() == Material.TURTLE_EGG && wcfg.disableCreatureTurtleEggTrampling) {
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDeath(EntityDeathEvent event) {
        WorldConfiguration wcfg =
                WorldGuard.getInstance().getPlatform().getGlobalStateManager().get(BukkitAdapter.adapt(event.getEntity().getWorld()));

        if (event instanceof PlayerDeathEvent && wcfg.disableDeathMessages) {
            ((PlayerDeathEvent) event).setDeathMessage("");
        }
    }

    private void onEntityDamageByBlock(EntityDamageByBlockEvent event) {
        Entity defender = event.getEntity();
        DamageCause type = event.getCause();

        ConfigurationManager cfg = WorldGuard.getInstance().getPlatform().getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(BukkitAdapter.adapt(defender.getWorld()));

        if (defender instanceof Wolf && ((Wolf) defender).isTamed()) {
            if (wcfg.antiWolfDumbness && !(type == DamageCause.VOID)) {
                event.setCancelled(true);
                return;
            }
        } else if (defender instanceof Player) {
            Player player = (Player) defender;
            LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);

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
                localPlayer.findFreePosition();
                if (wcfg.safeFallOnVoid) {
                    localPlayer.resetFallDistance();
                }
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
                                && !StateFlag.test(WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery()
                    .queryState(localPlayer.getLocation(), (RegionAssociable) null, Flags.OTHER_EXPLOSION))))) {
                event.setCancelled(true);
                return;
            }
        } else {

            // for whatever reason, plugin-caused explosions with a null entity count as block explosions and aren't
            // handled anywhere else
            if (type == DamageCause.BLOCK_EXPLOSION
                    && (wcfg.blockOtherExplosions
                            || (wcfg.explosionFlagCancellation
                                && !StateFlag.test(WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery()
                    .queryState(BukkitAdapter.adapt(defender.getLocation()), (RegionAssociable) null, Flags.OTHER_EXPLOSION))))) {
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

        WorldConfiguration wcfg =
                WorldGuard.getInstance().getPlatform().getGlobalStateManager().get(BukkitAdapter.adapt(defender.getWorld()));

        if (defender instanceof ItemFrame) {
            if (checkItemFrameProtection(attacker, (ItemFrame) defender)) {
                event.setCancelled(true);
                return;
            }
        } else if (defender instanceof ArmorStand && !(attacker instanceof Player)) {
            if (wcfg.blockEntityArmorStandDestroy) {
                event.setCancelled(true);
                return;
            }
        }

        if (attacker instanceof EnderCrystal) {
            // this isn't handled elsewhere because ender crystal explosions don't carry a player cause
            // in the same way that creepers or tnt can
            if (wcfg.useRegions && wcfg.explosionFlagCancellation) {
                if (!WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery().getApplicableRegions(BukkitAdapter.adapt(defender.getLocation()))
                        .testState(null, Flags.OTHER_EXPLOSION)) {
                    event.setCancelled(true);
                    return;
                }
            }
        }

        if (defender instanceof Player) {
            Player player = (Player) defender;
            LocalPlayer localPlayer = plugin.wrapPlayer(player);

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
                        ApplicableRegionSet set = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery().getApplicableRegions(localPlayer.getLocation());

                        if (!set.testState(localPlayer, Flags.MOB_DAMAGE) && !(attacker instanceof Tameable)) {
                            event.setCancelled(true);
                            return;
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

        ConfigurationManager cfg = WorldGuard.getInstance().getPlatform().getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(BukkitAdapter.adapt(defender.getWorld()));
        if (defender instanceof Player) {
            Player player = (Player) defender;
            LocalPlayer localPlayer = plugin.wrapPlayer(player);


            // Check Mob
            if (!(attacker instanceof Player)) {
                if (wcfg.disableMobDamage) {
                    event.setCancelled(true);
                    return;
                }
                if (wcfg.useRegions) {
                    if (!WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery().getApplicableRegions(localPlayer.getLocation()).testState(localPlayer, Flags.MOB_DAMAGE)) {
                        event.setCancelled(true);
                        return;
                    }
                }
                if (event.getDamager() instanceof Fireball) {
                    Fireball fireball = (Fireball) event.getDamager();
                    if (fireball instanceof WitherSkull) {
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
                        RegionQuery query = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery();
                        if (!query.testState(localPlayer.getLocation(), localPlayer, Flags.GHAST_FIREBALL) && wcfg.explosionFlagCancellation) {
                            event.setCancelled(true);
                            return;
                        }

                    }
                }
            }
        } else if (defender instanceof ItemFrame) {
            if (checkItemFrameProtection(attacker, (ItemFrame) defender)) {
                event.setCancelled(true);
                return;
            }
        } else if (defender instanceof ArmorStand && Entities.isNonPlayerCreature(attacker)) {
            if (wcfg.blockEntityArmorStandDestroy) {
                event.setCancelled(true);
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

        ConfigurationManager cfg = WorldGuard.getInstance().getPlatform().getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(BukkitAdapter.adapt(defender.getWorld()));

        if (defender instanceof Wolf && ((Wolf) defender).isTamed()) {
            if (wcfg.antiWolfDumbness) {
                event.setCancelled(true);
                return;
            }
        } else if (defender instanceof Player) {
            Player player = (Player) defender;
            LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);

            if (type == DamageCause.WITHER) {
                // wither boss DoT tick
                if (wcfg.disableMobDamage) {
                    event.setCancelled(true);
                    return;
                }

                if (wcfg.useRegions) {
                    ApplicableRegionSet set = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery().getApplicableRegions(localPlayer.getLocation());

                    if (!set.testState(plugin.wrapPlayer(player), Flags.MOB_DAMAGE)) {
                        event.setCancelled(true);
                        return;
                    }
                }
            }

            if (type == DamageCause.DROWNING && cfg.hasAmphibiousMode(localPlayer)) {
                player.setRemainingAir(player.getMaximumAir());
                event.setCancelled(true);
                return;
            }

            ItemStack helmet = player.getInventory().getHelmet();

            if (type == DamageCause.DROWNING && wcfg.pumpkinScuba
                    && helmet != null
                    && (helmet.getType() == Material.CARVED_PUMPKIN
                    || helmet.getType() == Material.JACK_O_LANTERN)) {
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
                localPlayer.findFreePosition();
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
        ConfigurationManager cfg = WorldGuard.getInstance().getPlatform().getGlobalStateManager();
        Location l = event.getLocation();
        World world = l.getWorld();
        BukkitWorldConfiguration wcfg = (BukkitWorldConfiguration) cfg.get(BukkitAdapter.adapt(world));
        Entity ent = event.getEntity();

        if (cfg.activityHaltToggle) {
            ent.remove();
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
                for (Block block : event.blockList()) {
                    if (!WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery().getApplicableRegions(BukkitAdapter.adapt(block.getLocation())).testState(null, Flags.GHAST_FIREBALL)) {
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
            if (wcfg.useRegions) {
                for (Block block : event.blockList()) {
                    if (!StateFlag.test(WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery().queryState(BukkitAdapter.adapt(block.getLocation()),
                            (RegionAssociable) null, Flags.WITHER_DAMAGE))) {
                        event.blockList().clear();
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        } else {
            // unhandled entity
            if (wcfg.blockOtherExplosions) {
                event.setCancelled(true);
                return;
            }
            if (wcfg.useRegions) {
                for (Block block : event.blockList()) {
                    if (!WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery().getApplicableRegions(BukkitAdapter.adapt(block.getLocation())).testState(null, Flags.OTHER_EXPLOSION)) {
                        event.blockList().clear();
                        if (wcfg.explosionFlagCancellation) event.setCancelled(true);
                        return;
                    }
                }
            }
        }


        if (wcfg.signChestProtection) {
            for (Block block : event.blockList()) {
                if (wcfg.isChestProtected(BukkitAdapter.adapt(block.getLocation()))) {
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
        ConfigurationManager cfg = WorldGuard.getInstance().getPlatform().getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(BukkitAdapter.adapt(event.getEntity().getWorld()));
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
        ConfigurationManager cfg = WorldGuard.getInstance().getPlatform().getGlobalStateManager();

        if (cfg.activityHaltToggle) {
            event.setCancelled(true);
            return;
        }

        WorldConfiguration wcfg = cfg.get(BukkitAdapter.adapt(event.getEntity().getWorld()));

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

        com.sk89q.worldedit.world.entity.EntityType weEntityType = BukkitAdapter.adapt(entityType);

        if (weEntityType != null && wcfg.blockCreatureSpawn.contains(weEntityType)) {
            event.setCancelled(true);
            return;
        }

        Location eventLoc = event.getLocation();

        if (wcfg.useRegions && cfg.useRegionsCreatureSpawnEvent) {
            ApplicableRegionSet set =
                    WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery().getApplicableRegions(BukkitAdapter.adapt(eventLoc));

            if (!set.testState(null, Flags.MOB_SPAWNING)) {
                event.setCancelled(true);
                return;
            }

            Set<com.sk89q.worldedit.world.entity.EntityType> entityTypes = set.queryValue(null, Flags.DENY_SPAWN);
            if (entityTypes != null && weEntityType != null && entityTypes.contains(weEntityType)) {
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
    public void onCreatePortal(PortalCreateEvent event) {
        ConfigurationManager cfg = WorldGuard.getInstance().getPlatform().getGlobalStateManager();
        final com.sk89q.worldedit.world.World world = BukkitAdapter.adapt(event.getWorld());
        WorldConfiguration wcfg = cfg.get(world);

        if (wcfg.regionNetherPortalProtection
                && event.getReason() == PortalCreateEvent.CreateReason.NETHER_PAIR
                && !event.getBlocks().isEmpty()) {
            final RegionManager regionManager = WorldGuard.getInstance().getPlatform().getRegionContainer()
                    .get(world);
            if (regionManager == null) return;
            LocalPlayer associable = null;
            if (event.getEntity() instanceof Player) {
                associable = plugin.wrapPlayer(((Player) event.getEntity()));
                if (WorldGuard.getInstance().getPlatform().getSessionManager().hasBypass(associable, world)) {
                    return;
                }
            }
            BlockVector3 min = null;
            BlockVector3 max = null;
            for (BlockState block : event.getBlocks()) {
                BlockVector3 loc = BlockVector3.at(block.getX(), block.getY(), block.getZ());
                min = min == null ? loc : loc.getMinimum(min);
                max = max == null ? loc : loc.getMaximum(max);
            }
            ProtectedCuboidRegion target = new ProtectedCuboidRegion("__portal_check", true, min, max);
            final ApplicableRegionSet regions = regionManager.getApplicableRegions(target);
            if (!regions.testState(associable, Flags.BUILD, Flags.BLOCK_PLACE)) {
                if (associable != null) {
                    // NB there is no way to cancel the teleport without PTA (since PlayerPortal doesn't have block info)
                    // removing PTA was a mistake
                    associable.print("Destination is an a protected area.");
                }
                event.setCancelled(true);
            }
        }

        // NOTE: as of right now, bukkit doesn't fire this event for this (despite deprecating EntityCreatePortalEvent for it)
        // maybe one day this code will be useful
        if (event.getEntity() instanceof EnderDragon && wcfg.blockEnderDragonPortalCreation) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityTransform(EntityTransformEvent event) {
        ConfigurationManager cfg = WorldGuard.getInstance().getPlatform().getGlobalStateManager();
        final Entity entity = event.getEntity();
        WorldConfiguration wcfg = cfg.get(BukkitAdapter.adapt(entity.getWorld()));

        final EntityType type = entity.getType();
        if (wcfg.disableVillagerZap && type == EntityType.VILLAGER
                && event.getTransformReason() == EntityTransformEvent.TransformReason.LIGHTNING) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPigZap(PigZapEvent event) {
        ConfigurationManager cfg = WorldGuard.getInstance().getPlatform().getGlobalStateManager();
        final Entity entity = event.getEntity();
        WorldConfiguration wcfg = cfg.get(BukkitAdapter.adapt(entity.getWorld()));

        if (wcfg.disablePigZap) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onCreeperPower(CreeperPowerEvent event) {
        ConfigurationManager cfg = WorldGuard.getInstance().getPlatform().getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(BukkitAdapter.adapt(event.getEntity().getWorld()));

        if (wcfg.disableCreeperPower) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityRegainHealth(EntityRegainHealthEvent event) {
        RegainReason regainReason = event.getRegainReason();
        if (regainReason != RegainReason.REGEN && regainReason != RegainReason.SATIATED) {
            return;
        }

        Entity ent = event.getEntity();
        World world = ent.getWorld();

        ConfigurationManager cfg = WorldGuard.getInstance().getPlatform().getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(BukkitAdapter.adapt(world));

        if (wcfg.disableHealthRegain) {
            event.setCancelled(true);
            return;
        }
        if (wcfg.useRegions && ent instanceof Player
                && !WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery().testState(
                        BukkitAdapter.adapt(ent.getLocation()),
                        WorldGuardPlugin.inst().wrapPlayer((Player) ent),
                        Flags.HEALTH_REGEN)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onFoodChange(FoodLevelChangeEvent event) {
        if (event.getItem() != null) return;
        HumanEntity ent = event.getEntity();
        if (!(ent instanceof Player)) return;
        LocalPlayer player = WorldGuardPlugin.inst().wrapPlayer((Player) ent);
        if (event.getFoodLevel() > player.getFoodLevel()) return;

        ConfigurationManager cfg = WorldGuard.getInstance().getPlatform().getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(player.getWorld());

        if (wcfg.useRegions
                && !WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery().testState(
                        player.getLocation(), player, Flags.HUNGER_DRAIN)) {
            event.setCancelled(true);
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

        ConfigurationManager cfg = WorldGuard.getInstance().getPlatform().getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(BukkitAdapter.adapt(ent.getWorld()));
        if (ent instanceof FallingBlock) {
            Material id = event.getBlock().getType();

            if (id == Material.GRAVEL && wcfg.noPhysicsGravel) {
                event.setCancelled(true);
                return;
            }

            if (id == Material.SAND && wcfg.noPhysicsSand) {
                event.setCancelled(true);
                return;
            }
        } else if (ent instanceof Enderman) {
            if (wcfg.disableEndermanGriefing) {
                event.setCancelled(true);
                return;
            }
        } else if (ent.getType() == EntityType.WITHER) {
            if (wcfg.blockWitherBlockDamage || wcfg.blockWitherExplosions) {
                event.setCancelled(true);
                return;
            }
            if (wcfg.useRegions) {
                if (!StateFlag.test(WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery().queryState(BukkitAdapter.adapt(location), (RegionAssociable) null, Flags.WITHER_DAMAGE))) {
                    event.setCancelled(true);
                    return;
                }
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
        ConfigurationManager cfg = WorldGuard.getInstance().getPlatform().getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(BukkitAdapter.adapt(world));
        if (wcfg.useRegions) {
            // bukkit throws this event when a player attempts to remove an item from a frame
            if (!(attacker instanceof Player)) {
                if (!StateFlag.test(WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery().queryState(BukkitAdapter.adapt(defender.getLocation()), (RegionAssociable) null, Flags.ENTITY_ITEM_FRAME_DESTROY))) {
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
