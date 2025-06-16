package com.arce.world;

import com.arce.math.Line2D;
import com.arce.math.Vector2D;
import com.arce.math.Ray2D;
import com.arce.logger.EngineLogger;
import java.util.ArrayList;
import java.util.List;

public class BSPNode {
    private static final EngineLogger logger = new EngineLogger(BSPNode.class);
    
    private Line2D splitter;
    private BSPNode frontChild;
    private BSPNode backChild;
    private List<Wall> walls;
    private boolean isLeaf;
    
    public BSPNode(List<Wall> walls) {
        this.walls = new ArrayList<>(walls);
        this.isLeaf = true;
        this.frontChild = null;
        this.backChild = null;
        this.splitter = null;
    }
    
    public BSPNode(Line2D splitter, BSPNode frontChild, BSPNode backChild) {
        this.splitter = splitter;
        this.frontChild = frontChild;
        this.backChild = backChild;
        this.isLeaf = false;
        this.walls = null;
    }
    
    public static BSPNode buildBSP(List<Wall> walls, int maxWallsPerNode) {
        return buildBSPRecursive(walls, maxWallsPerNode, 0, 50);
    }
    
    private static BSPNode buildBSPRecursive(List<Wall> walls, int maxWallsPerNode, int depth, int maxDepth) {
        if (depth >= maxDepth) {
            logger.logInfo("BSP: Max depth reached ({}), creating leaf with {} walls", maxDepth, walls.size());
            return new BSPNode(walls);
        }
        
        if (walls.size() <= maxWallsPerNode) {
            return new BSPNode(walls);
        }
        
        Line2D splitter = chooseBestSplitter(walls);
        if (splitter == null) {
            logger.logInfo("BSP: No suitable splitter found, creating leaf with {} walls", walls.size());
            return new BSPNode(walls);
        }
        
        List<Wall> frontWalls = new ArrayList<>();
        List<Wall> backWalls = new ArrayList<>();
        List<Wall> colinearWalls = new ArrayList<>();
        
        for (Wall wall : walls) {
            classifyWall(wall, splitter, frontWalls, backWalls, colinearWalls);
        }
        
        if (frontWalls.size() <= backWalls.size()) {
            frontWalls.addAll(colinearWalls);
        } else {
            backWalls.addAll(colinearWalls);
        }
        
        if (frontWalls.size() >= walls.size() || backWalls.size() >= walls.size()) {
            logger.logInfo("BSP: No progress made (front: {}, back: {}, total: {}), creating leaf", 
                         frontWalls.size(), backWalls.size(), walls.size());
            return new BSPNode(walls);
        }
        
        BSPNode frontChild = null;
        BSPNode backChild = null;
        
        if (!frontWalls.isEmpty()) {
            frontChild = buildBSPRecursive(frontWalls, maxWallsPerNode, depth + 1, maxDepth);
        }
        
        if (!backWalls.isEmpty()) {
            backChild = buildBSPRecursive(backWalls, maxWallsPerNode, depth + 1, maxDepth);
        }
        
        return new BSPNode(splitter, frontChild, backChild);
    }
    
    private static Line2D chooseBestSplitter(List<Wall> walls) {
        Line2D bestSplitter = null;
        int bestScore = Integer.MAX_VALUE;
        
        for (Wall wall : walls) {
            Line2D candidate = wall.getLine();
            int score = evaluateSplitter(candidate, walls);
            
            if (score < bestScore) {
                bestScore = score;
                bestSplitter = candidate;
            }
        }
        
        if (bestSplitter != null) {
            int frontCount = 0;
            int backCount = 0;
            
            for (Wall wall : walls) {
                WallClassification classification = classifyWallRelativeToLine(wall, bestSplitter);
                if (classification == WallClassification.FRONT) {
                    frontCount++;
                } else if (classification == WallClassification.BACK) {
                    backCount++;
                } else if (classification == WallClassification.SPANNING) {
                    frontCount++;
                    backCount++;
                }
            }
            
            if (frontCount == 0 || backCount == 0) {
                logger.logInfo("BSP: Best splitter doesn't actually split walls (front: {}, back: {})", 
                             frontCount, backCount);
                return null;
            }
        }
        
        return bestSplitter;
    }
    
    private static int evaluateSplitter(Line2D splitter, List<Wall> walls) {
        int frontCount = 0;
        int backCount = 0;
        int splitCount = 0;
        
        for (Wall wall : walls) {
            WallClassification classification = classifyWallRelativeToLine(wall, splitter);
            
            switch (classification) {
                case FRONT:
                    frontCount++;
                    break;
                case BACK:
                    backCount++;
                    break;
                case SPANNING:
                    splitCount += 10;
                    frontCount++;
                    backCount++;
                    break;
                case COLINEAR:
                    break;
            }
        }
        
        int balance = Math.abs(frontCount - backCount);
        return balance + splitCount;
    }

