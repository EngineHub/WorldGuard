// $Id$
/*
 * This file is a part of WorldGuard.
 * Copyright (c) sk89q <http://www.sk89q.com>
 * Copyright (c) the WorldGuard team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software 
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.sk89q.worldguard.bukkit.definitions;

import java.util.List;

import org.bukkit.block.Block;
import org.bukkit.block.BlockState;

import com.sk89q.rebar.util.MaterialPattern;
import com.sk89q.rulelists.Criteria;
import com.sk89q.worldguard.bukkit.BukkitContext;
import com.sk89q.worldguard.bukkit.resolvers.BlockResolver;

public class BlockCriteria implements Criteria<BukkitContext> {
    
    public enum Direction {
        AT,
        ABOVE,
        BELOW,
        AROUND
    }

    private final BlockResolver resolver;
    private Direction direction;
    private MaterialPattern[] patterns = new MaterialPattern[0];

    public BlockCriteria(BlockResolver resolver) {
        this.resolver = resolver;
    }

    public MaterialPattern[] getPatterns() {
        return patterns;
    }

    public void setPatterns(MaterialPattern[] patterns) {
        this.patterns = patterns;
    }

    public void setPatterns(List<MaterialPattern> patterns) {
        MaterialPattern[] arr = new MaterialPattern[patterns.size()];
        patterns.toArray(arr);
        this.patterns = arr;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }
    
    private boolean matches(BlockState block) {
        for (MaterialPattern pattern : patterns) {
            if (pattern.matches(block.getTypeId(), block.getRawData())) {
                return true;
            }
        }
        
        return false;
    }
    
    private boolean matches(Block block) {
        for (MaterialPattern pattern : patterns) {
            if (pattern.matches(block.getTypeId(), block.getData())) {
                return true;
            }
        }
        
        return false;
    }

    @Override
    public boolean matches(BukkitContext context) {
        BlockState block = resolver.resolve(context);

        if (block == null) {
            return false;
        }

        switch (getDirection()) {
        case AT:
            return matches(block);
        case ABOVE:
            return matches(block.getBlock().getRelative(0, 1, 0));
        case BELOW:
            return matches(block.getBlock().getRelative(0, -1, 0));
        case AROUND:
            return matches(block.getBlock().getRelative(0, -1, 0)) ||
                    matches(block.getBlock().getRelative(0, 1, 0)) ||
                    matches(block.getBlock().getRelative(-1, 0, 0)) ||
                    matches(block.getBlock().getRelative(1, 0, 0)) ||
                    matches(block.getBlock().getRelative(0, 0, -1)) ||
                    matches(block.getBlock().getRelative(0, 0, 1));
        }

        return false;
    }

}