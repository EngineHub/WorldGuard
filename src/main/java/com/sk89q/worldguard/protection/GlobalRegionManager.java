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

package com.sk89q.worldguard.protection;

import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.BukkitUtil;
import com.sk89q.worldguard.bukkit.ConfigurationManager;
import com.sk89q.worldguard.bukkit.WorldConfiguration;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.List;

import static com.sk89q.worldguard.bukkit.BukkitUtil.toVector;

public class GlobalRegionManager {

    private final WorldGuardPlugin plugin;
    private final ConfigurationManager config;
    private final ManagerContainer container;

    public GlobalRegionManager(WorldGuardPlugin plugin) {
        this.plugin = plugin;
        config = plugin.getGlobalStateManager();
        container = new ManagerContainer(config);
    }

    @Nullable
    public RegionManager load(World world) {
        return container.load(world.getName());
    }

    public void preload() {
        for (World world : plugin.getServer().getWorlds()) {
            load(world);
        }
    }

    public void unload(String name) {
        container.unload(name);
    }

    public void unload() {
        container.unloadAll();
    }

    public void unloadAll() {
        container.unloadAll();
    }

    @Nullable
    public RegionManager get(World world) {
        return container.get(world.getName());
    }

    public List<RegionManager> getLoaded() {
        return container.getLoaded();
    }

    public boolean hasBypass(LocalPlayer player, World world) {
        return player.hasPermission("worldguard.region.bypass." + world.getName());
    }

    public boolean hasBypass(Player player, World world) {
        return plugin.hasPermission(player, "worldguard.region.bypass." + world.getName());
    }

    public boolean canBuild(Player player, Block block) {
        return canBuild(player, block.getLocation());
    }

    public boolean canBuild(Player player, Location loc) {
        World world = loc.getWorld();
        WorldConfiguration worldConfig = config.get(world);

        if (!worldConfig.useRegions) {
            return true;
        }

        LocalPlayer localPlayer = plugin.wrapPlayer(player);

        if (!hasBypass(player, world)) {
            RegionManager mgr = get(world);

            if (mgr != null && !mgr.getApplicableRegions(BukkitUtil.toVector(loc)).canBuild(localPlayer)) {
                return false;
            }
        }

        return true;
    }

    public boolean canConstruct(Player player, Block block) {
        return canConstruct(player, block.getLocation());
    }

    public boolean canConstruct(Player player, Location loc) {
        World world = loc.getWorld();
        WorldConfiguration worldConfig = config.get(world);

        if (!worldConfig.useRegions) {
            return true;
        }

        LocalPlayer localPlayer = plugin.wrapPlayer(player);

        if (!hasBypass(player, world)) {
            RegionManager mgr = get(world);

            if (mgr != null) {
                final ApplicableRegionSet applicableRegions = mgr.getApplicableRegions(BukkitUtil.toVector(loc));
                if (!applicableRegions.canBuild(localPlayer)) {
                    return false;
                }
                if (!applicableRegions.canConstruct(localPlayer)) {
                    return false;
                }
            }
        }

        return true;
    }

    public boolean allows(StateFlag flag, Location loc) {
        return allows(flag, loc, null);
    }

    public boolean allows(StateFlag flag, Location loc, @Nullable LocalPlayer player) {
        World world = loc.getWorld();
        WorldConfiguration worldConfig = config.get(world);

        if (!worldConfig.useRegions) {
            return true;
        }

        RegionManager mgr = get(world);
        return mgr == null || mgr.getApplicableRegions(toVector(loc)).allows(flag, player);
    }

}
