package com.arce.math;

public class Vector2D {
    public double x, y;
    
    public Vector2D(double x, double y) {
        this.x = x;
        this.y = y;
    }
    
    public Vector2D() {
        this(0, 0);
    }
    
    public Vector2D(Vector2D other) {
        this(other.x, other.y);
    }
    
    // Основные операции
    public Vector2D add(Vector2D other) {
        return new Vector2D(x + other.x, y + other.y);
    }
    
    public Vector2D subtract(Vector2D other) {
        return new Vector2D(x - other.x, y - other.y);
    }
    
    public Vector2D multiply(double scalar) {
        return new Vector2D(x * scalar, y * scalar);
    }
    
    public double dot(Vector2D other) {
        return x * other.x + y * other.y;
    }
    
    public double cross(Vector2D other) {
        return x * other.y - y * other.x;
    }
    
    public double length() {
        return Math.sqrt(x * x + y * y);
    }
    
    public double lengthSquared() {
        return x * x + y * y;
    }
    
    public Vector2D normalize() {
        double len = length();
        if (len > 0.0001) {
            return new Vector2D(x / len, y / len);
        }
        return new Vector2D(0, 0);
    }
    
    public double distanceTo(Vector2D other) {
        return subtract(other).length();
    }
    
    public Vector2D perpendicular() {
        return new Vector2D(-y, x);
    }
    
    public double angle() {
        return Math.atan2(y, x);
    }
    
    @Override
    public String toString() {
        return String.format("Vector2D(%.3f, %.3f)", x, y);
    }
}