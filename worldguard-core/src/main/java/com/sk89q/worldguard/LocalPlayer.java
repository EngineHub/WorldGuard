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

import com.google.common.annotations.Beta;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.weather.WeatherType;
import com.sk89q.worldguard.domains.Association;
import com.sk89q.worldguard.protection.association.RegionAssociable;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import java.util.List;

public interface LocalPlayer extends Player, RegionAssociable {

    /**
     * Returns the default namespace for this player.
     *
     * @return the default namespace.
     */
    String getDefaultNamespace();

    default boolean isDefaultNamespace(String otherNamespace) {
        String namespace = getDefaultNamespace();
        // If both are null, they're the same (the global)
        if (namespace == null && otherNamespace == null) {
            return true;
        }

        // If only one is null, they're a mismatch
        if (namespace == null || otherNamespace == null) {
            return false;
        }

        // Compare string equality
        return namespace.equalsIgnoreCase(otherNamespace);
    }

    /**
     * Returns true if this player is inside a group.
     * 
     * @param group The group to check
     * @return Whether this player is in {@code group}
     */
    boolean hasGroup(String group);

    /**
     * Kick this player.
     * 
     * @param msg The message to kick the player with
     */
    void kick(String msg);
    
    /**
     * Ban this player.
     * 
     * @param msg The message to ban the player with
     */
    void ban(String msg);

    @Override
    default Association getAssociation(List<ProtectedRegion> regions) {
        boolean member = false;

        for (ProtectedRegion region : regions) {
            if (region.isOwner(this)) {
                return Association.OWNER;
            } else if (!member && region.isMember(this)) {
                member = true;
            }
        }

        return member ? Association.MEMBER : Association.NON_MEMBER;
    }

    /**
     * Gets the health of this player.
     *
     * @return The health
     */
    double getHealth();

    /**
     * Sets the health of this player.
     *
     * @param health The health
     */
    void setHealth(double health);

    /**
     * Gets the max health of this player.
     *
     * @return The max health
     */
    double getMaxHealth();

    /**
     * Gets the food level of this player.
     *
     * @return The food level
     */
    double getFoodLevel();

    /**
     * Sets the food level of this player.
     *
     * @param foodLevel The food level
     */
    void setFoodLevel(double foodLevel);

    /**
     * Gets the saturation of this player.
     *
     * @return The saturation
     */
    double getSaturation();

    /**
     * Sets the saturation of this player.
     *
     * @param saturation The saturation
     */
    void setSaturation(double saturation);

    /**
     * Gets the exhaustion of this player.
     *
     * @return The exhaustion
     */
    float getExhaustion();

    /**
     * Sets the exhaustion of this player.
     *
     * @param exhaustion The exhaustion
     */
    void setExhaustion(float exhaustion);

    /**
     * Gets the players weather
     *
     * @return The players weather
     */
    WeatherType getPlayerWeather();

    /**
     * Sets the players WeatherType
     *
     * @param weather The weather type
     */
    void setPlayerWeather(WeatherType weather);

    /**
     * Resets the players weather to normal.
     */
    void resetPlayerWeather();

    /**
     * Gets if the players time is relative.
     *
     * @return If the time is relative
     */
    boolean isPlayerTimeRelative();

    /**
     * Gets the time offset of the player.
     *
     * @return The players time offset
     */
    long getPlayerTimeOffset();

    /**
     * Sets the players time.
     *
     * @param time The players time
     * @param relative If it's relative
     */
    void setPlayerTime(long time, boolean relative);

    /**
     * Resets the players time to normal.
     */
    void resetPlayerTime();

    // TODO Move this to WorldEdit's Entity class - honestly most of this class could be a Facet
    /**
     * Gets the number of ticks the player is on fire for.
     *
     * @return The number of fire ticks
     */
    int getFireTicks();

    /**
     * Sets the number of ticks the player is on fire for.
     *
     * @param fireTicks The fire ticks
     */
    void setFireTicks(int fireTicks);

    /**
     * Sets the target of the compass
     *
     * @param location The location
     */
    void setCompassTarget(Location location);

    /**
     * This should preferably take Components but there's no way to do that yet
     *
     * @param title the title to display
     * @param subtitle the subtitle to display
     */
    @Beta
    void sendTitle(String title, String subtitle);

    /**
     * Clears fall distance.
     */
    void resetFallDistance();

    /**
     * Teleport the player, potentially async, displaying the message on a success.
     *  @param location location to teleport to
     * @param successMessage message to display on success
     * @param failMessage message to display on failure
     */
    void teleport(Location location, String successMessage, String failMessage);
}
