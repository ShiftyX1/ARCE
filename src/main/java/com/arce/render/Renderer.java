package com.arce.render;

import com.arce.world.Wall;
import com.arce.assets.Texture;
import com.arce.core.managers.AssetManager;
import com.arce.core.managers.SpriteManager;
import com.arce.entities.Sprite;
import com.arce.math.Vector2D;
import com.arce.logger.EngineLogger;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.List;

public class Renderer {
    private final EngineLogger logger;
    private BufferedImage frameBuffer;
    private Graphics2D graphics;
    private int screenWidth;
    private int screenHeight;
    private boolean showDebugInfo = true;
    
    private AssetManager assetManager;
    
    private double[] depthBuffer;

    private Color[] wallColors = {
        Color.GRAY,
        Color.RED,
        Color.BLUE,
        Color.GREEN,
        Color.YELLOW,
        Color.MAGENTA,
        Color.CYAN,
        Color.ORANGE
    };
    
    public Renderer(int screenWidth, int screenHeight) {
        this.logger = new EngineLogger(Renderer.class);
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.depthBuffer = new double[screenWidth];
        
        initializeFrameBuffer();
        logger.logSuccess("Renderer initialized: " + screenWidth + "x" + screenHeight);
    }

    public void setAssetManager(AssetManager assetManager) {
        this.assetManager = assetManager;
        logger.logInfo("Asset manager connected to renderer");
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
    
    public BufferedImage renderFrame(RayCaster.RaycastColumn[] columns, Camera camera, SpriteManager spriteManager) {
        clearScreen();
        clearDepthBuffer();
        renderSkyAndFloor();
        renderWalls(columns);
        renderSprites(camera, spriteManager);
        
        if (showDebugInfo) {
            renderDebugInfo(camera, spriteManager);
        }
        
        return frameBuffer;
    }
    
    public BufferedImage renderFrame(RayCaster.RaycastColumn[] columns, Camera camera) {
        return renderFrame(columns, camera, null);
    }
    
    private void clearScreen() {
        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, screenWidth, screenHeight);
    }
    
    private void clearDepthBuffer() {
        for (int i = 0; i < depthBuffer.length; i++) {
            depthBuffer[i] = Double.MAX_VALUE;
        }
    }
    
    private void renderSkyAndFloor() {
        int horizon = screenHeight / 2;
        
        // Небо (верхняя половина)
        graphics.setColor(new Color(135, 206, 235)); // Светло-голубой
        graphics.fillRect(0, 0, screenWidth, horizon);
        
        // Пол (нижняя половина)
        graphics.setColor(new Color(139, 69, 19)); // Коричневый
        graphics.fillRect(0, horizon, screenWidth, screenHeight - horizon);
    }
    
    private void renderWalls(RayCaster.RaycastColumn[] columns) {
        for (int x = 0; x < columns.length; x++) {
            RayCaster.RaycastColumn column = columns[x];
            
            if (column.hasHit()) {
                renderWallColumn(x, column);
                if (x < depthBuffer.length) {
                    depthBuffer[x] = column.distance;
                }
            }
        }
    }
    
    private void renderWallColumn(int x, RayCaster.RaycastColumn column) {
        Wall wall = column.hitWall;
        
        if (assetManager != null) {
            renderTexturedWallColumn(x, column);
        } else {
            renderColoredWallColumn(x, column);
        }
    }
    
