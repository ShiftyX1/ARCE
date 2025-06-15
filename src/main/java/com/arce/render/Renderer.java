package com.arce.render;

import com.arce.world.Wall;
import com.arce.world.Sector;
import com.arce.logger.EngineLogger;
import java.awt.*;
import java.awt.image.BufferedImage;

public class Renderer {
    private final EngineLogger logger;
    private BufferedImage frameBuffer;
    private Graphics2D graphics;
    private int screenWidth;
    private int screenHeight;
    private boolean showDebugInfo = true;

    private Color[] wallColors = {
        Color.GRAY,      // 0 - по умолчанию
        Color.RED,       // 1 - красная стена
        Color.BLUE,      // 2 - синяя стена
        Color.GREEN,     // 3 - зеленая стена
        Color.YELLOW,    // 4 - желтая стена
        Color.MAGENTA,   // 5 - пурпурная стена
        Color.CYAN,      // 6 - голубая стена
        Color.ORANGE     // 7 - оранжевая стена
    };
    
    public Renderer(int screenWidth, int screenHeight) {
        this.logger = new EngineLogger(Renderer.class);
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        
        initializeFrameBuffer();
        logger.logSuccess("Renderer initialized: " + screenWidth + "x" + screenHeight);
    }

    public void setShowDebugInfo(boolean show) {
        this.showDebugInfo = show;
    }
    
    private void initializeFrameBuffer() {
        frameBuffer = new BufferedImage(screenWidth, screenHeight, BufferedImage.TYPE_INT_RGB);
        graphics = frameBuffer.createGraphics();
        
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
    }
    
    public BufferedImage renderFrame(RayCaster.RaycastColumn[] columns, Camera camera) {
        clearScreen();
        renderSkyAndFloor();
        renderWalls(columns);
        
        if (showDebugInfo) {
            renderDebugInfo(camera);
        }
        
        return frameBuffer;
    }
    
    private void clearScreen() {
        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, screenWidth, screenHeight);
    }
    
    private void renderSkyAndFloor() {
        int horizon = screenHeight / 2;
        
        graphics.setColor(new Color(135, 206, 235)); // Светло-голубой
        graphics.fillRect(0, 0, screenWidth, horizon);
        
        graphics.setColor(new Color(139, 69, 19)); // Коричневый
        graphics.fillRect(0, horizon, screenWidth, screenHeight - horizon);
    }
    
    private void renderWalls(RayCaster.RaycastColumn[] columns) {
        for (int x = 0; x < columns.length; x++) {
            RayCaster.RaycastColumn column = columns[x];
            
            if (column.hasHit()) {
                renderWallColumn(x, column);
            }
        }
    }
    
    private void renderWallColumn(int x, RayCaster.RaycastColumn column) {
        Wall wall = column.hitWall;
        
        Color wallColor = getWallColor(wall);
        
        wallColor = applyDistanceShading(wallColor, column.distance);
        
        if (isHorizontalWall(wall)) {
            wallColor = wallColor.darker();
        }
        
        graphics.setColor(wallColor);
        graphics.drawLine(x, column.wallTop, x, column.wallBottom);
        
        renderWallTexture(x, column, wallColor);
    }
    
    private void renderWallTexture(int x, RayCaster.RaycastColumn column, Color baseColor) {
        double texturePos = column.wallTextureX * 64;
        
        if ((int) texturePos % 8 < 4) {
            Color darkerColor = new Color(
                Math.max(0, baseColor.getRed() - 30),
                Math.max(0, baseColor.getGreen() - 30),
                Math.max(0, baseColor.getBlue() - 30)
            );
            graphics.setColor(darkerColor);
            graphics.drawLine(x, column.wallTop, x, column.wallBottom);
        }
    }
    
    private Color getWallColor(Wall wall) {
        int textureId = wall.getTextureId();
        if (textureId >= 0 && textureId < wallColors.length) {
            return wallColors[textureId];
        }
        return wallColors[0];
    }
    
    private Color applyDistanceShading(Color color, double distance) {      
        double maxDistance = 200.0;
        double factor = Math.max(0.2, 1.0 - (distance / maxDistance));
        
        int r = (int) (color.getRed() * factor);
        int g = (int) (color.getGreen() * factor);
        int b = (int) (color.getBlue() * factor);
        
        return new Color(
            Math.max(0, Math.min(255, r)),
            Math.max(0, Math.min(255, g)),
            Math.max(0, Math.min(255, b))
        );
    }
    
    private boolean isHorizontalWall(Wall wall) {
        double dx = Math.abs(wall.getLine().end.x - wall.getLine().start.x);
        double dy = Math.abs(wall.getLine().end.y - wall.getLine().start.y);
        return dx > dy;
    }

    private void renderDebugInfo(Camera camera) {
        if (!showDebugInfo) return;
        
        graphics.setColor(Color.WHITE);
        graphics.setFont(new Font("Arial", Font.PLAIN, 12));
        
        String posInfo = String.format("Pos: (%.1f, %.1f)", camera.getPosition().x, camera.getPosition().y);
        graphics.drawString(posInfo, 10, 20);
        
        String angleInfo = String.format("Angle: %.1f°", Math.toDegrees(camera.getAngle()));
        graphics.drawString(angleInfo, 10, 35);
        
        String fovInfo = String.format("FOV: %.1f°", Math.toDegrees(camera.getFov()));
        graphics.drawString(fovInfo, 10, 50);
        
        graphics.drawString("WASD - Move, Q/E - Turn, M - Map, ESC - Exit", 10, screenHeight - 20);
        
        int centerX = screenWidth / 2;
        int centerY = screenHeight / 2;
        graphics.setColor(Color.RED);
        graphics.drawLine(centerX - 5, centerY, centerX + 5, centerY);
        graphics.drawLine(centerX, centerY - 5, centerX, centerY + 5);
    }
    
    public BufferedImage renderTopDownView(Camera camera, RayCaster rayCaster) {
        BufferedImage topView = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = topView.createGraphics();
        
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, 200, 200);
        
        g.setColor(Color.WHITE);
        for (Wall wall : rayCaster.getGameMap().getWalls()) {
            int x1 = (int) (wall.getLine().start.x / 2);
            int y1 = (int) (wall.getLine().start.y / 2);
            int x2 = (int) (wall.getLine().end.x / 2);
            int y2 = (int) (wall.getLine().end.y / 2);
            g.drawLine(x1, y1, x2, y2);
        }
        
        g.setColor(Color.RED);
        int playerX = (int) (camera.getPosition().x / 2);
        int playerY = (int) (camera.getPosition().y / 2);
        g.fillOval(playerX - 2, playerY - 2, 4, 4);

        g.setColor(Color.YELLOW);
        int dirX = playerX + (int) (Math.cos(camera.getAngle()) * 10);
        int dirY = playerY + (int) (Math.sin(camera.getAngle()) * 10);
        g.drawLine(playerX, playerY, dirX, dirY);
        
        g.dispose();
        return topView;
    }
    
    public void resize(int newWidth, int newHeight) {
        if (newWidth != screenWidth || newHeight != screenHeight) {
            screenWidth = newWidth;
            screenHeight = newHeight;
            
            graphics.dispose();
            initializeFrameBuffer();
            
            logger.logInfo("Renderer resized to: " + screenWidth + "x" + screenHeight);
        }
    }
    
    public int getScreenWidth() { return screenWidth; }
    public int getScreenHeight() { return screenHeight; }
    public BufferedImage getFrameBuffer() { return frameBuffer; }
    
    @Override
    protected void finalize() {
        if (graphics != null) {
            graphics.dispose();
        }
    }
}