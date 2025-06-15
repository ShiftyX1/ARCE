package com.arce.core;

import com.arce.logger.EngineLogger;
import com.arce.world.GameMap;
import com.arce.player.Player;
import com.arce.render.Camera;
import com.arce.render.RayCaster;
import com.arce.render.Renderer;
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
            window = new Window(
                config.getWindowWidth(), 
                config.getWindowHeight(), 
                config.getWindowTitle()
            );
            
            gameMap = GameMap.createTestMap();
            logger.logSuccess("Game map created");
            
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
            
            renderer = new Renderer(config.getWindowWidth(), config.getWindowHeight());
            
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
                        logger.logDebug("FPS: " + frames + ", Player: " + player);
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
    }
    
    private void render() {
        if (renderer != null && rayCaster != null && player != null) {
            RayCaster.RaycastColumn[] columns = rayCaster.castRays(player.getCamera());
            
            BufferedImage frame;
            
            if (window != null && window.isShowTopDownMap()) {
                frame = renderer.renderTopDownView(player.getCamera(), rayCaster);
            } else {
                frame = renderer.renderFrame(columns, player.getCamera());
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
            
            config.saveConfig();
            
            logger.logSuccess("Engine shutdown completed");
            logger.logEnd("shutdown");
        }
    }
    
    public EngineConfig getConfig() { return config; }
    public Player getPlayer() { return player; }
    public GameMap getGameMap() { return gameMap; }
}