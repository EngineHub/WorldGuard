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

import com.sk89q.worldguard.protection.regions.flags.FlagDatabase.FlagType;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regionmanager.RegionManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.CreatureType;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.*;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BlockType;
import static com.sk89q.worldguard.bukkit.BukkitUtil.*;

public class WorldGuardEntityListener extends EntityListener {

    /**
     * Plugin.
     */
    private WorldGuardPlugin plugin;

    /**
     * Construct the object;
     * 
     * @param plugin
     */
    public WorldGuardEntityListener(WorldGuardPlugin plugin) {
        this.plugin = plugin;
    }

    public void onEntityDamageByBlock(EntityDamageByBlockEvent event) {

        Entity defender = event.getEntity();
        DamageCause type = event.getCause();

        if (defender instanceof Player) {
            Player player = (Player) defender;

            WorldGuardConfiguration cfg = plugin.getWgConfiguration();
            WorldGuardWorldConfiguration wcfg = cfg.getWorldConfig(player.getWorld().getName());

            if (cfg.isInvinciblePlayer(player.getName())) {
                event.setCancelled(true);
                return;
            }

            if (wcfg.disableLavaDamage && type == DamageCause.LAVA) {
                event.setCancelled(true);
                return;
            }

            if (wcfg.disableContactDamage && type == DamageCause.CONTACT) {
                event.setCancelled(true);
                return;
            }
            
        }
    }

    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Entity attacker = event.getDamager();
        Entity defender = event.getEntity();

