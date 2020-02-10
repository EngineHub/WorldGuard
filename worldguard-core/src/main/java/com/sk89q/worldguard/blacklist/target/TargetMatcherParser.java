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

package com.sk89q.worldguard.blacklist.target;

import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import com.sk89q.worldedit.world.item.ItemType;
import com.sk89q.worldedit.world.item.ItemTypes;

public class TargetMatcherParser {

    public TargetMatcher fromInput(String input) throws TargetMatcherParseException {
        input = input.toLowerCase().trim();
        BlockType blockType = BlockTypes.get(input);
        if (blockType != null) {
            if (blockType.hasItemType()) {
                return new ItemBlockMatcher(blockType);
            } else {
                return new BlockMatcher(blockType);
            }
        } else {
            ItemType itemType = ItemTypes.get(input);
            if (itemType == null) {
                throw new TargetMatcherParseException("Неизвестное имя блока или предмета: " + input);
            }
            return new ItemMatcher(itemType);
        }
    }
}