    // ОПТИМИЗИРОВАННАЯ версия рендеринга текстурированной стены
    private void renderTexturedWallColumn(int x, RayCaster.RaycastColumn column) {
        Wall wall = column.hitWall;
        Texture texture = assetManager.getTextureById(wall.getTextureId());
        
        int wallHeight = column.wallBottom - column.wallTop;
        
        int textureWidth = texture.getWidth();
        int textureHeight = texture.getHeight();
        
        double worldWallHeight = column.currentSector != null ? column.currentSector.getWallHeight() : 64.0;
        
        int[] frameBufferData = ((DataBufferInt) frameBuffer.getRaster().getDataBuffer()).getData();
        
        double uCoord = column.wallTextureX / textureWidth;
        double vStep = worldWallHeight / (wallHeight * textureHeight);
        double vStart = 0.0;
        
        double shadingFactor = Math.max(0.2, 1.0 - (column.distance / 200.0));
        
        boolean isHorizontal = isHorizontalWall(wall);
        double horizontalDarkening = isHorizontal ? 0.8 : 1.0;
        
        for (int y = column.wallTop; y <= column.wallBottom; y++) {
            if (y >= 0 && y < screenHeight) {
                double v = vStart + (y - column.wallTop) * vStep;
                
                int pixelRgb = texture.getPixelRGB(uCoord, v);
                
                int r = (int) (((pixelRgb >> 16) & 0xFF) * shadingFactor * horizontalDarkening);
                int g = (int) (((pixelRgb >>  8) & 0xFF) * shadingFactor * horizontalDarkening);
                int b = (int) ((pixelRgb & 0xFF) * shadingFactor * horizontalDarkening);
                
                r = Math.max(0, Math.min(255, r));
                g = Math.max(0, Math.min(255, g));
                b = Math.max(0, Math.min(255, b));
                
                int finalRgb = (r << 16) | (g << 8) | b;
                frameBufferData[y * screenWidth + x] = finalRgb;
            }
        }
    }
    
