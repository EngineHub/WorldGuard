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
package com.sk89q.worldguard.protection.regions.flags;

import com.sk89q.worldguard.protection.regions.flags.info.*;

/**
 *
 * @author Michael
 */
public class Flags {

    public enum FlagType {

        PASSTHROUGH, BUILD, PVP, MOB_DAMAGE, CREEPER_EXPLOSION,
        TNT, LIGHTER, FIRE_SPREAD, LAVA_FIRE, CHEST_ACCESS, WATER_FLOW,
        LEVER_AND_BUTTON, PLACE_VEHICLE, GREET_MESSAGE, FAREWELL_MESSAGE,
        NOTIFY_GREET, NOTIFY_FAREWELL, DENY_SPAWN, HEAL_DELAY, HEAL_AMOUNT,
        TELE_LOC, TELE_PERM, SPAWN_LOC, SPAWN_PERM, BUYABLE, PRICE
    };

    // State flags
    public static StateRegionFlagInfo PASSTHROUGH = new StateRegionFlagInfo("passthrough", FlagType.PASSTHROUGH);
    public static StateRegionFlagInfo BUILD = new StateRegionFlagInfo("build", FlagType.BUILD);
    public static StateRegionFlagInfo PVP = new StateRegionFlagInfo("pvp", FlagType.PVP);
    public static StateRegionFlagInfo MOB_DAMAGE = new StateRegionFlagInfo("mobdamage", FlagType.MOB_DAMAGE);
    public static StateRegionFlagInfo CREEPER_EXPLOSION = new StateRegionFlagInfo("creeperexp", FlagType.CREEPER_EXPLOSION);
    public static StateRegionFlagInfo TNT = new StateRegionFlagInfo("tnt", FlagType.TNT);
    public static StateRegionFlagInfo LIGHTER = new StateRegionFlagInfo("lighter", FlagType.LIGHTER);
    public static StateRegionFlagInfo FIRE_SPREAD = new StateRegionFlagInfo("firespread", FlagType.FIRE_SPREAD);
    public static StateRegionFlagInfo LAVA_FIRE = new StateRegionFlagInfo("lavafire", FlagType.LAVA_FIRE);
    public static StateRegionFlagInfo CHEST_ACCESS = new StateRegionFlagInfo("chest", FlagType.CHEST_ACCESS);
    public static StateRegionFlagInfo WATER_FLOW = new StateRegionFlagInfo("waterflow", FlagType.WATER_FLOW);
    public static StateRegionFlagInfo LEVER_AND_BUTTON = new StateRegionFlagInfo("leverandbutton", FlagType.LEVER_AND_BUTTON);
    public static StateRegionFlagInfo PLACE_VEHICLE = new StateRegionFlagInfo("placevehicle", FlagType.PLACE_VEHICLE);

    // Boolean flags
    public static BooleanRegionFlagInfo BUYABLE = new BooleanRegionFlagInfo("buyable", FlagType.BUYABLE);
    public static BooleanRegionFlagInfo NOTIFY_GREET = new BooleanRegionFlagInfo("notifygreet", FlagType.NOTIFY_GREET);
    public static BooleanRegionFlagInfo NOTIFY_FAREWELL = new BooleanRegionFlagInfo("notifyfarewell", FlagType.NOTIFY_FAREWELL);

    // Integer flags
    public static IntegerRegionFlagInfo HEAL_DELAY = new IntegerRegionFlagInfo("healdelay", FlagType.HEAL_DELAY);
    public static IntegerRegionFlagInfo HEAL_AMOUNT = new IntegerRegionFlagInfo("healamount", FlagType.HEAL_AMOUNT);

    // Double flags
    public static DoubleRegionFlagInfo PRICE = new DoubleRegionFlagInfo("price", FlagType.PRICE);

    // String flags
    public static StringRegionFlagInfo GREET_MESSAGE = new StringRegionFlagInfo("gmsg", FlagType.GREET_MESSAGE);
    public static StringRegionFlagInfo FAREWELL_MESSAGE = new StringRegionFlagInfo("fmsg", FlagType.FAREWELL_MESSAGE);
    public static StringRegionFlagInfo DENY_SPAWN = new StringRegionFlagInfo("denyspawn", FlagType.DENY_SPAWN);

    // Location flags
    public static LocationRegionFlagInfo TELE_LOC = new LocationRegionFlagInfo("teleloc", FlagType.TELE_LOC);
    public static LocationRegionFlagInfo SPAWN_LOC = new LocationRegionFlagInfo("spawnloc", FlagType.SPAWN_LOC);
    
    // RegionGroup flags
    public static RegionGroupRegionFlagInfo TELE_PERM = new RegionGroupRegionFlagInfo("teleperm", FlagType.TELE_PERM);
    public static RegionGroupRegionFlagInfo SPAWN_PERM = new RegionGroupRegionFlagInfo("spawnperm", FlagType.SPAWN_PERM);

}
