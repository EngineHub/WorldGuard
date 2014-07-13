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

package com.sk89q.worldguard.internal;

import org.bukkit.block.Block;
import org.bukkit.block.Hopper;
import org.bukkit.material.Attachable;
import org.bukkit.material.MaterialData;

import javax.annotation.Nullable;

/**
 * Block related utility methods.
 */
public final class Blocks {

    private Blocks() {
    }

    /**
     * Get the block that this block attaches to.
     *
     * @param block the block to check
     * @return the block attached to or null
     */
    @Nullable
    public static Block getAttachesTo(Block block) {
        MaterialData data = block.getState().getData();

        if (data instanceof Attachable) {
            Attachable attachable = (Attachable) data;
            return block.getRelative(attachable.getAttachedFace());
        }

        return null;
    }

}
