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
import org.bukkit.event.weather.ThunderChangeEvent;
import org.bukkit.event.weather.WeatherChangeEvent;

import com.sk89q.rulelists.Criteria;
import com.sk89q.worldguard.bukkit.BukkitContext;

public class WeatherCriteria implements Criteria<BukkitContext> {
    
    public enum Type {
        STORM,
        THUNDERSTORM,
    }

    private Set<Type> causes = new HashSet<Type>();
    private Boolean isStarting = null;

    public WeatherCriteria(Set<Type> causes) {
        this.causes = causes;
    }

    public Set<Type> getCauses() {
        return causes;
    }

    public void setCauses(Set<Type> causes) {
        this.causes = causes;
    }

    public Boolean getIsStarting() {
        return isStarting;
    }

    public void setIsStarting(Boolean isStarting) {
        this.isStarting = isStarting;
    }

    @Override
    public boolean matches(BukkitContext context) {
        Event event = context.getEvent();
        boolean startVal = true;
        
        if (isStarting != null) {
            startVal = event instanceof WeatherChangeEvent && 
                    ((WeatherChangeEvent) event).toWeatherState() == isStarting;
        }

        if (event instanceof WeatherChangeEvent) {
            return startVal && causes.contains(Type.STORM);
        } else if (event instanceof ThunderChangeEvent) {
            return startVal && causes.contains(Type.THUNDERSTORM);
        } else {
            return startVal;
        }
    }

}
