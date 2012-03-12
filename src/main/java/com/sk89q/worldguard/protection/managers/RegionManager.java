// $Id$
/*
 * WorldGuard
 * Copyright (C) 2010 sk89q <http://www.sk89q.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.worldguard.protection.managers;

import java.util.List;
import java.util.Map;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.databases.ProtectionDatabaseException;
import com.sk89q.worldguard.protection.databases.ProtectionDatabase;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

/**
 * An abstract class for getting, setting, and looking up regions. The most
 * simple implementation uses a flat list and iterates through the entire list
 * to look for applicable regions, but a more complicated (and more efficient)
 * implementation may use space partitioning techniques.
 *
 * @author sk89q
 */
public abstract class RegionManager {

    protected ProtectionDatabase loader;

    /**
     * Construct the object.
     *
     * @param loader The loader for this region
     */
    public RegionManager(ProtectionDatabase loader) {
        this.loader = loader;
    }

    /**
     * Load the list of regions. If the regions do not load properly, then
     * the existing list should be used (as stored previously).
     *
     * @throws ProtectionDatabaseException when an error occurs
     */
    public void load() throws ProtectionDatabaseException {
        loader.load(this);
    }

    /**
     * Save the list of regions.
     *
     * @throws ProtectionDatabaseException when an error occurs while saving
     */
    public void save() throws ProtectionDatabaseException {
        loader.save(this);
    }

    /**
     * Get a map of protected regions. Use one of the region manager methods
     * if possible if working with regions.
     *
     * @return map of regions, with keys being region IDs (lowercase)
     */
    public abstract Map<String, ProtectedRegion> getRegions();

    /**
     * Set a list of protected regions. Keys should be lowercase in the given
     * map fo regions.
     *
     * @param regions map of regions
     */
    public abstract void setRegions(Map<String, ProtectedRegion> regions);

    /**
     * Adds a region. If a region by the given name already exists, then
     * the existing region will be replaced.
     *
     * @param region region to add
     */
    public abstract void addRegion(ProtectedRegion region);

    /**
     * Return whether a region exists by an ID.
     *
     * @param id id of the region, can be mixed-case
     * @return whether the region exists
     */
    public abstract boolean hasRegion(String id);

    /**
     * Get a region by its ID. Includes symbolic names like #&lt;index&gt;
     *
     * @param id id of the region, can be mixed-case
     * @return region or null if it doesn't exist
     */
    public ProtectedRegion getRegion(String id) {
        if (id.startsWith("#")) {
            int index;
            try {
                index = Integer.parseInt(id.substring(1)) - 1;
            } catch (NumberFormatException e) {
                return null;
            }
            for (ProtectedRegion region : getRegions().values()) {
                if (index == 0) {
                    return region;
                }
                --index;
            }
            return null;
        }

        return getRegionExact(id);
    }

    /**
     * Get a region by its ID.
     *
     * @param id id of the region, can be mixed-case
     * @return region or null if it doesn't exist
     */
    public ProtectedRegion getRegionExact(String id) {
        return getRegions().get(id.toLowerCase());
    }

    /**
     * Removes a region, including inheriting children.
     *
     * @param id id of the region, can be mixed-case
     */
    public abstract void removeRegion(String id);

    /**
     * Get an object for a point for rules to be applied with. Use this in order
     * to query for flag data or membership data for a given point.
     *
     * @param loc Bukkit location
     * @return applicable region set
     */
    public ApplicableRegionSet getApplicableRegions(org.bukkit.Location loc) {
        return getApplicableRegions(com.sk89q.worldedit.bukkit.BukkitUtil.toVector(loc));
    }

    /**
     * Get an object for a point for rules to be applied with. Use this in order
     * to query for flag data or membership data for a given point.
     *
     * @param pt point
     * @return applicable region set
     */
    public abstract ApplicableRegionSet getApplicableRegions(Vector pt);

    /**
     * Get an object for a point for rules to be applied with. This gets
     * a set for the given reason.
     *
     * @param region region
     * @return regino set
     */
    public abstract ApplicableRegionSet getApplicableRegions(
            ProtectedRegion region);

    /**
     * Get a list of region IDs that contain a point.
     *
     * @param pt point
     * @return list of region Ids
     */
    public abstract List<String> getApplicableRegionsIDs(Vector pt);

    /**
     * Returns true if the provided region overlaps with any other region that
     * is not owned by the player.
     *
     * @param region region to check
     * @param player player to check against
     * @return whether there is an overlap
     */
    public abstract boolean overlapsUnownedRegion(ProtectedRegion region,
            LocalPlayer player);

    /**
     * Get the number of regions.
     *
     * @return number of regions
     */
    public abstract int size();

    /**
     * Get the number of regions for a player.
     *
     * @param player player
     * @return name number of regions that a player owns
     */
    public abstract int getRegionCountOfPlayer(LocalPlayer player);
}
