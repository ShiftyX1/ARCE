package com.arce.entities;

import com.arce.math.Vector2D;
import com.arce.world.Sector;

public class Sprite {
    private static int nextId = 1;
    
    private final int id;
    private String name;
    private Vector2D position;
    private double height;          // Высота над полом сектора
    private double width;           // Ширина спрайта в мире
    private double spriteHeight;    // Высота спрайта в мире
    private String textureName;
    private SpriteType type;
    private boolean visible;
    private boolean active;
    private Sector currentSector;
    
    private String[] animationFrames;
    private double frameRate;
    private double currentFrame;
    private boolean looping;
    
    public Sprite(String name, Vector2D position, String textureName, SpriteType type) {
        this.id = nextId++;
        this.name = name;
        this.position = new Vector2D(position);
        this.textureName = textureName;
        this.type = type;
        this.height = 0.0;
        this.width = 32.0;
        this.spriteHeight = 32.0;
        this.visible = true;
        this.active = true;
        this.currentFrame = 0.0;
        this.frameRate = 1.0;
        this.looping = true;
    }
    
    public void update(double deltaTime) {
        if (!active) return;
        
        if (type == SpriteType.ANIMATED && animationFrames != null) {
            currentFrame += frameRate * deltaTime;
            
            if (currentFrame >= animationFrames.length) {
                if (looping) {
                    currentFrame = 0.0;
                } else {
                    currentFrame = animationFrames.length - 1;
                }
            }
        }
    }
    
    public String getCurrentTextureName() {
        if (type == SpriteType.ANIMATED && animationFrames != null) {
            int frameIndex = (int) currentFrame;
            if (frameIndex >= 0 && frameIndex < animationFrames.length) {
                return animationFrames[frameIndex];
            }
        }
        return textureName;
    }
    
    public void setAnimation(String[] frames, double frameRate, boolean looping) {
        this.animationFrames = frames.clone();
        this.frameRate = frameRate;
        this.looping = looping;
        this.currentFrame = 0.0;
        this.type = SpriteType.ANIMATED;
    }
    
    public double distanceToCamera(Vector2D cameraPosition) {
        return position.distanceTo(cameraPosition);
    }
    
    public int getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public Vector2D getPosition() { return new Vector2D(position); }
    public void setPosition(Vector2D position) { this.position = new Vector2D(position); }
    
    public double getHeight() { return height; }
    public void setHeight(double height) { this.height = height; }
    
    public double getWidth() { return width; }
    public void setWidth(double width) { this.width = width; }
    
    public double getSpriteHeight() { return spriteHeight; }
    public void setSpriteHeight(double spriteHeight) { this.spriteHeight = spriteHeight; }
    
    public String getTextureName() { return textureName; }
    public void setTextureName(String textureName) { this.textureName = textureName; }
    
    public SpriteType getType() { return type; }
    public void setType(SpriteType type) { this.type = type; }
    
    public boolean isVisible() { return visible; }
    public void setVisible(boolean visible) { this.visible = visible; }
    
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    
    public Sector getCurrentSector() { return currentSector; }
    public void setCurrentSector(Sector currentSector) { this.currentSector = currentSector; }
    
    @Override
    public String toString() {
        return String.format("Sprite(id: %d, name: %s, pos: %s, texture: %s)", 
                           id, name, position, getCurrentTextureName());
    }
    
    // Типы спрайтов
    public enum SpriteType {
        BILLBOARD,  // Всегда повернут к игроку
        FIXED,      // Фиксированный угол
        ANIMATED    // Анимированный
    }
}