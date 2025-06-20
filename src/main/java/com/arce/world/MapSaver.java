package com.arce.world;

import com.arce.logger.EngineLogger;
import com.arce.math.Vector2D;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class MapSaver {
    private final EngineLogger logger;
    private final Gson gson;
    
    public MapSaver() {
        this.logger = new EngineLogger(MapSaver.class);
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }
    
    public boolean saveMap(GameMap gameMap, String filePath, String mapName, String author) {
        logger.logStart("saveMap: " + filePath);
        
        try {
            MapData mapData = convertFromGameMap(gameMap, mapName, author);
            String jsonContent = gson.toJson(mapData);
            
            Path path = Paths.get(filePath);
            Files.createDirectories(path.getParent());
            Files.writeString(path, jsonContent);
            
            logger.logSuccess("Map saved: " + filePath);
            logger.logEnd("saveMap");
            return true;
            
        } catch (Exception e) {
            logger.logError("Failed to save map: " + filePath, e);
            return false;
        }
    }
    
    private MapData convertFromGameMap(GameMap gameMap, String mapName, String author) {
        MapData mapData = new MapData();
        mapData.name = mapName;
        mapData.author = author;
        mapData.description = "Generated by ARCE Level Editor";
        
        Vector2D playerPos = gameMap.getPlayerStartPosition();
        mapData.playerStart = new MapData.PlayerStart(
            playerPos.x, playerPos.y, gameMap.getPlayerStartAngle()
        );
        
        Map<Wall, Integer> wallToId = new HashMap<>();
        Map<Sector, Integer> sectorToId = new HashMap<>();
        
        int wallId = 1;
        for (Wall wall : gameMap.getWalls()) {
            wallToId.put(wall, wallId++);
        }
        
        for (Sector sector : gameMap.getSectors()) {
            sectorToId.put(sector, sector.getId());
        }
        
        for (Wall wall : gameMap.getWalls()) {
            MapData.WallData wallData = new MapData.WallData();
            wallData.id = wallToId.get(wall);
            wallData.start = new MapData.PointData(wall.getLine().start.x, wall.getLine().start.y);
            wallData.end = new MapData.PointData(wall.getLine().end.x, wall.getLine().end.y);
            wallData.textureId = wall.getTextureId();
            wallData.solid = wall.isSolid();
            
            if (wall.getFrontSector() != null) {
                wallData.frontSectorId = sectorToId.get(wall.getFrontSector());
            }
            if (wall.getBackSector() != null) {
                wallData.backSectorId = sectorToId.get(wall.getBackSector());
            }
            
            mapData.walls.add(wallData);
        }
        
        for (Sector sector : gameMap.getSectors()) {
            MapData.SectorData sectorData = new MapData.SectorData();
            sectorData.id = sector.getId();
            sectorData.floorHeight = sector.getFloorHeight();
            sectorData.ceilingHeight = sector.getCeilingHeight();
            sectorData.floorTextureId = sector.getFloorTextureId();
            sectorData.ceilingTextureId = sector.getCeilingTextureId();
            sectorData.lightLevel = sector.getLightLevel();
            
            for (Wall wall : sector.getWalls()) {
                Integer id = wallToId.get(wall);
                if (id != null) {
                    sectorData.wallIds.add(id);
                }
            }
            
            mapData.sectors.add(sectorData);
        }
        
        logger.logInfo("Converted GameMap to MapData: {} sectors, {} walls", 
                      mapData.sectors.size(), mapData.walls.size());
        
        return mapData;
    }
    
    public boolean ensureMapsDirectory(String mapsDirectory) {
        try {
            Path path = Paths.get(mapsDirectory);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
                logger.logInfo("Created maps directory: " + mapsDirectory);
            }
            return true;
        } catch (IOException e) {
            logger.logError("Failed to create maps directory: " + mapsDirectory, e);
            return false;
        }
    }
} 