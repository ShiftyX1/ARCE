package com.arce.world;

import com.arce.math.Vector2D;
import java.util.ArrayList;
import java.util.List;

public class Sector {
    private int id;
    private List<Wall> walls;
    private double floorHeight;
    private double ceilingHeight;
    private int floorTextureId;
    private int ceilingTextureId;
    private int lightLevel;      // 0-255
    
    public Sector(int id) {
        this.id = id;
        this.walls = new ArrayList<>();
        this.floorHeight = 0.0;
        this.ceilingHeight = 64.0;
        this.floorTextureId = 0;
        this.ceilingTextureId = 0;
        this.lightLevel = 255;
    }
    
    public void addWall(Wall wall) {
        walls.add(wall);
        if (wall.getFrontSector() == null) {
            wall.setFrontSector(this);
        }
    }
    
    public boolean containsPoint(Vector2D point) {
        int windingNumber = 0;
        
        for (Wall wall : walls) {
            Vector2D start = wall.getLine().start;
            Vector2D end = wall.getLine().end;
            
            if (start.y <= point.y) {
                if (end.y > point.y) {
                    if (isLeft(start, end, point) > 0) {
                        windingNumber++;
                    }
                }
            } else {
                if (end.y <= point.y) {
                    if (isLeft(start, end, point) < 0) {
                        windingNumber--;
                    }
                }
            }
        }
        
        return windingNumber != 0;
    }
    
    private double isLeft(Vector2D p0, Vector2D p1, Vector2D p2) {
        return ((p1.x - p0.x) * (p2.y - p0.y) - (p2.x - p0.x) * (p1.y - p0.y));
    }
    
    public double getWallHeight() {
        return ceilingHeight - floorHeight;
    }
    
    public int getId() { return id; }
    
    public List<Wall> getWalls() { return walls; }
    
    public double getFloorHeight() { return floorHeight; }
    public void setFloorHeight(double floorHeight) { this.floorHeight = floorHeight; }
    
    public double getCeilingHeight() { return ceilingHeight; }
    public void setCeilingHeight(double ceilingHeight) { this.ceilingHeight = ceilingHeight; }
    
    public int getFloorTextureId() { return floorTextureId; }
    public void setFloorTextureId(int floorTextureId) { this.floorTextureId = floorTextureId; }
    
    public int getCeilingTextureId() { return ceilingTextureId; }
    public void setCeilingTextureId(int ceilingTextureId) { this.ceilingTextureId = ceilingTextureId; }
    
    public int getLightLevel() { return lightLevel; }
    public void setLightLevel(int lightLevel) { 
        this.lightLevel = Math.max(0, Math.min(255, lightLevel)); 
    }
    
    @Override
    public String toString() {
        return String.format("Sector(id: %d, walls: %d, floor: %.1f, ceiling: %.1f)", 
                           id, walls.size(), floorHeight, ceilingHeight);
    }
}