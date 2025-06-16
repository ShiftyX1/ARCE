package com.arce.world;

import com.arce.logger.EngineLogger;
import com.arce.math.Vector2D;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class MapLoader {
    private final EngineLogger logger;
    private final Gson gson;
    
    public MapLoader() {
        this.logger = new EngineLogger(MapLoader.class);
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }
    
    public GameMap loadMap(String filePath) {
        logger.logStart("loadMap: " + filePath);
        
        try {
            Path path = Paths.get(filePath);
            if (!Files.exists(path)) {
                logger.logError("Map file not found: " + filePath, new FileNotFoundException());
                return null;
            }
            
            String jsonContent = Files.readString(path);
            MapData mapData = gson.fromJson(jsonContent, MapData.class);
            
            GameMap gameMap = convertToGameMap(mapData);
            
            logger.logSuccess("Map loaded: " + mapData.name);
            logger.logEnd("loadMap");
            return gameMap;
            
        } catch (Exception e) {
            logger.logError("Failed to load map: " + filePath, e);
            return null;
        }
    }
    
    public GameMap loadMapFromResource(String resourcePath) {
        logger.logStart("loadMapFromResource: " + resourcePath);
        
        try {
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourcePath);
            if (inputStream == null) {
                logger.logError("Map resource not found: " + resourcePath, new FileNotFoundException());
                return null;
            }
            
            String jsonContent = new String(inputStream.readAllBytes());
            MapData mapData = gson.fromJson(jsonContent, MapData.class);
            
            GameMap gameMap = convertToGameMap(mapData);
            
            logger.logSuccess("Map loaded from resource: " + mapData.name);
            logger.logEnd("loadMapFromResource");
            return gameMap;
            
        } catch (Exception e) {
            logger.logError("Failed to load map from resource: " + resourcePath, e);
            return null;
        }
    }
    
    private GameMap convertToGameMap(MapData mapData) {
        GameMap gameMap = new GameMap();
        
        Map<Integer, Wall> wallMap = new HashMap<>();
        
        for (MapData.WallData wallData : mapData.walls) {
            Vector2D start = new Vector2D(wallData.start.x, wallData.start.y);
            Vector2D end = new Vector2D(wallData.end.x, wallData.end.y);
            
            Wall wall = new Wall(start, end);
            wall.setTextureId(wallData.textureId);
            wall.setSolid(wallData.solid);
            
            wallMap.put(wallData.id, wall);
            gameMap.addWall(wall);
        }
        
        Map<Integer, Sector> sectorMap = new HashMap<>();
        for (MapData.SectorData sectorData : mapData.sectors) {
            Sector sector = new Sector(sectorData.id);
            sector.setFloorHeight(sectorData.floorHeight);
            sector.setCeilingHeight(sectorData.ceilingHeight);
            sector.setFloorTextureId(sectorData.floorTextureId);
            sector.setCeilingTextureId(sectorData.ceilingTextureId);
            sector.setLightLevel(sectorData.lightLevel);
            
            for (Integer wallId : sectorData.wallIds) {
                Wall wall = wallMap.get(wallId);
                if (wall != null) {
                    sector.addWall(wall);
                }
            }
            
            sectorMap.put(sectorData.id, sector);
            gameMap.addSector(sector);
        }
        
        for (MapData.WallData wallData : mapData.walls) {
            Wall wall = wallMap.get(wallData.id);
            if (wall != null) {
                if (wallData.frontSectorId != null) {
                    wall.setFrontSector(sectorMap.get(wallData.frontSectorId));
                }
                if (wallData.backSectorId != null) {
                    wall.setBackSector(sectorMap.get(wallData.backSectorId));
                }
            }
        }
        
        if (mapData.playerStart != null) {
            gameMap.setPlayerStartPosition(new Vector2D(mapData.playerStart.x, mapData.playerStart.y));
            gameMap.setPlayerStartAngle(mapData.playerStart.angle);
        }
        
        gameMap.buildBSP();
        
        logger.logInfo("Converted map: {} sectors, {} walls", 
                      mapData.sectors.size(), mapData.walls.size());
        
        return gameMap;
    }
    
    public String[] getAvailableMaps(String mapsDirectory) {
        try {
            Path mapsPath = Paths.get(mapsDirectory);
            if (!Files.exists(mapsPath)) {
                return new String[0];
            }
            
            return Files.list(mapsPath)
                    .filter(path -> path.toString().endsWith(".json"))
                    .map(path -> path.getFileName().toString())
                    .toArray(String[]::new);
                    
        } catch (IOException e) {
            logger.logError("Failed to list maps in directory: " + mapsDirectory, e);
            return new String[0];
        }
    }
} 