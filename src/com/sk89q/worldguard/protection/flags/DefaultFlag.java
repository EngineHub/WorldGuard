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
package com.sk89q.worldguard.protection.flags;

/**
 *
 * @author sk89q
 */
public final class DefaultFlag {
    
    public static final StateFlag PASSTHROUGH = new StateFlag("passthrough", 'z', false);
    public static final StateFlag BUILD = new StateFlag("build", 'b', true);
    public static final StateFlag PVP = new StateFlag("pvp", 'p', true);
    public static final StateFlag MOB_DAMAGE = new StateFlag("mob-damage", 'm', true);
    public static final StateFlag CREEPER_EXPLOSION = new StateFlag("creeper-explosion", 'c', true);
    public static final StateFlag TNT = new StateFlag("tnt", 't', true);
    public static final StateFlag LIGHTER = new StateFlag("lighter", 'l', true);
    public static final StateFlag FIRE_SPREAD = new StateFlag("fire-spread", 'f', true);
    public static final StateFlag LAVA_FIRE = new StateFlag("lava-fire", 'F', true);
    public static final StateFlag CHEST_ACCESS = new StateFlag("chest-access", 'C', true);
    public static final StateFlag WATER_FLOW = new StateFlag("water-flow", true);
    public static final StateFlag USE = new StateFlag("use", true);
    public static final StateFlag PLACE_VEHICLE = new StateFlag("vehicle-place", true);
    public static final StringFlag GREET_MESSAGE = new StringFlag("greet-message");
    public static final StringFlag FAREWELL_MESSAGE = new StringFlag("farewell-message");
    public static final BooleanFlag NOTIFY_GREET = new BooleanFlag("notify-greet");
    public static final BooleanFlag NOTIFY_FAREWELL = new BooleanFlag("notify-farewell");
    public static final StringFlag DENY_SPAWN = new StringFlag("deny-spawn");
    public static final IntegerFlag HEAL_DELAY = new IntegerFlag("heal-delay");
    public static final IntegerFlag HEAL_AMOUNT = new IntegerFlag("heal-amount");
    public static final LocationFlag TELE_LOC = new LocationFlag("teleport-loc");
    public static final RegionGroupFlag TELE_PERM = new RegionGroupFlag("teleport-groups");
    public static final LocationFlag SPAWN_LOC = new LocationFlag("teleport-location");
    public static final RegionGroupFlag SPAWN_PERM = new RegionGroupFlag("spawn-groups");
    public static final BooleanFlag BUYABLE = new BooleanFlag("buyable");
    public static final DoubleFlag PRICE = new DoubleFlag("price");
    
    public static final Flag<?>[] flagsList = new Flag<?>[] {
        PASSTHROUGH, BUILD, PVP, MOB_DAMAGE, CREEPER_EXPLOSION,
        TNT, LIGHTER, FIRE_SPREAD, LAVA_FIRE, CHEST_ACCESS, WATER_FLOW,
        USE, PLACE_VEHICLE, GREET_MESSAGE, FAREWELL_MESSAGE, NOTIFY_GREET,
        NOTIFY_FAREWELL, DENY_SPAWN, HEAL_DELAY, HEAL_AMOUNT, TELE_LOC,
        TELE_PERM, SPAWN_LOC, SPAWN_PERM, BUYABLE, PRICE
    };
    
    private DefaultFlag() {
    }
    
    public static Flag<?>[] getFlags() {
        return flagsList;
    }
    
    /**
     * Get the legacy flag.
     * 
     * @param flagString
     * @return null if not found
     */
    public static StateFlag getLegacyFlag(String flagString) {
        for (Flag<?> flag : flagsList) {
            if (flag instanceof StateFlag && flagString.equals(flag.getLegacyCode())) {
                return (StateFlag) flag;
            }
        }
        
        return null;
    }
}
