package com.arce.core;

import com.arce.logger.EngineLogger;
import com.arce.world.GameMap;
import com.arce.player.Player;
import com.arce.render.RayCaster;
import com.arce.render.Renderer;
import com.arce.core.managers.AssetManager;
import com.arce.core.managers.SpriteManager;
import com.arce.entities.Sprite;
import com.arce.math.Vector2D;
import java.awt.image.BufferedImage;

public class Engine {
    private final EngineLogger logger;
    private final EngineConfig config;
    private volatile boolean running = false;
    private Window window;
    
    private GameMap gameMap;
    private Player player;
    private RayCaster rayCaster;
    private Renderer renderer;
    
    private AssetManager assetManager;
    private SpriteManager spriteManager;
    
    private long lastUpdateTime;
    private double deltaTime;

    public Engine() {
        this.logger = new EngineLogger(Engine.class);
        this.config = new EngineConfig();
        
        logger.logInfo("Engine initialized with config:");
        logger.logInfo("  Window: {}x{}", config.getWindowWidth(), config.getWindowHeight());
        logger.logInfo("  Target FPS: {}", config.getTargetFPS());
        logger.logInfo("  Debug mode: {}", config.isDebugMode());
    }

    public void start() {
        logger.logStart("start");
        
        if (!initialize()) {
            logger.logError("Failed to initialize engine", new RuntimeException("Initialization failed"));
            return;
        }
        
        running = true;
        logger.logSuccess("Engine started successfully");
        
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
        run();
        
        logger.logEnd("start");
    }
    
    private boolean initialize() {
        logger.logStart("initialize");
        
        try {
            initializeManagers();
            
            window = new Window(
                config.getWindowWidth(), 
                config.getWindowHeight(), 
                config.getWindowTitle()
            );
            
            gameMap = GameMap.createTestMap();
            logger.logSuccess("Game map created");
            
            spriteManager = new SpriteManager(gameMap);
            
            player = new Player(
                gameMap.getPlayerStartPosition(),
                gameMap.getPlayerStartAngle(),
                gameMap,
                config.getWindowWidth(),
                config.getWindowHeight()
            );
            
            player.setMoveSpeed(config.getPlayerMoveSpeed());
            player.setTurnSpeed(config.getPlayerTurnSpeed());

            rayCaster = new RayCaster(gameMap);
            rayCaster.setMaxRenderDistance(config.getRenderDistance());
            rayCaster.setSpriteManager(spriteManager);
            
            renderer = new Renderer(config.getWindowWidth(), config.getWindowHeight());
            renderer.setAssetManager(assetManager);
            
            createTestSprites();
            
            window.show();
            
            lastUpdateTime = System.nanoTime();
            
            logger.logSuccess("Engine initialization completed");
            logger.logEnd("initialize");
            return true;
            
        } catch (Exception e) {
            logger.logError("Engine initialization failed", e);
            logger.logEnd("initialize");
            return false;
        }
    }
    
    private void initializeManagers() {
        logger.logStart("initializeManagers");
        
        assetManager = new AssetManager();
        
        assetManager.loadTexture("wall_1", "wall_1.png");
        // assetManager.loadTexture("wood_wall", "wood.png");
        // assetManager.loadTexture("enemy_sprite", "enemy.png");
        // assetManager.loadTexture("torch_sprite", "torch.png");
        
        assetManager.loadTexture("wall_2", "wall_2.png");
        assetManager.loadTexture("wall_3", "wall_3.png");
        assetManager.loadTexture("wall_4", "wall_4.png");
        assetManager.loadTexture("wall_5", "wall_5.png");
        assetManager.loadTexture("wall_6", "wall_6.png");
        assetManager.loadTexture("wall_7", "wall_7.png");
        assetManager.loadTexture("wall_8", "wall_8.png");
        
        assetManager.createProceduralTexture("checker", (name, size) -> {
            return createCheckerTexture(name, size);
        }, 64);
        
        logger.logSuccess("Asset manager initialized with " + assetManager.getTextureCount() + " textures");
        logger.logEnd("initializeManagers");
    }
    
