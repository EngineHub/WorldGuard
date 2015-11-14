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

import com.sk89q.worldguard.bukkit.WorldConfiguration;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
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
    private boolean isInvincible(Player player) {
        return getPlugin().getSessionManager().get(player).isInvincible(player);
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        Entity victim = event.getEntity();
        WorldConfiguration worldConfig = getPlugin().getGlobalStateManager().get(victim.getWorld());

        if (victim instanceof Player) {
            Player player = (Player) victim;

            if (isInvincible(player)) {
                player.setFireTicks(0);
                event.setCancelled(true);

                if (event instanceof EntityDamageByEntityEvent) {
                    EntityDamageByEntityEvent byEntityEvent = (EntityDamageByEntityEvent) event;
                    Entity attacker = byEntityEvent.getDamager();

                    if (worldConfig.regionInvinciblityRemovesMobs
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

        if (entity instanceof Player) {
            Player player = (Player) entity;

            if (isInvincible(player)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();

            if (event.getFoodLevel() < player.getFoodLevel() && isInvincible(player)) {
                event.setCancelled(true);
            }
        }
    }

}
