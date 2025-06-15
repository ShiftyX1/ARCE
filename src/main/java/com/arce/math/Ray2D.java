package com.arce.math;

public class Ray2D {
    public Vector2D origin;
    public Vector2D direction;
    
    public Ray2D(Vector2D origin, Vector2D direction) {
        this.origin = new Vector2D(origin);
        this.direction = direction.normalize();
    }
    
    public Ray2D(Vector2D origin, double angle) {
        this.origin = new Vector2D(origin);
        this.direction = new Vector2D(Math.cos(angle), Math.sin(angle));
    }
    
    public Vector2D getPoint(double distance) {
        return origin.add(direction.multiply(distance));
    }
    
    @Override
    public String toString() {
        return String.format("Ray2D(origin: %s, direction: %s)", origin, direction);
    }
}