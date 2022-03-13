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

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.weather.WeatherType;
import com.sk89q.worldedit.world.weather.WeatherTypes;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.util.MiniMessageUtil;
import com.sk89q.worldguard.util.MessagingUtil;
import io.papermc.lib.PaperLib;
import org.bukkit.BanList.Type;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.Map;

public class BukkitPlayer extends com.sk89q.worldedit.bukkit.BukkitPlayer implements LocalPlayer {

    protected final WorldGuardPlugin plugin;
    private final boolean silenced;
    private String name;

    public BukkitPlayer(WorldGuardPlugin plugin, Player player) {
        this(plugin, player, false);
    }

    BukkitPlayer(WorldGuardPlugin plugin, Player player, boolean silenced) {
        super(player);
        this.plugin = plugin;
        this.silenced = silenced;
    }

    @Override
    public String getName() {
        if (this.name == null) {
            // getName() takes longer than before in newer versions of Minecraft
            this.name = getPlayer().getName();
        }
        return name;
    }

    @Override
    public boolean hasGroup(String group) {
        return plugin.inGroup(getPlayer(), group);
    }

    @Override
    public void kick(String msg) {
        if (!silenced) {
            getPlayer().kickPlayer(msg);
        }
    }

    @Override
    public void ban(String msg) {
        if (!silenced) {
            Bukkit.getBanList(Type.NAME).addBan(getName(), null, null, null);
            getPlayer().kickPlayer(msg);
        }
    }

    @Override
    public double getHealth() {
        return getPlayer().getHealth();
    }

    @Override
    public void setHealth(double health) {
        getPlayer().setHealth(health);
    }

    @Override
    public double getMaxHealth() {
        return getPlayer().getMaxHealth();
    }

    @Override
    public double getFoodLevel() {
        return getPlayer().getFoodLevel();
    }

    @Override
    public void setFoodLevel(double foodLevel) {
        getPlayer().setFoodLevel((int) foodLevel);
    }

    @Override
    public double getSaturation() {
        return getPlayer().getSaturation();
    }

    @Override
    public void setSaturation(double saturation) {
        getPlayer().setSaturation((float) saturation);
    }

    @Override
    public float getExhaustion() {
        return getPlayer().getExhaustion();
    }

    @Override
    public void setExhaustion(float exhaustion) {
        getPlayer().setExhaustion(exhaustion);
    }

    @Override
    public WeatherType getPlayerWeather() {
        org.bukkit.WeatherType playerWeather = getPlayer().getPlayerWeather();
        return playerWeather == null ? null : playerWeather == org.bukkit.WeatherType.CLEAR ? WeatherTypes.CLEAR : WeatherTypes.RAIN;
    }

    @Override
    public void setPlayerWeather(WeatherType weather) {
        getPlayer().setPlayerWeather(weather == WeatherTypes.CLEAR ? org.bukkit.WeatherType.CLEAR : org.bukkit.WeatherType.DOWNFALL);
    }

    @Override
    public void resetPlayerWeather() {
        getPlayer().resetPlayerWeather();
    }

    @Override
    public boolean isPlayerTimeRelative() {
        return getPlayer().isPlayerTimeRelative();
    }

    @Override
    public long getPlayerTimeOffset() {
        return getPlayer().getPlayerTimeOffset();
    }

    @Override
    public void setPlayerTime(long time, boolean relative) {
        getPlayer().setPlayerTime(time, relative);
    }

    @Override
    public void resetPlayerTime() {
        getPlayer().resetPlayerTime();
    }

    @Override
    public int getFireTicks() {
        return getPlayer().getFireTicks();
    }

    @Override
    public void setFireTicks(int fireTicks) {
        getPlayer().setFireTicks(fireTicks);
    }

    @Override
    public void setCompassTarget(Location location) {
        getPlayer().setCompassTarget(BukkitAdapter.adapt(location));
    }

    @Override
    @Deprecated
    public void sendTitle(String title, String subtitle) {
        if (WorldGuard.getInstance().getPlatform().getGlobalStateManager().get(getWorld()).forceDefaultTitleTimes) {
            getPlayer().sendTitle(title, subtitle, 10, 70, 20);
        } else {
            getPlayer().sendTitle(title, subtitle, -1, -1, -1);
        }
    }

    @Override
    public void sendMiniMessage(String message, Map<String, String> replacements) {
        if (!PaperLib.isPaper()) {
            plugin.getLogger().warning("Trying to send a minimessage formatted message without minimessage support.");
            return;
        }
        MiniMessageUtil.sendMiniMessage(this, message, replacements);
    }

    @Override
    public void sendMiniMessageTitle(String title, String subtitle, Map<String, String> replacements) {
        if (!PaperLib.isPaper()) {
            plugin.getLogger().warning("Trying to send a minimessage formatted title without minimessage support.");
            return;
        }
        MiniMessageUtil.sendMiniMessageTitle(this, title, subtitle, replacements);
    }

    @Override
    public void resetFallDistance() {
        getPlayer().setFallDistance(0);
    }

    @Override
    public void teleport(Location location, String successMessage, String failMessage) {
        teleport(location, successMessage, failMessage, Collections.emptyMap());
    }

    @Override
    public void teleport(Location location, String successMessage, String failMessage, Map<String, String> replacements) {
        PaperLib.teleportAsync(getPlayer(), BukkitAdapter.adapt(location))
                .thenApply(success -> {
                    if (success) {
                        // The success message can be cleared via flag
                        if (!successMessage.isEmpty()) {
                            MessagingUtil.sendStringToChat(this, successMessage, replacements);
                        }
                    } else {
                        printError(failMessage);
                    }
                    return success;
                });
    }

    @Override
    public String[] getGroups() {
        return plugin.getGroups(getPlayer());
    }

    @Override
    public void printRaw(String msg) {
        if (!silenced) {
            super.printRaw(msg);
        }
    }

    @Override
    public boolean hasPermission(String perm) {
        return plugin.hasPermission(getPlayer(), perm);
    }
}
