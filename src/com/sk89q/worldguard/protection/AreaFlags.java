// $Id$
/*
 * WorldGuard
 * Copyright (C) 2010 sk89q <http://www.sk89q.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.sk89q.worldguard.protection;

/**
 * 
 * @author sk89q
 */
public class AreaFlags {
    public enum State {
        NONE,
        ALLOW,
        DENY,
    };

    public State allowBuild = State.NONE;
    public State allowPvP = State.NONE;
    public State allowMobDamage = State.NONE;
    public State allowCreeperExplosions = State.NONE;
    public State allowTNT = State.NONE;
    public State allowLighter = State.NONE;
    public State allowFireSpread = State.NONE;
    public State allowLavaFire = State.NONE;
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof AreaFlags)) {
            return false;
        }
        
        AreaFlags other = (AreaFlags)obj;
        
        return other.allowBuild == allowBuild
                && other.allowPvP == allowPvP
                && other.allowMobDamage == allowMobDamage
                && other.allowCreeperExplosions == allowCreeperExplosions
                && other.allowTNT == allowTNT
                && other.allowLighter == allowLighter
                && other.allowFireSpread == allowFireSpread
                && other.allowLavaFire == allowLavaFire;
    }
}
