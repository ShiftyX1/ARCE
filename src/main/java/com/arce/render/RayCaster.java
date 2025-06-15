package com.arce.render;

import com.arce.math.Ray2D;
import com.arce.math.Vector2D;
import com.arce.world.GameMap;
import com.arce.world.Wall;
import com.arce.world.Sector;
import com.arce.logger.EngineLogger;

public class RayCaster {
    private final EngineLogger logger;
    private GameMap gameMap;
    private double maxRenderDistance;
    
    public RayCaster(GameMap gameMap) {
        this.logger = new EngineLogger(RayCaster.class);
        this.gameMap = gameMap;
        this.maxRenderDistance = 1000.0;
    }
    
    public RaycastColumn[] castRays(Camera camera) {
        int screenWidth = camera.getScreenWidth();
        RaycastColumn[] columns = new RaycastColumn[screenWidth];
        
        for (int x = 0; x < screenWidth; x++) {
            Ray2D ray = camera.createRay(x);
            columns[x] = castSingleRay(ray, camera);
        }
        
        return columns;
    }
    
    public RaycastColumn castSingleRay(Ray2D ray, Camera camera) {
        GameMap.RaycastResult result = gameMap.raycast(ray, maxRenderDistance);
        
        if (result == null) {
            return new RaycastColumn(maxRenderDistance, null, null, ray, camera);
        }
        
        double correctedDistance = correctFishEyeDistance(result.distance, ray, camera);
        
        Sector currentSector = gameMap.findSector(camera.getPosition());
        
        return new RaycastColumn(correctedDistance, result.hitWall, currentSector, ray, camera);
    }
    
    private double correctFishEyeDistance(double distance, Ray2D ray, Camera camera) {
        Vector2D cameraDirection = camera.getDirection();
        double cosAngle = ray.direction.dot(cameraDirection);
        return distance * cosAngle;
    }
    
    public static class RaycastColumn {
        public double distance;
        public Wall hitWall;
        public Sector currentSector;
        public Ray2D ray;
        public Camera camera;
        
        public int wallHeight;
        public int wallTop;
        public int wallBottom;
        public double wallTextureX;
        
        public RaycastColumn(double distance, Wall hitWall, Sector currentSector, Ray2D ray, Camera camera) {
            this.distance = distance;
            this.hitWall = hitWall;
            this.currentSector = currentSector;
            this.ray = ray;
            this.camera = camera;
            
            calculateWallProjection();
        }
        
        private void calculateWallProjection() {
            if (hitWall == null || currentSector == null) {
                wallHeight = 0;
                wallTop = camera.getScreenHeight() / 2;
                wallBottom = camera.getScreenHeight() / 2;
                wallTextureX = 0;
                return;
            }
            
            int screenHeight = camera.getScreenHeight();
            double cameraHeight = camera.getHeight();
            
            double worldWallHeight = currentSector.getWallHeight();
            
            if (distance > 0.1) {
                wallHeight = (int) (worldWallHeight * screenHeight / distance);
            } else {
                wallHeight = screenHeight;
            }
            
            int wallCenter = screenHeight / 2 + (int) ((cameraHeight - currentSector.getFloorHeight() - worldWallHeight / 2) * screenHeight / distance);
            wallTop = wallCenter - wallHeight / 2;
            wallBottom = wallCenter + wallHeight / 2;
            
            wallTop = Math.max(0, wallTop);
            wallBottom = Math.min(screenHeight - 1, wallBottom);
            
            calculateTextureX();
        }
        
        private void calculateTextureX() {
            if (hitWall == null) {
                wallTextureX = 0;
                return;
            }
            
            Vector2D hitPoint = ray.getPoint(distance);
            Vector2D wallStart = hitWall.getLine().start;
            Vector2D wallEnd = hitWall.getLine().end;
            
            double wallLength = wallStart.distanceTo(wallEnd);
            double hitPosition = wallStart.distanceTo(hitPoint);
            
            wallTextureX = hitPosition / wallLength;
            
            wallTextureX = wallTextureX - Math.floor(wallTextureX);
        }
        
        public boolean hasHit() {
            return hitWall != null;
        }
        
        public boolean isPortal() {
            return hitWall != null && hitWall.isPortal();
        }
        
        @Override
        public String toString() {
            return String.format("RaycastColumn(distance: %.2f, wallHeight: %d, hasHit: %b)", 
                               distance, wallHeight, hasHit());
        }
    }
    
    public double getMaxRenderDistance() { return maxRenderDistance; }
    public void setMaxRenderDistance(double maxRenderDistance) { 
        this.maxRenderDistance = maxRenderDistance; 
    }
    
    public GameMap getGameMap() { return gameMap; }
    public void setGameMap(GameMap gameMap) { this.gameMap = gameMap; }
}