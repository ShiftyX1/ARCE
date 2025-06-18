package com.arce.render;

import com.arce.math.Ray2D;
import com.arce.math.Vector2D;
import com.arce.world.GameMap;
import com.arce.world.Wall;
import com.arce.world.Sector;
import com.arce.core.managers.SpriteManager;
import com.arce.logger.EngineLogger;

public class RayCaster {
    private final EngineLogger logger;
    private GameMap gameMap;
    private SpriteManager spriteManager;
    private double maxRenderDistance;
    
    public RayCaster(GameMap gameMap) {
        this.logger = new EngineLogger(RayCaster.class);
        this.gameMap = gameMap;
        this.maxRenderDistance = 1000.0;
    }
    
    public void setSpriteManager(SpriteManager spriteManager) {
        this.spriteManager = spriteManager;
        logger.logInfo("Sprite manager connected to raycaster");
    }
    
    public RaycastColumn[] castRays(Camera camera) {
        int screenWidth = camera.getScreenWidth();
        RaycastColumn[] columns = new RaycastColumn[screenWidth];
        
        for (int x = 0; x < screenWidth; x++) {
            Ray2D ray = camera.createRay(x);
            columns[x] = castSingleRay(ray, camera, x);
        }
        
        return columns;
    }
    
    public RaycastColumn castSingleRay(Ray2D ray, Camera camera, int screenX) {
        GameMap.RaycastResult result = gameMap.raycast(ray, maxRenderDistance);
        
        if (result == null) {
            return new RaycastColumn(maxRenderDistance, null, null, ray, camera, screenX);
        }
        
        Sector currentSector = gameMap.findSector(camera.getPosition());
        return new RaycastColumn(result.distance, result.hitWall, currentSector, ray, camera, screenX);
    }
    
    public static class RaycastColumn {
        public double distance;
        public Wall hitWall;
        public Sector currentSector;
        public Ray2D ray;
        public Camera camera;
        public int screenX;
        
        public int wallHeight;
        public int wallTop;
        public int wallBottom;
        public double wallTextureX;
        
        public RaycastColumn(double distance, Wall hitWall, Sector currentSector, Ray2D ray, Camera camera, int screenX) {
            this.distance = distance;
            this.hitWall = hitWall;
            this.currentSector = currentSector;
            this.ray = ray;
            this.camera = camera;
            this.screenX = screenX;
            
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
            
            double floorHeight = currentSector.getFloorHeight();
            double ceilingHeight = currentSector.getCeilingHeight();
            
            Vector2D cameraDirection = camera.getDirection();
            double cosAngle = ray.direction.dot(cameraDirection);
            double perpDistance = distance * cosAngle;
            
            if (perpDistance > 0.1) {
                double projectionHeight = screenHeight / perpDistance;
                
                double worldWallHeight = ceilingHeight - floorHeight;
                wallHeight = (int)(worldWallHeight * projectionHeight);
                
                double wallMidHeight = (floorHeight + ceilingHeight) / 2.0;
                double verticalOffset = (cameraHeight - wallMidHeight) * projectionHeight;
                
                int wallCenter = screenHeight / 2 + (int)verticalOffset;
                
                wallTop = wallCenter - wallHeight / 2;
                wallBottom = wallCenter + wallHeight / 2;
            } else {
                wallHeight = screenHeight;
                wallTop = 0;
                wallBottom = screenHeight - 1;
            }
            
            wallTop = Math.max(0, wallTop);
            wallBottom = Math.min(screenHeight - 1, wallBottom);
            
            wallHeight = Math.max(1, wallBottom - wallTop);
            
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
            
            Vector2D wallDirection = wallEnd.subtract(wallStart);
            Vector2D startToHit = hitPoint.subtract(wallStart);
            
            double t = startToHit.dot(wallDirection) / wallDirection.dot(wallDirection);
            t = Math.max(0.0, Math.min(1.0, t));
            
            double wallLength = wallDirection.length();
            wallTextureX = t * wallLength;
            
            wallTextureX = wallTextureX % 64.0;
            if (wallTextureX < 0) wallTextureX += 64.0;
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
    
    public SpriteManager getSpriteManager() { return spriteManager; }
}