package com.arce.world;

import com.arce.math.Line2D;
import com.arce.math.Vector2D;

public class Wall {
    private Line2D line;
    private Sector frontSector;  // Сектор "спереди" стены
    private Sector backSector;   // Сектор "сзади" стены (может быть null)
    private int textureId;
    private boolean solid;       // Твердая стена или портал между секторами
    
    public Wall(Vector2D start, Vector2D end) {
        this.line = new Line2D(start, end);
        this.solid = true;
        this.textureId = 1;
    }
    
    public Wall(Line2D line) {
        this.line = new Line2D(line.start, line.end);
        this.solid = true;
        this.textureId = 1;
    }
    
    public Line2D getLine() { return line; }
    
    public Sector getFrontSector() { return frontSector; }
    public void setFrontSector(Sector frontSector) { this.frontSector = frontSector; }
    
    public Sector getBackSector() { return backSector; }
    public void setBackSector(Sector backSector) { 
        this.backSector = backSector;
        this.solid = (backSector == null);
    }
    
    public int getTextureId() { return textureId; }
    public void setTextureId(int textureId) { this.textureId = textureId; }
    
    public boolean isSolid() { return solid; }
    public void setSolid(boolean solid) { this.solid = solid; }
    
    public boolean isPortal() {
        return !solid && backSector != null;
    }
    
    public Sector getOtherSector(Sector currentSector) {
        if (currentSector == frontSector) {
            return backSector;
        } else if (currentSector == backSector) {
            return frontSector;
        }
        return null;
    }
    
    @Override
    public String toString() {
        return String.format("Wall(%s, texture: %d, solid: %b)", 
                           line, textureId, solid);
    }
}