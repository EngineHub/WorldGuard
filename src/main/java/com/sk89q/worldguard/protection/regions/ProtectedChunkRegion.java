package com.sk89q.worldguard.protection.regions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldedit.ChunkVector;
import com.sk89q.worldedit.ChunkVector2D;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.protection.UnsupportedIntersectionException;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class ProtectedChunkRegion extends ProtectedRegion {

    Set<ChunkVector2D> chunks;
    Set<ChunkVector> partialChunks;
    private static int BLOCKS_PER_CHUNK = 32768;
    private int maxY = 0;
    
    public ProtectedChunkRegion(String id, List<ChunkVector2D> chunks, int maxY) {
        super(id);
        this.chunks = new HashSet<ChunkVector2D>(chunks);
        this.maxY = maxY;
        this.partialChunks = new HashSet<ChunkVector>();
        for (ChunkVector2D cv : chunks) {
          for (int i = 0; i < this.maxY >> 4; i ++) {
              partialChunks.add(cv.toChunkVector(i));
          }
        }
        setMinMax(chunks);
    }

    public void addChunk(ChunkVector2D cv) {
        chunks.add(cv);
    }
    
    /**
     * Given a list of chunkvectors, sets the minimum and maximum points
     * @param chunks
     */
    private void setMinMax(List<ChunkVector2D> chunks) {
        int minX = chunks.get(0).getBlockX();
        int minY = 0;
        int minZ = chunks.get(0).getBlockZ();
        int maxX = minX + 15;
        int maxY = 127;
        int maxZ = minZ + 15;

        for (ChunkVector2D v : chunks) {
            int x = v.getBlockX() << 4;
            int z = v.getBlockZ() << 4;

            if (x < minX) minX = x;
            if (z < minZ) minZ = z;

            if (x + 15 > maxX) maxX = x + 15;
            if (z + 15 > maxZ) maxZ = z + 15;
        }
        
        min = new BlockVector(minX, minY, minZ);
        max = new BlockVector(maxX, maxY, maxZ);
    }

    public List<BlockVector2D> getChunks() {
        return new ArrayList<BlockVector2D>(chunks);
    }
    
    @Override
    public boolean contains(Vector pt) {
        if (pt instanceof ChunkVector)
            return chunks.contains(new ChunkVector2D(pt.getBlockX(), pt.getBlockZ()));
        
        return chunks.contains(ChunkVector2D.fromVector(pt));
    }
    
    @Override
    public boolean contains(BlockVector2D pt) {
        if (pt instanceof ChunkVector2D)
            return chunks.contains(pt);
        
        return chunks.contains(ChunkVector2D.fromVector2D(pt));
    }

    @Override
    public boolean contains(int x, int y, int z) {
        return chunks.contains(ChunkVector2D.fromBlock(x, y, z));
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
            } else if (pr instanceof ProtectedChunkRegion && this.containsAny(((ProtectedChunkRegion)pr).getChunks())) {
                intersecting.add(pr);
            } else if (pr instanceof ProtectedCuboidRegion) {
                intersecting.add(pr);
            } else if (pr instanceof ProtectedPolygonalRegion && containsAny(pr.getPoints())) {
                intersecting.add(pr);
            } else {
                throw new UnsupportedIntersectionException();
            }
        }
        return intersecting;
    }
    
    public boolean containsAny(Set<ChunkVector> chunkoids) {
        for (ChunkVector bv : chunkoids) {
            if (chunks.contains(bv.to2D())) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public List<BlockVector2D> getPoints() {
        throw new UnsupportedOperationException("chunkoid regions do not support point lists");
    }

    @Override
    public String getTypeName() {
        return "chunk";
    }

    @Override
    public int volume() {
        return chunks.size() * BLOCKS_PER_CHUNK;
    }

    public List<ChunkVector> getPartialChunks() {
        return new ArrayList<ChunkVector>(partialChunks);
    }
}
