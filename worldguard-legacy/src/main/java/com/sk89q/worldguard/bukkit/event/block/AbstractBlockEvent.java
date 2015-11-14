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

import com.google.common.base.Predicate;
import com.sk89q.worldguard.bukkit.cause.Cause;
import com.sk89q.worldguard.bukkit.event.DelegateEvent;
import com.sk89q.worldguard.bukkit.event.BulkEvent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.Event;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This event is an internal event. We do not recommend handling or throwing
 * this event or its subclasses as the interface is highly subject to change.
 */
abstract class AbstractBlockEvent extends DelegateEvent implements BulkEvent {

    private final World world;
    private final List<Block> blocks;
    private final Material effectiveMaterial;

    protected AbstractBlockEvent(@Nullable Event originalEvent, Cause cause, World world, List<Block> blocks, Material effectiveMaterial) {
        super(originalEvent, cause);
        checkNotNull(world);
        checkNotNull(blocks);
        checkNotNull(effectiveMaterial);
        this.world = world;
        this.blocks = blocks;
        this.effectiveMaterial = effectiveMaterial;
    }

    protected AbstractBlockEvent(@Nullable Event originalEvent, Cause cause, Block block) {
        this(originalEvent, cause, block.getWorld(), createList(checkNotNull(block)), block.getType());
    }

    protected AbstractBlockEvent(@Nullable Event originalEvent, Cause cause, Location target, Material effectiveMaterial) {
        this(originalEvent, cause, target.getWorld(), createList(target.getBlock()), effectiveMaterial);
    }

    private static List<Block> createList(Block block) {
        List<Block> blocks = new ArrayList<Block>();
        blocks.add(block);
        return blocks;
    }

    /**
     * Get the world.
     *
     * @return the world
     */
    public World getWorld() {
        return world;
    }

    /**
     * Get the affected blocks.
     *
     * @return a list of affected block
     */
    public List<Block> getBlocks() {
        return blocks;
    }

    /**
     * Filter the list of affected blocks with the given predicate. If the
     * predicate returns {@code false}, then the block is removed.
     *
     * @param predicate the predicate
     * @param cancelEventOnFalse true to cancel the event and clear the block
     *                           list once the predicate returns {@code false}
     * @return true if one or more blocks were filtered out
     */
    public boolean filter(Predicate<Location> predicate, boolean cancelEventOnFalse) {
        boolean hasRemoval = false;

        Iterator<Block> it = blocks.iterator();
        while (it.hasNext()) {
            if (!predicate.apply(it.next().getLocation())) {
                hasRemoval = true;

                if (cancelEventOnFalse) {
                    getBlocks().clear();
                    setCancelled(true);
                    break;
                } else {
                    it.remove();
                }
            }
        }

        return hasRemoval;
    }

    /**
     * Filter the list of affected blocks with the given predicate. If the
     * predicate returns {@code false}, then the block is removed.
     *
     * <p>This method will <strong>not</strong> fail fast and
     * cancel the event the first instance that the predicate returns
     * {@code false}. See {@link #filter(Predicate, boolean)} to adjust
     * this behavior.</p>
     *
     * @param predicate the predicate
     * @return true if one or more blocks were filtered out
     */
    public boolean filter(Predicate<Location> predicate) {
        return filter(predicate, false);
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

    @Override
    public Result getResult() {
        if (blocks.isEmpty()) {
            return Result.DENY;
        }
        return super.getResult();
    }

    @Override
    public Result getExplicitResult() {
        return super.getResult();
    }

}
