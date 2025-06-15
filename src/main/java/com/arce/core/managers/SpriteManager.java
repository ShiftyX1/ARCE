package com.arce.core.managers;

import com.arce.entities.Sprite;
import com.arce.math.Vector2D;
import com.arce.world.GameMap;
import com.arce.world.Sector;
import com.arce.render.Camera;
import com.arce.logger.EngineLogger;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SpriteManager {
    private final EngineLogger logger;
    private final Map<Integer, Sprite> sprites;
    private final Map<Sector, List<Sprite>> spritesBySector;
    private GameMap gameMap;
    
    public SpriteManager(GameMap gameMap) {
        this.logger = new EngineLogger(SpriteManager.class);
        this.sprites = new ConcurrentHashMap<>();
        this.spritesBySector = new ConcurrentHashMap<>();
        this.gameMap = gameMap;
    }
    
    public Sprite createSprite(String name, Vector2D position, String textureName) {
        Sprite sprite = new Sprite(name, position, textureName, Sprite.SpriteType.BILLBOARD);
        addSprite(sprite);
        return sprite;
    }
    
    public Sprite createAnimatedSprite(String name, Vector2D position, 
                                     String[] frameNames, double frameRate) {
        Sprite sprite = new Sprite(name, position, frameNames[0], Sprite.SpriteType.ANIMATED);
        sprite.setAnimation(frameNames, frameRate, true);
        addSprite(sprite);
        return sprite;
    }
    
    private void addSprite(Sprite sprite) {
        sprites.put(sprite.getId(), sprite);
        
        Sector sector = gameMap.findSector(sprite.getPosition());
        sprite.setCurrentSector(sector);
        
        if (sector != null) {
            spritesBySector.computeIfAbsent(sector, k -> new ArrayList<>()).add(sprite);
        }
        
        logger.logDebug("Added sprite: " + sprite);
    }
    
    public void destroySprite(int spriteId) {
        Sprite sprite = sprites.remove(spriteId);
        if (sprite != null) {
            if (sprite.getCurrentSector() != null) {
                List<Sprite> sectorSprites = spritesBySector.get(sprite.getCurrentSector());
                if (sectorSprites != null) {
                    sectorSprites.remove(sprite);
                }
            }
            logger.logDebug("Destroyed sprite: " + sprite);
        }
    }
    
    public void update(double deltaTime) {
        for (Sprite sprite : sprites.values()) {
            if (sprite.isActive()) {
                sprite.update(deltaTime);
                
                Sector newSector = gameMap.findSector(sprite.getPosition());
                if (newSector != sprite.getCurrentSector()) {
                    moveSpriteBetweenSectors(sprite, sprite.getCurrentSector(), newSector);
                }
            }
        }
    }
    
    private void moveSpriteBetweenSectors(Sprite sprite, Sector oldSector, Sector newSector) {
        if (oldSector != null) {
            List<Sprite> oldSectorSprites = spritesBySector.get(oldSector);
            if (oldSectorSprites != null) {
                oldSectorSprites.remove(sprite);
            }
        }
        
        sprite.setCurrentSector(newSector);
        if (newSector != null) {
            spritesBySector.computeIfAbsent(newSector, k -> new ArrayList<>()).add(sprite);
        }
        
        logger.logDebug("Sprite moved between sectors: " + sprite.getName());
    }
    
    public List<Sprite> getVisibleSprites(Camera camera) {
        List<Sprite> visibleSprites = new ArrayList<>();
        Vector2D cameraPos = camera.getPosition();
        
        Sector cameraSector = gameMap.findSector(cameraPos);
        if (cameraSector != null) {
            List<Sprite> sectorSprites = spritesBySector.get(cameraSector);
            if (sectorSprites != null) {
                for (Sprite sprite : sectorSprites) {
                    if (sprite.isVisible() && isInView(sprite, camera)) {
                        visibleSprites.add(sprite);
                    }
                }
            }
        }
        
        visibleSprites.sort((s1, s2) -> Double.compare(
            s2.distanceToCamera(cameraPos), 
            s1.distanceToCamera(cameraPos)
        ));
        
        return visibleSprites;
    }
    
    private boolean isInView(Sprite sprite, Camera camera) {
        Vector2D cameraPos = camera.getPosition();
        Vector2D cameraDir = camera.getDirection();
        Vector2D toSprite = sprite.getPosition().subtract(cameraPos);
        
        double dot = toSprite.dot(cameraDir);
        if (dot <= 0) return false;
        
        double angle = Math.acos(dot / toSprite.length());
        return angle <= camera.getFov() / 2.0 + 0.2;
    }
    
    public Sprite getSprite(int id) {
        return sprites.get(id);
    }
    
    public Collection<Sprite> getAllSprites() {
        return sprites.values();
    }
    
    public List<Sprite> getSpritesInSector(Sector sector) {
        return spritesBySector.getOrDefault(sector, Collections.emptyList());
    }
    
    public List<Sprite> findSpritesByName(String name) {
        return sprites.values().stream()
                .filter(sprite -> sprite.getName().equals(name))
                .toList();
    }
    
    public void clear() {
        sprites.clear();
        spritesBySector.clear();
        logger.logInfo("Cleared all sprites");
    }
    
    public int getSpriteCount() {
        return sprites.size();
    }
    
    public Map<String, Integer> getSpriteTypeStats() {
        Map<String, Integer> stats = new HashMap<>();
        for (Sprite sprite : sprites.values()) {
            String type = sprite.getType().toString();
            stats.put(type, stats.getOrDefault(type, 0) + 1);
        }
        return stats;
    }
    
    @Override
    public String toString() {
        return String.format("SpriteManager(sprites: %d)", sprites.size());
    }
}