    private static WallClassification classifyWallRelativeToLine(Wall wall, Line2D splitter) {
        double startSide = splitter.whichSide(wall.getLine().start);
        double endSide = splitter.whichSide(wall.getLine().end);
        
        final double EPSILON = 0.0001;
        
        boolean startOnLine = Math.abs(startSide) < EPSILON;
        boolean endOnLine = Math.abs(endSide) < EPSILON;
        
        if (startOnLine && endOnLine) {
            return WallClassification.COLINEAR;
        }
        
        if (startSide > EPSILON && endSide > EPSILON) {
            return WallClassification.FRONT;
        }
        
        if (startSide < -EPSILON && endSide < -EPSILON) {
            return WallClassification.BACK;
        }
        
        return WallClassification.SPANNING;
    }
    
    private static void classifyWall(Wall wall, Line2D splitter, 
                                   List<Wall> frontWalls, List<Wall> backWalls, 
                                   List<Wall> colinearWalls) {
        WallClassification classification = classifyWallRelativeToLine(wall, splitter);
        
        switch (classification) {
            case FRONT:
                frontWalls.add(wall);
                break;
            case BACK:
                backWalls.add(wall);
                break;
            case COLINEAR:
                colinearWalls.add(wall);
                break;
            case SPANNING:
                Vector2D intersection = wall.getLine().intersect(splitter);
                if (intersection != null) {
                    Wall frontWall = new Wall(wall.getLine().start, intersection);
                    Wall backWall = new Wall(intersection, wall.getLine().end);
                    
                    copyWallProperties(wall, frontWall);
                    copyWallProperties(wall, backWall);
                    
                    if (splitter.whichSide(wall.getLine().start) > 0) {
                        frontWalls.add(frontWall);
                        backWalls.add(backWall);
                    } else {
                        frontWalls.add(backWall);
                        backWalls.add(frontWall);
                    }
                }
                break;
        }
    }
    
    private static void copyWallProperties(Wall source, Wall target) {
        target.setTextureId(source.getTextureId());
        target.setSolid(source.isSolid());
        target.setFrontSector(source.getFrontSector());
        target.setBackSector(source.getBackSector());
    }
    
    public void traverse(Vector2D viewPoint, BSPTraversalCallback callback) {
        if (isLeaf) {
            for (Wall wall : walls) {
                callback.processWall(wall);
            }
        } else {
            double side = splitter.whichSide(viewPoint);
            
            if (side >= 0) {
                if (backChild != null) {
                    backChild.traverse(viewPoint, callback);
                }
                if (frontChild != null) {
                    frontChild.traverse(viewPoint, callback);
                }
            } else {
                if (frontChild != null) {
                    frontChild.traverse(viewPoint, callback);
                }
                if (backChild != null) {
                    backChild.traverse(viewPoint, callback);
                }
            }
        }
    }
    
    public Wall raycast(Ray2D ray, double maxDistance) {
        return raycastRecursive(ray, maxDistance, 0);
    }
    
    private Wall raycastRecursive(Ray2D ray, double maxDistance, double currentDistance) {
        if (currentDistance >= maxDistance) {
            return null;
        }
        
        if (isLeaf) {
            Wall closestWall = null;
            double closestDistance = maxDistance;
            
            for (Wall wall : walls) {
                Line2D.IntersectionResult intersection = wall.getLine().intersectRay(ray);
                if (intersection != null && intersection.distance < closestDistance) {
                    closestDistance = intersection.distance;
                    closestWall = wall;
                }
            }
            
            return closestWall;
        } else {
            double startSide = splitter.whichSide(ray.origin);
            
            BSPNode firstChild, secondChild;
            if (startSide >= 0) {
                firstChild = frontChild;
                secondChild = backChild;
            } else {
                firstChild = backChild;
                secondChild = frontChild;
            }
            
            if (firstChild != null) {
                Wall result = firstChild.raycastRecursive(ray, maxDistance, currentDistance);
                if (result != null) {
                    return result;
                }
            }
            
            Line2D.IntersectionResult intersection = splitter.intersectRay(ray);
            if (intersection != null && intersection.distance < maxDistance) {
                if (secondChild != null) {
                    return secondChild.raycastRecursive(ray, maxDistance, intersection.distance);
                }
            }
            
            return null;
        }
    }
    
    public boolean isLeaf() { return isLeaf; }
    public List<Wall> getWalls() { return walls; }
    public Line2D getSplitter() { return splitter; }
    public BSPNode getFrontChild() { return frontChild; }
    public BSPNode getBackChild() { return backChild; }
    
    private enum WallClassification {
        FRONT, BACK, COLINEAR, SPANNING
    }
    
    public interface BSPTraversalCallback {
        void processWall(Wall wall);
    }
}