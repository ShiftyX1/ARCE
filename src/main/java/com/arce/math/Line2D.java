package com.arce.math;

public class Line2D {
    public Vector2D start, end;
    
    public Line2D(Vector2D start, Vector2D end) {
        this.start = new Vector2D(start);
        this.end = new Vector2D(end);
    }
    
    public Line2D(double x1, double y1, double x2, double y2) {
        this.start = new Vector2D(x1, y1);
        this.end = new Vector2D(x2, y2);
    }
    
    public Vector2D getDirection() {
        return end.subtract(start);
    }
    
    public Vector2D getNormal() {
        return getDirection().perpendicular().normalize();
    }
    
    public double length() {
        return start.distanceTo(end);
    }
    
    // Определяем, с какой стороны линии находится точка
    // > 0 - слева, < 0 - справа, = 0 - на линии
    public double whichSide(Vector2D point) {
        Vector2D toPoint = point.subtract(start);
        Vector2D direction = getDirection();
        return direction.cross(toPoint);
    }
    
    public Vector2D intersect(Line2D other) {
        Vector2D dir1 = getDirection();
        Vector2D dir2 = other.getDirection();
        
        double cross = dir1.cross(dir2);
        
        if (Math.abs(cross) < 0.0001) {
            return null;
        }
        
        Vector2D startDiff = other.start.subtract(start);
        double t = startDiff.cross(dir2) / cross;
        
        return start.add(dir1.multiply(t));
    }
    
    public IntersectionResult intersectRay(Ray2D ray) {
        Vector2D dir = getDirection();
        Vector2D toStart = start.subtract(ray.origin);
        
        double cross = ray.direction.cross(dir);
        
        if (Math.abs(cross) < 0.0001) {
            return null;
        }
        
        double t = toStart.cross(dir) / cross;
        double u = toStart.cross(ray.direction) / cross;
        
        if (t >= 0 && u >= 0 && u <= 1) {
            Vector2D point = ray.origin.add(ray.direction.multiply(t));
            return new IntersectionResult(point, t, this);
        }
        
        return null;
    }
    
    @Override
    public String toString() {
        return String.format("Line2D(%s -> %s)", start, end);
    }
    
    public static class IntersectionResult {
        public Vector2D point;
        public double distance;
        public Line2D line;
        
        public IntersectionResult(Vector2D point, double distance, Line2D line) {
            this.point = point;
            this.distance = distance;
            this.line = line;
        }
    }
}