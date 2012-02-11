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

import org.bukkit.entity.CreatureType;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author sk89q
 */
public final class DefaultFlag {

    public static final StateFlag PASSTHROUGH = new StateFlag("passthrough", 'z', false);
    public static final StateFlag BUILD = new StateFlag("build", 'b', true);
    public static final StateFlag PVP = new StateFlag("pvp", 'p', true);
    public static final StateFlag MOB_DAMAGE = new StateFlag("mob-damage", 'm', true);
    public static final StateFlag MOB_SPAWNING = new StateFlag("mob-spawning", true);
    public static final StateFlag CREEPER_EXPLOSION = new StateFlag("creeper-explosion", 'c', true);
    public static final StateFlag ENDERDRAGON_BLOCK_DAMAGE = new StateFlag("enderdragon-block-damage", true);
    public static final StateFlag GHAST_FIREBALL = new StateFlag("ghast-fireball", true);
    public static final StateFlag SLEEP = new StateFlag("sleep", true);
    public static final StateFlag TNT = new StateFlag("tnt", 't', true);
    public static final StateFlag LIGHTER = new StateFlag("lighter", 'l', true);
    public static final StateFlag FIRE_SPREAD = new StateFlag("fire-spread", 'f', true);
    public static final StateFlag LAVA_FIRE = new StateFlag("lava-fire", 'F', true);
    public static final StateFlag LIGHTNING = new StateFlag("lightning", true);
    public static final StateFlag CHEST_ACCESS = new StateFlag("chest-access", 'C', false);
    public static final StateFlag WATER_FLOW = new StateFlag("water-flow", true);
    public static final StateFlag LAVA_FLOW = new StateFlag("lava-flow", true);
    public static final StateFlag USE = new StateFlag("use", true);
    public static final StateFlag PLACE_VEHICLE = new StateFlag("vehicle-place", false);
    public static final StateFlag DESTROY_VEHICLE = new StateFlag("vehicle-destroy", false);
    public static final StateFlag PISTONS = new StateFlag("pistons", true);
    public static final StateFlag SNOW_FALL = new StateFlag("snow-fall", true);
    public static final StateFlag SNOW_MELT = new StateFlag("snow-melt", true);
    public static final StateFlag ICE_FORM = new StateFlag("ice-form", true);
    public static final StateFlag ICE_MELT = new StateFlag("ice-melt", true);
    public static final StateFlag MUSHROOMS = new StateFlag("mushroom-growth", true);
    public static final StateFlag LEAF_DECAY = new StateFlag("leaf-decay", true);
    public static final StateFlag GRASS_SPREAD = new StateFlag("grass-growth", true);
    public static final StateFlag ENDER_BUILD = new StateFlag("enderman-grief", true);
    public static final StateFlag INVINCIBILITY = new StateFlag("invincible", false);
    public static final StateFlag EXP_DROPS = new StateFlag("exp-drops", true);
    public static final StateFlag ENTRY = new StateFlag("entry", true);
    public static final RegionGroupFlag ENTRY_PERM = new RegionGroupFlag("entry-group", RegionGroupFlag.RegionGroup.NON_MEMBERS);
    public static final StateFlag EXIT = new StateFlag("exit", true);
    public static final RegionGroupFlag EXIT_PERM = new RegionGroupFlag("exit-group", RegionGroupFlag.RegionGroup.NON_MEMBERS);
    public static final StringFlag GREET_MESSAGE = new StringFlag("greeting");
    public static final StringFlag FAREWELL_MESSAGE = new StringFlag("farewell");
    public static final BooleanFlag NOTIFY_ENTER = new BooleanFlag("notify-enter");
    public static final BooleanFlag NOTIFY_LEAVE = new BooleanFlag("notify-leave");
    public static final SetFlag<CreatureType> DENY_SPAWN = new SetFlag<CreatureType>("deny-spawn", new CreatureTypeFlag(null));
    public static final IntegerFlag HEAL_DELAY = new IntegerFlag("heal-delay");
    public static final IntegerFlag HEAL_AMOUNT = new IntegerFlag("heal-amount");
    public static final IntegerFlag MIN_HEAL = new IntegerFlag("heal-min-health");
    public static final IntegerFlag MAX_HEAL = new IntegerFlag("heal-max-health");
    public static final IntegerFlag FEED_DELAY = new IntegerFlag("feed-delay");
    public static final IntegerFlag FEED_AMOUNT = new IntegerFlag("feed-amount");
    public static final IntegerFlag MIN_FOOD = new IntegerFlag("feed-min-hunger");
    public static final IntegerFlag MAX_FOOD = new IntegerFlag("feed-max-hunger");
    public static final VectorFlag TELE_LOC = new VectorFlag("teleport");
    public static final RegionGroupFlag TELE_PERM = new RegionGroupFlag("teleport-group", RegionGroupFlag.RegionGroup.MEMBERS);
    public static final VectorFlag SPAWN_LOC = new VectorFlag("spawn");
    public static final RegionGroupFlag SPAWN_PERM = new RegionGroupFlag("spawn-group", RegionGroupFlag.RegionGroup.MEMBERS);
    public static final BooleanFlag BUYABLE = new BooleanFlag("buyable");
    public static final DoubleFlag PRICE = new DoubleFlag("price");
    public static final SetFlag<String> BLOCKED_CMDS = new SetFlag<String>("blocked-cmds", new CommandStringFlag(null));
    public static final SetFlag<String> ALLOWED_CMDS = new SetFlag<String>("allowed-cmds", new CommandStringFlag(null));

