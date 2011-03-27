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

package com.sk89q.worldguard.protection;

import com.sk89q.worldguard.protection.databases.CSVDatabase;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import java.io.*;
import java.util.Map;
import java.util.HashMap;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldguard.domains.DefaultDomain;


public class CSVDatabaseTest {
    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testLoadSave() throws Exception {
        File temp = File.createTempFile("worldguard_csv_test", ".tmp"); 
        temp.deleteOnExit();
        
        Map<String,ProtectedRegion> regions =
                new HashMap<String,ProtectedRegion>();
        regions.put("test1", getTestRegion1());
        regions.put("test2", getTestRegion2());
        
        CSVDatabase writeDB = new CSVDatabase(temp);
        writeDB.setRegions(regions);
        writeDB.save();
        
        CSVDatabase readDB = new CSVDatabase(temp);
        readDB.load();

        Map<String,ProtectedRegion> loaded = readDB.getRegions();
        
        ProtectedRegion region1 = loaded.get("test1");
        checkTestRegion1(region1);
    }
    
    private void checkTestRegion1(ProtectedRegion region) {
      /*  AreaFlags flags = new AreaFlags();
        flags.setFlag(AreaFlags.FLAG_FIRE_SPREAD, State.ALLOW);
        flags.setFlag(AreaFlags.FLAG_PVP, State.DENY);
        flags.setFlag(AreaFlags.FLAG_LIGHTER, State.DENY);
        region.setFlags(flags);
        
        assertEquals(region.getFlags(), flags);
        */
    }
    
    private ProtectedRegion getTestRegion1() {
        BlockVector min = new BlockVector(1, 2, 3);
        BlockVector max = new BlockVector(4, 5, 6);
        
        ProtectedRegion region = new ProtectedCuboidRegion("test2", min, max);
        
     /*   AreaFlags flags = new AreaFlags();
        flags.setFlag(AreaFlags.FLAG_FIRE_SPREAD, State.ALLOW);
        flags.setFlag(AreaFlags.FLAG_PVP, State.DENY);
        flags.setFlag(AreaFlags.FLAG_LIGHTER, State.DENY);
        region.setFlags(flags);
       */
        DefaultDomain domain = new DefaultDomain();
        domain.addGroup("members");
        domain.addGroup("sturmehs");
        domain.addPlayer("hollie");
        domain.addPlayer("chad");
        domain.addPlayer("tetsu");
        region.setOwners(domain);
        
        region.setPriority(444);
        
        return region;
    }
    
    private ProtectedRegion getTestRegion2() {
        BlockVector min = new BlockVector(7, 8, 9);
        BlockVector max = new BlockVector(10, 11, 12);
        
        ProtectedRegion region = new ProtectedCuboidRegion("test2", min, max);
        /*
        AreaFlags flags = new AreaFlags();
        flags.setFlag(AreaFlags.FLAG_FIRE_SPREAD, State.ALLOW);
        flags.setFlag(AreaFlags.FLAG_PVP, State.ALLOW);
        flags.setFlag(AreaFlags.FLAG_LIGHTER, State.DENY);
        region.setFlags(flags);
        */
        
        DefaultDomain domain = new DefaultDomain();
        domain.addGroup("admins");
        domain.addPlayer("jon");
        domain.addPlayer("ester");
        domain.addPlayer("amy");
        region.setOwners(domain);

        region.setPriority(555);
        
        return region;
    }
}
