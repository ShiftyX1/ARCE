package com.arce.core.console;

public interface Command {
    /**
     * Выполняет команду
     * @param args аргументы команды
     * @return результат выполнения команды
     */
    String execute(String[] args);
    
    /**
     * Получает описание команды
     * @return описание команды
     */
    String getDescription();
    
    /**
     * Получает синтаксис использования команды
     * @return синтаксис команды
     */
    String getUsage();
} 