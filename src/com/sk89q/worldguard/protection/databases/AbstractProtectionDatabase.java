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

package com.sk89q.worldguard.protection.databases;

import java.io.IOException;
import com.sk89q.worldguard.protection.managers.RegionManager;

public abstract class AbstractProtectionDatabase implements ProtectionDatabase {

    /**
     * Load the list of regions into a region manager.
     * 
     * @throws IOException
     */
    public void load(RegionManager manager) throws IOException {
        load();
        manager.setRegions(getRegions());
    }
    
    /**
     * Save the list of regions from a region manager.
     * 
     * @throws IOException
     */
    public void save(RegionManager manager) throws IOException {
        setRegions(manager.getRegions());
        save();
    }
    
}
