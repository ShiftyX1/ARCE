package com.arce.core;

import com.arce.logger.EngineLogger;
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
        frame.setSize(width, height);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        
        this.setPreferredSize(new Dimension(width, height));
        this.setFocusable(true);
        this.addKeyListener(this);
        
        frame.add(this);
        frame.pack();
        
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                logger.logInfo("Window close requested");
                closeRequested = true;
            }
        });
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
            g.drawString("ARCE Engine Loading...", width/2 - 120, height/2);
            
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.PLAIN, 14));
            g.drawString("WASD - Move, Arrows/QE - Turn, ESC - Exit", width/2 - 140, height/2 + 40);
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