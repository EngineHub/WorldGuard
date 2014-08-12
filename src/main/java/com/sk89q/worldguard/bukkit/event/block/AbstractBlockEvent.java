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

package com.sk89q.worldguard.bukkit.event.block;

import com.sk89q.worldguard.util.cause.Cause;
import com.sk89q.worldguard.bukkit.event.AbstractInteractEvent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.Event;

import javax.annotation.Nullable;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

abstract class AbstractBlockEvent extends AbstractInteractEvent {

    private final Location target;
    @Nullable
    private final Block block;
    private final Material effectiveMaterial;

    protected AbstractBlockEvent(Event originalEvent, List<? extends Cause<?>> causes, Block block) {
        super(originalEvent, causes);
        checkNotNull(block);
        this.target = block.getLocation();
        this.block = block;
        this.effectiveMaterial = block.getType();
    }

    protected AbstractBlockEvent(Event originalEvent, List<? extends Cause<?>> causes, Location target, Material effectiveMaterial) {
        super(originalEvent, causes);
        this.target = target;
        this.block = null;
        this.effectiveMaterial = effectiveMaterial;
    }

    /**
     * Get the target block being affected.
     *
     * @return a block
     */
    public Location getTarget() {
        return target;
    }

    /**
     * Get the block.
     *
     * @return the block
     */
    @Nullable
    public Block getBlock() {
        return block;
    }

    /**
     * Get the effective material of the block, regardless of what the block
     * currently is.
     *
     * @return the effective material
     */
    public Material getEffectiveMaterial() {
        return effectiveMaterial;
    }

}
