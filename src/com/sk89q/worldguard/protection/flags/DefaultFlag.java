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
public class DefaultFlag {
    
    public static final Flag<?> PASSTHROUGH = new StateFlag("passthrough", 'z');
    public static final Flag<?> BUILD = new StateFlag("build", 'b');
    public static final Flag<?> PVP = new StateFlag("pvp", 'p');
    public static final Flag<?> MOB_DAMAGE = new StateFlag("mob-damage", 'm');
    public static final Flag<?> CREEPER_EXPLOSION = new StateFlag("creeper-explosion", 'c');
    public static final Flag<?> TNT = new StateFlag("tnt", 't');
    public static final Flag<?> LIGHTER = new StateFlag("lighter", 'l');
    public static final Flag<?> FIRE_SPREAD = new StateFlag("fire-spread", 'f');
    public static final Flag<?> LAVA_FIRE = new StateFlag("lava-fire", 'F');
    public static final Flag<?> CHEST_ACCESS = new StateFlag("chest-access", 'C');
    public static final Flag<?> WATER_FLOW = new StateFlag("water-flow");
    public static final Flag<?> LEVER_AND_BUTTON = new StateFlag("interface");
    public static final Flag<?> PLACE_VEHICLE = new StateFlag("vehicle-place");
    public static final Flag<?> GREET_MESSAGE = new StringFlag("greet-message");
    public static final Flag<?> FAREWELL_MESSAGE = new StringFlag("farewell-message");
    public static final Flag<?> NOTIFY_GREET = new StateFlag("notify-greet");
    public static final Flag<?> NOTIFY_FAREWELL = new StateFlag("notify-farewell");
    public static final Flag<?> DENY_SPAWN = new StringFlag("deny-spawn");
    public static final Flag<?> HEAL_DELAY = new IntegerFlag("heal-delay");
    public static final Flag<?> HEAL_AMOUNT = new IntegerFlag("heal-amount");
    public static final Flag<?> TELE_LOC = new LocationFlag("teleport-loc");
    public static final Flag<?> TELE_PERM = new RegionGroupFlag("teleport-groups");
    public static final Flag<?> SPAWN_LOC = new LocationFlag("teleport-location");
    public static final Flag<?> SPAWN_PERM = new RegionGroupFlag("spawn-groups");
    public static final Flag<?> BUYABLE = new StateFlag("buyable");
    public static final Flag<?> PRICE = new DoubleFlag("price");
    
    private DefaultFlag() {
    }
    
}
