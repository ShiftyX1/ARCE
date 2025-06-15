package com.arce.player;

import com.arce.math.Vector2D;
import com.arce.world.GameMap;
import com.arce.world.Sector;
import com.arce.render.Camera;
import com.arce.logger.EngineLogger;
import java.awt.event.KeyEvent;

public class Player {
    private final EngineLogger logger;
    private Camera camera;
    private GameMap gameMap;
    private Sector currentSector;
    
    private double radius;
    private double height;
    private double eyeHeight;
    
    private double moveSpeed;
    private double turnSpeed;
    private double strafeSpeed;
    
    private Vector2D velocity;
    private boolean onGround;
    
    public Player(Vector2D startPosition, double startAngle, GameMap gameMap, 
                  int screenWidth, int screenHeight) {
        this.logger = new EngineLogger(Player.class);
        this.gameMap = gameMap;
        
        this.radius = 8.0;
        this.height = 56.0;
        this.eyeHeight = 41.0;
        
        this.moveSpeed = 150.0;
        this.turnSpeed = Math.toRadians(120);
        this.strafeSpeed = 150.0;
        
        this.velocity = new Vector2D(0, 0);
        this.onGround = true;
        
        this.camera = new Camera(startPosition, startAngle, screenWidth, screenHeight);
        
        updateCameraHeight();
        
        this.currentSector = gameMap.findSector(startPosition);
        
        logger.logSuccess("Player created at " + startPosition + ", sector: " + 
                         (currentSector != null ? currentSector.getId() : "none"));
    }
    
    public void update(boolean[] keys, double deltaTime) {
        handleInput(keys, deltaTime);
        updatePhysics(deltaTime);
        updateCamera();
    }
    
    private void handleInput(boolean[] keys, double deltaTime) {
        Vector2D movement = new Vector2D(0, 0);
        
        if (keys[KeyEvent.VK_W] || keys[KeyEvent.VK_UP]) {
            Vector2D forward = camera.getDirection().multiply(moveSpeed * deltaTime);
            movement = movement.add(forward);
        }
        if (keys[KeyEvent.VK_S] || keys[KeyEvent.VK_DOWN]) {
            Vector2D backward = camera.getDirection().multiply(-moveSpeed * deltaTime);
            movement = movement.add(backward);
        }
        
        if (keys[KeyEvent.VK_A]) {
            Vector2D left = camera.getDirection().perpendicular().multiply(-strafeSpeed * deltaTime);
            movement = movement.add(left);
        }
        if (keys[KeyEvent.VK_D]) {
            Vector2D right = camera.getDirection().perpendicular().multiply(strafeSpeed * deltaTime);
            movement = movement.add(right);
        }
        
        if (keys[KeyEvent.VK_LEFT] || keys[KeyEvent.VK_Q]) {
            camera.rotate(-turnSpeed * deltaTime);
        }
        if (keys[KeyEvent.VK_RIGHT] || keys[KeyEvent.VK_E]) {
            camera.rotate(turnSpeed * deltaTime);
        }
        
        velocity = movement;
    }
    
    private void updatePhysics(double deltaTime) {
        if (velocity.lengthSquared() > 0.001) {
            Vector2D newPosition = attemptMove(camera.getPosition(), velocity);
            camera.setPosition(newPosition);
            
            Sector newSector = gameMap.findSector(newPosition);
            if (newSector != null && newSector != currentSector) {
                logger.logDebug("Player moved to sector: " + newSector.getId());
                currentSector = newSector;
                updateCameraHeight();
            }
        }
    }
    
    private Vector2D attemptMove(Vector2D currentPos, Vector2D movement) {
        Vector2D newPosition = currentPos.add(movement);
        
        Vector2D testX = new Vector2D(newPosition.x, currentPos.y);
        if (checkCollision(testX)) {
            newPosition = new Vector2D(currentPos.x, newPosition.y);
        }
        
        Vector2D testY = new Vector2D(newPosition.x, currentPos.y);
        if (checkCollision(testY)) {
            newPosition = new Vector2D(newPosition.x, currentPos.y);
        }
        
        if (checkCollision(newPosition)) {
            return currentPos;
        }
        
        return newPosition;
    }
    
