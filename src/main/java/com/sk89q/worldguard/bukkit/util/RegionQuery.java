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

package com.sk89q.worldguard.bukkit.util;

import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.BukkitUtil;
import com.sk89q.worldguard.bukkit.ConfigurationManager;
import com.sk89q.worldguard.bukkit.WorldConfiguration;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.GlobalRegionManager;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;

import static com.sk89q.worldguard.bukkit.BukkitUtil.toVector;

public class RegionQuery {

    private final WorldGuardPlugin plugin;
    private final ConfigurationManager config;
    private final GlobalRegionManager globalManager;
    @Nullable
    private final LocalPlayer localPlayer;

    public RegionQuery(WorldGuardPlugin plugin, @Nullable Player player) {
        this.plugin = plugin;
        this.config = plugin.getGlobalStateManager();
        this.globalManager = plugin.getGlobalRegionManager();
        this.localPlayer = player != null ? plugin.wrapPlayer(player) : null;
    }

    public boolean canBuild(Block block) {
        return canBuild(block.getLocation());
    }

    public boolean canBuild(Location location) {
        World world = location.getWorld();
        WorldConfiguration worldConfig = config.get(world);

        if (!worldConfig.useRegions) {
            return true;
        }

        if (globalManager.hasBypass(localPlayer, world)) {
            return true;
        } else {
            RegionManager manager = globalManager.get(location.getWorld());
            return manager.getApplicableRegions(BukkitUtil.toVector(location)).canBuild(localPlayer);
        }
    }

    public boolean canConstruct(Location location) {
        World world = location.getWorld();
        WorldConfiguration worldConfig = config.get(world);

        if (!worldConfig.useRegions) {
            return true;
        }

        if (globalManager.hasBypass(localPlayer, world)) {
            return true;
        } else {
            RegionManager manager = globalManager.get(location.getWorld());
            return manager.getApplicableRegions(BukkitUtil.toVector(location)).canConstruct(localPlayer);
        }
    }

    public boolean allows(StateFlag flag, Location location) {
        World world = location.getWorld();
        WorldConfiguration worldConfig = config.get(world);

        if (!worldConfig.useRegions) {
            return true;
        }

        RegionManager manager = globalManager.get(location.getWorld());
        return manager.getApplicableRegions(toVector(location)).allows(flag, localPlayer);
    }

}
