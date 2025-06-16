package com.arce;

import com.arce.core.console.GameConsole;
import com.arce.core.managers.MapManager;
import com.arce.logger.EngineLogger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

public class ConsoleDemo extends JFrame {
    private static final EngineLogger logger = new EngineLogger(ConsoleDemo.class);
    
    private GameConsole console;
    private MapManager mapManager;
    private JLabel statusLabel;
    private JLayeredPane layeredPane;
    
    public ConsoleDemo() {
        initializeDemo();
    }
    
    private void initializeDemo() {
        setTitle("ARCE Console Demo - Press ` to open the console");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        
        mapManager = new MapManager();
        
        console = new GameConsole(mapManager);
        
        layeredPane = new JLayeredPane();
        setContentPane(layeredPane);
        
        JPanel gameArea = createGameArea();
        gameArea.setBounds(0, 0, 800, 600);
        layeredPane.add(gameArea, JLayeredPane.DEFAULT_LAYER);
        
        console.setBounds(0, 0, 800, 300);
        layeredPane.add(console, JLayeredPane.POPUP_LAYER);
        
        setupKeyboardHandling();
        
        logger.logInfo("Console demo initialized");
    }
    
    private JPanel createGameArea() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.DARK_GRAY);
        
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(Color.DARK_GRAY);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        
        JLabel titleLabel = new JLabel("ARCE Engine - Console Demo");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        gbc.gridx = 0; gbc.gridy = 0;
        centerPanel.add(titleLabel, gbc);
        
        JLabel instructionsLabel = new JLabel("<html><center>" +
            "Press <b>`</b> (tilde) to open the console<br><br>" +
            "Available commands:<br>" +
            "• <b>help</b> - list commands<br>" +
            "• <b>maps</b> - list available maps<br>" +
            "• <b>map &lt;name&gt;</b> - load map<br>" +
            "• <b>testmap</b> - load test map<br>" +
            "• <b>mapinfo</b> - show map info<br>" +
            "• <b>clear</b> - clear console" +
            "</center></html>");
        instructionsLabel.setForeground(Color.LIGHT_GRAY);
        instructionsLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        gbc.gridy = 1;
        centerPanel.add(instructionsLabel, gbc);
        
        panel.add(centerPanel, BorderLayout.CENTER);
        
        statusLabel = new JLabel("Status: Ready | Current map: None");
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        panel.add(statusLabel, BorderLayout.SOUTH);
        
        Timer statusTimer = new Timer(1000, e -> updateStatus());
        statusTimer.start();
        
        return panel;
    }
    
    private void setupKeyboardHandling() {
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(e -> {
            if (e.getID() == KeyEvent.KEY_PRESSED) {
                if (e.getKeyCode() == KeyEvent.VK_BACK_QUOTE || e.getKeyCode() == 192) {
                    console.toggleConsole();
                    e.consume();
                    return true;
                }
            }
            return false;
        });
        
        setFocusable(true);
        requestFocus();
    }
    
    private void updateStatus() {
        String currentMap = mapManager.hasCurrentMap() ? 
            mapManager.getCurrentMapName() : "None";
        String stats = mapManager.hasCurrentMap() ? 
            " | " + mapManager.getMapStatistics() : "";
        
        statusLabel.setText("Status: Ready | Current map: " + currentMap + stats);
    }
    
    @Override
    public void doLayout() {
        super.doLayout();
        
        if (layeredPane != null) {
            Component[] components = layeredPane.getComponents();
            for (Component comp : components) {
                if (comp != console) {
                    comp.setBounds(0, 0, getWidth(), getHeight());
                }
            }
            
            if (console != null) {
                console.setBounds(0, 0, getWidth(), getHeight() / 2);
            }
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ConsoleDemo().setVisible(true);
            
            logger.logInfo("=== ARCE Console Demo Started ===");
            logger.logInfo("Press ` (backtick) to open/close console");
        });
    }
} 