    private com.arce.assets.Texture createCheckerTexture(String name, int size) {
        java.awt.image.BufferedImage image = new java.awt.image.BufferedImage(
            size, size, java.awt.image.BufferedImage.TYPE_INT_RGB);
        java.awt.Graphics2D g = image.createGraphics();
        
        int checkSize = size / 8;
        for (int x = 0; x < size; x += checkSize) {
            for (int y = 0; y < size; y += checkSize) {
                boolean isEven = ((x / checkSize) + (y / checkSize)) % 2 == 0;
                g.setColor(isEven ? java.awt.Color.WHITE : java.awt.Color.BLACK);
                g.fillRect(x, y, checkSize, checkSize);
            }
        }
        
        g.dispose();
        return new com.arce.assets.Texture(name, image);
    }
    
    private void createTestSprites() {
        logger.logStart("createTestSprites");
        
        spriteManager.createSprite("torch1", new Vector2D(120, 80), "red_wall");
        spriteManager.createSprite("torch2", new Vector2D(280, 80), "blue_wall");
        spriteManager.createSprite("pillar", new Vector2D(200, 200), "brick");
        
        String[] fireFrames = {"red_wall", "yellow_wall", "red_wall", "yellow_wall"};
        spriteManager.createAnimatedSprite("fire", new Vector2D(160, 160), fireFrames, 4.0);
        
        for (Sprite sprite : spriteManager.getAllSprites()) {
            sprite.setWidth(24);
            sprite.setSpriteHeight(32);
            sprite.setHeight(0);
        }
        
        logger.logSuccess("Created " + spriteManager.getSpriteCount() + " test sprites");
        logger.logEnd("createTestSprites");
    }
    
    private void run() {
        logger.logStart("run");
        
        long lastTime = System.nanoTime();
        double nsPerTick = 1_000_000_000.0 / config.getTargetFPS();
        double delta = 0;
        int frames = 0;
        long timer = System.currentTimeMillis();
        
        try {
            while (running) {
                long now = System.nanoTime();
                delta += (now - lastTime) / nsPerTick;
                lastTime = now;
                
                deltaTime = (now - lastUpdateTime) / 1_000_000_000.0;
                lastUpdateTime = now;
                
                if (window != null && window.isCloseRequested()) {
                    logger.logInfo("Window close detected, shutting down engine");
                    break;
                }
                
                if (delta >= 1) {
                    update();
                    render();
                    frames++;
                    delta--;
                }
                
                if (config.isShowFPS() && System.currentTimeMillis() - timer >= 1000) {
                    if (config.isDebugMode()) {
                        logger.logDebug("FPS: " + frames + ", Player: " + player + 
                                       ", Sprites: " + spriteManager.getSpriteCount());
                    }
                    frames = 0;
                    timer += 1000;
                }
                
                Thread.sleep(1);
            }
        } catch (InterruptedException e) {
            logger.logError("Engine interrupted", e);
            Thread.currentThread().interrupt();
        }
        
        shutdown();
        System.exit(0);
        
        logger.logEnd("run");
    }
    
    private void update() {
        if (player != null && window != null) {
            boolean[] keys = window.getKeyStates();
            player.update(keys, deltaTime);
        }
        
        if (spriteManager != null) {
            spriteManager.update(deltaTime);
        }
    }
    
    private void render() {
        if (renderer != null && rayCaster != null && player != null) {
            RayCaster.RaycastColumn[] columns = rayCaster.castRays(player.getCamera());
            
            BufferedImage frame;
            
            if (window != null && window.isShowTopDownMap()) {
                frame = renderer.renderTopDownView(player.getCamera(), rayCaster, spriteManager);
            } else {
                frame = renderer.renderFrame(columns, player.getCamera(), spriteManager);
            }
            
            if (window != null) {
                window.displayFrame(frame);
            }
        }
    }
    
    public void shutdown() {
        if (running) {
            logger.logStart("shutdown");
            running = false;
            
            if (window != null) {
                window.hide();
            }
            
            if (assetManager != null) {
                assetManager.unloadAll();
            }
            
            if (spriteManager != null) {
                spriteManager.clear();
            }
            
            config.saveConfig();
            
            logger.logSuccess("Engine shutdown completed");
            logger.logEnd("shutdown");
        }
    }
    
    public EngineConfig getConfig() { return config; }
    public Player getPlayer() { return player; }
    public GameMap getGameMap() { return gameMap; }
    public AssetManager getAssetManager() { return assetManager; }
    public SpriteManager getSpriteManager() { return spriteManager; }
    public Renderer getRenderer() { return renderer; }
    public RayCaster getRayCaster() { return rayCaster; }
}