package com.sk89q.worldguard.protection.regions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldedit.ChunkVector;
import com.sk89q.worldedit.ChunkVector2D;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.protection.UnsupportedIntersectionException;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class ProtectedChunkoidRegion extends ProtectedRegion {

    private Set<ChunkVector> partialChunks;
    private Set<ChunkVector2D> intersectingChunks;
    private static int BLOCKS_PER_PARTIAL_CHUNK = 4096;
    private int maxY;

    public ProtectedChunkoidRegion(String id, List<ChunkVector> partialChunks, int maxY) {
        super(id);
        this.maxY = maxY;
        this.partialChunks = new HashSet<ChunkVector>(partialChunks);
        this.intersectingChunks = new HashSet<ChunkVector2D>();
        for (ChunkVector cv : partialChunks) {
            intersectingChunks.add(ChunkVector2D.fromVector(cv));
        }
        setMinMaxPoints(new ArrayList<Vector>(partialChunks));
    }

    @Override
    public List<BlockVector2D> getPoints() {
        throw new UnsupportedOperationException("chunkoid regions do not support point lists");
    }

    public Set<ChunkVector> getChunkoids() {
        return partialChunks;
    }

    public void addPartialChunk(ChunkVector cv) {
        partialChunks.add(cv);
        intersectingChunks.add(cv.to2D());
    }

    public void addWholeChunk(ChunkVector2D cv) {
        for (int y = 0; y < maxY >> 4 ; y++) {
            partialChunks.add(new ChunkVector(cv.getBlockX(), y, cv.getBlockZ()));
        }
        intersectingChunks.add(cv);
    }

    public Set<ChunkVector2D> getIntersectingChunks() {
        return intersectingChunks;
    }

    public boolean containsAny(Set<ChunkVector> chunkoids) {
        for (ChunkVector bv : chunkoids) {
            if (partialChunks.contains(bv)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the list of intersecting chunks 
     * @return
     */
    public List<BlockVector2D> getChunks() {
        return new ArrayList<BlockVector2D>(intersectingChunks);
    }

    @Override
    public int volume() {
        return BLOCKS_PER_PARTIAL_CHUNK * partialChunks.size();
    }

    @Override
    public boolean contains(BlockVector2D pt) {
        return pt.containedWithin(min.toVector2D(), max.toVector2D());
    }

    @Override
    public boolean contains(int x, int y, int z) {
        return contains(ChunkVector.fromBlock(x, y, z));
    }

    @Override
    public boolean contains(Vector pt) {
        if (pt instanceof ChunkVector) {
            return partialChunks.contains(pt);
        }
        return partialChunks.contains(ChunkVector.fromVector(pt));
    }

    @Override
    public String getTypeName() {
        return "chunkoid";
    }

    @Override
    protected boolean intersectsEdges(ProtectedRegion region) {
        if (region instanceof ProtectedChunkoidRegion) {
            return containsAny(((ProtectedChunkoidRegion) region).getChunkoids());
        }
        return (containsAny(region.getPoints()));
    }

    @Override
    public List<ProtectedRegion> getIntersectingRegions(List<ProtectedRegion> regions) throws UnsupportedIntersectionException {
        List<ProtectedRegion> intersecting = new ArrayList<ProtectedRegion>();
        for (ProtectedRegion pr : regions) {
            if (!intersectsBoundingBox(pr)) {
                continue;
            }

            if (pr instanceof ProtectedChunkoidRegion && this.containsAny(((ProtectedChunkoidRegion) pr).getChunkoids())) {
                intersecting.add(pr);
            } else if (pr instanceof ProtectedChunkRegion) {

            } else if ((pr instanceof ProtectedCuboidRegion || pr instanceof ProtectedPolygonalRegion) && containsAny(pr.getPoints())) {
                intersecting.add(pr);
            } else {
                throw new UnsupportedIntersectionException();
            }
        }
        return intersecting;
    }
}
