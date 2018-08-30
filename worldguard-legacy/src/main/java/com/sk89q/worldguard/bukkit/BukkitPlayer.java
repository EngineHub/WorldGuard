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

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseItemStack;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.extent.inventory.BlockBag;
import com.sk89q.worldedit.session.SessionKey;
import com.sk89q.worldedit.util.HandSide;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.weather.WeatherType;
import com.sk89q.worldedit.world.weather.WeatherTypes;
import com.sk89q.worldguard.LocalPlayer;
import org.bukkit.BanList.Type;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.annotation.Nullable;

public class BukkitPlayer extends LocalPlayer {

    private final WorldGuardPlugin plugin;
    private final Player player;
    private final com.sk89q.worldedit.bukkit.BukkitPlayer worldEditPlayer;
    private final String name;
    private final boolean silenced;

    public BukkitPlayer(WorldGuardPlugin plugin, Player player) {
        this(plugin, player, false);
    }

    BukkitPlayer(WorldGuardPlugin plugin, Player player, boolean silenced) {
        checkNotNull(plugin);
        checkNotNull(player);

        this.plugin = plugin;
        this.player = player;
        // getName() takes longer than before in newer versions of Minecraft
        this.name = player.getName();
        this.silenced = silenced;
        this.worldEditPlayer = new com.sk89q.worldedit.bukkit.BukkitPlayer((WorldEditPlugin) Bukkit.getPluginManager().getPlugin("WorldEdit"), player);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public UUID getUniqueId() {
        return player.getUniqueId();
    }

    @Override
    public boolean hasGroup(String group) {
        return plugin.inGroup(player, group);
    }

    @Override
    public void kick(String msg) {
        if (!silenced) {
            player.kickPlayer(msg);
        }
    }

    @Override
    public void ban(String msg) {
        if (!silenced) {
            Bukkit.getBanList(Type.NAME).addBan(player.getName(), null, null, null);
            player.kickPlayer(msg);
        }
    }

    @Override
    public double getHealth() {
        return player.getHealth();
    }

    @Override
    public void setHealth(double health) {
        player.setHealth(health);
    }

    @Override
    public double getMaxHealth() {
        return player.getMaxHealth();
    }

    @Override
    public double getFoodLevel() {
        return player.getFoodLevel();
    }

    @Override
    public void setFoodLevel(double foodLevel) {
        player.setFoodLevel((int) foodLevel);
    }

    @Override
    public double getSaturation() {
        return player.getSaturation();
    }

    @Override
    public void setSaturation(double saturation) {
        player.setSaturation((float) saturation);
    }

    @Override
    public WeatherType getPlayerWeather() {
        return null;
    }

    @Override
    public void setPlayerWeather(WeatherType weather) {
        player.setPlayerWeather(weather == WeatherTypes.CLEAR ? org.bukkit.WeatherType.CLEAR : org.bukkit.WeatherType.DOWNFALL);
    }

    @Override
    public void resetPlayerWeather() {
        player.resetPlayerWeather();
    }

    @Override
    public boolean isPlayerTimeRelative() {
        return player.isPlayerTimeRelative();
    }

    @Override
    public long getPlayerTimeOffset() {
        return player.getPlayerTimeOffset();
    }

    @Override
    public void setPlayerTime(long time, boolean relative) {
        player.setPlayerTime(time, relative);
    }

    @Override
    public void resetPlayerTime() {
        player.resetPlayerTime();
    }

    @Override
    public String[] getGroups() {
        return plugin.getGroups(player);
    }

    @Override
    public void printRaw(String msg) {
        if (!silenced) {
            player.sendMessage(msg);
        }
    }

    @Override
    public void printDebug(String msg) {
        worldEditPlayer.printDebug(msg);
    }

    @Override
    public void print(String msg) {
        worldEditPlayer.print(msg);
    }

    @Override
    public void printError(String msg) {
        worldEditPlayer.printError(msg);
    }

    @Override
    public boolean hasPermission(String perm) {
        return plugin.hasPermission(player, perm);
    }

    public Player getPlayer() {
        return player;
    }

    @Override
    public World getWorld() {
        return BukkitAdapter.adapt(player.getWorld());
    }

    @Override
    public BaseItemStack getItemInHand(HandSide handSide) {
        return worldEditPlayer.getItemInHand(handSide);
    }

    @Override
    public void giveItem(BaseItemStack itemStack) {
        worldEditPlayer.giveItem(itemStack);
    }

    @Override
    public BlockBag getInventoryBlockBag() {
        return worldEditPlayer.getInventoryBlockBag();
    }

    @Override
    public void setPosition(Vector pos, float pitch, float yaw) {
        worldEditPlayer.setPosition(pos, pitch, yaw);
    }

    @Nullable
    @Override
    public BaseEntity getState() {
        return worldEditPlayer.getState();
    }

    @Override
    public com.sk89q.worldedit.util.Location getLocation() {
        Location loc = player.getLocation();
        return BukkitAdapter.adapt(loc);
    }

    @Override
    public SessionKey getSessionKey() {
        return worldEditPlayer.getSessionKey();
    }

    @Nullable
    @Override
    public <T> T getFacet(Class<? extends T> cls) {
        return worldEditPlayer.getFacet(cls);
    }
}
