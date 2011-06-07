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
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.entity.*;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.painting.PaintingBreakByEntityEvent;
import org.bukkit.event.painting.PaintingBreakEvent;
import org.bukkit.event.painting.PaintingPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BlockType;
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
        PluginManager pm = plugin.getServer().getPluginManager();

        pm.registerEvent(Event.Type.ENTITY_DAMAGE, this, Priority.High, plugin);
        pm.registerEvent(Event.Type.ENTITY_EXPLODE, this, Priority.High, plugin);
        pm.registerEvent(Event.Type.CREATURE_SPAWN, this, Priority.High, plugin);
        pm.registerEvent(Event.Type.ENTITY_INTERACT, this, Priority.High, plugin);
        pm.registerEvent(Event.Type.CREEPER_POWER, this, Priority.High, plugin);
        pm.registerEvent(Event.Type.PIG_ZAP, this, Priority.High, plugin);
        pm.registerEvent(Event.Type.PAINTING_BREAK, this, Priority.High, plugin);
        pm.registerEvent(Event.Type.PAINTING_PLACE, this, Priority.High, plugin);
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

        if (defender instanceof Wolf) {
            if (wcfg.antiWolfDumbness && !(type == DamageCause.VOID)) {
                event.setCancelled(true);
                return;
            }
        } else if (defender instanceof Player) {
            Player player = (Player) defender;

            if (cfg.hasGodMode(player)) {
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
                findFreePosition(player);
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

            if (attacker != null && attacker instanceof LivingEntity
                    && !(attacker instanceof Player)) {
                if (attacker instanceof Creeper && wcfg.blockCreeperExplosions) {
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
    private void onEntityDamageByProjectile(EntityDamageByProjectileEvent event) {
        Entity defender = event.getEntity();
        Entity attacker = event.getDamager();

        if (defender instanceof Player) {
            Player player = (Player) defender;

            ConfigurationManager cfg = plugin.getGlobalStateManager();
            WorldConfiguration wcfg = cfg.get(player.getWorld());
            
            if (cfg.hasGodMode(player)) {
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

        if (event instanceof EntityDamageByProjectileEvent) {
            this.onEntityDamageByProjectile((EntityDamageByProjectileEvent) event);
            return;
        } else if (event instanceof EntityDamageByEntityEvent) {
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

        if (defender instanceof Wolf) {
            if (wcfg.antiWolfDumbness) {
                event.setCancelled(true);
                return;
            }
        } else if (defender instanceof Player) {
            Player player = (Player) defender;

            if (cfg.hasGodMode(player)) {
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
                findFreePosition(player);
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
        } else if (ent instanceof Fireball) {
            if (wcfg.useRegions) {
                Vector pt = toVector(l);
                RegionManager mgr = plugin.getGlobalRegionManager().get(world);

                if (!mgr.getApplicableRegions(pt).allows(DefaultFlag.GHAST_FIREBALL)) {
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
        WorldConfiguration wcfg = cfg.get(event.getEntity().getWorld());

        //CreatureType creaType = (CreatureType) CreatureType.valueOf(event.getMobType().toString());
        CreatureType creaType = event.getCreatureType();
        Boolean cancelEvent = false;

        if (wcfg.blockCreatureSpawn.contains(creaType)) {
            cancelEvent = true;
        }
        
        Location eventLoc = event.getLocation();
        
        if (wcfg.useRegions) {
            Vector pt = toVector(eventLoc);
            RegionManager mgr = plugin.getGlobalRegionManager().get(eventLoc.getWorld());
            ApplicableRegionSet set = mgr.getApplicableRegions(pt);

            if (!set.allows(DefaultFlag.MOB_SPAWNING)) {
            	cancelEvent = true;
            }
        }
        
        // TODO: Monsters and stuff
/*
        if (wcfg.useRegions) {
            Vector pt = toVector(event.getEntity().getLocation());
            RegionManager mgr = plugin.getGlobalRegionManager().get(event.getEntity().getWorld().getName());

            Boolean flagValue = mgr.getApplicableRegions(pt).getFlag(DefaultFlag.DENY_SPAWN).getValue("").contains(creaType.getName());
            if (flagValue != null) {
                if (flagValue) {
                    cancelEvent = true;
                } else {
                    cancelEvent = false;
                }
            }
        }*/

        if (cancelEvent) {
            event.setCancelled(true);
            return;
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

        if (wcfg.useRegions) {
            if (!plugin.getGlobalRegionManager().canBuild(player, placedOn.getLocation())) {
                player.sendMessage(ChatColor.DARK_RED + "You don't have permission for this area.");
                event.setCancelled(true);
                return;
            }
        }
    }
    
    /**
     * Find a position for the player to stand that is not inside a block.
     * Blocks above the player will be iteratively tested until there is
     * a series of two free blocks. The player will be teleported to
     * that free position.
     *
     * @param player
     */
    public void findFreePosition(Player player) {
        Location loc = player.getLocation();
        int x = loc.getBlockX();
        int y = Math.max(0, loc.getBlockY());
        int origY = y;
        int z = loc.getBlockZ();
        World world = player.getWorld();

        byte free = 0;

        while (y <= 129) {
            if (BlockType.canPassThrough(world.getBlockTypeIdAt(x, y, z))) {
                free++;
            } else {
                free = 0;
            }

            if (free == 2) {
                if (y - 1 != origY || y == 1) {
                    loc.setX(x + 0.5);
                    loc.setY(y);
                    loc.setZ(z + 0.5);
                    if (y <= 2 && world.getBlockAt(x,0,z).getType() == Material.AIR) {
                        world.getBlockAt(x,0,z).setTypeId(20);
                        loc.setY(2);
                    }
                    player.setFallDistance(0F);
                    player.teleport(loc);
                }
                return;
            }

            y++;
        }
    }
}
