package com.arce;

import com.arce.core.Engine;
import com.arce.logger.EngineLogger;

public class Main {
    private static final EngineLogger logger = new EngineLogger(Main.class);
    
    public static void main(String[] args) {
        logger.logInfo("Initializing ARCE Engine...");
        
        try {
            Engine engine = new Engine();
            engine.start();
        } catch (Exception e) {
            logger.logError("Failed to start engine", e);
            System.exit(1);
        }
        
        logger.logInfo("ARCE Engine terminated");
    }
}