    private void renderColoredWallColumn(int x, RayCaster.RaycastColumn column) {
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
    
    private void renderSprites(Camera camera, SpriteManager spriteManager) {
        if (spriteManager == null) return;
        
        List<Sprite> visibleSprites = spriteManager.getVisibleSprites(camera);
        
        for (Sprite sprite : visibleSprites) {
            renderSprite(sprite, camera);
        }
    }
    
    private void renderSprite(Sprite sprite, Camera camera) {
        Vector2D spritePos = sprite.getPosition();
        Vector2D cameraPos = camera.getPosition();
        Vector2D cameraDir = camera.getDirection();
        Vector2D cameraPlane = camera.getCameraPlane();
        
        Vector2D spriteVector = spritePos.subtract(cameraPos);
        
        double distance = spriteVector.length();
        if (distance < 0.1) return;
        
        double invDet = 1.0 / (cameraPlane.x * cameraDir.y - cameraDir.x * cameraPlane.y);
        double transformX = invDet * (cameraDir.y * spriteVector.x - cameraDir.x * spriteVector.y);
        double transformY = invDet * (-cameraPlane.y * spriteVector.x + cameraPlane.x * spriteVector.y);
        
        if (transformY <= 0) return;
        
        int spriteScreenX = (int)((screenWidth / 2) * (1 + transformX / transformY));
        
        int spriteHeight = Math.abs((int)(screenHeight / transformY));
        int spriteWidth = Math.abs((int)(screenHeight / transformY));
        
        int drawStartY = Math.max(0, (-spriteHeight / 2) + (screenHeight / 2));
        int drawEndY = Math.min(screenHeight - 1, (spriteHeight / 2) + (screenHeight / 2));
        int drawStartX = Math.max(0, (-spriteWidth / 2) + spriteScreenX);
        int drawEndX = Math.min(screenWidth - 1, (spriteWidth / 2) + spriteScreenX);
        
        if (assetManager != null) {
            renderTexturedSprite(sprite, drawStartX, drawEndX, drawStartY, drawEndY, 
                               spriteWidth, spriteHeight, transformY);
        } else {
            renderColoredSprite(sprite, drawStartX, drawEndX, drawStartY, drawEndY);
        }
    }
    
    // ОПТИМИЗИРОВАННАЯ версия рендеринга текстурированного спрайта
    private void renderTexturedSprite(Sprite sprite, int startX, int endX, int startY, int endY,
                                    int spriteWidth, int spriteHeight, double distance) {
        String textureName = sprite.getCurrentTextureName();
        Texture texture = assetManager.getTexture(textureName);
        
        int[] frameBufferData = ((DataBufferInt) frameBuffer.getRaster().getDataBuffer()).getData();
        
        double shadingFactor = Math.max(0.2, 1.0 - (distance / 200.0));
        
        for (int x = startX; x < endX; x++) {
            if (x >= 0 && x < depthBuffer.length && distance < depthBuffer[x]) {
                
                double u = (double)(x - startX) / spriteWidth;
                
                for (int y = startY; y < endY; y++) {
                    if (y >= 0 && y < screenHeight) {
                        double v = (double)(y - startY) / spriteHeight;
                        
                        int pixelRgb = texture.getPixelRGB(u, v);
                        
                        int red = (pixelRgb >> 16) & 0xFF;
                        int green = (pixelRgb >> 8) & 0xFF;
                        int blue = pixelRgb & 0xFF;
                        
                        if (red < 10 && green < 10 && blue < 10) {
                            continue;
                        }
                        
                        int r = (int) (red * shadingFactor);
                        int g = (int) (green * shadingFactor);
                        int b = (int) (blue * shadingFactor);
                        
                        r = Math.max(0, Math.min(255, r));
                        g = Math.max(0, Math.min(255, g));
                        b = Math.max(0, Math.min(255, b));
                        
                        int rgb = (r << 16) | (g << 8) | b;
                        frameBufferData[y * screenWidth + x] = rgb;
                    }
                }
            }
        }
    }
    
    private void renderColoredSprite(Sprite sprite, int startX, int endX, int startY, int endY) {
        graphics.setColor(Color.MAGENTA);
        graphics.fillOval(startX, startY, endX - startX, endY - startY);
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
    
    private double calculateShadingFactor(double distance) {
        double maxDistance = 200.0;
        return Math.max(0.2, 1.0 - (distance / maxDistance));
    }
    
    private boolean isHorizontalWall(Wall wall) {
        double dx = Math.abs(wall.getLine().end.x - wall.getLine().start.x);
        double dy = Math.abs(wall.getLine().end.y - wall.getLine().start.y);
        return dx > dy;
    }

    private void renderDebugInfo(Camera camera, SpriteManager spriteManager) {
        if (!showDebugInfo) return;
        
        graphics.setColor(Color.WHITE);
        graphics.setFont(new Font("Arial", Font.PLAIN, 12));
        
        String posInfo = String.format("Pos: (%.1f, %.1f)", camera.getPosition().x, camera.getPosition().y);
        graphics.drawString(posInfo, 10, 20);
        
        String angleInfo = String.format("Angle: %.1f°", Math.toDegrees(camera.getAngle()));
        graphics.drawString(angleInfo, 10, 35);
        
        String fovInfo = String.format("FOV: %.1f°", Math.toDegrees(camera.getFov()));
        graphics.drawString(fovInfo, 10, 50);
        
        if (spriteManager != null) {
            String spriteInfo = String.format("Sprites: %d", spriteManager.getSpriteCount());
            graphics.drawString(spriteInfo, 10, 65);
            
            if (assetManager != null) {
                String textureInfo = String.format("Textures: %d", assetManager.getTextureCount());
                graphics.drawString(textureInfo, 10, 80);
            }
        }
        
        graphics.drawString("WASD - Move, Q/E - Turn, M - Map, ESC - Exit", 10, screenHeight - 20);
        
        int centerX = screenWidth / 2;
        int centerY = screenHeight / 2;
        graphics.setColor(Color.RED);
        graphics.drawLine(centerX - 5, centerY, centerX + 5, centerY);
        graphics.drawLine(centerX, centerY - 5, centerX, centerY + 5);
    }
    
    // 2D вид с поддержкой спрайтов (рендерим внутриигровую карту)
    public BufferedImage renderTopDownView(Camera camera, RayCaster rayCaster, SpriteManager spriteManager) {
        BufferedImage topView = new BufferedImage(400, 400, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = topView.createGraphics();
        
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, 400, 400);
        
        g.setColor(Color.WHITE);
        for (Wall wall : rayCaster.getGameMap().getWalls()) {
            int x1 = (int) (wall.getLine().start.x);
            int y1 = (int) (wall.getLine().start.y);
            int x2 = (int) (wall.getLine().end.x);
            int y2 = (int) (wall.getLine().end.y);
            g.drawLine(x1, y1, x2, y2);
        }
        
        if (spriteManager != null) {
            g.setColor(Color.CYAN);
            for (Sprite sprite : spriteManager.getAllSprites()) {
                int spriteX = (int) sprite.getPosition().x;
                int spriteY = (int) sprite.getPosition().y;
                g.fillOval(spriteX - 3, spriteY - 3, 6, 6);
                
                g.setColor(Color.YELLOW);
                g.setFont(new Font("Arial", Font.PLAIN, 8));
                g.drawString(sprite.getName(), spriteX + 5, spriteY);
                g.setColor(Color.CYAN);
            }
        }
        
        g.setColor(Color.RED);
        int playerX = (int) camera.getPosition().x;
        int playerY = (int) camera.getPosition().y;
        g.fillOval(playerX - 3, playerY - 3, 6, 6);
        
        g.setColor(Color.YELLOW);
        int dirX = playerX + (int) (Math.cos(camera.getAngle()) * 20);
        int dirY = playerY + (int) (Math.sin(camera.getAngle()) * 20);
        g.drawLine(playerX, playerY, dirX, dirY);
        
        g.dispose();
        return topView;
    }
    
    public BufferedImage renderTopDownView(Camera camera, RayCaster rayCaster) {
        return renderTopDownView(camera, rayCaster, null);
    }
    
    public void resize(int newWidth, int newHeight) {
        if (newWidth != screenWidth || newHeight != screenHeight) {
            screenWidth = newWidth;
            screenHeight = newHeight;
            
            depthBuffer = new double[screenWidth];
            
            graphics.dispose();
            initializeFrameBuffer();
            
            logger.logInfo("Renderer resized to: " + screenWidth + "x" + screenHeight);
        }
    }
    
    public int getScreenWidth() { return screenWidth; }
    public int getScreenHeight() { return screenHeight; }
    public BufferedImage getFrameBuffer() { return frameBuffer; }
    
    public BufferedImage renderNoMapScreen() {
        clearScreen();
        
        graphics.setColor(new Color(32, 32, 48)); // Темно-синий фон
        graphics.fillRect(0, 0, screenWidth, screenHeight);
        
        graphics.setColor(Color.WHITE);
        graphics.setFont(new Font("Arial", Font.BOLD, 32));
        FontMetrics fm = graphics.getFontMetrics();
        String title = "ARCE Engine";
        int titleX = (screenWidth - fm.stringWidth(title)) / 2;
        graphics.drawString(title, titleX, screenHeight / 2 - 100);
        
        graphics.setColor(Color.CYAN);
        graphics.setFont(new Font("Arial", Font.PLAIN, 18));
        fm = graphics.getFontMetrics();
        String instruction = "Press ` (tilde) to open the console";
        int instX = (screenWidth - fm.stringWidth(instruction)) / 2;
        graphics.drawString(instruction, instX, screenHeight / 2 - 40);
        
        graphics.setColor(Color.LIGHT_GRAY);
        graphics.setFont(new Font("Arial", Font.PLAIN, 14));
        fm = graphics.getFontMetrics();
        
        String[] commands = {
            "Console commands:",
            "maps - list maps",
            "map <name> - load map", 
            "testmap - load test map",
            "help - show commands"
        };
        
        int startY = screenHeight / 2 + 20;
        for (int i = 0; i < commands.length; i++) {
            int cmdX = (screenWidth - fm.stringWidth(commands[i])) / 2;
            graphics.drawString(commands[i], cmdX, startY + i * 20);
        }
        
        long time = System.currentTimeMillis();
        int dots = (int)((time / 500) % 4);
        String loading = "Waiting for map to load" + ".".repeat(dots);
        graphics.setColor(Color.YELLOW);
        graphics.setFont(new Font("Arial", Font.ITALIC, 16));
        fm = graphics.getFontMetrics();
        int loadX = (screenWidth - fm.stringWidth(loading)) / 2;
        graphics.drawString(loading, loadX, screenHeight - 60);
        
        return frameBuffer;
    }

    @Override
    protected void finalize() {
        if (graphics != null) {
            graphics.dispose();
        }
    }
}