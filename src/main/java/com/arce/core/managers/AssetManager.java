package com.arce.core.managers;

import com.arce.assets.Texture;
import com.arce.logger.EngineLogger;
import java.awt.Color;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.io.File;

public class AssetManager {
    private final EngineLogger logger;
    private final Map<String, Texture> textures;
    private String assetPath = "assets/";
    
    public AssetManager() {
        this.logger = new EngineLogger(AssetManager.class);
        this.textures = new ConcurrentHashMap<>();
        
        createDefaultTextures();
    }
    
    private void createDefaultTextures() {
        textures.put("default", Texture.createDefault());
        textures.put("brick", Texture.createBrick("brick"));
        textures.put("red_wall", Texture.createSolid("red_wall", Color.RED, 64));
        textures.put("blue_wall", Texture.createSolid("blue_wall", Color.BLUE, 64));
        textures.put("green_wall", Texture.createSolid("green_wall", Color.GREEN, 64));
        textures.put("yellow_wall", Texture.createSolid("yellow_wall", Color.YELLOW, 64));
        
        logger.logSuccess("Created default textures");
    }
    
    public Texture loadTexture(String name, String filename) {
        if (textures.containsKey(name)) {
            logger.logDebug("Texture already loaded: " + name);
            return textures.get(name);
        }
        
        String fullPath = assetPath + "textures/" + filename;
        
        try {
            File file = new File(fullPath);
            if (!file.exists()) {
                logger.logError("Texture file not found: " + fullPath, new RuntimeException("File not found"));
                return getDefaultTexture();
            }
            
            Texture texture = new Texture(name, fullPath);
            textures.put(name, texture);
            logger.logSuccess("Loaded texture: " + name + " from " + filename);
            return texture;
            
        } catch (Exception e) {
            logger.logError("Failed to load texture: " + name, e);
            return getDefaultTexture();
        }
    }
    
    public Texture getTexture(String name) {
        Texture texture = textures.get(name);
        if (texture == null) {
            logger.logError("Texture not found: " + name + ", using default", new RuntimeException("Texture not found"));
            return getDefaultTexture();
        }
        return texture;
    }
    
    public Texture getTextureById(int textureId) {
        String[] defaultNames = {"default", "wall_1", "wall_2", "wall_3", "wall_4", "wall_5", "wall_6", "wall_7", "wall_8", "red_wall", "blue_wall", "green_wall", "yellow_wall", "brick"};
        
        if (textureId >= 0 && textureId < defaultNames.length) {
            return getTexture(defaultNames[textureId]);
        }
        
        return getDefaultTexture();
    }
    
    private Texture getDefaultTexture() {
        return textures.get("default");
    }
    
    public Texture createProceduralTexture(String name, TextureGenerator generator, int size) {
        try {
            Texture texture = generator.generate(name, size);
            textures.put(name, texture);
            logger.logSuccess("Created procedural texture: " + name);
            return texture;
        } catch (Exception e) {
            logger.logError("Failed to create procedural texture: " + name, e);
            return getDefaultTexture();
        }
    }
    
    public void unloadTexture(String name) {
        Texture texture = textures.remove(name);
        if (texture != null && !isDefaultTexture(name)) {
            texture.dispose();
            logger.logInfo("Unloaded texture: " + name);
        }
    }
    
    private boolean isDefaultTexture(String name) {
        return name.equals("default") || name.equals("brick") || 
               name.equals("red_wall") || name.equals("blue_wall") ||
               name.equals("green_wall") || name.equals("yellow_wall");
    }
    
    public void unloadAll() {
        textures.entrySet().removeIf(entry -> {
            if (!isDefaultTexture(entry.getKey())) {
                entry.getValue().dispose();
                return true;
            }
            return false;
        });
        logger.logInfo("Unloaded all non-default textures");
    }
    
    public void setAssetPath(String path) {
        this.assetPath = path.endsWith("/") ? path : path + "/";
        logger.logInfo("Asset path set to: " + this.assetPath);
    }
    
    public String getAssetPath() {
        return assetPath;
    }
    
    public int getTextureCount() { 
        return textures.size(); 
    }
    
    public Map<String, Texture> getAllTextures() {
        return Map.copyOf(textures);
    }
    
    public interface TextureGenerator {
        Texture generate(String name, int size);
    }
    
    @Override
    public String toString() {
        return String.format("AssetManager(textures: %d, path: %s)", textures.size(), assetPath);
    }
}