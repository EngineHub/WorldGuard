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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.sk89q.worldguard.bukkit.cause.Cause;
import com.sk89q.worldguard.bukkit.event.BulkEvent;
import com.sk89q.worldguard.bukkit.event.DelegateEvent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.event.Event;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

/**
 * This event is an internal event. We do not recommend handling or throwing
 * this event or its subclasses as the interface is highly subject to change.
 */
abstract class AbstractBlockEvent extends DelegateEvent implements BulkEvent {

    private final World world;
    private List<Block> blocks;
    private final List<BlockState> blockStates;
    private final Material effectiveMaterial;

    protected AbstractBlockEvent(@Nullable Event originalEvent, Cause cause, World world, List<Block> blocks, Material effectiveMaterial) {
        super(originalEvent, cause);
        checkNotNull(world);
        checkNotNull(blocks);
        checkNotNull(effectiveMaterial);
        this.world = world;
        this.blocks = blocks;
        this.effectiveMaterial = effectiveMaterial;
        this.blockStates = null;
    }

    protected AbstractBlockEvent(@Nullable Event originalEvent, Cause cause, World world, List<BlockState> blocks) {
        super(originalEvent, cause);
        checkNotNull(world);
        checkNotNull(blocks);
        checkArgument(!blocks.isEmpty());
        this.world = world;
        this.blockStates = blocks;
        this.blocks = null;
        this.effectiveMaterial = blocks.get(0).getType();
    }

    protected AbstractBlockEvent(@Nullable Event originalEvent, Cause cause, Block block) {
        this(originalEvent, cause, block.getWorld(), createList(checkNotNull(block)), block.getType());
    }

    protected AbstractBlockEvent(@Nullable Event originalEvent, Cause cause, Location target, Material effectiveMaterial) {
        this(originalEvent, cause, target.getWorld(), createList(target.getBlock()), effectiveMaterial);
    }

    private static List<Block> createList(Block block) {
        List<Block> blocks = new ArrayList<>();
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
        if (blocks == null) { // be lazy here because we often don't call getBlocks internally, just filter
            blocks = blockStates.stream().map(BlockState::getBlock).collect(Collectors.toList());
        }
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
        return blocks == null
                ? filterInternal(blockStates, BlockState::getLocation, predicate, cancelEventOnFalse)
                : filterInternal(blocks, Block::getLocation, predicate, cancelEventOnFalse);
    }

    private <B> boolean filterInternal(List<B> blockList, Function<B, Location> locFunc,
                                       Predicate<Location> predicate, boolean cancelEventOnFalse) {
        boolean hasRemoval = false;
        Iterator<B> it = blockList.iterator();
        while (it.hasNext()) {
            if (!predicate.test(locFunc.apply(it.next()))) {
                hasRemoval = true;

                if (cancelEventOnFalse) {
                    blockList.clear();
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
        if (blocks == null ? blockStates.isEmpty() : blocks.isEmpty()) {
            return Result.DENY;
        }
        return super.getResult();
    }

    @Override
    public Result getExplicitResult() {
        return super.getResult();
    }

}
