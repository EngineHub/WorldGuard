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

import org.bukkit.GameMode;
import org.bukkit.entity.EntityType;

/**
 *
 * @author sk89q
 */
public final class DefaultFlag {

    public static final StateFlag PASSTHROUGH = new StateFlag("passthrough", false, RegionGroup.ALL);
    public static final StateFlag BUILD = new StateFlag("build", true, RegionGroup.NON_MEMBERS);
    public static final RegionGroupFlag CONSTRUCT = new RegionGroupFlag("construct", RegionGroup.MEMBERS);
    public static final StateFlag PVP = new StateFlag("pvp", true, RegionGroup.ALL);
    public static final StateFlag MOB_DAMAGE = new StateFlag("mob-damage", true, RegionGroup.ALL);
    public static final StateFlag MOB_SPAWNING = new StateFlag("mob-spawning", true, RegionGroup.ALL);
    public static final StateFlag CREEPER_EXPLOSION = new StateFlag("creeper-explosion", true, RegionGroup.ALL);
    public static final StateFlag ENDERDRAGON_BLOCK_DAMAGE = new StateFlag("enderdragon-block-damage", true);
    public static final StateFlag GHAST_FIREBALL = new StateFlag("ghast-fireball", true, RegionGroup.ALL);
    public static final StateFlag OTHER_EXPLOSION = new StateFlag("other-explosion", true);
    public static final StateFlag SLEEP = new StateFlag("sleep", true);
    public static final StateFlag TNT = new StateFlag("tnt", true, RegionGroup.ALL);
    public static final StateFlag LIGHTER = new StateFlag("lighter", true);
    public static final StateFlag FIRE_SPREAD = new StateFlag("fire-spread", true);
    public static final StateFlag LAVA_FIRE = new StateFlag("lava-fire", true);
    public static final StateFlag LIGHTNING = new StateFlag("lightning", true);
    public static final StateFlag CHEST_ACCESS = new StateFlag("chest-access", false);
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
    public static final StateFlag MYCELIUM_SPREAD = new StateFlag("mycelium-spread", true);
    public static final StateFlag VINE_GROWTH = new StateFlag("vine-growth", true);
    public static final StateFlag ENDER_BUILD = new StateFlag("enderman-grief", true);
    public static final StateFlag INVINCIBILITY = new StateFlag("invincible", false, RegionGroup.ALL);
    public static final StateFlag EXP_DROPS = new StateFlag("exp-drops", true, RegionGroup.ALL);
    public static final StateFlag SEND_CHAT = new StateFlag("send-chat", true);
    public static final StateFlag RECEIVE_CHAT = new StateFlag("receive-chat", true);
    public static final StateFlag ENTRY = new StateFlag("entry", true);
    public static final StateFlag EXIT = new StateFlag("exit", true);
    public static final StateFlag ITEM_DROP = new StateFlag("item-drop", true);
    public static final StateFlag ENDERPEARL = new StateFlag("enderpearl", true);
    public static final StateFlag ENTITY_PAINTING_DESTROY = new StateFlag("entity-painting-destroy", true);
    public static final StateFlag ENTITY_ITEM_FRAME_DESTROY = new StateFlag("entity-item-frame-destroy", true);
    public static final StateFlag POTION_SPLASH = new StateFlag("potion-splash", true);
    public static final StringFlag GREET_MESSAGE = new StringFlag("greeting", RegionGroup.ALL);
    public static final StringFlag FAREWELL_MESSAGE = new StringFlag("farewell", RegionGroup.ALL);
    public static final BooleanFlag NOTIFY_ENTER = new BooleanFlag("notify-enter", RegionGroup.ALL);
    public static final BooleanFlag NOTIFY_LEAVE = new BooleanFlag("notify-leave", RegionGroup.ALL);
    public static final SetFlag<EntityType> DENY_SPAWN = new SetFlag<EntityType>("deny-spawn", RegionGroup.ALL, new EntityTypeFlag(null));
    public static final EnumFlag<GameMode> GAME_MODE = new EnumFlag<GameMode>("game-mode", GameMode.class, RegionGroup.ALL);
    public static final IntegerFlag HEAL_DELAY = new IntegerFlag("heal-delay", RegionGroup.ALL);
    public static final IntegerFlag HEAL_AMOUNT = new IntegerFlag("heal-amount", RegionGroup.ALL);
    public static final IntegerFlag MIN_HEAL = new IntegerFlag("heal-min-health", RegionGroup.ALL);
    public static final IntegerFlag MAX_HEAL = new IntegerFlag("heal-max-health", RegionGroup.ALL);
    public static final IntegerFlag FEED_DELAY = new IntegerFlag("feed-delay", RegionGroup.ALL);
    public static final IntegerFlag FEED_AMOUNT = new IntegerFlag("feed-amount", RegionGroup.ALL);
    public static final IntegerFlag MIN_FOOD = new IntegerFlag("feed-min-hunger", RegionGroup.ALL);
    public static final IntegerFlag MAX_FOOD = new IntegerFlag("feed-max-hunger", RegionGroup.ALL);
    // public static final IntegerFlag MAX_PLAYERS = new IntegerFlag("max-players-allowed", RegionGroup.ALL);
    // public static final StringFlag MAX_PLAYERS_MESSAGE = new StringFlag("max-players-reject-message", RegionGroup.ALL);
    public static final LocationFlag TELE_LOC = new LocationFlag("teleport", RegionGroup.MEMBERS);
    public static final LocationFlag SPAWN_LOC = new LocationFlag("spawn", RegionGroup.MEMBERS);
    public static final StateFlag ENABLE_SHOP = new StateFlag("allow-shop", false);
    public static final BooleanFlag BUYABLE = new BooleanFlag("buyable");
    public static final DoubleFlag PRICE = new DoubleFlag("price");
    public static final SetFlag<String> BLOCKED_CMDS = new SetFlag<String>("blocked-cmds", RegionGroup.ALL, new CommandStringFlag(null));
    public static final SetFlag<String> ALLOWED_CMDS = new SetFlag<String>("allowed-cmds", RegionGroup.ALL, new CommandStringFlag(null));

