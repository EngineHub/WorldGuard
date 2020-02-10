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

package com.sk89q.worldguard.bukkit;

import com.sk89q.worldguard.bukkit.cause.Cause;
import com.sk89q.worldguard.bukkit.event.block.BreakBlockEvent;
import com.sk89q.worldguard.bukkit.event.block.PlaceBlockEvent;
import com.sk89q.worldguard.bukkit.event.block.UseBlockEvent;
import com.sk89q.worldguard.bukkit.event.entity.DamageEntityEvent;
import com.sk89q.worldguard.bukkit.event.entity.SpawnEntityEvent;
import com.sk89q.worldguard.bukkit.event.entity.UseEntityEvent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import javax.annotation.Nullable;

import static com.sk89q.worldguard.bukkit.event.DelegateEvents.setSilent;
import static com.sk89q.worldguard.bukkit.util.Events.fireAndTestCancel;

/**
 * A helper class to query whether a block or entity is protected by
 * WorldGuard.
 */
public class ProtectionQuery {

    /**
     * Test whether a block can be placed at a given location.
     *
     * <p>The cause does not have to be a player. It can be {@code null}
     * if the cause is not known, although the checks will executed
     * assuming that the actor is a non-member of all regions.</p>
     *
     * @param cause the cause, which can be a block, an entity, or a player, or {@code null} if unknown
     * @param location the location of the block
     * @param newMaterial the new material
     * @return true if the action is permitted
     */
    public boolean testBlockPlace(@Nullable Object cause, Location location, Material newMaterial) {
        return !fireAndTestCancel(setSilent(new PlaceBlockEvent(null, Cause.create(cause), location, newMaterial)));
    }

    /**
     * Test whether a block can be broken.
     *
     * <p>The cause does not have to be a player. It can be {@code null}
     * if the cause is not known, although the checks will executed
     * assuming that the actor is a non-member of all regions.</p>
     *
     * @param cause the cause, which can be a block, an entity, or a player, or {@code null} if unknown
     * @param block the block broken
     * @return true if the action is permitted
     */
    public boolean testBlockBreak(@Nullable Object cause, Block block) {
        return !fireAndTestCancel(setSilent(new BreakBlockEvent(null, Cause.create(cause), block)));
    }

    /**
     * Test whether a block can be interacted with.
     *
     * <p>The cause does not have to be a player. It can be {@code null}
     * if the cause is not known, although the checks will executed
     * assuming that the actor is a non-member of all regions.</p>
     *
     * @param cause the cause, which can be a block, an entity, or a player, or {@code null} if unknown
     * @param block the block that is interacted with
     * @return true if the action is permitted
     */
    public boolean testBlockInteract(@Nullable Object cause, Block block) {
        return !fireAndTestCancel(setSilent(new UseBlockEvent(null, Cause.create(cause), block)));
    }

    /**
     * Test whether an entity can be placed.
     *
     * <p>The cause does not have to be a player. It can be {@code null}
     * if the cause is not known, although the checks will executed
     * assuming that the actor is a non-member of all regions.</p>
     *
     * @param cause the cause, which can be a block, an entity, or a player, or {@code null} if unknown
     * @param location the location that the entity will be spawned at
     * @param type the type that is to be spawned
     * @return true if the action is permitted
     */
    public boolean testEntityPlace(@Nullable Object cause, Location location, EntityType type) {
        return !fireAndTestCancel(setSilent(new SpawnEntityEvent(null, Cause.create(cause), location, type)));
    }

    /**
     * Test whether an entity can be destroyed.
     *
     * <p>The cause does not have to be a player. It can be {@code null}
     * if the cause is not known, although the checks will executed
     * assuming that the actor is a non-member of all regions.</p>
     *
     * @param cause the cause, which can be a block, an entity, or a player, or {@code null} if unknown
     * @param entity the entity broken
     * @return true if the action is permitted
     */
    public boolean testEntityDestroy(@Nullable Object cause, Entity entity) {
        return !fireAndTestCancel(setSilent(new SpawnEntityEvent(null, Cause.create(cause), entity)));
    }

    /**
     * Test whether an entity can be interacted with.
     *
     * <p>The cause does not have to be a player. It can be {@code null}
     * if the cause is not known, although the checks will executed
     * assuming that the actor is a non-member of all regions.</p>
     *
     * @param cause the cause, which can be a block, an entity, or a player, or {@code null} if unknown
     * @param entity the entity interacted with
     * @return true if the action is permitted
     */
    public boolean testEntityInteract(@Nullable Object cause, Entity entity) {
        return !fireAndTestCancel(setSilent(new UseEntityEvent(null, Cause.create(cause), entity)));
    }

    /**
     * Test whether an entity can be damaged.
     *
     * <p>The cause does not have to be a player. It can be {@code null}
     * if the cause is not known, although the checks will executed
     * assuming that the actor is a non-member of all regions.</p>
     *
     * @param cause the cause, which can be a block, an entity, or a player, or {@code null} if unknown
     * @param entity the entity damaged
     * @return true if the action is permitted
     */
    public boolean testEntityDamage(@Nullable Object cause, Entity entity) {
        return !fireAndTestCancel(setSilent(new DamageEntityEvent(null, Cause.create(cause), entity)));
    }

}
