package com.arce.core;

import com.arce.logger.EngineLogger;
import java.io.*;
import java.util.Properties;

public class EngineConfig {
    private final EngineLogger logger;
    private final Properties properties;
    private static final String CONFIG_FILE = "engine.properties";
    
    private int windowWidth = 800;
    private int windowHeight = 600;
    private String windowTitle = "ARCE Engine";
    private boolean windowResizable = false;
    
    private int targetFPS = 60;
    private boolean vsyncEnabled = true;
    private int renderDistance = 2000;
    
    private double playerMoveSpeed = 5.0;
    private double playerTurnSpeed = 3.0;
    private double mouseSensitivity = 1.0;
    
    private boolean debugMode = false;
    private boolean showFPS = true;
    private boolean showPlayerInfo = false;
    
    public EngineConfig() {
        this.logger = new EngineLogger(EngineConfig.class);
        this.properties = new Properties();
        loadConfig();
    }
    
    private void loadConfig() {
        logger.logStart("loadConfig");
        
        try {
            File configFile = new File(CONFIG_FILE);
            if (configFile.exists()) {
                try (FileInputStream fis = new FileInputStream(configFile)) {
                    properties.load(fis);
                    applyProperties();
                    logger.logSuccess("Configuration loaded from " + CONFIG_FILE);
                }
            } else {
                logger.logInfo("Config file not found, using defaults");
                saveConfig();
            }
        } catch (IOException e) {
            logger.logError("Failed to load configuration", e);
            logger.logInfo("Using default configuration");
        }
        
        logger.logEnd("loadConfig");
    }
    
    private void applyProperties() {
        windowWidth = getIntProperty("window.width", windowWidth);
        windowHeight = getIntProperty("window.height", windowHeight);
        windowTitle = getStringProperty("window.title", windowTitle);
        windowResizable = getBooleanProperty("window.resizable", windowResizable);
        
        targetFPS = getIntProperty("performance.target_fps", targetFPS);
        vsyncEnabled = getBooleanProperty("performance.vsync", vsyncEnabled);
        renderDistance = getIntProperty("performance.render_distance", renderDistance);
        
        playerMoveSpeed = getDoubleProperty("player.move_speed", playerMoveSpeed);
        playerTurnSpeed = getDoubleProperty("player.turn_speed", playerTurnSpeed);
        mouseSensitivity = getDoubleProperty("player.mouse_sensitivity", mouseSensitivity);
        
        debugMode = getBooleanProperty("debug.enabled", debugMode);
        showFPS = getBooleanProperty("debug.show_fps", showFPS);
        showPlayerInfo = getBooleanProperty("debug.show_player_info", showPlayerInfo);
    }
    
    public void saveConfig() {
        logger.logStart("saveConfig");
        
        try {
            properties.setProperty("window.width", String.valueOf(windowWidth));
            properties.setProperty("window.height", String.valueOf(windowHeight));
            properties.setProperty("window.title", windowTitle);
            properties.setProperty("window.resizable", String.valueOf(windowResizable));
            
            properties.setProperty("performance.target_fps", String.valueOf(targetFPS));
            properties.setProperty("performance.vsync", String.valueOf(vsyncEnabled));
            properties.setProperty("performance.render_distance", String.valueOf(renderDistance));
            
            properties.setProperty("player.move_speed", String.valueOf(playerMoveSpeed));
            properties.setProperty("player.turn_speed", String.valueOf(playerTurnSpeed));
            properties.setProperty("player.mouse_sensitivity", String.valueOf(mouseSensitivity));
            
            properties.setProperty("debug.enabled", String.valueOf(debugMode));
            properties.setProperty("debug.show_fps", String.valueOf(showFPS));
            properties.setProperty("debug.show_player_info", String.valueOf(showPlayerInfo));

            try (FileOutputStream fos = new FileOutputStream(CONFIG_FILE)) {
                properties.store(fos, "ARCE Engine Configuration");
                logger.logSuccess("Configuration saved to " + CONFIG_FILE);
            }
        } catch (IOException e) {
            logger.logError("Failed to save configuration", e);
        }
        
        logger.logEnd("saveConfig");
    }
    
    private int getIntProperty(String key, int defaultValue) {
        try {
            return Integer.parseInt(properties.getProperty(key, String.valueOf(defaultValue)));
        } catch (NumberFormatException e) {
            logger.logError("Invalid integer value for " + key, e);
            return defaultValue;
        }
    }
    
    private double getDoubleProperty(String key, double defaultValue) {
        try {
            return Double.parseDouble(properties.getProperty(key, String.valueOf(defaultValue)));
        } catch (NumberFormatException e) {
            logger.logError("Invalid double value for " + key, e);
            return defaultValue;
        }
    }
    
    private boolean getBooleanProperty(String key, boolean defaultValue) {
        return Boolean.parseBoolean(properties.getProperty(key, String.valueOf(defaultValue)));
    }
    
    private String getStringProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
    
    public int getWindowWidth() { return windowWidth; }
    public void setWindowWidth(int windowWidth) { 
        this.windowWidth = windowWidth;
        logger.logInfo("Window width changed to: " + windowWidth);
    }
    
    public int getWindowHeight() { return windowHeight; }
    public void setWindowHeight(int windowHeight) { 
        this.windowHeight = windowHeight;
        logger.logInfo("Window height changed to: " + windowHeight);
    }
    
    public String getWindowTitle() { return windowTitle; }
    public void setWindowTitle(String windowTitle) { 
        this.windowTitle = windowTitle;
        logger.logInfo("Window title changed to: " + windowTitle);
    }
    
    public boolean isWindowResizable() { return windowResizable; }
    public void setWindowResizable(boolean windowResizable) { 
        this.windowResizable = windowResizable;
    }
    
    public int getTargetFPS() { return targetFPS; }
    public void setTargetFPS(int targetFPS) { 
        this.targetFPS = Math.max(1, Math.min(300, targetFPS));
        logger.logInfo("Target FPS changed to: " + this.targetFPS);
    }
    
    public boolean isVsyncEnabled() { return vsyncEnabled; }
    public void setVsyncEnabled(boolean vsyncEnabled) { this.vsyncEnabled = vsyncEnabled; }
    
    public int getRenderDistance() { return renderDistance; }
    public void setRenderDistance(int renderDistance) { this.renderDistance = renderDistance; }
    
    public double getPlayerMoveSpeed() { return playerMoveSpeed; }
    public void setPlayerMoveSpeed(double playerMoveSpeed) { this.playerMoveSpeed = playerMoveSpeed; }
    
    public double getPlayerTurnSpeed() { return playerTurnSpeed; }
    public void setPlayerTurnSpeed(double playerTurnSpeed) { this.playerTurnSpeed = playerTurnSpeed; }
    
    public double getMouseSensitivity() { return mouseSensitivity; }
    public void setMouseSensitivity(double mouseSensitivity) { this.mouseSensitivity = mouseSensitivity; }
    
    public boolean isDebugMode() { return debugMode; }
    public void setDebugMode(boolean debugMode) { 
        this.debugMode = debugMode;
        logger.logInfo("Debug mode: " + (debugMode ? "enabled" : "disabled"));
    }
    
    public boolean isShowFPS() { return showFPS; }
    public void setShowFPS(boolean showFPS) { this.showFPS = showFPS; }
    
    public boolean isShowPlayerInfo() { return showPlayerInfo; }
    public void setShowPlayerInfo(boolean showPlayerInfo) { this.showPlayerInfo = showPlayerInfo; }
}