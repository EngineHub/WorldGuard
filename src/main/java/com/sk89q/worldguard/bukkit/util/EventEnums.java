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

package com.sk89q.worldguard.bukkit.util;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

/**
 * Utility methods to deal with event-related enums in Bukkit.
 */
public final class EventEnums {

    private EventEnums() {
    }

    /**
     * Return whether the given damage cause is fire-reltaed.
     *
     * @param cause the cause
     * @return true if fire related
     */
    public static boolean isFireCause(DamageCause cause) {
        return cause == DamageCause.FIRE || cause == DamageCause.FIRE_TICK;
    }

    /**
     * Return whether the given cause is an explosion.
     *
     * @param cause the cause
     * @return true if it is an explosion cuase
     */
    public static boolean isExplosionCause(DamageCause cause) {
        return cause == DamageCause.BLOCK_EXPLOSION || cause == DamageCause.ENTITY_EXPLOSION;
    }

    /**
     * Restore the statistic associated with the given cause. For example,
     * for the {@link DamageCause#DROWNING} cause, the entity would have its
     * air level set to its maximum.
     *
     * @param entity the entity
     * @param cause the cuase
     */
    public static void restoreStatistic(Entity entity, DamageCause cause) {
        if (cause == DamageCause.DROWNING && entity instanceof LivingEntity) {
            LivingEntity living = (LivingEntity) entity;
            living.setRemainingAir(living.getMaximumAir());
        }

        if (isFireCause(cause)) {
            entity.setFireTicks(0);
        }

        if (cause == DamageCause.LAVA) {
            entity.setFireTicks(0);
        }
    }

}
