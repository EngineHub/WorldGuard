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

import com.sk89q.worldguard.protection.regions.flags.Flags.FlagType;

/**
 * Used for default flags.
 * 
 * @author sk89q
 */
public class GlobalFlags {
    public boolean canBuild = true;
    public boolean canAccessChests = false;
    public boolean canPvP = true;
    public boolean canLighter = true;
    public boolean canTnt = true;
    public boolean canLeverandbutton = true;
    public boolean canPlaceVehicle = true;
    public boolean allowCreeper = true;
    public boolean allowMobDamage = true;
    public boolean allowWaterflow = true;


    public boolean getDefaultValue(FlagType type)
    {
        switch(type)
        {
            case BUILD:
                return this.canBuild;
            case CHEST_ACCESS:
                return this.canAccessChests;
            case PVP:
                return this.canPvP;
            case LIGHTER:
                return this.canLighter;
            case TNT:
                return this.canTnt;
            case LEVER_AND_BUTTON:
                return this.canLeverandbutton;
            case PLACE_VEHICLE:
                return this.canPlaceVehicle;
            case CREEPER_EXPLOSION:
                return this.allowCreeper;
            case MOB_DAMAGE:
                return this.allowMobDamage;
            case WATER_FLOW:
                return this.allowWaterflow;
            default:
                return true;
        }
    }
}
