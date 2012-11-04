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

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.sk89q.rulelists.Criteria;
import com.sk89q.worldguard.bukkit.BukkitContext;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.bukkit.resolvers.EntityResolver;

public class PermissionCriteria implements Criteria<BukkitContext> {

    private final WorldGuardPlugin wg;
    private final EntityResolver entityResolver;
    private String permission;

    public PermissionCriteria(WorldGuardPlugin wg, EntityResolver entityResolver) {
        this.wg = wg;
        this.entityResolver = entityResolver;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    @Override
    public boolean matches(BukkitContext context) {
        Entity entity = entityResolver.resolve(context);

        if (entity == null) {
            return false;
        }
        
        if (entity instanceof Player) {
            return wg.hasPermission((Player) entity, permission);
        }
        
        return false;
    }

}
