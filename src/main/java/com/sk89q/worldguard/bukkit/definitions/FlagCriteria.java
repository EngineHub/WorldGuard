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
import org.bukkit.entity.Player;

import com.sk89q.rulelists.Criteria;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.BukkitContext;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.bukkit.resolvers.BlockResolver;
import com.sk89q.worldguard.bukkit.resolvers.EntityResolver;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.StateFlag;

public class FlagCriteria implements Criteria<BukkitContext> {

    private final WorldGuardPlugin plugin;
    private final StateFlag flag;
    private BlockResolver blockResolver;
    private EntityResolver entityResolver;

    public FlagCriteria(WorldGuardPlugin plugin, StateFlag flag) {
        this.plugin = plugin;
        this.flag = flag;
    }

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

        ApplicableRegionSet set = context.getApplicableRegionSet();

        if (set == null) {
            return false;
        }

        if (blockResolver != null) {
            BlockState block = blockResolver.resolve(context);

            if (block == null) {
                return false;
            }

            if (flag == DefaultFlag.BUILD) {
                matches = false;
            } else {
                matches = set.allows(flag);
            }

            if (!matches) {
                return false;
            }
        }

        if (entityResolver != null) {
            Entity entity = entityResolver.resolve(context);

            if (entity == null) {
                return false;
            }

            LocalPlayer localPlayer = null;
            if (entity instanceof Player) {
                localPlayer = plugin.wrapPlayer((Player) entity);
            }

            if (flag == DefaultFlag.BUILD) {
                matches = localPlayer != null && set.canBuild(localPlayer);
            } else {
                matches = set.allows(flag, localPlayer);
            }
        }

        return matches;
    }

}
