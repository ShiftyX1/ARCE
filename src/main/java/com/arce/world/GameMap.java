package com.arce.world;

import com.arce.math.Vector2D;
import com.arce.math.Ray2D;
import com.arce.logger.EngineLogger;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class GameMap {
    private final EngineLogger logger;
    private List<Sector> sectors;
    private List<Wall> walls;
    private BSPNode bspRoot;
    private Map<Integer, Sector> sectorMap;
    private Vector2D playerStartPosition;
    private double playerStartAngle;
    
    public GameMap() {
        this.logger = new EngineLogger(GameMap.class);
        this.sectors = new ArrayList<>();
        this.walls = new ArrayList<>();
        this.sectorMap = new HashMap<>();
        this.playerStartPosition = new Vector2D(0, 0);
        this.playerStartAngle = 0;
    }
    
    public void addSector(Sector sector) {
        sectors.add(sector);
        sectorMap.put(sector.getId(), sector);
        logger.logDebug("Added sector: " + sector);
    }
    
    public void addWall(Wall wall) {
        walls.add(wall);
        logger.logDebug("Added wall: " + wall);
    }
    
    public void buildBSP() {
        logger.logStart("buildBSP");
        
        if (walls.isEmpty()) {
            logger.logError("Cannot build BSP: no walls in map", new RuntimeException("No walls"));
            return;
        }
        
        int maxWallsPerNode = 8;
        bspRoot = BSPNode.buildBSP(walls, maxWallsPerNode);
        
        logger.logSuccess("BSP tree built with " + walls.size() + " walls");
        logger.logEnd("buildBSP");
    }
    
    public Sector findSector(Vector2D position) {
        for (Sector sector : sectors) {
            if (sector.containsPoint(position)) {
                return sector;
            }
        }
        return null;
    }
    
    public RaycastResult raycast(Ray2D ray, double maxDistance) {
        if (bspRoot == null) {
            logger.logError("BSP tree not built, cannot raycast", new RuntimeException("BSP not built"));
            return null;
        }
        
        Wall hitWall = bspRoot.raycast(ray, maxDistance);
        if (hitWall != null) {
            var intersection = hitWall.getLine().intersectRay(ray);
            if (intersection != null) {
                return new RaycastResult(intersection.point, intersection.distance, hitWall, ray);
            }
        }
        
        return null;
    }
    
    public void traverseForRendering(Vector2D viewPoint, BSPNode.BSPTraversalCallback callback) {
        if (bspRoot != null) {
            bspRoot.traverse(viewPoint, callback);
        }
    }
    
    public static GameMap createTestMap() {
        GameMap map = new GameMap();
        
        Sector mainRoom = new Sector(1);
        mainRoom.setFloorHeight(0);
        mainRoom.setCeilingHeight(64);
        mainRoom.setLightLevel(200);
        
        Wall[] mainWalls = {
            new Wall(new Vector2D(50, 50), new Vector2D(350, 50)),     // Нижняя
            new Wall(new Vector2D(350, 50), new Vector2D(350, 250)),   // Правая
            new Wall(new Vector2D(350, 250), new Vector2D(50, 250)),   // Верхняя
            new Wall(new Vector2D(50, 250), new Vector2D(50, 50))      // Левая
        };
        
        mainWalls[0].setTextureId(1); // Красная
        mainWalls[1].setTextureId(2); // Синяя
        mainWalls[2].setTextureId(3); // Зеленая
        mainWalls[3].setTextureId(4); // Желтая
        
        Wall pillar1 = new Wall(new Vector2D(150, 100), new Vector2D(170, 100));
        pillar1.setTextureId(5); // Пурпурная
        
        Wall pillar2 = new Wall(new Vector2D(170, 100), new Vector2D(170, 120));
        pillar2.setTextureId(5);
        
        Wall pillar3 = new Wall(new Vector2D(170, 120), new Vector2D(150, 120));
        pillar3.setTextureId(5);
        
        Wall pillar4 = new Wall(new Vector2D(150, 120), new Vector2D(150, 100));
        pillar4.setTextureId(5);
        
        for (Wall wall : mainWalls) {
            mainRoom.addWall(wall);
            map.addWall(wall);
        }
        
        map.addWall(pillar1);
        map.addWall(pillar2);
        map.addWall(pillar3);
        map.addWall(pillar4);
        
        map.addSector(mainRoom);
        
        map.setPlayerStartPosition(new Vector2D(200, 150));
        map.setPlayerStartAngle(0);
        
        map.buildBSP();
        
        return map;
    }
    
    
    public static GameMap createComplexTestMap() {
        GameMap map = new GameMap();
        
        Sector room1 = new Sector(1);
        room1.setFloorHeight(0);
        room1.setCeilingHeight(64);
        
        // Комната 2 (выше)
        Sector room2 = new Sector(2);
        room2.setFloorHeight(32);
        room2.setCeilingHeight(96);
        
        // Стены для комнаты 1
        Wall[] room1Walls = {
            new Wall(new Vector2D(0, 0), new Vector2D(150, 0)),
            new Wall(new Vector2D(150, 0), new Vector2D(150, 100)),
            new Wall(new Vector2D(150, 100), new Vector2D(0, 100)),
            new Wall(new Vector2D(0, 100), new Vector2D(0, 0))
        };
        
        // Стены для комнаты 2
        Wall[] room2Walls = {
            new Wall(new Vector2D(200, 0), new Vector2D(350, 0)),
            new Wall(new Vector2D(350, 0), new Vector2D(350, 100)),
            new Wall(new Vector2D(350, 100), new Vector2D(200, 100)),
            new Wall(new Vector2D(200, 100), new Vector2D(200, 0))
        };
        
        // Соединяющий коридор (портал)
        Wall portal = new Wall(new Vector2D(150, 40), new Vector2D(200, 40));
        portal.setSolid(false);
        portal.setFrontSector(room1);
        portal.setBackSector(room2);
        
        // Добавляем все к карте
        for (Wall wall : room1Walls) {
            room1.addWall(wall);
            map.addWall(wall);
        }
        
        for (Wall wall : room2Walls) {
            room2.addWall(wall);
            map.addWall(wall);
        }
        
        map.addWall(portal);
        map.addSector(room1);
        map.addSector(room2);
        
        map.setPlayerStartPosition(new Vector2D(75, 50));
        map.setPlayerStartAngle(0);
        
        map.buildBSP();
        return map;
    }
    
    public List<Sector> getSectors() { return sectors; }
    public List<Wall> getWalls() { return walls; }
    public BSPNode getBspRoot() { return bspRoot; }
    
    public Vector2D getPlayerStartPosition() { return playerStartPosition; }
    public void setPlayerStartPosition(Vector2D position) { 
        this.playerStartPosition = new Vector2D(position); 
    }
    
    public double getPlayerStartAngle() { return playerStartAngle; }
    public void setPlayerStartAngle(double angle) { this.playerStartAngle = angle; }
    
    public Sector getSector(int id) { return sectorMap.get(id); }
    
    public static class RaycastResult {
        public Vector2D hitPoint;
        public double distance;
        public Wall hitWall;
        public Ray2D ray;
        
        public RaycastResult(Vector2D hitPoint, double distance, Wall hitWall, Ray2D ray) {
            this.hitPoint = hitPoint;
            this.distance = distance;
            this.hitWall = hitWall;
            this.ray = ray;
        }
        
        @Override
        public String toString() {
            return String.format("RaycastResult(distance: %.2f, wall: %s)", distance, hitWall);
        }
    }
}