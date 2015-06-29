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

import com.sk89q.worldguard.util.Enums;
import org.bukkit.entity.*;
import org.bukkit.entity.minecart.ExplosiveMinecart;
import org.bukkit.projectiles.ProjectileSource;

import javax.annotation.Nullable;

public final class Entities {

    private Entities() {
    }

    /**
     * Test whether the given entity is tameable and tamed.
     *
     * @param entity the entity, or null
     * @return true if tamed
     */
    public static boolean isTamed(@Nullable Entity entity) {
        return entity instanceof Tameable && ((Tameable) entity).isTamed();
    }

    /**
     * Return if the given entity type is TNT-based.
     *
     * @param entity the entity
     * @return true if TNT based
     */
    public static boolean isTNTBased(Entity entity) {
        return entity instanceof TNTPrimed || entity instanceof ExplosiveMinecart;
    }

    /**
     * Return if the given entity type is a fireball
     * (not including wither skulls).
     *
     * @param type the type
     * @return true if a fireball
     */
    public static boolean isFireball(EntityType type) {
        return type == EntityType.FIREBALL || type == EntityType.SMALL_FIREBALL;
    }

    /**
     * Test whether the given entity can be ridden if it is right clicked.
     *
     * @param entity the entity
     * @return true if the entity can be ridden
     */
    public static boolean isRiddenOnUse(Entity entity) {
        return entity instanceof Vehicle;
    }

    /**
     * Test whether the given entity type is a vehicle type.
     *
     * @param type the type
     * @return true if the type is a vehicle type
     */
    public static boolean isVehicle(EntityType type) {
        return type == EntityType.BOAT
                || isMinecart(type);
    }

    /**
     * Test whether the given entity type is a Minecart type.
     *
     * @param type the type
     * @return true if the type is a Minecart type
     */
    public static boolean isMinecart(EntityType type) {
        return type == EntityType.MINECART
                || type == EntityType.MINECART_CHEST
                || type == EntityType.MINECART_COMMAND
                || type == EntityType.MINECART_FURNACE
                || type == EntityType.MINECART_HOPPER
                || type == EntityType.MINECART_MOB_SPAWNER
                || type == EntityType.MINECART_TNT;
    }

    /**
     * Get the underlying shooter of a projectile if one exists.
     *
     * @param entity the entity
     * @return the shooter
     */
    public static Entity getShooter(Entity entity) {

        while (entity instanceof Projectile) {
            Projectile projectile = (Projectile) entity;
            ProjectileSource remover = projectile.getShooter();
            if (remover instanceof Entity && remover != entity) {
                entity = (Entity) remover;
            } else {
                return entity;
            }
        }

        return entity;
    }

    /**
     * Test whether an entity is hostile.
     *
     * @param entity the entity
     * @return true if hostile
     */
    public static boolean isHostile(Entity entity) {
        return entity instanceof Monster
                || entity instanceof Slime
                || entity instanceof Flying
                || entity instanceof EnderDragon;
    }

    /**
     * Test whether an entity is a non-hostile creature.
     *
     * @param entity
     * @return true if non-hostile
     */
    public static boolean isNonHostile(Entity entity) {
        return !isHostile(entity)
                && (entity instanceof Creature || entity instanceof WaterMob);
    }

    /**
     * Test whether an entity is ambient.
     *
     * @param entity the entity
     * @return true if ambient
     */
    public static boolean isAmbient(Entity entity) {
        return entity instanceof Ambient;
    }

    /**
     * Test whether an entity is an NPC.
     *
     * @param entity the entity
     * @return true if an NPC
     */
    public static boolean isNPC(Entity entity) {
        return entity instanceof NPC || entity.hasMetadata("NPC");
    }

    /**
     * Test whether an entity is a creature (a living thing) that is
     * not a player.
     *
     * @param entity the entity
     * @return true if a non-player creature
     */
    public static boolean isNonPlayerCreature(Entity entity) {
        return entity instanceof LivingEntity && !(entity instanceof Player);
    }

    private static final org.bukkit.entity.EntityType armorStandType =
            Enums.findByValue(org.bukkit.entity.EntityType.class, "ARMOR_STAND");

    /**
     * Test whether using the given entity should be considered "building"
     * rather than merely using an entity.
     *
     * @param entity the entity
     * @return true if considered building
     */
    public static boolean isConsideredBuildingIfUsed(Entity entity) {
        return entity instanceof Hanging
                || entity.getType() == armorStandType;
    }

}