    private boolean checkCollision(Vector2D position) {
        for (int angle = 0; angle < 360; angle += 45) {
            double radians = Math.toRadians(angle);
            Vector2D checkPoint = position.add(new Vector2D(
                Math.cos(radians) * radius,
                Math.sin(radians) * radius
            ));
            
            if (isPointInWall(checkPoint)) {
                return true;
            }
        }
        
        return false;
    }
    
    private boolean isPointInWall(Vector2D point) {
        Sector sector = gameMap.findSector(point);
        return sector == null;
    }
    
    private void updateCamera() {
        updateCameraHeight();
    }
    
    private void updateCameraHeight() {
        if (currentSector != null) {
            double floorHeight = currentSector.getFloorHeight();
            camera.setHeight(floorHeight + eyeHeight);
        }
    }
    
    public void teleport(Vector2D position, double angle) {
        camera.setPosition(position);
        camera.setAngle(angle);
        currentSector = gameMap.findSector(position);
        updateCameraHeight();
        
        logger.logInfo("Player teleported to " + position + 
                      ", sector: " + (currentSector != null ? currentSector.getId() : "none"));
    }
    
    public void setMoveSpeed(double moveSpeed) {
        this.moveSpeed = moveSpeed;
        logger.logDebug("Move speed set to: " + moveSpeed);
    }
    
    public void setTurnSpeed(double turnSpeedDegrees) {
        this.turnSpeed = Math.toRadians(turnSpeedDegrees);
        logger.logDebug("Turn speed set to: " + turnSpeedDegrees + "°/s");
    }
    
    public void setStrafeSpeed(double strafeSpeed) {
        this.strafeSpeed = strafeSpeed;
        logger.logDebug("Strafe speed set to: " + strafeSpeed);
    }
    
    public PlayerState getState() {
        return new PlayerState(
            camera.getPosition(),
            camera.getAngle(),
            currentSector,
            velocity,
            onGround
        );
    }
    
    public Camera getCamera() { return camera; }
    public GameMap getGameMap() { return gameMap; }
    public Sector getCurrentSector() { return currentSector; }
    public Vector2D getPosition() { return camera.getPosition(); }
    public double getRadius() { return radius; }
    public double getHeight() { return height; }
    public double getEyeHeight() { return eyeHeight; }
    public Vector2D getVelocity() { return velocity; }
    public boolean isOnGround() { return onGround; }
    
    public void setRadius(double radius) { this.radius = radius; }
    public void setHeight(double height) { this.height = height; }
    public void setEyeHeight(double eyeHeight) { 
        this.eyeHeight = eyeHeight;
        updateCameraHeight();
    }
    
    @Override
    public String toString() {
        return String.format("Player(pos: %s, sector: %s, angle: %.1f)", 
                           getPosition(), 
                           currentSector != null ? currentSector.getId() : "none",
                           Math.toDegrees(camera.getAngle()));
    }
    
    public static class PlayerState {
        public Vector2D position;
        public double angle;
        public Sector sector;
        public Vector2D velocity;
        public boolean onGround;
        
        public PlayerState(Vector2D position, double angle, Sector sector, 
                          Vector2D velocity, boolean onGround) {
            this.position = new Vector2D(position);
            this.angle = angle;
            this.sector = sector;
            this.velocity = new Vector2D(velocity);
            this.onGround = onGround;
        }
        
        @Override
        public String toString() {
            return String.format("PlayerState(pos: %s, angle: %.1f, sector: %s)", 
                               position, Math.toDegrees(angle), 
                               sector != null ? sector.getId() : "none");
        }
    }
}