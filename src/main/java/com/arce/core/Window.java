package com.arce.core;

import com.arce.logger.EngineLogger;
import com.arce.core.console.GameConsole;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;

public class Window extends JPanel implements KeyListener {
    private boolean showTopDownMap = false;
    private final EngineLogger logger;
    private final int width;
    private final int height;
    private JFrame frame;
    private boolean[] keys = new boolean[256];
    private volatile boolean closeRequested = false;
    private BufferedImage currentFrame;
    private GameConsole gameConsole;
    
    public Window(int width, int height, String title) {
        this.logger = new EngineLogger(Window.class);
        this.width = width;
        this.height = height;
        
        initWindow(title);
        logger.logSuccess("Window created: " + width + "x" + height);
    }
    
    private void initWindow(String title) {
        frame = new JFrame(title);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        
        this.setPreferredSize(new Dimension(width, height));
        this.setSize(width, height);
        this.setFocusable(true);
        this.addKeyListener(this);
        this.setBackground(Color.BLACK);
        this.setLayout(null);
        
        frame.add(this);
        frame.pack();
        frame.setLocationRelativeTo(null);
        
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                logger.logInfo("Window close requested");
                closeRequested = true;
            }
        });
    }
    
    public void setGameConsole(GameConsole console) {
        this.gameConsole = console;
        
        console.setBounds(0, 0, width, height / 2);
        console.setVisible(false);
        
        this.add(console);
        this.setComponentZOrder(console, 0);
        
        logger.logInfo("Game console integrated into window");
    }
    
    public void show() {
        frame.setVisible(true);
        this.requestFocus();
        logger.logSuccess("Window displayed");
    }
    
    public void hide() {
        if (frame != null) {
            frame.setVisible(false);
            frame.dispose();
            logger.logInfo("Window hidden and disposed");
        }
    }
    
    public boolean isCloseRequested() {
        return closeRequested;
    }
    
    public void displayFrame(BufferedImage frame) {
        this.currentFrame = frame;
        repaint();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        if (currentFrame != null) {
            g.drawImage(currentFrame, 0, 0, null);
        } else {
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, width, height);
            
            g.setColor(Color.GREEN);
            g.setFont(new Font("Arial", Font.BOLD, 24));
            g.drawString("ARCE Engine", width/2 - 80, height/2 - 40);
            
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.PLAIN, 16));
            g.drawString("Нажмите ` для открытия консоли", width/2 - 120, height/2);
            g.drawString("Загрузите карту через консоль", width/2 - 110, height/2 + 20);
            
            g.setColor(Color.LIGHT_GRAY);
            g.setFont(new Font("Arial", Font.PLAIN, 12));
            g.drawString("Команды: maps, map <название>, testmap", width/2 - 140, height/2 + 50);
            g.drawString("WASD - Move, Arrows/QE - Turn, M - Map, ESC - Exit", width/2 - 160, height/2 + 70);
        }
    }
    
    public boolean[] getKeyStates() {
        return keys.clone();
    }
    
    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() < keys.length) {
            keys[e.getKeyCode()] = true;
        }
        
        if (e.getKeyCode() == KeyEvent.VK_BACK_QUOTE) {
            if (gameConsole != null) {
                gameConsole.toggleConsole();
                e.consume();
                return;
            }
        }
        
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            logger.logInfo("ESC pressed - shutting down");
            closeRequested = true;
        }
        
        if (e.getKeyCode() == KeyEvent.VK_M) {
            showTopDownMap = !showTopDownMap;
            logger.logInfo("Top-down map: " + (showTopDownMap ? "ON" : "OFF"));
        }
    }
    
    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() < keys.length) {
            keys[e.getKeyCode()] = false;
        }
    }

    public boolean isShowTopDownMap() {
        return showTopDownMap;
    }
    
    @Override
    public void keyTyped(KeyEvent e) {
    }
}