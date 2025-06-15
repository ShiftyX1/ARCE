package com.arce.logger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EngineLogger {
    public final Logger logger;

    public EngineLogger(Class<?> clazz) {
        this.logger = LoggerFactory.getLogger(clazz);
    }

    public void logStart(String methodName) {
        logger.info("Starting method: {}", methodName);
    }

    public void logEnd(String methodName) {
        logger.info("Ending method: {}", methodName);
    }

    public void logError(String message, Exception e) {
        logger.error("ERROR: {} | Details: {}", message, e.getMessage(), e);
    }
    
    public void logSuccess(String operation) {
        logger.info("Success: {}", operation);
    }
    
    // Используем args, потому-что я без понятия че там еще будем передавать в логгер
    public void logInfo(String message, Object... args) {
        logger.info(message, args);
    }
    
    public void logDebug(String message) {
        logger.debug(message);
    }
}