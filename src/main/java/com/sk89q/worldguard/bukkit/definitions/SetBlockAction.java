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

import org.bukkit.block.BlockState;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import com.sk89q.rebar.util.MaterialPattern;
import com.sk89q.rulelists.Action;
import com.sk89q.worldguard.bukkit.BukkitContext;
import com.sk89q.worldguard.bukkit.resolvers.BlockResolver;

public class SetBlockAction implements Action<BukkitContext> {

    private final BlockResolver blockResolver;
    private MaterialPattern material;

    public SetBlockAction(BlockResolver blockResolver) {
        this.blockResolver = blockResolver;
    }

    public MaterialPattern getMaterial() {
        return material;
    }

    public void setMaterial(MaterialPattern material) {
        this.material = material;
    }

    @Override
    public void apply(BukkitContext context) {
        BlockState state = blockResolver.resolve(context);
        Event event = context.getEvent();
        
        if (state == null) {
            return;
        }

        state.setTypeId(material.getMaterial());
        state.setRawData((byte) material.getDefaultData());
        
        if (!(event instanceof BlockPlaceEvent || event instanceof BlockBreakEvent)) {
            state.update();
        }
    }

}