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

import java.util.Iterator;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
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
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreeperPowerEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PigZapEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.inventory.ItemStack;

import com.sk89q.rulelists.KnownAttachment;
import com.sk89q.rulelists.RuleSet;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.blacklist.events.ItemUseBlacklistEvent;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.GlobalRegionManager;
import com.sk89q.worldguard.protection.events.DisallowedPVPEvent;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;

/**
 * The listener for entity events.
 */
public class WorldGuardEntityListener implements Listener {

    private WorldGuardPlugin plugin;

    public WorldGuardEntityListener(WorldGuardPlugin plugin) {
        this.plugin = plugin;
    }

    public void registerEvents() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityInteract(EntityInteractEvent event) {
        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(event.getEntity().getWorld());

        // RuleLists
        RuleSet rules = wcfg.getRuleList().get(KnownAttachment.BLOCK_INTERACT);
        BukkitContext context = new BukkitContext(event);
        context.setSourceEntity(event.getEntity());
        context.setTargetBlock(event.getBlock().getState());
        if (rules.process(context)) {
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDeath(EntityDeathEvent event) {
        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(event.getEntity().getWorld());

        // RuleLists
        RuleSet rules = wcfg.getRuleList().get(KnownAttachment.ENTITY_DEATH);
        BukkitContext context = new BukkitContext(event);
        context.setTargetEntity(event.getEntity());

        // Set a message if it's a death from a player
        if (event instanceof PlayerDeathEvent) {
            context.setMessage(((PlayerDeathEvent) event).getDeathMessage());
        }

        rules.process(context);

        // Set the message back
        if (event instanceof PlayerDeathEvent) {
            ((PlayerDeathEvent) event).setDeathMessage(context.getMessage());
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(event.getEntity().getWorld());

        // Redirect damage done by an entity
        if (event instanceof EntityDamageByEntityEvent) {
            this.onEntityDamageByEntity((EntityDamageByEntityEvent) event);

            // RuleLists
            RuleSet rules = wcfg.getRuleList().get(KnownAttachment.ENTITY_DAMAGE);
            BukkitContext context = new BukkitContext(event);
            context.setSourceEntity(((EntityDamageByEntityEvent) event).getDamager());
            context.setTargetEntity(event.getEntity());
            if (rules.process(context)) {
                event.setCancelled(true);
                return;
            }

        // Redirect damage done by blocks
        } else if (event instanceof EntityDamageByBlockEvent) {
            Entity defender = event.getEntity();

            // God-mode/amphibious mode
            if (defender instanceof Player) {
                Player player = (Player) defender;

                if (isInvincible(player)) {
                    event.setCancelled(true);
                    return;
                }
            }

            // RuleLists
            RuleSet rules = wcfg.getRuleList().get(KnownAttachment.ENTITY_DAMAGE);
            BukkitContext context = new BukkitContext(event);
            Block damager = ((EntityDamageByBlockEvent) event).getDamager();
            if (damager != null) { // Should NOT be null!
                context.setSourceBlock(damager.getState());
            }
            context.setTargetEntity(event.getEntity());
            if (rules.process(context)) {
                event.setCancelled(true);
                return;
            }

        // Other damage
        } else {
            Entity defender = event.getEntity();
            DamageCause type = event.getCause();

            // God-mode/amphibious mode
            if (defender instanceof Player) {
                Player player = (Player) defender;

                if (isInvincible(player)) {
                    event.setCancelled(true);
                    player.setFireTicks(0);
                    return;
                }

                if (type == DamageCause.DROWNING && cfg.hasAmphibiousMode(player)) {
                    player.setRemainingAir(player.getMaximumAir());
                    event.setCancelled(true);
                    return;
                }
            }

            // RuleLists
            RuleSet rules = wcfg.getRuleList().get(KnownAttachment.ENTITY_DAMAGE);
            BukkitContext context = new BukkitContext(event);
            context.setTargetEntity(event.getEntity());
            if (rules.process(context)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    /**
     * Handle a damage by entity event.
     *
     * @param event event
     */
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

            if (attacker != null && attacker instanceof Player) {
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

            if (attacker != null && attacker instanceof TNTPrimed) {
                if (wcfg.useRegions) {
                    Vector pt = toVector(defender.getLocation());
                    RegionManager mgr = plugin.getGlobalRegionManager().get(player.getWorld());
                    ApplicableRegionSet set = mgr.getApplicableRegions(pt);
                    if (!set.allows(DefaultFlag.TNT, localPlayer)) {
                        event.setCancelled(true);
                        return;
                    }
                }
            }

            if (attacker != null && attacker instanceof Fireball) {
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
                        if (!set.allows(DefaultFlag.GHAST_FIREBALL, localPlayer)) {
                            event.setCancelled(true);
                            return;
                        }
                    }

                }
            }

            if (attacker != null && attacker instanceof LivingEntity && !(attacker instanceof Player)) {
                if (wcfg.useRegions) {
                    Vector pt = toVector(defender.getLocation());
                    RegionManager mgr = plugin.getGlobalRegionManager().get(player.getWorld());
                    ApplicableRegionSet set = mgr.getApplicableRegions(pt);

                    if (!set.allows(DefaultFlag.MOB_DAMAGE, localPlayer)) {
                        event.setCancelled(true);
                        return;
                    }

                    if (attacker instanceof Creeper) {
                        if (!set.allows(DefaultFlag.CREEPER_EXPLOSION, localPlayer)) {
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
            if (attacker != null && attacker instanceof LivingEntity && !(attacker instanceof Player)) {
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
            if (event.getDamager() instanceof EnderPearl) return;
            if (attacker != null && attacker instanceof Player) {
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
    public void onEntityCombust(EntityCombustEvent event) {
        Entity entity = event.getEntity();

        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(entity.getWorld());

        // God mode and regions
        if (entity instanceof Player) {
            Player player = (Player) entity;

            if (cfg.hasGodMode(player) || (wcfg.useRegions && RegionQueryUtil.isInvincible(plugin, player))) {
                event.setCancelled(true);
                return;
            }
        }

        // RuleLists
        RuleSet rules = wcfg.getRuleList().get(KnownAttachment.ENTITY_IGNITE);
        BukkitContext context = new BukkitContext(event);
        context.setTargetEntity(entity);
        if (rules.process(context)) {
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        ConfigurationManager cfg = plugin.getGlobalStateManager();
        Location l = event.getLocation();
        World world = l.getWorld();
        WorldConfiguration wcfg = cfg.get(world);
        Entity ent = event.getEntity();

        // RuleLists
        RuleSet rules = wcfg.getRuleList().get(KnownAttachment.ENTITY_EXPLODE);
        BukkitContext context = new BukkitContext(event);
        context.setTargetEntity(event.getEntity());
        if (rules.process(context)) {
            event.setCancelled(true);
            return;
        }

        /* --- No short-circuit returns below this line --- */

        // Regions
        if (ent instanceof Creeper) {
            if (wcfg.useRegions) {
                if (wcfg.useRegions) {
                    RegionManager mgr = plugin.getGlobalRegionManager().get(world);

                    for (Block block : event.blockList()) {
                        if (!mgr.getApplicableRegions(toVector(block)).allows(DefaultFlag.CREEPER_EXPLOSION)) {
                            event.blockList().clear();
                            return;
                        }
                    }
                }
            }
        } else if (ent instanceof EnderDragon) {
            if (wcfg.useRegions) {
                RegionManager mgr = plugin.getGlobalRegionManager().get(world);

                for (Block block : event.blockList()) {
                    if (!mgr.getApplicableRegions(toVector(block)).allows(DefaultFlag.ENDERDRAGON_BLOCK_DAMAGE)) {
                        event.blockList().clear();
                        return;
                    }
                }
            }
        } else if (ent instanceof TNTPrimed) {
            if (wcfg.useRegions) {
                RegionManager mgr = plugin.getGlobalRegionManager().get(world);

                for (Block block : event.blockList()) {
                    if (!mgr.getApplicableRegions(toVector(block)).allows(DefaultFlag.TNT)) {
                        event.blockList().clear();
                        return;
                    }
                }
            }
        } else if (ent instanceof Fireball) {
            if (wcfg.useRegions) {
                RegionManager mgr = plugin.getGlobalRegionManager().get(world);

                for (Block block : event.blockList()) {
                    if (!mgr.getApplicableRegions(toVector(block)).allows(DefaultFlag.GHAST_FIREBALL)) {
                        event.blockList().clear();
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

        // Now apply RuleLists for each block
        Iterator<Block> iter = event.blockList().iterator();
        while (iter.hasNext()) {
            Block block = iter.next();

            rules = wcfg.getRuleList().get(KnownAttachment.BLOCK_BREAK);
            context = new BukkitContext(event);
            context.setSourceEntity(event.getEntity());
            context.setTargetBlock(block.getState());
            rules.process(context);
            if (context.isCancelled()) {
                iter.remove();
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onExplosionPrime(ExplosionPrimeEvent event) {
        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(event.getEntity().getWorld());

        // RuleLists
        RuleSet rules = wcfg.getRuleList().get(KnownAttachment.ENTITY_EXPLODE);
        BukkitContext context = new BukkitContext(event);
        context.setTargetEntity(event.getEntity());
        if (rules.process(context)) {
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        EntityType entityType = event.getEntityType();
        Location eventLoc = event.getLocation();

        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(event.getEntity().getWorld());

        // Regions
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

        // RuleLists
        RuleSet rules = wcfg.getRuleList().get(KnownAttachment.ENTITY_SPAWN);
        BukkitContext context = new BukkitContext(event);
        context.setTargetEntity(event.getEntity());
        if (rules.process(context)) {
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPigZap(PigZapEvent event) {
        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(event.getEntity().getWorld());

        // RuleLists
        RuleSet rules = wcfg.getRuleList().get(KnownAttachment.ENTITY_STRIKE);
        BukkitContext context = new BukkitContext(event);
        context.setTargetEntity(event.getEntity());
        rules.process(context);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onCreeperPower(CreeperPowerEvent event) {
        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(event.getEntity().getWorld());

        // RuleLists
        RuleSet rules = wcfg.getRuleList().get(KnownAttachment.ENTITY_STRIKE);
        BukkitContext context = new BukkitContext(event);
        context.setTargetEntity(event.getEntity());
        if (rules.process(context)) {
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        Entity ent = event.getEntity();
        Block block = event.getBlock();
        Location location = block.getLocation();

        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(ent.getWorld());

        if (ent instanceof Enderman) {
            if (event.getTo() == Material.AIR) { // Pickup
                // Blacklist
                if (wcfg.useRegions) {
                    if (!plugin.getGlobalRegionManager().allows(DefaultFlag.ENDER_BUILD, location)) {
                        event.setCancelled(true);
                        return;
                    }
                }

            // Place
            } else {
                if (wcfg.useRegions) {
                    if (!plugin.getGlobalRegionManager().allows(DefaultFlag.ENDER_BUILD, location)) {
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        }

        // RuleLists
        RuleSet rules = wcfg.getRuleList().get(event.getTo() == Material.AIR ?
                KnownAttachment.BLOCK_BREAK : KnownAttachment.BLOCK_PLACE);
        BukkitContext context = new BukkitContext(event);
        context.setSourceEntity(ent);
        BlockState newState = event.getBlock().getState(); // This event is lame
        newState.setType(event.getTo()); // Need to construct our own BlockState
        context.setTargetBlock(event.getBlock().getState());
        context.setPlacedBlock(newState);
        if (rules.process(context)) {
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (event.getFoodLevel() < player.getFoodLevel() && isInvincible(player)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPotionSplash(PotionSplashEvent event) {
        GlobalRegionManager global = plugin.getGlobalRegionManager();
        int blockedEntities = 0;

        for (LivingEntity e : event.getAffectedEntities()) {
            if (!global.allows(DefaultFlag.POTION_SPLASH, e.getLocation(),
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
     * wants to cancel a PvP damage event.
     *
     * <p>If this event is not cancelled, the attacking player is notified that
     * PvP is disabled and WorldGuard cancels the damage event.
     *
     * @param attackingPlayer The attacker
     * @param defendingPlayer The defender
     * @param event The event that caused WorldGuard to act
     * @param aggressorTriggered whether the aggressor triggered the incident.
     */
    public void tryCancelPVPEvent(final Player attackingPlayer, final Player defendingPlayer, EntityDamageByEntityEvent event, boolean aggressorTriggered) {
        final DisallowedPVPEvent disallowedPVPEvent = new DisallowedPVPEvent(attackingPlayer, defendingPlayer, event);
        plugin.getServer().getPluginManager().callEvent(disallowedPVPEvent);

        if (!disallowedPVPEvent.isCancelled()) {
            if (aggressorTriggered) {
                attackingPlayer.sendMessage(ChatColor.DARK_RED
                        + "You are in a no-PvP area.");
            } else {
                attackingPlayer.sendMessage(ChatColor.DARK_RED
                        + "That player is in a no-PvP area.");
            }
            event.setCancelled(true);
        }
    }
}
