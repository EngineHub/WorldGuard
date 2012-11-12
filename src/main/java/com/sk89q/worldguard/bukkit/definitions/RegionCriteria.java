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
import org.bukkit.entity.Entity;

import com.sk89q.rulelists.Criteria;
import com.sk89q.worldguard.bukkit.BukkitContext;
import com.sk89q.worldguard.bukkit.resolvers.BlockResolver;
import com.sk89q.worldguard.bukkit.resolvers.EntityResolver;
import com.sk89q.worldguard.protection.ApplicableRegionSet;

public class RegionCriteria implements Criteria<BukkitContext> {

    private BlockResolver blockResolver;
    private EntityResolver entityResolver;

    public BlockResolver getBlockResolver() {
        return blockResolver;
    }

    public void setBlockResolver(BlockResolver blockResolver) {
        this.blockResolver = blockResolver;
    }

    public EntityResolver getEntityResolver() {
        return entityResolver;
    }

    public void setEntityResolver(EntityResolver entityResolver) {
        this.entityResolver = entityResolver;
    }

    @Override
    public boolean matches(BukkitContext context) {
        boolean matches = false;

        if (blockResolver != null) {
            BlockState block = blockResolver.resolve(context);

            if (block == null) {
                return false;
            }

            ApplicableRegionSet set = context.getRegionQuery().lookup(block.getBlock());

            if (set == null) {
                return false;
            }

            context.setApplicableRegionSet(set);
            matches = true;
        }

        if (entityResolver != null) {
            Entity entity = entityResolver.resolve(context);

            if (entity == null) {
                return false;
            }

            ApplicableRegionSet set = context.getRegionQuery().lookup(entity);

            if (set == null) {
                return false;
            }

            context.setApplicableRegionSet(set);
            matches = true;
        }

        return matches;
    }

}
