/*
 * WorldGuard, a suite of tools for Minecraft
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldGuard team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.worldguard.protection.flags;

import com.sk89q.worldedit.util.formatting.ColorCodeBuilder;
import com.sk89q.worldedit.util.formatting.Style;
import com.sk89q.worldedit.util.formatting.StyledFragment;
import com.sk89q.worldedit.world.entity.EntityType;
import com.sk89q.worldedit.world.gamemode.GameMode;
import com.sk89q.worldedit.world.weather.WeatherType;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;

import javax.annotation.Nullable;

/**
 * The flags that are used in WorldGuard.
 */
public final class Flags {

    // Overrides membership check
    public static final StateFlag PASSTHROUGH = register(new StateFlag("passthrough", false));

    // This flag is unlike the others. It forces the checking of region membership
    public static final StateFlag BUILD = register(new BuildFlag("build", true));

    // These flags are used in tandem with the BUILD flag - if the player can
    // build, then the following flags do not need to be checked (although they
    // are still checked for DENY), so they are false by default
    public static final StateFlag BLOCK_BREAK = register(new StateFlag("block-break", false));
    public static final StateFlag BLOCK_PLACE = register(new StateFlag("block-place", false));
    public static final StateFlag USE = register(new StateFlag("use", false));
    public static final StateFlag INTERACT = register(new StateFlag("interact", false));
    public static final StateFlag DAMAGE_ANIMALS = register(new StateFlag("damage-animals", false));
    public static final StateFlag PVP = register(new StateFlag("pvp", false));
    public static final StateFlag SLEEP = register(new StateFlag("sleep", false));
    public static final StateFlag TNT = register(new StateFlag("tnt", false));
    public static final StateFlag CHEST_ACCESS = register(new StateFlag("chest-access", false));
    public static final StateFlag PLACE_VEHICLE = register(new StateFlag("vehicle-place", false));
    public static final StateFlag DESTROY_VEHICLE = register(new StateFlag("vehicle-destroy", false));
    public static final StateFlag LIGHTER = register(new StateFlag("lighter", false));
    public static final StateFlag RIDE = register(new StateFlag("ride", false));
    public static final StateFlag POTION_SPLASH = register(new StateFlag("potion-splash", false));

    // These flags are similar to the ones above (used in tandem with BUILD),
    // but their defaults are set to TRUE because it is more user friendly.
    // However, it is not possible to disable these flags by default in all
    // regions because setting DENY in __global__ would also override the
    // BUILD flag. In the future, StateFlags will need a DISALLOW state.
    public static final StateFlag ITEM_PICKUP = register(new StateFlag("item-pickup", true)); // Intentionally true
    public static final StateFlag ITEM_DROP = register(new StateFlag("item-drop", true)); // Intentionally true
    public static final StateFlag EXP_DROPS = register(new StateFlag("exp-drops", true)); // Intentionally true

    // These flags adjust behavior and are not checked in tandem with the
    // BUILD flag so they need to be TRUE for their defaults.
    public static final StateFlag MOB_DAMAGE = register(new StateFlag("mob-damage", true));
    public static final StateFlag MOB_SPAWNING = register(new StateFlag("mob-spawning", true));
    public static final StateFlag CREEPER_EXPLOSION = register(new StateFlag("creeper-explosion", true));
    public static final StateFlag ENDERDRAGON_BLOCK_DAMAGE = register(new StateFlag("enderdragon-block-damage", true));
    public static final StateFlag GHAST_FIREBALL = register(new StateFlag("ghast-fireball", true));
    public static final StateFlag FIREWORK_DAMAGE = register(new StateFlag("firework-damage", true));
    public static final StateFlag OTHER_EXPLOSION = register(new StateFlag("other-explosion", true));
    public static final StateFlag WITHER_DAMAGE = register(new StateFlag("wither-damage", true));
    public static final StateFlag FIRE_SPREAD = register(new StateFlag("fire-spread", true));
    public static final StateFlag LAVA_FIRE = register(new StateFlag("lava-fire", true));
    public static final StateFlag LIGHTNING = register(new StateFlag("lightning", true));
    public static final StateFlag WATER_FLOW = register(new StateFlag("water-flow", true));
    public static final StateFlag LAVA_FLOW = register(new StateFlag("lava-flow", true));
    public static final StateFlag PISTONS = register(new StateFlag("pistons", true));
    public static final StateFlag SNOW_FALL = register(new StateFlag("snow-fall", true));
    public static final StateFlag SNOW_MELT = register(new StateFlag("snow-melt", true));
    public static final StateFlag ICE_FORM = register(new StateFlag("ice-form", true));
    public static final StateFlag ICE_MELT = register(new StateFlag("ice-melt", true));
    public static final StateFlag MUSHROOMS = register(new StateFlag("mushroom-growth", true));
    public static final StateFlag LEAF_DECAY = register(new StateFlag("leaf-decay", true));
    public static final StateFlag GRASS_SPREAD = register(new StateFlag("grass-growth", true));
    public static final StateFlag MYCELIUM_SPREAD = register(new StateFlag("mycelium-spread", true));
    public static final StateFlag VINE_GROWTH = register(new StateFlag("vine-growth", true));
    public static final StateFlag SOIL_DRY = register(new StateFlag("soil-dry", true));
    public static final StateFlag ENDER_BUILD = register(new StateFlag("enderman-grief", true));
    public static final StateFlag INVINCIBILITY = register(new StateFlag("invincible", false));
    public static final StateFlag SEND_CHAT = register(new StateFlag("send-chat", true));
    public static final StateFlag RECEIVE_CHAT = register(new StateFlag("receive-chat", true));
    public static final StateFlag ENTRY = register(new StateFlag("entry", true, RegionGroup.NON_MEMBERS));
    public static final StateFlag EXIT = register(new StateFlag("exit", true, RegionGroup.NON_MEMBERS));
    public static final StateFlag ENDERPEARL = register(new StateFlag("enderpearl", true));
    public static final StateFlag CHORUS_TELEPORT = register(new StateFlag("chorus-fruit-teleport", true));
    public static final StateFlag ENTITY_PAINTING_DESTROY = register(new StateFlag("entity-painting-destroy", true));
    public static final StateFlag ENTITY_ITEM_FRAME_DESTROY = register(new StateFlag("entity-item-frame-destroy", true));
    public static final StateFlag FALL_DAMAGE = register(new StateFlag("fall-damage", true));

