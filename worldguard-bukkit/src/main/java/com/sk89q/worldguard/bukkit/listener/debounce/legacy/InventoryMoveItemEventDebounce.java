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

package com.sk89q.worldguard.bukkit.listener.debounce.legacy;

import com.sk89q.worldguard.bukkit.listener.debounce.legacy.InventoryMoveItemEventDebounce.Key;
import io.papermc.lib.PaperLib;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.InventoryHolder;

public class InventoryMoveItemEventDebounce extends AbstractEventDebounce<Key> {

    public InventoryMoveItemEventDebounce(int debounceTime) {
        super(debounceTime);
    }

    public Entry tryDebounce(InventoryMoveItemEvent event) {
        return super.getEntry(new Key(event), event);
    }

    protected static class Key {
        private final Object cause;
        private final Object source;
        private final Object target;

        public Key(InventoryMoveItemEvent event) {
            cause = transform(PaperLib.getHolder(event.getInitiator(), false).getHolder());
            source = transform(PaperLib.getHolder(event.getSource(), false).getHolder());
            target = transform(PaperLib.getHolder(event.getDestination(), false).getHolder());
        }

        private Object transform(InventoryHolder holder) {
            if (holder instanceof BlockState blockState) {
                return new BlockMaterialKey(blockState);
            } else if (holder instanceof DoubleChest doubleChest) {
                InventoryHolder left = PaperLib.isPaper() ? doubleChest.getLeftSide(false) : doubleChest.getLeftSide();
                if (left instanceof Chest chest) {
                    return new BlockMaterialKey(chest);
                } else {
                    return holder;
                }
            } else {
                return holder;
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Key key = (Key) o;

            if (cause != null ? !cause.equals(key.cause) : key.cause != null)
                return false;
            if (source != null ? !source.equals(key.source) : key.source != null)
                return false;
            if (target != null ? !target.equals(key.target) : key.target != null)
                return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = cause != null ? cause.hashCode() : 0;
            result = 31 * result + (source != null ? source.hashCode() : 0);
            result = 31 * result + (target != null ? target.hashCode() : 0);
            return result;
        }
    }

    private static class BlockMaterialKey {
        private final Block block;
        private final Material material;

        private BlockMaterialKey(BlockState block) {
            this.block = block.getBlock();
            material = block.getType();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            BlockMaterialKey that = (BlockMaterialKey) o;

            if (!block.equals(that.block)) return false;
            if (material != that.material) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = block.hashCode();
            result = 31 * result + material.hashCode();
            return result;
        }
    }

}
