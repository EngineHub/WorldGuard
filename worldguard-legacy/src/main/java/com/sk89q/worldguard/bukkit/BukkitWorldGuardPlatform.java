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

package com.sk89q.worldguard.bukkit;

import com.sk89q.worldedit.util.report.ReportList;
import com.sk89q.worldedit.world.gamemode.GameMode;
import com.sk89q.worldedit.world.gamemode.GameModes;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.protection.events.flags.FlagContextCreateEvent;
import com.sk89q.worldguard.bukkit.session.BukkitSessionManager;
import com.sk89q.worldguard.bukkit.util.report.PerformanceReport;
import com.sk89q.worldguard.bukkit.util.report.PluginReport;
import com.sk89q.worldguard.bukkit.util.report.SchedulerReport;
import com.sk89q.worldguard.bukkit.util.report.ServerReport;
import com.sk89q.worldguard.bukkit.util.report.ServicesReport;
import com.sk89q.worldguard.bukkit.util.report.WorldReport;
import com.sk89q.worldguard.internal.platform.DebugHandler;
import com.sk89q.worldguard.internal.platform.StringMatcher;
import com.sk89q.worldguard.internal.platform.WorldGuardPlatform;
import com.sk89q.worldguard.protection.flags.FlagContext;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.session.SessionManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permissible;

import java.nio.file.Path;
import java.util.Set;

public class BukkitWorldGuardPlatform implements WorldGuardPlatform {

    private SessionManager sessionManager;
    private BukkitConfigurationManager configuration;
    private BukkitRegionContainer regionContainer;
    private BukkitDebugHandler debugHandler;
    private StringMatcher stringMatcher;

    public BukkitWorldGuardPlatform() {
    }

    @Override
    public String getPlatformName() {
        return "Bukkit-Official";
    }

    @Override
    public String getPlatformVersion() {
        return WorldGuardPlugin.inst().getDescription().getVersion();
    }

    @Override
    public void notifyFlagContextCreate(FlagContext.FlagContextBuilder flagContextBuilder) {
        Bukkit.getServer().getPluginManager().callEvent(new FlagContextCreateEvent(flagContextBuilder));
    }

    @Override
    public BukkitConfigurationManager getGlobalStateManager() {
        return configuration;
    }

    @Override
    public StringMatcher getMatcher() {
        return stringMatcher;
    }

    @Override
    public SessionManager getSessionManager() {
        return this.sessionManager;
    }

    @Override
    public void broadcastNotification(String message) {
        Bukkit.broadcast(message, "worldguard.notify");
        Set<Permissible> subs = Bukkit.getPluginManager().getPermissionSubscriptions("worldguard.notify");
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            if (!(subs.contains(player) && player.hasPermission("worldguard.notify")) &&
                    WorldGuardPlugin.inst().hasPermission(player, "worldguard.notify")) { // Make sure the player wasn't already broadcasted to.
                player.sendMessage(message);
            }
        }
        WorldGuard.logger.info(message);
    }

    @Override
    public void load() {
        sessionManager = new BukkitSessionManager();
        configuration = new BukkitConfigurationManager(WorldGuardPlugin.inst());
        configuration.load();
        regionContainer = new BukkitRegionContainer(WorldGuardPlugin.inst());
        regionContainer.initialize();
        debugHandler = new BukkitDebugHandler(WorldGuardPlugin.inst());
        stringMatcher = new BukkitStringMatcher();
    }

    @Override
    public void unload() {
        configuration.unload();
        regionContainer.unload();
    }

    @Override
    public RegionContainer getRegionContainer() {
        return this.regionContainer;
    }

    @Override
    public DebugHandler getDebugHandler() {
        return debugHandler;
    }

    @Override
    public GameMode getDefaultGameMode() {
        return GameModes.get(Bukkit.getServer().getDefaultGameMode().name().toLowerCase());
    }

    @Override
    public Path getConfigDir() {
        return WorldGuardPlugin.inst().getDataFolder().toPath();
    }

    @Override
    public void stackPlayerInventory(LocalPlayer localPlayer) {
        boolean ignoreMax = localPlayer.hasPermission("worldguard.stack.illegitimate");
        boolean ignoreDamaged = localPlayer.hasPermission("worldguard.stack.damaged");

        Player player = ((BukkitPlayer) localPlayer).getPlayer();

        ItemStack[] items = player.getInventory().getContents();
        int len = items.length;

        int affected = 0;

        for (int i = 0; i < len; i++) {
            ItemStack item = items[i];

            // Avoid infinite stacks and stacks with durability
            if (item == null || item.getAmount() <= 0
                    || (!ignoreMax && item.getMaxStackSize() == 1)) {
                continue;
            }

            int max = ignoreMax ? 64 : item.getMaxStackSize();

            if (item.getAmount() < max) {
                int needed = max - item.getAmount(); // Number of needed items until max

                // Find another stack of the same type
                for (int j = i + 1; j < len; j++) {
                    ItemStack item2 = items[j];

                    // Avoid infinite stacks and stacks with durability
                    if (item2 == null || item2.getAmount() <= 0
                            || (!ignoreMax && item.getMaxStackSize() == 1)) {
                        continue;
                    }

                    // Same type?
                    // Blocks store their color in the damage value
                    if (item2.getType() == item.getType() &&
                            (ignoreDamaged || item.getDurability() == item2.getDurability()) &&
                            ((item.getItemMeta() == null && item2.getItemMeta() == null)
                                    || (item.getItemMeta() != null &&
                                    item.getItemMeta().equals(item2.getItemMeta())))) {
                        // This stack won't fit in the parent stack
                        if (item2.getAmount() > needed) {
                            item.setAmount(max);
                            item2.setAmount(item2.getAmount() - needed);
                            break;
                            // This stack will
                        } else {
                            items[j] = null;
                            item.setAmount(item.getAmount() + item2.getAmount());
                            needed = max - item.getAmount();
                        }

                        affected++;
                    }
                }
            }
        }

        if (affected > 0) {
            player.getInventory().setContents(items);
        }
    }

    @Override
    public void addPlatformReports(ReportList report) {
        report.add(new ServerReport());
        report.add(new PluginReport());
        report.add(new SchedulerReport());
        report.add(new ServicesReport());
        report.add(new WorldReport());
        report.add(new PerformanceReport());
    }
}
