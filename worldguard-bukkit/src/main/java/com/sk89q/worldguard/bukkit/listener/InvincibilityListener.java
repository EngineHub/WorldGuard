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

import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.bukkit.util.Entities;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Tameable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;

public class InvincibilityListener extends AbstractListener {

    /**
     * Construct the listener.
     *
     * @param plugin an instance of WorldGuardPlugin
     */
    public InvincibilityListener(WorldGuardPlugin plugin) {
        super(plugin);
    }

    /**
     * Test whether a player should be invincible.
     *
     * @param player The player
     * @return True if invincible
     */
    private boolean isInvincible(LocalPlayer player) {
        return WorldGuard.getInstance().getPlatform().getSessionManager().get(player).isInvincible(player);
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        Entity victim = event.getEntity();
        if (Entities.isNPC(victim)) return;

        if (victim instanceof Player player) {
            LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);

            if (isInvincible(localPlayer)) {
                player.setFireTicks(0);
                event.setCancelled(true);

                if (event instanceof EntityDamageByEntityEvent) {
                    EntityDamageByEntityEvent byEntityEvent = (EntityDamageByEntityEvent) event;
                    Entity attacker = byEntityEvent.getDamager();

                    if (attacker instanceof Projectile && ((Projectile) attacker).getShooter() instanceof Entity) {
                        attacker = (Entity) ((Projectile) attacker).getShooter();
                    }

                    if (getWorldConfig(player.getWorld()).regionInvinciblityRemovesMobs
                            && attacker instanceof LivingEntity && !(attacker instanceof Player)
                            && !(attacker instanceof Tameable && ((Tameable) attacker).isTamed())) {
                        attacker.remove();
                    }
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityCombust(EntityCombustEvent event) {
        Entity entity = event.getEntity();
        if (Entities.isNPC(entity)) return;

        if (entity instanceof Player player) {
            LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);

            if (isInvincible(localPlayer)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (Entities.isNPC(event.getEntity())) return;

        if (event.getEntity() instanceof Player player) {
            LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);

            if (event.getFoodLevel() < player.getFoodLevel() && isInvincible(localPlayer)) {
                event.setCancelled(true);
            }
        }
    }

}
