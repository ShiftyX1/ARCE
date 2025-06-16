package com.arce;

import com.arce.core.managers.MapManager;
import com.arce.logger.EngineLogger;
import com.arce.world.*;
import com.arce.math.Vector2D;

public class MapDemo {
    private static final EngineLogger logger = new EngineLogger(MapDemo.class);
    
    public static void main(String[] args) {
        logger.logInfo("=== ARCE Map System Demo ===");
        
        MapManager mapManager = new MapManager();
        
        demonstrateMapCreation(mapManager);
        demonstrateMapLoading(mapManager);
        demonstrateTestMaps(mapManager);
        demonstrateEditor();
        
        logger.logInfo("=== Demo Complete ===");
    }
    
    private static void demonstrateMapCreation(MapManager mapManager) {
        logger.logInfo("--- Map Creation Demo ---");
        
        GameMap newMap = mapManager.createNewMap("demo_map");
        
        Sector room = new Sector(1);
        room.setFloorHeight(0);
        room.setCeilingHeight(64);
        room.setLightLevel(255);
        
        Wall[] walls = {
            new Wall(new Vector2D(0, 0), new Vector2D(200, 0)),
            new Wall(new Vector2D(200, 0), new Vector2D(200, 200)),
            new Wall(new Vector2D(200, 200), new Vector2D(0, 200)),
            new Wall(new Vector2D(0, 200), new Vector2D(0, 0))
        };
        
        for (int i = 0; i < walls.length; i++) {
            walls[i].setTextureId(i + 1);
            room.addWall(walls[i]);
            newMap.addWall(walls[i]);
        }
        
        newMap.addSector(room);
        newMap.setPlayerStartPosition(new Vector2D(100, 100));
        newMap.setPlayerStartAngle(0);
        newMap.buildBSP();
        
        boolean saved = mapManager.saveCurrentMap("maps/demo_map.json", "Demo Map", "MapDemo");
        logger.logInfo("Map creation and save: " + (saved ? "SUCCESS" : "FAILED"));
        
        logger.logInfo("Map statistics: " + mapManager.getMapStatistics());
    }
    
    private static void demonstrateMapLoading(MapManager mapManager) {
        logger.logInfo("--- Map Loading Demo ---");
        
        boolean loaded = mapManager.loadMap("test_map");
        logger.logInfo("Map loading: " + (loaded ? "SUCCESS" : "FAILED"));
        
        if (loaded) {
            GameMap currentMap = mapManager.getCurrentMap();
            logger.logInfo("Loaded map details:");
            logger.logInfo("  - Sectors: " + currentMap.getSectors().size());
            logger.logInfo("  - Walls: " + currentMap.getWalls().size());
            logger.logInfo("  - Player start: " + currentMap.getPlayerStartPosition());
        }
    }
    
    private static void demonstrateTestMaps(MapManager mapManager) {
        logger.logInfo("--- Test Maps Demo ---");
        
        mapManager.loadTestMap();
        logger.logInfo("Simple test map: " + mapManager.getMapStatistics());
        
        mapManager.loadComplexTestMap();
        logger.logInfo("Complex test map: " + mapManager.getMapStatistics());
        
        String[] loadedMaps = mapManager.getLoadedMaps();
        logger.logInfo("Loaded maps: " + String.join(", ", loadedMaps));
    }
    
    private static void demonstrateEditor() {
        logger.logInfo("--- Level Editor Launch ---");
        logger.logInfo("To launch the editor, run:");
        logger.logInfo("  java -cp target/classes com.arce.editor.LevelEditor");
        logger.logInfo("");
        logger.logInfo("Editor features:");
        logger.logInfo("  - Create new maps");
        logger.logInfo("  - Draw walls and sectors");
        logger.logInfo("  - Set player position");
        logger.logInfo("  - Save and load maps in JSON format");
        logger.logInfo("  - BSP trees and portal system");
    }
    
    public static GameMap createExampleMap() {
        logger.logInfo("Creating example map programmatically");
        
        GameMap map = new GameMap();
        
        Sector mainRoom = new Sector(1);
        mainRoom.setFloorHeight(0);
        mainRoom.setCeilingHeight(96);
        mainRoom.setLightLevel(255);
        
        Sector upperRoom = new Sector(2);
        upperRoom.setFloorHeight(32);
        upperRoom.setCeilingHeight(128);
        upperRoom.setLightLevel(200);
        
        Wall[] mainWalls = {
            new Wall(new Vector2D(0, 0), new Vector2D(300, 0)),
            new Wall(new Vector2D(300, 0), new Vector2D(300, 200)),
            new Wall(new Vector2D(300, 200), new Vector2D(0, 200)),
            new Wall(new Vector2D(0, 200), new Vector2D(0, 0))
        };
        
        for (int i = 0; i < mainWalls.length; i++) {
            mainWalls[i].setTextureId(i + 1);
            mainRoom.addWall(mainWalls[i]);
            map.addWall(mainWalls[i]);
        }
        
        Wall[] upperWalls = {
            new Wall(new Vector2D(350, 50), new Vector2D(500, 50)),
            new Wall(new Vector2D(500, 50), new Vector2D(500, 150)),
            new Wall(new Vector2D(500, 150), new Vector2D(350, 150)),
            new Wall(new Vector2D(350, 150), new Vector2D(350, 50))
        };
        
        for (int i = 0; i < upperWalls.length; i++) {
            upperWalls[i].setTextureId(5);
            upperRoom.addWall(upperWalls[i]);
            map.addWall(upperWalls[i]);
        }
        
        Wall portal = new Wall(new Vector2D(300, 90), new Vector2D(350, 90));
        portal.setSolid(false);
        portal.setFrontSector(mainRoom);
        portal.setBackSector(upperRoom);
        portal.setTextureId(0);
        map.addWall(portal);
        
        map.addSector(mainRoom);
        map.addSector(upperRoom);
        
        map.setPlayerStartPosition(new Vector2D(150, 100));
        map.setPlayerStartAngle(0);
        
        map.buildBSP();
        
        logger.logSuccess("Example map created with portal system");
        return map;
    }
}