package com.arce.core.console;

import com.arce.logger.EngineLogger;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CommandProcessor {
    private final EngineLogger logger;
    private final Map<String, Command> commands;
    
    public CommandProcessor() {
        this.logger = new EngineLogger(CommandProcessor.class);
        this.commands = new HashMap<>();
        
        registerCommand("help", new HelpCommand(this));
        registerCommand("clear", new ClearCommand());
        
        logger.logInfo("CommandProcessor initialized");
    }
    
    public void registerCommand(String name, Command command) {
        commands.put(name.toLowerCase(), command);
        logger.logDebug("Registered command: " + name);
    }
    
    public String processCommand(String input) {
        if (input == null || input.trim().isEmpty()) {
            return "";
        }
        
        String[] parts = input.trim().split("\\s+");
        String commandName = parts[0].toLowerCase();
        String[] args = new String[parts.length - 1];
        System.arraycopy(parts, 1, args, 0, args.length);
        
        Command command = commands.get(commandName);
        if (command != null) {
            try {
                return command.execute(args);
            } catch (Exception e) {
                logger.logError("Error executing command: " + commandName, e);
                return "Error executing command: " + e.getMessage();
            }
        } else {
            return "Unknown command: " + commandName + ". Type 'help' for command list.";
        }
    }
    
    public Set<String> getCommandNames() {
        return commands.keySet();
    }
    
    public Command getCommand(String name) {
        return commands.get(name.toLowerCase());
    }
    
    private static class HelpCommand implements Command {
        private final CommandProcessor processor;
        
        public HelpCommand(CommandProcessor processor) {
            this.processor = processor;
        }
        
        @Override
        public String execute(String[] args) {
            if (args.length > 0) {
                String commandName = args[0].toLowerCase();
                Command command = processor.getCommand(commandName);
                if (command != null) {
                    return String.format("=== %s ===\n%s\nUsage: %s", 
                                       commandName, command.getDescription(), command.getUsage());
                } else {
                    return "Command not found: " + commandName;
                }
            } else {
                StringBuilder sb = new StringBuilder("=== Available commands ===\n");
                for (String commandName : processor.getCommandNames()) {
                    Command command = processor.getCommand(commandName);
                    sb.append(String.format("%-12s - %s\n", commandName, command.getDescription()));
                }
                sb.append("\nType 'help <command>' for detailed information.");
                return sb.toString();
            }
        }
        
        @Override
        public String getDescription() {
            return "Shows help for commands";
        }
        
        @Override
        public String getUsage() {
            return "help [command]";
        }
    }
    
    private static class ClearCommand implements Command {
        @Override
        public String execute(String[] args) {
            return "CLEAR_CONSOLE";
        }
        
        @Override
        public String getDescription() {
            return "Clears console";
        }
        
        @Override
        public String getUsage() {
            return "clear";
        }
    }
} 