    public static final Flag<?>[] flagsList = new Flag<?>[] {
        PASSTHROUGH, BUILD, CONSTRUCT, PVP, CHEST_ACCESS, PISTONS,
        TNT, LIGHTER, USE, PLACE_VEHICLE, DESTROY_VEHICLE, SLEEP,
        MOB_DAMAGE, MOB_SPAWNING, DENY_SPAWN, INVINCIBILITY, EXP_DROPS,
        CREEPER_EXPLOSION, OTHER_EXPLOSION, ENDERDRAGON_BLOCK_DAMAGE, GHAST_FIREBALL, ENDER_BUILD,
        GREET_MESSAGE, FAREWELL_MESSAGE, NOTIFY_ENTER, NOTIFY_LEAVE,
        EXIT, ENTRY, LIGHTNING, ENTITY_PAINTING_DESTROY, ENDERPEARL,
        ENTITY_ITEM_FRAME_DESTROY, ITEM_DROP, /*MAX_PLAYERS, MAX_PLAYERS_MESSAGE,*/
        HEAL_AMOUNT, HEAL_DELAY, MIN_HEAL, MAX_HEAL,
        FEED_DELAY, FEED_AMOUNT, MIN_FOOD, MAX_FOOD,
        SNOW_FALL, SNOW_MELT, ICE_FORM, ICE_MELT, GAME_MODE,
        MUSHROOMS, LEAF_DECAY, GRASS_SPREAD, MYCELIUM_SPREAD, VINE_GROWTH,
        SEND_CHAT, RECEIVE_CHAT, FIRE_SPREAD, LAVA_FIRE, LAVA_FLOW, WATER_FLOW,
        TELE_LOC, SPAWN_LOC, POTION_SPLASH,
        BLOCKED_CMDS, ALLOWED_CMDS, PRICE, BUYABLE, ENABLE_SHOP
    };

    private DefaultFlag() {
    }

    public static Flag<?>[] getFlags() {
        return flagsList;
    }
    
    /**
     * Try to match the flag with the given ID using a fuzzy name match.
     * 
     * @param id the flag ID
     * @return a flag, or null
     */
    public static Flag<?> fuzzyMatchFlag(String id) {
        for (Flag<?> flag : DefaultFlag.getFlags()) {
            if (flag.getName().replace("-", "").equalsIgnoreCase(id.replace("-", ""))) {
                return flag;
            }
        }
        
        return null;
    }
}
