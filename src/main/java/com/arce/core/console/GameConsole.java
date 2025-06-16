package com.arce.core.console;

import com.arce.core.managers.MapManager;
import com.arce.logger.EngineLogger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

public class GameConsole extends JPanel {
    private final EngineLogger logger;
    private final CommandProcessor commandProcessor;
    
    private JTextArea outputArea;
    private JTextField inputField;
    private JScrollPane scrollPane;
    
    private boolean visible = false;
    private List<String> commandHistory;
    private int historyIndex = -1;
    private int maxLines = 100;
    
    private Timer animationTimer;
    private float animationProgress = 0.0f;
    private boolean animating = false;
    
    public GameConsole(MapManager mapManager) {
        this.logger = new EngineLogger(GameConsole.class);
        this.commandProcessor = new CommandProcessor();
        this.commandHistory = new ArrayList<>();
        
        registerMapCommands(mapManager);
        
        initializeUI();
        
        setVisible(false);
        
        logger.logInfo("GameConsole initialized");
    }
    
    private void registerMapCommands(MapManager mapManager) {
        commandProcessor.registerCommand("map", new MapCommands.LoadMapCommand(mapManager));
        commandProcessor.registerCommand("maps", new MapCommands.ListMapsCommand(mapManager));
        commandProcessor.registerCommand("mapinfo", new MapCommands.MapInfoCommand(mapManager));
        commandProcessor.registerCommand("switch", new MapCommands.SwitchMapCommand(mapManager));
        commandProcessor.registerCommand("testmap", new MapCommands.LoadTestMapCommand(mapManager));
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout());
        setBackground(new Color(0, 0, 0, 200));
        setBorder(BorderFactory.createLineBorder(Color.GREEN, 2));
        
        outputArea = new JTextArea();
        outputArea.setBackground(Color.BLACK);
        outputArea.setForeground(Color.GREEN);
        outputArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        outputArea.setEditable(false);
        outputArea.setCaretColor(Color.GREEN);
        
        scrollPane = new JScrollPane(outputArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(null);
        
        add(scrollPane, BorderLayout.CENTER);
        
        JPanel inputPanel = createInputPanel();
        add(inputPanel, BorderLayout.SOUTH);
        
        appendOutput("=== ARCE Engine Console ===");
        appendOutput("Type 'help' for command list");
        appendOutput("Press ` (tilde) to open/close console");
        appendOutput("");
    }
    
    private JPanel createInputPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.BLACK);
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        JLabel promptLabel = new JLabel("] ");
        promptLabel.setForeground(Color.GREEN);
        promptLabel.setFont(new Font(Font.MONOSPACED, Font.BOLD, 12));
        panel.add(promptLabel, BorderLayout.WEST);
        
        inputField = new JTextField();
        inputField.setBackground(Color.BLACK);
        inputField.setForeground(Color.GREEN);
        inputField.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        inputField.setBorder(null);
        inputField.setCaretColor(Color.GREEN);
        
        inputField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                handleInputKeyPressed(e);
            }
        });
        
        panel.add(inputField, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void handleInputKeyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_ENTER:
                executeCommand();
                break;
                
            case KeyEvent.VK_UP:
                navigateHistory(-1);
                e.consume();
                break;
                
            case KeyEvent.VK_DOWN:
                navigateHistory(1);
                e.consume();
                break;
                
            case KeyEvent.VK_TAB:
                // TODO: Автодополнение команд
                e.consume();
                break;
                
            case KeyEvent.VK_ESCAPE:
                hideConsole();
                e.consume();
                break;
        }
    }

    private void executeCommand() {
        String input = inputField.getText().trim();
        if (input.isEmpty()) {
            return;
        }
        
        commandHistory.add(input);
        if (commandHistory.size() > 50) {
            commandHistory.remove(0);
        }
        historyIndex = commandHistory.size();
        
        appendOutput("] " + input);
        
        String result = commandProcessor.processCommand(input);
        
        if ("CLEAR_CONSOLE".equals(result)) {
            clearOutput();
        } else if (!result.isEmpty()) {
            appendOutput(result);
        }
        
        inputField.setText("");
        
        scrollToBottom();
    }

    private void navigateHistory(int direction) {
        if (commandHistory.isEmpty()) {
            return;
        }
        
        historyIndex += direction;
        historyIndex = Math.max(0, Math.min(historyIndex, commandHistory.size()));
        
        if (historyIndex < commandHistory.size()) {
            inputField.setText(commandHistory.get(historyIndex));
        } else {
            inputField.setText("");
        }
    }
    
    private void appendOutput(String text) {
        String currentText = outputArea.getText();
        String[] lines = currentText.split("\n");
        
        if (lines.length >= maxLines) {
            StringBuilder sb = new StringBuilder();
            for (int i = 1; i < lines.length; i++) {
                sb.append(lines[i]).append("\n");
            }
            currentText = sb.toString();
        }
        
        if (!currentText.isEmpty() && !currentText.endsWith("\n")) {
            currentText += "\n";
        }
        
        outputArea.setText(currentText + text);
        scrollToBottom();
    }
    
    private void clearOutput() {
        outputArea.setText("");
    }
    
    private void scrollToBottom() {
        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = scrollPane.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }
    
    public void showConsole() {
        if (visible) {
            return;
        }
        
        visible = true;
        setVisible(true);
        inputField.requestFocusInWindow();
        
        logger.logDebug("Console shown");
    }
    
    public void hideConsole() {
        if (!visible) {
            return;
        }
        
        visible = false;
        setVisible(false);
        
        Container parent = getParent();
        if (parent != null) {
            parent.requestFocusInWindow();
        }
        
        logger.logDebug("Console hidden");
    }
    
    public void toggleConsole() {
        if (visible) {
            hideConsole();
        } else {
            showConsole();
        }
    }
    
    /**
     * Обновлял размер консоли во время анимации
     * @deprecated Размер теперь управляется родительским контейнером
     */
    @Deprecated
    private void updateAnimationSize() {
        // Метод оставлен для совместимости, но не используется
    }
    
    public boolean isConsoleVisible() {
        return visible;
    }
    
    public void registerCommand(String name, Command command) {
        commandProcessor.registerCommand(name, command);
    }
    
    public void setMaxLines(int maxLines) {
        this.maxLines = maxLines;
    }
} 