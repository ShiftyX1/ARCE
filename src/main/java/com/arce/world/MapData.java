package com.arce.world;

import java.util.List;
import java.util.ArrayList;

/**
 * Структура данных для сериализации карт в JSON формат
 * Похоже на формат WAD файлов Quake, но в JSON
 */
public class MapData {
    public String name;
    public String author;
    public String description;
    public int version;
    public PlayerStart playerStart;
    public List<SectorData> sectors;
    public List<WallData> walls;
    
    public MapData() {
        this.sectors = new ArrayList<>();
        this.walls = new ArrayList<>();
        this.version = 1;
    }
    
    public static class PlayerStart {
        public double x;
        public double y;
        public double angle;
        
        public PlayerStart() {}
        
        public PlayerStart(double x, double y, double angle) {
            this.x = x;
            this.y = y;
            this.angle = angle;
        }
    }
    
    public static class SectorData {
        public int id;
        public double floorHeight;
        public double ceilingHeight;
        public int floorTextureId;
        public int ceilingTextureId;
        public int lightLevel;
        public List<Integer> wallIds;
        
        public SectorData() {
            this.wallIds = new ArrayList<>();
        }
        
        public SectorData(int id, double floorHeight, double ceilingHeight) {
            this();
            this.id = id;
            this.floorHeight = floorHeight;
            this.ceilingHeight = ceilingHeight;
            this.lightLevel = 255;
        }
    }
    
    public static class WallData {
        public int id;
        public PointData start;
        public PointData end;
        public int textureId;
        public boolean solid;
        public Integer frontSectorId;
        public Integer backSectorId;
        
        public WallData() {}
        
        public WallData(int id, double startX, double startY, double endX, double endY) {
            this.id = id;
            this.start = new PointData(startX, startY);
            this.end = new PointData(endX, endY);
            this.textureId = 1;
            this.solid = true;
        }
    }
    
    public static class PointData {
        public double x;
        public double y;
        
        public PointData() {}
        
        public PointData(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }
} 