    public static final Flag<?>[] flagsList = new Flag<?>[] {
        PASSTHROUGH, BUILD, PVP, CHEST_ACCESS, PISTONS,
        TNT, LIGHTER, USE, PLACE_VEHICLE, DESTROY_VEHICLE, SLEEP,
        MOB_DAMAGE, MOB_SPAWNING, DENY_SPAWN, INVINCIBILITY, EXP_DROPS,
        CREEPER_EXPLOSION, ENDERDRAGON_BLOCK_DAMAGE, GHAST_FIREBALL, ENDER_BUILD,
        GREET_MESSAGE, FAREWELL_MESSAGE, NOTIFY_ENTER, NOTIFY_LEAVE,
        EXIT, EXIT_PERM, ENTRY, ENTRY_PERM, LIGHTNING,
        HEAL_AMOUNT, HEAL_DELAY, MIN_HEAL, MAX_HEAL,
        FEED_DELAY, FEED_AMOUNT, MIN_FOOD, MAX_FOOD,
        SNOW_FALL, SNOW_MELT, ICE_FORM, ICE_MELT,
        MUSHROOMS, LEAF_DECAY, GRASS_SPREAD,
        FIRE_SPREAD, LAVA_FIRE, LAVA_FLOW, WATER_FLOW,
        TELE_LOC, TELE_PERM, SPAWN_LOC, SPAWN_PERM,
        BLOCKED_CMDS, ALLOWED_CMDS, PRICE, BUYABLE,
    };

    public static final Map<String, Flag<?>> customFlagsList = new HashMap<String, Flag<?>>();

    static {
        ENTRY.setGroupFlag(ENTRY_PERM);
        EXIT.setGroupFlag(EXIT_PERM);
    }

    private DefaultFlag() {
    }

    public static Flag<?>[] getFlags() {
        // check if we need to merge custom flags
        if (!(customFlagsList.size() > 0)) {
            return flagsList;
        }
        int size = customFlagsList.size() + flagsList.length;
        // merge the custom flags and default flags
        Flag<?>[] flags = customFlagsList.values().toArray(new Flag<?>[size]);
        int k = 0;
        for (int i = customFlagsList.size(); i < size; i++) {
            flags[i] = flagsList[k];
            k++;
        }
        return flags;
    }

    /**
     * <p>Adds a custom Flag<?> to the Default.getFlags() list.</p>
     * @param flag to add
     * @return <p>false if the flag name is already taken by the default flags
     * or an other custom flag.</p>
     * <p>true if the flag was added successfully</p>
     */
    public static boolean addCustomFlag(Flag<?> flag) {
        if (customFlagsList.containsKey(flag.getName())) {
            return false;
        }
        for (Flag<?> fl : flagsList) {
            if (fl.getName().equalsIgnoreCase(flag.getName())) {
                return false;
            }
        }
        customFlagsList.put(flag.getName(), flag);
        return true;
    }

    /**
     * Get the legacy flag.
     *
     * @param flagString
     * @return null if not found
     */
    public static StateFlag getLegacyFlag(String flagString) {
        for (Flag<?> flag : flagsList) {
            if (flag instanceof StateFlag && flagString.equals(String.valueOf(flag.getLegacyCode()))) {
                return (StateFlag) flag;
            }
        }

        return null;
    }

    public static class DuplicateFlagException extends Exception {

        public DuplicateFlagException() {
            super("There is already a flag with that name. Cannot add the custom flag!");
        }
    }
}
