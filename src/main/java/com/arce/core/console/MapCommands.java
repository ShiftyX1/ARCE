package com.arce.core.console;

import com.arce.core.managers.MapManager;
import com.arce.logger.EngineLogger;

public class MapCommands {
    
    public static class LoadMapCommand implements Command {
        private final MapManager mapManager;
        private final EngineLogger logger;
        
        public LoadMapCommand(MapManager mapManager) {
            this.mapManager = mapManager;
            this.logger = new EngineLogger(LoadMapCommand.class);
        }
        
        @Override
        public String execute(String[] args) {
            if (args.length == 0) {
                return "Usage instructions: " + getUsage();
            }
            
            String mapName = args[0];
            
            boolean success = mapManager.loadMap(mapName);
            
            if (success) {
                logger.logInfo("Map loaded via console: " + mapName);
                return "Map loaded: " + mapName + "\n" + mapManager.getMapStatistics();
            } else {
                return "Error loading map: " + mapName + "\nCheck file name and presence in maps/ directory";
            }
        }
        
        @Override
        public String getDescription() {
            return "Loads map by file name";
        }
        
        @Override
        public String getUsage() {
            return "map <file_name>";
        }
    }
    
    public static class ListMapsCommand implements Command {
        private final MapManager mapManager;
        
        public ListMapsCommand(MapManager mapManager) {
            this.mapManager = mapManager;
        }
        
        @Override
        public String execute(String[] args) {
            String[] availableMaps = mapManager.getAvailableMaps();
            String[] loadedMaps = mapManager.getLoadedMaps();
            
            StringBuilder result = new StringBuilder();
            
            result.append("=== Available maps ===\n");
            if (availableMaps.length > 0) {
                for (String map : availableMaps) {
                    result.append("  ").append(map).append("\n");
                }
            } else {
                result.append("  Maps not found in maps/ directory\n");
            }
            
            result.append("\n=== Loaded maps ===\n");
            if (loadedMaps.length > 0) {
                String currentMapName = mapManager.getCurrentMapName();
                for (String map : loadedMaps) {
                    if (map.equals(currentMapName)) {
                        result.append("  ").append(map).append(" *current*\n");
                    } else {
                        result.append("  ").append(map).append("\n");
                    }
                }
            } else {
                result.append("  Maps not loaded\n");
            }
            
            result.append("\nUse 'map <name>' to load map");
            
            return result.toString();
        }
        
        @Override
        public String getDescription() {
            return "Shows list of available and loaded maps";
        }
        
        @Override
        public String getUsage() {
            return "maps";
        }
    }
    
    public static class MapInfoCommand implements Command {
        private final MapManager mapManager;
        
        public MapInfoCommand(MapManager mapManager) {
            this.mapManager = mapManager;
        }
        
        @Override
        public String execute(String[] args) {
            if (!mapManager.hasCurrentMap()) {
                return "Map not loaded. Use 'maps' for list of available maps.";
            }
            
            String mapName = mapManager.getCurrentMapName();
            String stats = mapManager.getMapStatistics();
            
            StringBuilder result = new StringBuilder();
            result.append("=== Map info ===\n");
            result.append("Name: ").append(mapName != null ? mapName : "Unknown").append("\n");
            result.append(stats).append("\n");
            
            var currentMap = mapManager.getCurrentMap();
            if (currentMap != null) {
                result.append("Player position: ").append(currentMap.getPlayerStartPosition()).append("\n");
                result.append("Player angle: ").append(String.format("%.1f°", 
                    Math.toDegrees(currentMap.getPlayerStartAngle()))).append("\n");
            }
            
            return result.toString();
        }
        
        @Override
        public String getDescription() {
            return "Shows information about current map";
        }
        
        @Override
        public String getUsage() {
            return "mapinfo";
        }
    }
    
    public static class SwitchMapCommand implements Command {
        private final MapManager mapManager;
        private final EngineLogger logger;
        
        public SwitchMapCommand(MapManager mapManager) {
            this.mapManager = mapManager;
            this.logger = new EngineLogger(SwitchMapCommand.class);
        }
        
        @Override
        public String execute(String[] args) {
            if (args.length == 0) {
                return "Usage instructions: " + getUsage();
            }
            
            String mapName = args[0];
            boolean success = mapManager.switchToMap(mapName);
            
            if (success) {
                logger.logInfo("Switched to map via console: " + mapName);
                return "Switched to map: " + mapName + "\n" + mapManager.getMapStatistics();
            } else {
                return "Error switching to map: " + mapName + 
                       "\nMap not loaded. Use 'map " + mapName + "' to load.";
            }
        }
        
        @Override
        public String getDescription() {
            return "Switches to already loaded map";
        }
        
        @Override
        public String getUsage() {
            return "switch <map_name>";
        }
    }
    
    public static class LoadTestMapCommand implements Command {
        private final MapManager mapManager;
        private final EngineLogger logger;
        
        public LoadTestMapCommand(MapManager mapManager) {
            this.mapManager = mapManager;
            this.logger = new EngineLogger(LoadTestMapCommand.class);
        }
        
        @Override
        public String execute(String[] args) {
            String testType = args.length > 0 ? args[0] : "simple";
            
            boolean success;
            String mapName;
            
            switch (testType.toLowerCase()) {
                case "complex":
                    success = mapManager.loadComplexTestMap();
                    mapName = "complex_test_map";
                    break;
                case "simple":
                default:
                    success = mapManager.loadTestMap();
                    mapName = "test_map";
                    break;
            }
            
            if (success) {
                logger.logInfo("Test map loaded via console: " + mapName);
                return "Test map loaded: " + mapName + "\n" + mapManager.getMapStatistics();
            } else {
                return "Error loading test map";
            }
        }
        
        @Override
        public String getDescription() {
            return "Loads built-in test map";
        }
        
        @Override
        public String getUsage() {
            return "testmap [simple|complex]";
        }
    }
} 