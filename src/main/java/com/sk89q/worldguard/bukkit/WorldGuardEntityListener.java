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

import java.util.Iterator;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
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
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.entity.PigZapEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import com.sk89q.rulelists.DefaultAttachments;
import com.sk89q.rulelists.RuleSet;
import com.sk89q.worldguard.region.RegionManager;
import com.sk89q.worldguard.region.flags.DefaultFlag;

/**
 * The listener for entity events.
 */
class WorldGuardEntityListener implements Listener {

    private WorldGuardPlugin plugin;

    /**
     * Construct the listener.
     *
     * @param plugin WorldGuard plugin
     */
    WorldGuardEntityListener(WorldGuardPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Register the events.
     */
    void registerEvents() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityInteract(EntityInteractEvent event) {
        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(event.getEntity().getWorld());

        // RuleLists
        RuleSet rules = wcfg.getRuleList().get(DefaultAttachments.BLOCK_INTERACT);
        BukkitContext context = new BukkitContext(plugin, event);
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

        /* --- No short-circuit returns below this line --- */

        // RuleLists
        RuleSet rules = wcfg.getRuleList().get(DefaultAttachments.ENTITY_DEATH);
        BukkitContext context = new BukkitContext(plugin, event);
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
            // RuleLists
            RuleSet rules = wcfg.getRuleList().get(DefaultAttachments.ENTITY_DAMAGE);
            BukkitContext context = new BukkitContext(plugin, event);
            context.setSourceEntity(((EntityDamageByEntityEvent) event).getDamager());
            context.setTargetEntity(event.getEntity());
            if (rules.process(context)) {
                event.setCancelled(true);
                return;
            }

        // Redirect damage done by blocks
        } else if (event instanceof EntityDamageByBlockEvent) {
            // RuleLists
            RuleSet rules = wcfg.getRuleList().get(DefaultAttachments.ENTITY_DAMAGE);
            BukkitContext context = new BukkitContext(plugin, event);
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
            // RuleLists
            RuleSet rules = wcfg.getRuleList().get(DefaultAttachments.ENTITY_DAMAGE);
            BukkitContext context = new BukkitContext(plugin, event);
            context.setTargetEntity(event.getEntity());
            if (rules.process(context)) {
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

        // RuleLists
        RuleSet rules = wcfg.getRuleList().get(DefaultAttachments.ENTITY_IGNITE);
        BukkitContext context = new BukkitContext(plugin, event);
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

        // RuleLists
        RuleSet rules = wcfg.getRuleList().get(DefaultAttachments.ENTITY_EXPLODE);
        BukkitContext context = new BukkitContext(plugin, event);
        context.setTargetEntity(event.getEntity());
        if (rules.process(context)) {
            event.setCancelled(true);
            return;
        }

        /* --- No short-circuit returns below this line --- */

        // Now apply RuleLists for each block
        Iterator<Block> iter = event.blockList().iterator();
        while (iter.hasNext()) {
            Block block = iter.next();

            rules = wcfg.getRuleList().get(DefaultAttachments.BLOCK_BREAK);
            context = new BukkitContext(plugin, event);
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
        RuleSet rules = wcfg.getRuleList().get(DefaultAttachments.ENTITY_EXPLODE);
        BukkitContext context = new BukkitContext(plugin, event);
        context.setTargetEntity(event.getEntity());
        if (rules.process(context)) {
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(event.getEntity().getWorld());

        // RuleLists
        RuleSet rules = wcfg.getRuleList().get(DefaultAttachments.ENTITY_SPAWN);
        BukkitContext context = new BukkitContext(plugin, event);
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
        RuleSet rules = wcfg.getRuleList().get(DefaultAttachments.ENTITY_STRIKE);
        BukkitContext context = new BukkitContext(plugin, event);
        context.setTargetEntity(event.getEntity());
        if (rules.process(context)) {
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onCreeperPower(CreeperPowerEvent event) {
        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(event.getEntity().getWorld());

        // RuleLists
        RuleSet rules = wcfg.getRuleList().get(DefaultAttachments.ENTITY_STRIKE);
        BukkitContext context = new BukkitContext(plugin, event);
        context.setTargetEntity(event.getEntity());
        if (rules.process(context)) {
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        Entity entity = event.getEntity();

        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(entity.getWorld());

        // RuleLists
        RuleSet rules = wcfg.getRuleList().get(event.getTo() == Material.AIR ?
                DefaultAttachments.BLOCK_BREAK : DefaultAttachments.BLOCK_PLACE);
        BukkitContext context = new BukkitContext(plugin, event);
        context.setSourceEntity(entity);
        BlockState newState = event.getBlock().getState(); // This event is lame
        newState.setType(event.getTo()); // Need to construct our own BlockState
        context.setTargetBlock(event.getBlock().getState());
        context.setPlacedBlock(newState);
        if (rules.process(context)) {
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPotionSplash(PotionSplashEvent event) {
        RegionManager global = plugin.getGlobalRegionManager();

        /* --- No short-circuit returns below this line --- */

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
}
