package com.sk89q.worldguard.protection.regions;

import java.util.ArrayList;
import java.util.List;

import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.util.RegionUtil;
import com.sk89q.worldguard.protection.UnsupportedIntersectionException;

public class ProtectedCylinderRegion extends ProtectedRegion {

    private BlockVector2D center;
    private BlockVector2D radius;
    private Vector2D calculationRadius;
    private int minY;
    private int maxY;
    private List<BlockVector2D> points;

    public ProtectedCylinderRegion(String id, BlockVector2D center,
            BlockVector2D radius, int minY, int maxY) {
        super(id);
        this.center = center;
        this.radius = radius;
        this.calculationRadius = radius.add(0.5f, 0.5f);
        this.minY = minY;
        this.maxY = maxY;
        points = RegionUtil.polygonizeCylinder(center, radius, -1);
        setMinMaxPoints(calculatePoints());
    }

    private List<Vector> calculatePoints() {
        List<Vector> vectorList = new ArrayList<Vector>();
        int y = minY;
        for (BlockVector2D bvec : points) {
            vectorList.add(bvec.toVector().setY(y));
            y = maxY;
        }
        return vectorList;
    }

    @Override
    public int volume() {
        return (int) Math.floor(radius.getX() * radius.getZ() * Math.PI
                * (maxY - minY + 1));
    }

    @Override
    public boolean contains(Vector pt) {
        final int blockY = pt.getBlockY();
        if (blockY < minY || blockY > maxY) {
            return false;
        }
        
        return pt.toVector2D().subtract(center).divide(calculationRadius).lengthSq() <= 1;
    }

    @Override
    public String getTypeName() {
        return "cylinder";
    }

    public BlockVector2D getCenter() {
        return center;
    }

    public void setCenter(BlockVector2D center) {
        this.center = center;
    }

    public BlockVector2D getRadius() {
        return radius;
    }

    public void setRadius(BlockVector2D radius) {
        this.radius = radius;
    }

    public int getMinY() {
        return minY;
    }

    public void setMinY(int minY) {
        this.minY = minY;
    }

    public int getMaxY() {
        return maxY;
    }

    public void setMaxY(int maxY) {
        this.maxY = maxY;
    }

    public void setPoints(List<BlockVector2D> points) {
        this.points = points;
    }

    @Override
    public List<BlockVector2D> getPoints() {
        return points;
    }

    @Override
    public List<ProtectedRegion> getIntersectingRegions(List<ProtectedRegion> regions)
            throws UnsupportedIntersectionException {
        List<ProtectedRegion> intersectingRegions = new ArrayList<ProtectedRegion>();

        for (ProtectedRegion region : regions) {
            if (!intersectsBoundingBox(region))
                continue;

            if (region instanceof ProtectedPolygonalRegion
                    || region instanceof ProtectedCuboidRegion
                    || region instanceof ProtectedCylinderRegion) {
                // If either region contains the points of the other,
                // or if any edges intersect, the regions intersect
                if (containsAny(region.getPoints()) || region.containsAny(getPoints())
                        || intersectsEdges(region)) {
                    intersectingRegions.add(region);
                    continue;
                }
            } else {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        }
        return intersectingRegions;
    }

}
