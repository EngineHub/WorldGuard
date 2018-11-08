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

    BukkitOfflinePlayer(OfflinePlayer offlinePlayer) {
        super(null, null);
        this.player = offlinePlayer;
    }

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
        return false;
    }

    @Override
    public void kick(String msg) {
    }

    @Override
    public void ban(String msg) {
    }

    @Override
    public double getHealth() {
        return 0;
    }

    @Override
    public void setHealth(double health) {

    }

    @Override
    public double getMaxHealth() {
        return 0;
    }

    @Override
    public double getFoodLevel() {
        return 0;
    }

    @Override
    public void setFoodLevel(double foodLevel) {

    }

    @Override
    public double getSaturation() {
        return 0;
    }

    @Override
    public void setSaturation(double saturation) {

    }

    @Override
    public WeatherType getPlayerWeather() {
        return WeatherTypes.CLEAR;
    }

    @Override
    public void setPlayerWeather(WeatherType weather) {

    }

    @Override
    public void resetPlayerWeather() {

    }

    @Override
    public boolean isPlayerTimeRelative() {
        return false;
    }

    @Override
    public long getPlayerTimeOffset() {
        return 0;
    }

    @Override
    public void setPlayerTime(long time, boolean relative) {

    }

    @Override
    public void resetPlayerTime() {

    }

    @Override
    public void printRaw(String msg) {
    }

    @Override
    public void printDebug(String msg) {

    }

    @Override
    public void print(String msg) {

    }

    @Override
    public void printError(String msg) {

    }

    @Override
    public String[] getGroups() {
        return new String[0];
    }

    @Override
    public boolean hasPermission(String perm) {
        return false;
    }

    @Override
    public World getWorld() {
        return null;
    }

    @Override
    public BaseItemStack getItemInHand(HandSide handSide) {
        return null;
    }

    @Override
    public void giveItem(BaseItemStack itemStack) {

    }

    @Override
    public BlockBag getInventoryBlockBag() {
        return null;
    }

    @Override
    public void setPosition(Vector3 pos, float pitch, float yaw) {

    }

    @Nullable
    @Override
    public BaseEntity getState() {
        return null;
    }

    @Override
    public Location getLocation() {
        return null;
    }

    @Override
    public SessionKey getSessionKey() {
        return null;
    }

    @Nullable
    @Override
    public <T> T getFacet(Class<? extends T> cls) {
        return null;
    }
}