    // FlagUtil that adjust behaviors that aren't state flags
    public static final StringFlag DENY_MESSAGE = register(new StringFlag("deny-message",
            ColorCodeBuilder.asColorCodes(new StyledFragment().append(new StyledFragment(Style.RED, Style.BOLD).append("Hey!"))
                    .append(new StyledFragment(Style.GRAY).append(" Sorry, but you can't %what% here.")))));
    public static final StringFlag ENTRY_DENY_MESSAGE = register(new StringFlag("entry-deny-message",
            ColorCodeBuilder.asColorCodes(new StyledFragment().append(new StyledFragment(Style.RED, Style.BOLD).append("Hey!"))
                    .append(new StyledFragment(Style.GRAY).append(" You are not permitted to enter this area.")))));
    public static final StringFlag EXIT_DENY_MESSAGE = register(new StringFlag("exit-deny-message",
            ColorCodeBuilder.asColorCodes(new StyledFragment().append(new StyledFragment(Style.RED, Style.BOLD).append("Hey!"))
                    .append(new StyledFragment(Style.GRAY).append(" You are not permitted to leave this area.")))));
    public static final BooleanFlag EXIT_OVERRIDE = register(new BooleanFlag("exit-override"));
    public static final StateFlag EXIT_VIA_TELEPORT = register(new StateFlag("exit-via-teleport", true));
    public static final StringFlag GREET_MESSAGE = register(new StringFlag("greeting"));
    public static final StringFlag FAREWELL_MESSAGE = register(new StringFlag("farewell"));
    public static final BooleanFlag NOTIFY_ENTER = register(new BooleanFlag("notify-enter"));
    public static final BooleanFlag NOTIFY_LEAVE = register(new BooleanFlag("notify-leave"));
    public static final SetFlag<EntityType> DENY_SPAWN = register(new SetFlag<>("deny-spawn", new EntityTypeFlag(null)));
    public static final Flag<GameMode> GAME_MODE = register(new GameModeTypeFlag("game-mode"));
    public static final StringFlag TIME_LOCK = register(new StringFlag("time-lock"));
    public static final Flag<WeatherType> WEATHER_LOCK = register(new WeatherTypeFlag("weather-lock"));
    public static final IntegerFlag HEAL_DELAY = register(new IntegerFlag("heal-delay"));
    public static final IntegerFlag HEAL_AMOUNT = register(new IntegerFlag("heal-amount"));
    public static final DoubleFlag MIN_HEAL = register(new DoubleFlag("heal-min-health"));
    public static final DoubleFlag MAX_HEAL = register(new DoubleFlag("heal-max-health"));
    public static final IntegerFlag FEED_DELAY = register(new IntegerFlag("feed-delay"));
    public static final IntegerFlag FEED_AMOUNT = register(new IntegerFlag("feed-amount"));
    public static final IntegerFlag MIN_FOOD = register(new IntegerFlag("feed-min-hunger"));
    public static final IntegerFlag MAX_FOOD = register(new IntegerFlag("feed-max-hunger"));
    // public static final IntegerFlag MAX_PLAYERS = register(new IntegerFlag("max-players-allowed"));
    // public static final StringFlag MAX_PLAYERS_MESSAGE = register(new StringFlag("max-players-reject-message"));
    public static final LocationFlag TELE_LOC = register(new LocationFlag("teleport", RegionGroup.MEMBERS));
    public static final LocationFlag SPAWN_LOC = register(new LocationFlag("spawn", RegionGroup.MEMBERS));
    public static final SetFlag<String> BLOCKED_CMDS = register(new SetFlag<>("blocked-cmds", new CommandStringFlag(null)));
    public static final SetFlag<String> ALLOWED_CMDS = register(new SetFlag<>("allowed-cmds", new CommandStringFlag(null)));

    // these 3 are not used by worldguard and should be re-implemented in plugins that may use them using custom flag api
    @Deprecated
    public static final StateFlag ENABLE_SHOP = register(new StateFlag("allow-shop", false));
    @Deprecated
    public static final BooleanFlag BUYABLE = register(new BooleanFlag("buyable"));
    @Deprecated
    public static final DoubleFlag PRICE = register(new DoubleFlag("price"));

    private Flags() {
    }

    public static <T extends Flag> T register(final T flag) throws FlagConflictException {
        WorldGuard.getInstance().getFlagRegistry().register(flag);
        return flag;
    }

    public static @Nullable Flag get(final String id) {
        return WorldGuard.getInstance().getFlagRegistry().get(id);
    }

    /**
     * Try to match the flag with the given ID using a fuzzy name match.
     *
     * @param flagRegistry the flag registry
     * @param id the flag ID
     * @return a flag, or null
     */
    public static Flag<?> fuzzyMatchFlag(FlagRegistry flagRegistry, String id) {
        for (Flag<?> flag : flagRegistry) {
            if (flag.getName().replace("-", "").equalsIgnoreCase(id.replace("-", ""))) {
                return flag;
            }
        }

        return null;
    }

    /**
     * Dummy method to call that initialises the class.
     */
    public static void registerAll() {}
}
