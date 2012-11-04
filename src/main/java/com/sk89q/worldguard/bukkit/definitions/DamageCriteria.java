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

import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import com.sk89q.rulelists.Criteria;
import com.sk89q.worldguard.bukkit.BukkitContext;

public class DamageCriteria implements Criteria<BukkitContext> {

    private Set<DamageCause> causes = new HashSet<DamageCause>();

    public DamageCriteria(Set<DamageCause> causes) {
        this.causes = causes;
    }

    public Set<DamageCause> getCauses() {
        return causes;
    }

    public void setCauses(Set<DamageCause> causes) {
        this.causes = causes;
    }

    @Override
    public boolean matches(BukkitContext context) {
        Event event = context.getEvent();

        if (event instanceof EntityDamageEvent) {
            return causes.contains(((EntityDamageEvent) event).getCause());
        } else {
            return false;
        }
    }

}
