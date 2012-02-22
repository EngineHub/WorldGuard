package com.sk89q.worldguard.protection.managers;

import com.sk89q.worldguard.protection.databases.ProtectionDatabase;

public abstract class SubRegionManager extends RegionManager {

    private final RegionManager parent;
    
    public SubRegionManager(ProtectionDatabase loader, RegionManager parent) {
        super(loader);
        if (parent instanceof SubRegionManager) {
            throw new UnsupportedOperationException("Unable to create nested subregionmanagers.");
        }
        this.parent = parent;
    }

    public RegionManager getParentManager() {
        return parent;
    }
}
