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
import com.sk89q.worldguard.bukkit.listener.module.BlockFadeListener;
import com.sk89q.worldguard.bukkit.listener.module.BlockFlowListener;
import com.sk89q.worldguard.bukkit.listener.module.BlockSpreadListener;
import com.sk89q.worldguard.bukkit.listener.module.ItemDurabilityListener;
import com.sk89q.worldguard.bukkit.listener.module.LavaSpreadLimiterListener;
import com.sk89q.worldguard.bukkit.listener.module.ObsidianGeneratorListener;
import com.sk89q.worldguard.bukkit.listener.module.SpongeListener;
import com.sk89q.worldguard.bukkit.listener.module.TickHaltingListener;
import com.sk89q.worldguard.bukkit.listener.module.WaterProtectionListener;
import com.sk89q.worldguard.protection.flags.StateFlag;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.Listener;

import java.util.Set;

import static com.sk89q.worldguard.bukkit.listener.Materials.isMushroom;
import static com.sk89q.worldguard.protection.flags.DefaultFlag.*;

/**
 * Implements the listeners for flags and configuration options.
 */
public class FlagListeners {

    private final WorldGuardPlugin plugin;

    public FlagListeners(WorldGuardPlugin plugin) {
        this.plugin = plugin;
    }

    private ConfigurationManager getConfig() {
        return plugin.getGlobalStateManager();
    }

    private WorldConfiguration getConfig(World world) {
        ConfigurationManager cfg = plugin.getGlobalStateManager();
        return cfg.get(world);
    }

    private WorldConfiguration getConfig(Block block) {
        return getConfig(block.getWorld());
    }

    private boolean testState(Block block, StateFlag flag) {
        return plugin.getGlobalRegionManager().allows(flag, block.getLocation());
    }

    private boolean isNonEmptyAndContains(Set<Integer> blocks, Material material) {
        return !blocks.isEmpty() && blocks.contains(material.getId());
    }

    private boolean isNonEmptyOrContains(Set<Integer> blocks, Material material) {
        return !blocks.isEmpty() || blocks.contains(material.getId());
    }

    public void registerEvents() {
        registerEvents(new BlockFadeListener(b -> b.getType() == Material.ICE && (getConfig(b).disableIceMelting || !testState(b, ICE_MELT))));
        registerEvents(new BlockFadeListener(b -> b.getType() == Material.SNOW && (getConfig(b).disableSnowMelting || !testState(b, SNOW_MELT))));
        registerEvents(new BlockFadeListener(b -> b.getType() == Material.SOIL && (getConfig(b).disableSoilDehydration || !testState(b, SOIL_DRY))));

        registerEvents(new BlockSpreadListener(b -> isMushroom(b.getType()) && (getConfig(b).disableMushroomSpread || !testState(b, MUSHROOMS))));
        registerEvents(new BlockSpreadListener(b -> b.getType() == Material.GRASS && (getConfig(b).disableGrassGrowth || !testState(b, GRASS_SPREAD))));
        registerEvents(new BlockSpreadListener(b -> b.getType() == Material.MYCEL && (getConfig(b).disableMyceliumSpread || !testState(b, MYCELIUM_SPREAD))));
        registerEvents(new BlockSpreadListener(b -> b.getType() == Material.VINE && (getConfig(b).disableVineGrowth || !testState(b, VINE_GROWTH))));

        registerEvents(new BlockFlowListener(b -> Materials.isWater(b.getType()) && getConfig(b).highFreqFlags && !testState(b, WATER_FLOW)));
        registerEvents(new BlockFlowListener(b -> Materials.isLava(b.getType()) && getConfig(b).highFreqFlags && !testState(b, LAVA_FLOW)));

        registerEvents(new TickHaltingListener(c -> getConfig().activityHaltToggle));

        registerEvents(new SpongeListener(w -> getConfig(w).spongeBehavior));
        registerEvents(new ItemDurabilityListener(w -> getConfig(w).itemDurability));
        registerEvents(new WaterProtectionListener(b -> isNonEmptyAndContains(getConfig(b).preventWaterDamage, b.getType())));
        registerEvents(new LavaSpreadLimiterListener(b -> isNonEmptyOrContains(getConfig(b).allowedLavaSpreadOver, b.getType())));
        registerEvents(new ObsidianGeneratorListener(b -> getConfig(b).disableObsidianGenerators));
    }

    private void registerEvents(Listener listener) {
        Bukkit.getServer().getPluginManager().registerEvents(listener, plugin);
    }

}
