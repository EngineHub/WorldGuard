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

import org.bukkit.World;
import org.bukkit.event.Event;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.event.world.WorldEvent;

import com.sk89q.rulelists.Action;
import com.sk89q.worldguard.bukkit.BukkitContext;

public class UpdateWorldAction implements Action<BukkitContext> {

    private Boolean storm = null;
    private Boolean thunderstorm = null;

    public UpdateWorldAction() {
    }

    public Boolean getStorm() {
        return storm;
    }

    public void setStorm(Boolean storm) {
        this.storm = storm;
    }

    public Boolean getThunderstorm() {
        return thunderstorm;
    }

    public void setThunderstorm(Boolean thunderstorm) {
        this.thunderstorm = thunderstorm;
    }

    @Override
    public void apply(BukkitContext context) {
        Event event = context.getEvent();
        World world = null;
        
        if (event instanceof WeatherChangeEvent) {
            world = ((WeatherChangeEvent) event).getWorld();
        } else if (event instanceof WorldEvent) {
            world = ((WorldEvent) event).getWorld();
        } 
        
        if (world != null) {
            if (storm != null) {
                world.setStorm(storm);
            }
            
            if (thunderstorm != null) {
                world.setThundering(thunderstorm);
            }
        }
    }

}