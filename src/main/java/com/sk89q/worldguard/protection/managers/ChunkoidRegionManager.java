package com.sk89q.worldguard.protection.managers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sk89q.worldedit.ChunkVector;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.UnsupportedIntersectionException;
import com.sk89q.worldguard.protection.databases.ProtectionDatabase;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedChunkoidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedChunkRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.GlobalProtectedRegion;;;

public class ChunkoidRegionManager extends RegionManager {

    /**
     * List of protected regions.
     */
    private Map<String, ProtectedRegion> regions;
    private Map<ChunkVector, Set<ProtectedRegion>> chunkToRegions;

    public ChunkoidRegionManager(ProtectionDatabase loader) throws IOException {
        super(loader);
        regions = new HashMap<String, ProtectedRegion>();
    }

    @Override
    public Map<String, ProtectedRegion> getRegions() {
        return regions;
    }

    @Override
    public void setRegions(Map<String, ProtectedRegion> regions) {
        this.regions = new HashMap<String, ProtectedRegion>(regions);
    }

    @Override
    public void addRegion(ProtectedRegion region) {
        if (region instanceof ProtectedChunkoidRegion) {
            regions.put(region.getId().toLowerCase(), region);
            for (ChunkVector cv : ((ProtectedChunkoidRegion) region).getChunkoids()) {
                Set<ProtectedRegion> regions = chunkToRegions.get(cv);
                if (regions == null) {
                    regions = chunkToRegions.put(cv, new HashSet<ProtectedRegion>());
                }
                regions.add(region);
            }
        } else if (region instanceof ProtectedChunkRegion) {
            regions.put(region.getId().toLowerCase(), region);
            for (ChunkVector cv : ((ProtectedChunkRegion) region).getPartialChunks()) {
                Set<ProtectedRegion> regions = chunkToRegions.get(cv);
                if (regions == null) {
                    regions = chunkToRegions.put(cv, new HashSet<ProtectedRegion>());
                }
                regions.add(region);
            }
        } else if (region instanceof GlobalProtectedRegion) {
            regions.put(region.getId().toLowerCase(), region);
        }
    }

    @Override
    public boolean hasRegion(String id) {
        return regions.containsKey(id.toLowerCase());
    }

    @Override
    public ProtectedRegion getRegion(String id) {
        return regions.get(id.toLowerCase());
    }

    @Override
    public void removeRegion(String id) {
        ProtectedChunkoidRegion pcr = (ProtectedChunkoidRegion) regions.remove(id.toLowerCase());

        if (pcr != null) {
            List<String> removeRegions = new ArrayList<String>();
            Iterator<ProtectedRegion> iter = regions.values().iterator();
            while (iter.hasNext()) {
                ProtectedRegion curRegion = iter.next();
                if (curRegion.getParent() == pcr) {
                    removeRegions.add(curRegion.getId().toLowerCase());
                }
            }

            for (ChunkVector cv : pcr.getChunkoids()) {
                Set<ProtectedRegion> regions = chunkToRegions.get(cv);
                if (regions == null) {
                    continue;
                }
                
                regions.remove(pcr);
                if (regions.isEmpty()) {
                    chunkToRegions.remove(cv);
                }
            }

            for (String remId : removeRegions) {
                removeRegion(remId);
            }
        }
    }

    @Override
    public ApplicableRegionSet getApplicableRegions(Vector pt) {
        Set<ProtectedRegion> appRegions = this.chunkToRegions.get(ChunkVector.fromVector(pt));
        return new ApplicableRegionSet(appRegions, regions.get("__global__"));
    }

    @Override
    public ApplicableRegionSet getApplicableRegions(ProtectedRegion region) {
        List<ProtectedRegion> appRegions = new ArrayList<ProtectedRegion>(regions.values());
        List<ProtectedRegion> intersecting;
        try {
           intersecting = region.getIntersectingRegions(appRegions);
        } catch (UnsupportedIntersectionException e) {
            intersecting = new ArrayList<ProtectedRegion>();
        }
        return new ApplicableRegionSet(intersecting, regions.get("__global__"));
    }

    @Override
    public List<String> getApplicableRegionsIDs(Vector pt) {
        List<String> ids = new ArrayList<String>();
        for (ProtectedRegion pr : chunkToRegions.get(ChunkVector.fromVector(pt))) {
            ids.add(pr.getId());
        }
        return ids;
    }

    @Override
    public boolean overlapsUnownedRegion(ProtectedRegion region, LocalPlayer player) {
        List<ProtectedRegion> appRegions = new ArrayList<ProtectedRegion>();

        for (ProtectedRegion other : regions.values()) {
            if (other.getOwners().contains(player)) {
                continue;
            }

            appRegions.add(other);
        }

        List<ProtectedRegion> intersectRegions;
        try {
            intersectRegions = region.getIntersectingRegions(appRegions);
        } catch (Exception e) {
            intersectRegions = new ArrayList<ProtectedRegion>();
        }

        return intersectRegions.size() > 0;
    }

    @Override
    public int size() {
        return regions.size();
    }

    @Override
    public int getRegionCountOfPlayer(LocalPlayer player) {
        int count = 0;

        for (Map.Entry<String, ProtectedRegion> entry : regions.entrySet()) {
            if (entry.getValue().getOwners().contains(player)) {
                count++;
            }
        }

        return count;
    }

}
