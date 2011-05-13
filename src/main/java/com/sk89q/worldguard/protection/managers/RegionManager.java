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

import java.io.IOException;
import java.util.List;
import java.util.Map;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
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
     * @param loader
     */
    public RegionManager(ProtectionDatabase loader) {
        this.loader = loader;
    }

    /**
     * Load the list of regions.
     *
     * @throws IOException
     */
    public void load() throws IOException {
        loader.load(this);
    }

    /**
     * Save the list of regions.
     *
     * @throws IOException
     */
    public void save() throws IOException {
        loader.save(this);
    }

    /**
     * Get a list of protected regions.
     * 
     * @return
     */
    public abstract Map<String, ProtectedRegion> getRegions();

    /**
     * Set a list of protected regions.
     * 
     * @param regions
     */
    public abstract void setRegions(Map<String, ProtectedRegion> regions);

    /**
     * Adds a region.
     * 
     * @param region
     */
    public abstract void addRegion(ProtectedRegion region);

    /**
     * Return whether a region exists by an ID.
     * 
     * @param id
     * @return
     */
    public abstract boolean hasRegion(String id);

    /**
     * Get a region by its ID.
     * 
     * @param id
     * @return
     */
    public abstract ProtectedRegion getRegion(String id);

    /**
     * Removes a region, including inheriting children.
     * 
     * @param id
     */
    public abstract void removeRegion(String id);

    /**
     * Get an object for a point for rules to be applied with.
     * 
     * @param pt
     * @return
     */
    public abstract ApplicableRegionSet getApplicableRegions(Vector pt);

    /**
     * Get an object for a point for rules to be applied with.
     * 
     * @param region
     * @return
     */
    public abstract ApplicableRegionSet getApplicableRegions(
            ProtectedRegion region);

    /**
     * Get a list of region IDs that contain a point.
     * 
     * @param pt
     * @return
     */
    public abstract List<String> getApplicableRegionsIDs(Vector pt);

    /**
     * Returns true if the provided region overlaps with any other region that
     * is not owned by the player.
     * 
     * @param region
     * @param player
     * @return
     */
    public abstract boolean overlapsUnownedRegion(ProtectedRegion region,
            LocalPlayer player);

    /**
     * Get the number of regions.
     * 
     * @return
     */
    public abstract int size();

    /**
     * Get the number of regions for a player.
     * 
     * @param player
     * @return
     */
    public abstract int getRegionCountOfPlayer(LocalPlayer player);
}
