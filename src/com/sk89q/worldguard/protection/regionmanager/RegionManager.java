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

package com.sk89q.worldguard.protection.regionmanager;

import java.util.List;
import java.util.Map;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.GlobalFlags;
import com.sk89q.worldguard.protection.dbs.ProtectionDatabase;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * An abstract class for getting, setting, and looking up regions. The most
 * simple implementation uses a flat list and iterates through the entire
 * list to look for applicable regions, but a more complicated (and more
 * efficient) implementation may use space partitioning techniques.
 *
 * @author sk89q
 */
public abstract class RegionManager {

    private static final Logger logger = Logger.getLogger("Minecraft.WorldGuard");
    
    /**
     * Global flags.
     */
    protected GlobalFlags global;


    protected ProtectionDatabase regionloader;


    public RegionManager(GlobalFlags global, ProtectionDatabase regionloader)
    {
        this.global = global;
        this.regionloader = regionloader;
    }


    /**
     * Load the list of regions.
     *
     * @throws IOException
     */
    public void load() throws IOException
    {
        if(this.regionloader == null)
        {
            return;
        }
        
        try {
            this.regionloader.load();
            this.setRegions(this.regionloader.getRegions());

        } catch (FileNotFoundException e) {
        } catch (IOException e) {
            logger.warning("WorldGuard: Failed to load regions: "
                    + e.getMessage());
        }
    }


    /**
     * Save the list of regions.
     *
     * @throws IOException
     */
    public abstract void save() throws IOException;


    /**
     * Get a list of protected regions.
     *
     * @return
     */
    public abstract Map<String, ProtectedRegion> getRegions();
    
    /**
     * Set a list of protected regions.
     *
     * @return
     */
    public abstract void setRegions(Map<String,ProtectedRegion> regions);
    
    /**
     * Adds a region.
     * 
     * @param id
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
    public abstract boolean overlapsUnownedRegion(ProtectedRegion region, LocalPlayer player);
    
    /**
     * Get the number of regions.
     * 
     * @return
     */
    public abstract int size();

    /**
     * Sets the global flags.
     *
     * @return
     */
    public void setGlobalFlags(GlobalFlags globalflags)
    {
        global = globalflags;
    }
}
