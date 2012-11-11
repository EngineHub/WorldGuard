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

import java.util.HashSet;
import java.util.Set;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Tameable;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;

import com.sk89q.rulelists.Criteria;
import com.sk89q.worldguard.bukkit.BukkitContext;
import com.sk89q.worldguard.bukkit.BukkitUtil;
import com.sk89q.worldguard.bukkit.resolvers.EntityResolver;
import com.sk89q.worldguard.bukkit.resolvers.SourceEntityResolver;

public class EntityCriteria implements Criteria<BukkitContext> {

    private final EntityResolver entityResolver;
    private Set<EntityType> types = new HashSet<EntityType>();
    private Class<?>[] ofTypes = new Class<?>[0];
    private Boolean isTamed = null;

    // Counteract 1.3->1.4 breaking change
    private EntityType fireballType =
            BukkitUtil.tryEnum(EntityType.class, "FIREBALL", "LARGE_FIREBALL");

    public EntityCriteria(EntityResolver entityResolver) {
        this.entityResolver = entityResolver;
    }

    public Class<?>[] getOfTypes() {
        return ofTypes;
    }

    public void setOfTypes(Class<?>[] ofTypes) {
        this.ofTypes = ofTypes;
    }

    public Set<EntityType> getTypes() {
        return types;
    }

    public void setTypes(Set<EntityType> types) {
        this.types = types;
    }

    public Boolean getIsTamed() {
        return isTamed;
    }

    public void setIsTamed(Boolean isTamed) {
        this.isTamed = isTamed;
    }

    @Override
    public boolean matches(BukkitContext context) {
        Entity entity = entityResolver.resolve(context);
        boolean matched = false;

        // Hack because BlockIgniteEvent doesn't give an entity
        if (context.getEvent() instanceof BlockIgniteEvent && entityResolver instanceof SourceEntityResolver) {
            BlockIgniteEvent igniteEvent = (BlockIgniteEvent) context.getEvent();

            if (igniteEvent.getCause() == IgniteCause.FIREBALL) {
                if (types.contains(fireballType)) {
                    matched = true;
                }
            }

            if (igniteEvent.getCause() == IgniteCause.LIGHTNING) {
                if (types.contains(EntityType.LIGHTNING)) {
                    matched = true;
                }
            }
        }

        if (entity == null) {
            return matched;
        }

        if (ofTypes.length != 0) {
            Class<?> cls = entity.getClass();

            for (Class<?> type : ofTypes) {
                if (type.isAssignableFrom(cls)) {
                    matched = true;
                    break;
                }
            }

            if (!matched) {
                return false;
            }
        }

        if (isTamed != null) {
            if (entity instanceof Tameable && ((Tameable) entity).isTamed() == isTamed) {
                matched = true;
            }

            if (!matched) {
                return false;
            }
        }

        if (types.size() != 0) {
            matched = types.contains(entity.getType());

            if (!matched) {
                return false;
            }
        }

        return matched;
    }

}