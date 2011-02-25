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

package com.sk89q.worldguard.protection.dbs;

import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.regions.AreaFlags;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion.CircularInheritanceException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 *
 * @author Redecouverte
 */
public class JSONContainer {

    private static Logger logger = Logger.getLogger("Minecraft.WorldGuard");
    
    private HashMap<String,ProtectedCuboidRegion> cRegions;
    private HashMap<String,ProtectedPolygonalRegion> pRegions;

    public JSONContainer(Map<String,ProtectedRegion> regions)
    {

        this.cRegions = new HashMap<String,ProtectedCuboidRegion>();
        this.pRegions = new HashMap<String,ProtectedPolygonalRegion>();

        for (Map.Entry<String,ProtectedRegion> entry : regions.entrySet()) {
                String id = entry.getKey();
                ProtectedRegion region = entry.getValue();
                region.setParentId();

                if(region instanceof ProtectedCuboidRegion)
                {
                    cRegions.put(id, (ProtectedCuboidRegion)region);
                }
                else if(region instanceof ProtectedPolygonalRegion)
                {
                    pRegions.put(id, (ProtectedPolygonalRegion)region);
                }
                else
                {
                    logger.info("regions of type '" + region.getClass().toString() + "' are not supported for saving, yet.");
                }
        }

    }

    public Map<String, ProtectedRegion> getRegions() {
        HashMap<String, ProtectedRegion> ret = new HashMap<String, ProtectedRegion>();
        ret.putAll(this.cRegions);
        ret.putAll(this.pRegions);

        for (Map.Entry<String, ProtectedRegion> entry : ret.entrySet()) {
            String id = entry.getKey();
            ProtectedRegion region = entry.getValue();

            String parentId = region.getParentId();
            if (parentId != null) {
                try {
                    region.setParent(ret.get(parentId));
                } catch (CircularInheritanceException ex) {
                }
            } else {
                try {
                    region.setParent(null);
                } catch (CircularInheritanceException ex) {
                }
            }

            if(region.getOwners() == null)
            {
                region.setOwners(new DefaultDomain());
            }
            else if(region.getMembers() == null)
            {
                region.setMembers(new DefaultDomain());
            }
            else if(region.getFlags() == null)
            {
                region.setFlags(new AreaFlags());
            }

        }

        return ret;
    }
}
