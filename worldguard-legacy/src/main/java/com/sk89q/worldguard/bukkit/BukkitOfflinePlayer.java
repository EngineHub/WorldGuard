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

import com.sk89q.worldedit.blocks.BaseItemStack;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.extent.inventory.BlockBag;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.session.SessionKey;
import com.sk89q.worldedit.util.HandSide;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.weather.WeatherType;
import com.sk89q.worldedit.world.weather.WeatherTypes;
import org.bukkit.OfflinePlayer;

import java.util.UUID;

import javax.annotation.Nullable;

class BukkitOfflinePlayer extends BukkitPlayer {

    private final OfflinePlayer player;

    BukkitOfflinePlayer(WorldGuardPlugin plugin, OfflinePlayer offlinePlayer) {
        super(plugin, offlinePlayer.getPlayer()); // null if they are offline
        this.player = offlinePlayer;
    }

    /// ========================================
    /// These are checked for RegionAssociable
    /// (to see if a player belongs to a region)
    /// ========================================

    @Override
    public String getName() {
        return player.getName();
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
    public String[] getGroups() {
        return plugin.getGroups(player);
    }

    /// ==========================================
    /// None of the following should ever be used.
    /// ==========================================

    @Override
    public boolean hasPermission(String perm) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void kick(String msg) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void ban(String msg) {
        throw new UnsupportedOperationException();
    }

    @Override
    public double getHealth() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setHealth(double health) {
        throw new UnsupportedOperationException();
    }

    @Override
    public double getMaxHealth() {
        throw new UnsupportedOperationException();
    }

    @Override
    public double getFoodLevel() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setFoodLevel(double foodLevel) {
        throw new UnsupportedOperationException();
    }

    @Override
    public double getSaturation() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setSaturation(double saturation) {
        throw new UnsupportedOperationException();
    }

    @Override
    public float getExhaustion() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setExhaustion(float exhaustion) {
        throw new UnsupportedOperationException();
    }

    @Override
    public WeatherType getPlayerWeather() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setPlayerWeather(WeatherType weather) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void resetPlayerWeather() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isPlayerTimeRelative() {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getPlayerTimeOffset() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setPlayerTime(long time, boolean relative) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void resetPlayerTime() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void printRaw(String msg) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void printDebug(String msg) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void print(String msg) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void printError(String msg) {
        throw new UnsupportedOperationException();
    }

    @Override
    public World getWorld() {
        throw new UnsupportedOperationException();
    }

    @Override
    public BaseItemStack getItemInHand(HandSide handSide) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void giveItem(BaseItemStack itemStack) {
        throw new UnsupportedOperationException();
    }

    @Override
    public BlockBag getInventoryBlockBag() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setPosition(Vector3 pos, float pitch, float yaw) {
        throw new UnsupportedOperationException();
    }

    @Nullable
    @Override
    public BaseEntity getState() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Location getLocation() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setCompassTarget(Location location) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SessionKey getSessionKey() {
        throw new UnsupportedOperationException();
    }

    @Nullable
    @Override
    public <T> T getFacet(Class<? extends T> cls) {
        throw new UnsupportedOperationException();
    }
}