        if (defender instanceof Player) {
            Player player = (Player) defender;

            WorldGuardConfiguration cfg = plugin.getWgConfiguration();
            WorldGuardWorldConfiguration wcfg = cfg.getWorldConfig(player.getWorld().getName());

            if (cfg.isInvinciblePlayer(player.getName())) {
                event.setCancelled(true);
                return;
            }

            if (attacker != null && attacker instanceof Player) {
                if (wcfg.useRegions) {
                    Vector pt = toVector(defender.getLocation());
                    RegionManager mgr = plugin.getGlobalRegionManager().getRegionManager(player.getWorld().getName());

                    if (!mgr.getApplicableRegions(pt).allowsFlag(FlagType.PVP)) {
                        ((Player) attacker).sendMessage(ChatColor.DARK_RED + "You are in a no-PvP area.");
                        event.setCancelled(true);
                        return;
                    }
                }
            }

            if (attacker != null && attacker instanceof Monster) {
                if (attacker instanceof Creeper && wcfg.blockCreeperExplosions) {
                    event.setCancelled(true);
                    return;
                }

                if (wcfg.useRegions) {
                    Vector pt = toVector(defender.getLocation());
                    RegionManager mgr = plugin.getGlobalRegionManager().getRegionManager(player.getWorld().getName());
                    ApplicableRegionSet set = mgr.getApplicableRegions(pt);

                    if (!set.allowsFlag(FlagType.MOB_DAMAGE)) {
                        event.setCancelled(true);
                        return;
                    }

                    if (attacker instanceof Creeper) {
                        if (!set.allowsFlag(FlagType.CREEPER_EXPLOSION)) {
                            event.setCancelled(true);
                            return;
                        }
                    }
                }

            }
        }
    }

    public void onEntityDamageByProjectile(EntityDamageByProjectileEvent event) {
        Entity defender = event.getEntity();
        Entity attacker = event.getDamager();

        if (defender instanceof Player) {
            Player player = (Player) defender;

            WorldGuardConfiguration cfg = plugin.getWgConfiguration();
            WorldGuardWorldConfiguration wcfg = cfg.getWorldConfig(player.getWorld().getName());

            if (cfg.isInvinciblePlayer(player.getName())) {
                event.setCancelled(true);
                return;
            }

            if (attacker != null && attacker instanceof Player) {
                if (wcfg.useRegions) {
                    Vector pt = toVector(defender.getLocation());
                    RegionManager mgr = plugin.getGlobalRegionManager().getRegionManager(player.getWorld().getName());

                    if (!mgr.getApplicableRegions(pt).allowsFlag(FlagType.PVP)) {
                        ((Player) attacker).sendMessage(ChatColor.DARK_RED + "You are in a no-PvP area.");
                        event.setCancelled(true);
                        return;
                    }
                }
            }
            if (attacker != null && attacker instanceof Skeleton) {
                if (wcfg.useRegions) {
                    Vector pt = toVector(defender.getLocation());
                    RegionManager mgr = plugin.getGlobalRegionManager().getRegionManager(player.getWorld().getName());

                    if (!mgr.getApplicableRegions(pt).allowsFlag(FlagType.MOB_DAMAGE)) {
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        }

    }

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

        if (defender instanceof Player) {
            Player player = (Player) defender;

            WorldGuardConfiguration cfg = plugin.getWgConfiguration();
            WorldGuardWorldConfiguration wcfg = cfg.getWorldConfig(player.getWorld().getName());

            if (cfg.isInvinciblePlayer(player.getName())) {
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

            if (type == DamageCause.DROWNING
                    && cfg.isAmphibiousPlayer(player.getName())) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @Override
    public void onEntityExplode(EntityExplodeEvent event) {

        if (event.isCancelled()) {
            return;
        }

        WorldGuardConfiguration cfg = plugin.getWgConfiguration();
        Location l = event.getLocation();
        WorldGuardWorldConfiguration wcfg = cfg.getWorldConfig(l.getWorld().getName());

        if (event.getEntity() instanceof LivingEntity) {


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
                RegionManager mgr = plugin.getGlobalRegionManager().getRegionManager(wcfg.getWorldName());

                if (!mgr.getApplicableRegions(pt).allowsFlag(FlagType.CREEPER_EXPLOSION)) {
                    event.setCancelled(true);
                    return;
                }
            }
        } else { // Shall assume that this is TNT
            if (wcfg.blockTNT) {
                event.setCancelled(true);
                return;
            }

            if (wcfg.useRegions) {
                Vector pt = toVector(l);
                RegionManager mgr = plugin.getGlobalRegionManager().getRegionManager(wcfg.getWorldName());

                if (!mgr.getApplicableRegions(pt).allowsFlag(FlagType.TNT)) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    @Override
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (event.isCancelled()) {
            return;
        }

        WorldGuardConfiguration cfg = plugin.getWgConfiguration();
        WorldGuardWorldConfiguration wcfg = cfg.getWorldConfig(event.getEntity().getWorld().getName());

        CreatureType creaType = (CreatureType) CreatureType.valueOf(event.getMobType().toString());
        String creaName = "";
        Boolean cancelEvent = false;

        switch (creaType) {
            case SPIDER:
                if (wcfg.blockCreatureSpawn.contains("spider")) {
                    cancelEvent = true;
                }
                creaName = "spider";
                break;
            case ZOMBIE:
                if (wcfg.blockCreatureSpawn.contains("zombie")) {
                    cancelEvent = true;
                }
                creaName = "zombie";
                break;
            case CREEPER:
                if (wcfg.blockCreatureSpawn.contains("creeper")) {
                    cancelEvent = true;
                }
                creaName = "creeper";
                break;
            case SKELETON:
                if (wcfg.blockCreatureSpawn.contains("skeleton")) {
                    cancelEvent = true;
                }
                creaName = "skeleton";
                break;
            case SQUID:
                if (wcfg.blockCreatureSpawn.contains("squid")) {
                    cancelEvent = true;
                }
                creaName = "squid";
                break;
            case PIG_ZOMBIE:
                if (wcfg.blockCreatureSpawn.contains("pigzombie")) {
                    cancelEvent = true;
                }
                creaName = "pigzombie";
                break;
            case GHAST:
                if (wcfg.blockCreatureSpawn.contains("ghast")) {
                    cancelEvent = true;
                }
                creaName = "ghast";
                break;
            case SLIME:
                if (wcfg.blockCreatureSpawn.contains("slime")) {
                    cancelEvent = true;
                }
                creaName = "slime";
                break;
            case PIG:
                if (wcfg.blockCreatureSpawn.contains("pig")) {
                    cancelEvent = true;
                }
                creaName = "pig";
                break;
            case COW:
                if (wcfg.blockCreatureSpawn.contains("cow")) {
                    cancelEvent = true;
                }
                creaName = "cow";
                break;
            case SHEEP:
                if (wcfg.blockCreatureSpawn.contains("sheep")) {
                    cancelEvent = true;
                }
                creaName = "sheep";
                break;
            case CHICKEN:
                if (wcfg.blockCreatureSpawn.contains("chicken")) {
                    cancelEvent = true;
                }
                creaName = "chicken";
                break;
        }

        if (wcfg.useRegions && !creaName.equals("")) {
            Vector pt = toVector(event.getEntity().getLocation());
            RegionManager mgr = plugin.getGlobalRegionManager().getRegionManager(event.getEntity().getWorld().getName());

            Boolean flagValue = mgr.getApplicableRegions(pt).getStringFlag(FlagType.DENY_SPAWN, true).getValue("").contains(creaName);
            if (flagValue != null) {
                if (!flagValue) {
                    cancelEvent = true;
                } else {
                    cancelEvent = false;
                }
            }
        }

        if (cancelEvent) {
            event.setCancelled(true);
            return;
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
                if (y - 1 != origY) {
                    loc.setX(x + 0.5);
                    loc.setY(y);
                    loc.setZ(z + 0.5);
                    player.teleportTo(loc);
                }

                return;
            }

            y++;
        }
    }
}
