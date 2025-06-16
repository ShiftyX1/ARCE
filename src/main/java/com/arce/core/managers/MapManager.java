package com.arce.core.managers;

import com.arce.logger.EngineLogger;
import com.arce.world.GameMap;
import com.arce.world.MapLoader;
import com.arce.world.MapSaver;

import java.util.HashMap;
import java.util.Map;

public class MapManager {
    private final EngineLogger logger;
    private final MapLoader mapLoader;
    private final MapSaver mapSaver;
    
    private GameMap currentMap;
    private Map<String, GameMap> loadedMaps;
    private String currentMapName;
    
    public MapManager() {
        this.logger = new EngineLogger(MapManager.class);
        this.mapLoader = new MapLoader();
        this.mapSaver = new MapSaver();
        this.loadedMaps = new HashMap<>();
        
        logger.logInfo("MapManager initialized");
    }
    
    public boolean loadMap(String mapName) {
        logger.logStart("loadMap: " + mapName);
        
        if (loadedMaps.containsKey(mapName)) {
            currentMap = loadedMaps.get(mapName);
            currentMapName = mapName;
            logger.logInfo("Map loaded from cache: " + mapName);
            return true;
        }
        
        String mapPath = "maps/" + mapName;
        if (!mapName.endsWith(".json")) {
            mapPath += ".json";
        }
        
        GameMap map = mapLoader.loadMap(mapPath);
        if (map == null) {
            map = mapLoader.loadMapFromResource(mapPath);
        }
        
        if (map != null) {
            loadedMaps.put(mapName, map);
            currentMap = map;
            currentMapName = mapName;
            
            logger.logSuccess("Map loaded: " + mapName);
            logger.logEnd("loadMap");
            return true;
        } else {
            logger.logError("Failed to load map: " + mapName, new RuntimeException("Map not found"));
            logger.logEnd("loadMap");
            return false;
        }
    }
    
    public boolean loadMapFromFile(String filePath) {
        logger.logStart("loadMapFromFile: " + filePath);
        
        GameMap map = mapLoader.loadMap(filePath);
        if (map != null) {
            String mapName = extractMapNameFromPath(filePath);
            loadedMaps.put(mapName, map);
            currentMap = map;
            currentMapName = mapName;
            
            logger.logSuccess("Map loaded from file: " + filePath);
            logger.logEnd("loadMapFromFile");
            return true;
        } else {
            logger.logError("Failed to load map from file: " + filePath, new RuntimeException("Load failed"));
            logger.logEnd("loadMapFromFile");
            return false;
        }
    }
    
    public boolean saveCurrentMap(String filePath, String mapName, String author) {
        if (currentMap == null) {
            logger.logError("No current map to save", new RuntimeException("No current map"));
            return false;
        }
        
        return mapSaver.saveMap(currentMap, filePath, mapName, author);
    }
    
    public GameMap createNewMap(String mapName) {
        GameMap newMap = new GameMap();
        loadedMaps.put(mapName, newMap);
        currentMap = newMap;
        currentMapName = mapName;
        
        logger.logInfo("New map created: " + mapName);
        return newMap;
    }
    
    public boolean loadTestMap() {
        logger.logInfo("Loading test map");
        
        GameMap testMap = GameMap.createTestMap();
        loadedMaps.put("test_map", testMap);
        currentMap = testMap;
        currentMapName = "test_map";
        
        logger.logSuccess("Test map loaded");
        return true;
    }
    
    public boolean loadComplexTestMap() {
        logger.logInfo("Loading complex test map");
        
        GameMap complexMap = GameMap.createComplexTestMap();
        loadedMaps.put("complex_test_map", complexMap);
        currentMap = complexMap;
        currentMapName = "complex_test_map";
        
        logger.logSuccess("Complex test map loaded");
        return true;
    }
    
    public boolean switchToMap(String mapName) {
        if (loadedMaps.containsKey(mapName)) {
            currentMap = loadedMaps.get(mapName);
            currentMapName = mapName;
            logger.logInfo("Switched to map: " + mapName);
            return true;
        } else {
            logger.logError("Map not found in loaded maps: " + mapName, new RuntimeException("Map not loaded"));
            return false;
        }
    }
    
    public void unloadMap(String mapName) {
        if (loadedMaps.containsKey(mapName)) {
            loadedMaps.remove(mapName);
            
            if (mapName.equals(currentMapName)) {
                currentMap = null;
                currentMapName = null;
            }
            
            logger.logInfo("Map unloaded: " + mapName);
        }
    }
    
    public String[] getAvailableMaps() {
        return mapLoader.getAvailableMaps("maps/");
    }
    
    public String[] getLoadedMaps() {
        return loadedMaps.keySet().toArray(new String[0]);
    }
    
    private String extractMapNameFromPath(String filePath) {
        String fileName = filePath.substring(filePath.lastIndexOf("/") + 1);
        if (fileName.endsWith(".json")) {
            fileName = fileName.substring(0, fileName.length() - 5);
        }
        return fileName;
    }
    
    public GameMap getCurrentMap() { return currentMap; }
    public String getCurrentMapName() { return currentMapName; }
    public boolean hasCurrentMap() { return currentMap != null; }
    
    public String getMapStatistics() {
        if (currentMap == null) {
            return "No map loaded";
        }
        
        return String.format("Map: %s | Sectors: %d | Walls: %d", 
                           currentMapName != null ? currentMapName : "Unknown",
                           currentMap.getSectors().size(),
                           currentMap.getWalls().size());
    }
} 