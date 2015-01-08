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

package com.sk89q.worldguard.bukkit.listener;

import com.sk89q.worldguard.bukkit.ConfigurationManager;
import com.sk89q.worldguard.bukkit.WorldConfiguration;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.bukkit.internal.RegionQueryUtil;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * This processes per-player state information and is also meant to be used
 * as a scheduled task.
 *
 * @author sk89q
 */
public class FlagStateManager implements Runnable {

    public static final int RUN_DELAY = 20;

    private WorldGuardPlugin plugin;
    private Map<String, PlayerFlagState> states;

    /**
     * Construct the object.
     *
     * @param plugin The plugin instance
     */
    public FlagStateManager(WorldGuardPlugin plugin) {
        this.plugin = plugin;

        states = new HashMap<String, PlayerFlagState>();
    }

    /**
     * Run the task.
     */
    @Override
    public void run() {
        Collection<? extends Player> players = plugin.getServer().getOnlinePlayers();
        ConfigurationManager config = plugin.getGlobalStateManager();

        for (Player player : players) {
            WorldConfiguration worldConfig = config.get(player.getWorld());

            if (!worldConfig.useRegions) {
                continue;
            }

            PlayerFlagState state;

            synchronized (this) {
                state = states.get(player.getName());

                if (state == null) {
                    state = new PlayerFlagState();
                    states.put(player.getName(), state);
                }
            }

            ApplicableRegionSet applicable = plugin.getRegionContainer().createQuery().getApplicableRegions(player.getLocation());

            if (!RegionQueryUtil.isInvincible(plugin, player, applicable)
                    && !plugin.getGlobalStateManager().hasGodMode(player)
                    && !(player.getGameMode() == GameMode.CREATIVE)) {
                processHeal(applicable, player, state);
                processFeed(applicable, player, state);
            }
        }
    }

    /**
     * Process healing for a player.
     *
     * @param applicable The set of applicable regions
     * @param player The player to process healing flags on
     * @param state The player's state
     */
    private void processHeal(ApplicableRegionSet applicable, Player player,
            PlayerFlagState state) {

        if (player.getHealth() <= 0) {
            return;
        }

        long now = System.currentTimeMillis();

        Integer healAmount = applicable.getFlag(DefaultFlag.HEAL_AMOUNT);
        Integer healDelay = applicable.getFlag(DefaultFlag.HEAL_DELAY);
        Double minHealth = applicable.getFlag(DefaultFlag.MIN_HEAL);
        Double maxHealth = applicable.getFlag(DefaultFlag.MAX_HEAL);

        if (healAmount == null || healDelay == null || healAmount == 0 || healDelay < 0) {
            return;
        }
        if (minHealth == null) {
            minHealth = 0.0;
        }
        if (maxHealth == null) {
            maxHealth = player.getMaxHealth();
        }

        // Apply a cap to prevent possible exceptions
        minHealth = Math.min(player.getMaxHealth(), minHealth);
        maxHealth = Math.min(player.getMaxHealth(), maxHealth);

        if (player.getHealth() >= maxHealth && healAmount > 0) {
            return;
        }

        if (healDelay <= 0) {
            player.setHealth(healAmount > 0 ? maxHealth : minHealth); // this will insta-kill if the flag is unset
            state.lastHeal = now;
        } else if (now - state.lastHeal > healDelay * 1000) {
            // clamp health between minimum and maximum
            player.setHealth(Math.min(maxHealth, Math.max(minHealth, player.getHealth() + healAmount)));
            state.lastHeal = now;
        }
    }

    /**
     * Process restoring hunger for a player.
     *
     * @param applicable The set of applicable regions
     * @param player The player to process hunger flags on
     * @param state The player's state
     */
    private void processFeed(ApplicableRegionSet applicable, Player player,
            PlayerFlagState state) {

        long now = System.currentTimeMillis();

        Integer feedAmount = applicable.getFlag(DefaultFlag.FEED_AMOUNT);
        Integer feedDelay = applicable.getFlag(DefaultFlag.FEED_DELAY);
        Integer minHunger = applicable.getFlag(DefaultFlag.MIN_FOOD);
        Integer maxHunger = applicable.getFlag(DefaultFlag.MAX_FOOD);

        if (feedAmount == null || feedDelay == null || feedAmount == 0 || feedDelay < 0) {
            return;
        }
        if (minHunger == null) {
            minHunger = 0;
        }
        if (maxHunger == null) {
            maxHunger = 20;
        }

        // Apply a cap to prevent possible exceptions
        minHunger = Math.min(20, minHunger);
        maxHunger = Math.min(20, maxHunger);

        if (player.getFoodLevel() >= maxHunger && feedAmount > 0) {
            return;
        }

        if (feedDelay <= 0) {
            player.setFoodLevel(feedAmount > 0 ? maxHunger : minHunger);
            player.setSaturation(player.getFoodLevel());
            state.lastFeed = now;
        } else if (now - state.lastFeed > feedDelay * 1000) {
            // clamp health between minimum and maximum
            player.setFoodLevel(Math.min(maxHunger, Math.max(minHunger, player.getFoodLevel() + feedAmount)));
            player.setSaturation(player.getFoodLevel());
            state.lastFeed = now;
        }
    }

    /**
     * Forget a player.
     *
     * @param player The player to forget
     */
    public synchronized void forget(Player player) {
        states.remove(player.getName());
    }

    /**
     * Forget all managed players. Use with caution.
     */
    public synchronized void forgetAll() {
        states.clear();
    }

    /**
     * Get a player's flag state. A new state will be created if there is no existing
     * state for the player.
     *
     * @param player The player to get a state for
     * @return The {@code player}'s state
     */
    public synchronized PlayerFlagState getState(Player player) {
        PlayerFlagState state = states.get(player.getName());

        if (state == null) {
            state = new PlayerFlagState();
            states.put(player.getName(), state);
        }

        return state;
    }

    /**
     * Keeps state per player.
     */
    public static class PlayerFlagState {
        public long lastHeal;
        public long lastFeed;
        public String lastGreeting;
        public String lastFarewell;
        public Boolean lastExitAllowed = null;
        public Boolean notifiedForLeave = false;
        public Boolean notifiedForEnter = false;
        public GameMode lastGameMode;
        public World lastWorld;
        public int lastBlockX;
        public int lastBlockY;
        public int lastBlockZ;

        /* Used to cache invincibility status */
        public World lastInvincibleWorld;
        public int lastInvincibleX;
        public int lastInvincibleY;
        public int lastInvincibleZ;
        public boolean wasInvincible;
    }
}
