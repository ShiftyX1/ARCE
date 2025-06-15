package com.arce.render;

import com.arce.math.Vector2D;
import com.arce.math.Ray2D;

public class Camera {
    private Vector2D position;
    private double angle;
    private double fov;
    private int screenWidth;
    private int screenHeight;
    private double height;
    
    private Vector2D direction;
    private Vector2D cameraPlane;
    private double halfFov;
    
    public Camera(Vector2D position, double angle, int screenWidth, int screenHeight) {
        this.position = new Vector2D(position);
        this.angle = angle;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.fov = Math.toRadians(90);
        this.height = 32.0;
        
        updateDerivedValues();
    }
    
    private void updateDerivedValues() {
        this.halfFov = fov / 2.0;
        this.direction = new Vector2D(Math.cos(angle), Math.sin(angle));
        
        double planeLength = Math.tan(halfFov);
        this.cameraPlane = direction.perpendicular().multiply(planeLength);
    }
    
    public Ray2D createRay(int screenX) {
        double cameraX = 2.0 * screenX / screenWidth - 1.0;
        
        Vector2D rayDirection = direction.add(cameraPlane.multiply(cameraX));
        
        return new Ray2D(position, rayDirection);
    }
    
    public Ray2D createRay(double rayAngle) {
        return new Ray2D(position, rayAngle);
    }
    
    public void move(Vector2D delta) {
        position = position.add(delta);
    }
    
    public void moveForward(double distance) {
        Vector2D movement = direction.multiply(distance);
        position = position.add(movement);
    }
    
    public void moveBackward(double distance) {
        moveForward(-distance);
    }
    
    public void strafeLeft(double distance) {
        Vector2D leftDirection = direction.perpendicular().multiply(-1);
        Vector2D movement = leftDirection.multiply(distance);
        position = position.add(movement);
    }
    
    public void strafeRight(double distance) {
        strafeLeft(-distance);
    }
    
    public void rotate(double deltaAngle) {
        angle += deltaAngle;
        while (angle > Math.PI * 2) angle -= Math.PI * 2;
        while (angle < 0) angle += Math.PI * 2;
        
        updateDerivedValues();
    }
    
    public void setAngle(double angle) {
        this.angle = angle;
        updateDerivedValues();
    }
    
    public Vector2D getPosition() { return new Vector2D(position); }
    public void setPosition(Vector2D position) { 
        this.position = new Vector2D(position); 
    }
    
    public double getAngle() { return angle; }
    
    public double getFov() { return fov; }
    public void setFov(double fov) { 
        this.fov = Math.max(Math.toRadians(30), Math.min(Math.toRadians(120), fov));
        updateDerivedValues();
    }
    
    public double getHeight() { return height; }
    public void setHeight(double height) { this.height = height; }
    
    public Vector2D getDirection() { return new Vector2D(direction); }
    public Vector2D getCameraPlane() { return new Vector2D(cameraPlane); }
    
    public int getScreenWidth() { return screenWidth; }
    public int getScreenHeight() { return screenHeight; }
    
    public void setScreenSize(int width, int height) {
        this.screenWidth = width;
        this.screenHeight = height;
    }
    
    @Override
    public String toString() {
        return String.format("Camera(pos: %s, angle: %.2f°, fov: %.1f°)", 
                           position, Math.toDegrees(angle), Math.toDegrees(fov));
    }
}