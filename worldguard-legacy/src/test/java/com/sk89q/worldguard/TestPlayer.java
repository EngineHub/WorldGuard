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

package com.sk89q.worldguard;

import com.sk89q.worldedit.blocks.BaseItemStack;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.extension.platform.AbstractPlayerActor;
import com.sk89q.worldedit.extent.inventory.BlockBag;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.session.SessionKey;
import com.sk89q.worldedit.util.HandSide;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.weather.WeatherType;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;

@org.junit.Ignore
public class TestPlayer extends AbstractPlayerActor implements LocalPlayer {

    private final UUID uuid = UUID.randomUUID();
    private final String name;
    private final Set<String> groups = new HashSet<>();
    
    public TestPlayer(String name) {
        this.name = name;
    }
    
    public void addGroup(String group) {
        groups.add(group.toLowerCase());
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public UUID getUniqueId() {
        return uuid;
    }

    @Override
    public boolean hasGroup(String group) {
        return groups.contains(group.toLowerCase());
    }

    @Override
    public void kick(String msg) {
        System.out.println("TestPlayer{" + this.name + "} kicked!");
    }

    @Override
    public void ban(String msg) {
        System.out.println("TestPlayer{" + this.name + "} banned!");
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
        return null;
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
        System.out.println("-> TestPlayer{" + this.name + "}: " + msg);
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
        return groups.toArray(new String[groups.size()]);
    }

    @Override
    public boolean hasPermission(String perm) {
        return true;
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
