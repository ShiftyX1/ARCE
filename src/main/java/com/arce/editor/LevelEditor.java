package com.arce.editor;

import com.arce.logger.EngineLogger;
import com.arce.math.Vector2D;
import com.arce.world.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class LevelEditor extends JFrame {
    private final EngineLogger logger;
    private final MapLoader mapLoader;
    private final MapSaver mapSaver;
    
    private GameMap currentMap;
    private EditorPanel editorPanel;
    private PropertiesPanel propertiesPanel;
    
    private EditorMode currentMode = EditorMode.SELECT;
    private List<Vector2D> currentPoints = new ArrayList<>();
    private Wall selectedWall;
    private Sector selectedSector;
    private Vector2D playerPosition;
    
    private boolean snapToGrid = true;
    private boolean diagonalMode = false;
    private int gridSize = 32;
    private Vector2D currentMousePos;
    
    private JLabel statusLabel;
    private JLabel modesLabel;
    private JLabel coordsLabel;
    
    public enum EditorMode {
        SELECT,
        DRAW_WALL,
        DRAW_SECTOR,
        SET_PLAYER
    }
    
    public LevelEditor() {
        this.logger = new EngineLogger(LevelEditor.class);
        this.mapLoader = new MapLoader();
        this.mapSaver = new MapSaver();
        
        initializeEditor();
        createNewMap();
        
        logger.logInfo("Level Editor initialized");
    }
    
    private void initializeEditor() {
        setTitle("ARCE Level Editor - Новая карта");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        
        setLayout(new BorderLayout());
        
        add(createToolbar(), BorderLayout.NORTH);
        
        editorPanel = new EditorPanel();
        add(editorPanel, BorderLayout.CENTER);
        
        propertiesPanel = new PropertiesPanel();
        add(propertiesPanel, BorderLayout.EAST);
        
        add(createStatusBar(), BorderLayout.SOUTH);
        
        setJMenuBar(createMenuBar());
    }
    
    private JToolBar createToolbar() {
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        
        ButtonGroup modeGroup = new ButtonGroup();
        
        JToggleButton selectBtn = new JToggleButton("Выбор");
        selectBtn.setSelected(true);
        selectBtn.addActionListener(e -> setMode(EditorMode.SELECT));
        modeGroup.add(selectBtn);
        toolbar.add(selectBtn);
        
        JToggleButton wallBtn = new JToggleButton("Стена");
        wallBtn.addActionListener(e -> setMode(EditorMode.DRAW_WALL));
        modeGroup.add(wallBtn);
        toolbar.add(wallBtn);
        
        JToggleButton sectorBtn = new JToggleButton("Сектор");
        sectorBtn.addActionListener(e -> setMode(EditorMode.DRAW_SECTOR));
        modeGroup.add(sectorBtn);
        toolbar.add(sectorBtn);
        
        JToggleButton playerBtn = new JToggleButton("Игрок");
        playerBtn.addActionListener(e -> setMode(EditorMode.SET_PLAYER));
        modeGroup.add(playerBtn);
        toolbar.add(playerBtn);
        
        toolbar.addSeparator();
        
        JToggleButton snapBtn = new JToggleButton("Сетка");
        snapBtn.setSelected(snapToGrid);
        snapBtn.setToolTipText("Привязка к сетке (Grid Snap)");
        snapBtn.addActionListener(e -> {
            snapToGrid = snapBtn.isSelected();
            logger.logInfo("Grid snap: " + (snapToGrid ? "ON" : "OFF"));
            editorPanel.repaint();
        });
        toolbar.add(snapBtn);
        
        JToggleButton diagonalBtn = new JToggleButton("45°");
        diagonalBtn.setSelected(diagonalMode);
        diagonalBtn.setToolTipText("Диагональные линии (45°, 90°)");
        diagonalBtn.addActionListener(e -> {
            diagonalMode = diagonalBtn.isSelected();
            logger.logInfo("Diagonal mode: " + (diagonalMode ? "ON" : "OFF"));
            editorPanel.repaint();
        });
        toolbar.add(diagonalBtn);
        
        toolbar.addSeparator();
        
        JLabel gridLabel = new JLabel("Сетка:");
        toolbar.add(gridLabel);
        
        JComboBox<Integer> gridSizeCombo = new JComboBox<>(new Integer[]{16, 32, 64, 128});
        gridSizeCombo.setSelectedItem(gridSize);
        gridSizeCombo.addActionListener(e -> {
            gridSize = (Integer) gridSizeCombo.getSelectedItem();
            logger.logInfo("Grid size changed to: " + gridSize);
            editorPanel.repaint();
        });
        toolbar.add(gridSizeCombo);
        
        toolbar.addSeparator();
        
        JButton testBtn = new JButton("Тест");
        testBtn.addActionListener(e -> testMap());
        toolbar.add(testBtn);
        
        return toolbar;
    }
    
    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        
        JMenu fileMenu = new JMenu("Файл");
        
        JMenuItem newItem = new JMenuItem("Новая карта");
        newItem.setAccelerator(KeyStroke.getKeyStroke("ctrl N"));
        newItem.addActionListener(e -> createNewMap());
        fileMenu.add(newItem);
        
        JMenuItem openItem = new JMenuItem("Открыть...");
        openItem.setAccelerator(KeyStroke.getKeyStroke("ctrl O"));
        openItem.addActionListener(e -> openMap());
        fileMenu.add(openItem);
        
        JMenuItem saveItem = new JMenuItem("Сохранить");
        saveItem.setAccelerator(KeyStroke.getKeyStroke("ctrl S"));
        saveItem.addActionListener(e -> saveMap());
        fileMenu.add(saveItem);
        
        JMenuItem saveAsItem = new JMenuItem("Сохранить как...");
        saveAsItem.addActionListener(e -> saveMapAs());
        fileMenu.add(saveAsItem);
        
        fileMenu.addSeparator();
        
        JMenuItem exitItem = new JMenuItem("Выход");
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(exitItem);
        
        menuBar.add(fileMenu);
        
        JMenu viewMenu = new JMenu("Вид");
        
        JCheckBoxMenuItem gridItem = new JCheckBoxMenuItem("Сетка", true);
        gridItem.addActionListener(e -> {
            editorPanel.setShowGrid(gridItem.isSelected());
            editorPanel.repaint();
        });
        viewMenu.add(gridItem);
        
        menuBar.add(viewMenu);
        
        return menuBar;
    }
    
    private JPanel createStatusBar() {
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBorder(BorderFactory.createLoweredBevelBorder());
        
        JLabel statusLabel = new JLabel("Готов");
        statusBar.add(statusLabel, BorderLayout.WEST);
        
        JLabel modesLabel = new JLabel();
        updateModesLabel(modesLabel);
        statusBar.add(modesLabel, BorderLayout.CENTER);
        
        JLabel coordsLabel = new JLabel("Координаты: (0, 0)");
        statusBar.add(coordsLabel, BorderLayout.EAST);
        
        this.statusLabel = statusLabel;
        this.modesLabel = modesLabel;
        this.coordsLabel = coordsLabel;
        
        return statusBar;
    }
    
    private void updateModesLabel(JLabel label) {
        String modes = "";
        if (snapToGrid) modes += "Сетка: " + gridSize + "px ";
        if (diagonalMode) modes += "45° ";
        if (modes.isEmpty()) modes = "Свободный режим";
        
        label.setText("| " + modes + " | Горячие клавиши: G - сетка, D - диагонали, ESC - отмена");
    }
    
    private void setMode(EditorMode mode) {
        this.currentMode = mode;
        this.currentPoints.clear();
        editorPanel.repaint();
        logger.logInfo("Editor mode changed to: " + mode);
    }
    
    private void createNewMap() {
        currentMap = new GameMap();
        currentMap.setPlayerStartPosition(new Vector2D(400, 300));
        currentMap.setPlayerStartAngle(0);
        playerPosition = new Vector2D(400, 300);
        
        selectedWall = null;
        selectedSector = null;
        currentPoints.clear();
        
        setTitle("ARCE Level Editor - Новая карта");
        editorPanel.repaint();
        propertiesPanel.updateProperties();
        
        logger.logInfo("New map created");
    }
    
    private void openMap() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
            "JSON Map Files", "json"));
        
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            GameMap loadedMap = mapLoader.loadMap(file.getAbsolutePath());
            
            if (loadedMap != null) {
                currentMap = loadedMap;
                playerPosition = loadedMap.getPlayerStartPosition();
                setTitle("ARCE Level Editor - " + file.getName());
                editorPanel.repaint();
                propertiesPanel.updateProperties();
                logger.logInfo("Map loaded: " + file.getName());
            } else {
                JOptionPane.showMessageDialog(this, "Ошибка загрузки карты!", 
                                            "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void saveMap() {
        saveMapAs();
    }
    
    private void saveMapAs() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
            "JSON Map Files", "json"));
        
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            String filePath = file.getAbsolutePath();
            if (!filePath.endsWith(".json")) {
                filePath += ".json";
            }
            
            String mapName = JOptionPane.showInputDialog(this, "Название карты:", 
                                                       file.getName().replace(".json", ""));
            String author = JOptionPane.showInputDialog(this, "Автор:", "Unknown");
            
            if (mapName != null && !mapName.trim().isEmpty()) {
                if (mapSaver.saveMap(currentMap, filePath, mapName, author)) {
                    setTitle("ARCE Level Editor - " + file.getName());
                    logger.logInfo("Map saved: " + filePath);
                } else {
                    JOptionPane.showMessageDialog(this, "Ошибка сохранения карты!", 
                                                "Ошибка", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
    
    private void testMap() {
        // TODO: Интеграция с игровым движком для тестирования
        JOptionPane.showMessageDialog(this, 
            "Функция тестирования будет добавлена позже.\n" +
            "Карта содержит: " + currentMap.getSectors().size() + " секторов, " + 
            currentMap.getWalls().size() + " стен",
            "Тест карты", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private class EditorPanel extends JPanel {
        private boolean showGrid = true;
        private double scale = 1.0;
        private Vector2D offset = new Vector2D(0, 0);
        
        public EditorPanel() {
            setBackground(Color.DARK_GRAY);
            setFocusable(true);
            
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    handleMouseClick(e);
                }
            });
            
            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    currentMousePos = screenToWorld(e.getPoint());
                    if (snapToGrid) {
                        currentMousePos = snapToGrid(currentMousePos);
                    }
                    
                    if (coordsLabel != null) {
                        coordsLabel.setText(String.format("Координаты: (%.0f, %.0f)", 
                                                        currentMousePos.x, currentMousePos.y));
                    }
                    
                    repaint();
                }
            });
            
            addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    switch (e.getKeyCode()) {
                        case KeyEvent.VK_G:
                            snapToGrid = !snapToGrid;
                            logger.logInfo("Grid snap toggled: " + (snapToGrid ? "ON" : "OFF"));
                            if (modesLabel != null) updateModesLabel(modesLabel);
                            repaint();
                            break;
                        case KeyEvent.VK_D:
                            diagonalMode = !diagonalMode;
                            logger.logInfo("Diagonal mode toggled: " + (diagonalMode ? "ON" : "OFF"));
                            if (modesLabel != null) updateModesLabel(modesLabel);
                            repaint();
                            break;
                        case KeyEvent.VK_ESCAPE:
                            currentPoints.clear();
                            logger.logInfo("Current operation cancelled");
                            repaint();
                            break;
                        case KeyEvent.VK_ENTER:
                            if (currentMode == EditorMode.DRAW_SECTOR && currentPoints.size() >= 3) {
                                finishSector();
                            }
                            break;
                    }
                }
            });
            
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    requestFocusInWindow();
                }
            });
        }
        
        public void setShowGrid(boolean show) {
            this.showGrid = show;
        }
        
        private Vector2D snapToGrid(Vector2D point) {
            if (!snapToGrid) return point;
            
            double snappedX = Math.round(point.x / gridSize) * gridSize;
            double snappedY = Math.round(point.y / gridSize) * gridSize;
            
            return new Vector2D(snappedX, snappedY);
        }
        
        private Vector2D applyDiagonalConstraint(Vector2D start, Vector2D end) {
            if (!diagonalMode || start == null) return end;
            
            double dx = end.x - start.x;
            double dy = end.y - start.y;
            
            double angle = Math.atan2(dy, dx);
            double degrees = Math.toDegrees(angle);
            
            double[] snapAngles = {0, 45, 90, 135, 180, 225, 270, 315};
            double closestAngle = snapAngles[0];
            double minDiff = Math.abs(degrees - snapAngles[0]);
            
            for (double snapAngle : snapAngles) {
                double diff = Math.abs(degrees - snapAngle);
                if (diff > 180) diff = 360 - diff;
                
                if (diff < minDiff) {
                    minDiff = diff;
                    closestAngle = snapAngle;
                }
            }
            
            double distance = Math.sqrt(dx * dx + dy * dy);
            double radians = Math.toRadians(closestAngle);
            
            return new Vector2D(
                start.x + distance * Math.cos(radians),
                start.y + distance * Math.sin(radians)
            );
        }
        
        private void handleMouseClick(MouseEvent e) {
            Vector2D worldPos = screenToWorld(e.getPoint());
            
            if (snapToGrid) {
                worldPos = snapToGrid(worldPos);
            }
            
            if (diagonalMode && currentMode == EditorMode.DRAW_WALL && !currentPoints.isEmpty()) {
                Vector2D lastPoint = currentPoints.get(currentPoints.size() - 1);
                worldPos = applyDiagonalConstraint(lastPoint, worldPos);
            }
            
            switch (currentMode) {
                case DRAW_WALL:
                    handleWallDrawing(worldPos);
                    break;
                case DRAW_SECTOR:
                    handleSectorDrawing(worldPos);
                    break;
                case SET_PLAYER:
                    setPlayerPosition(worldPos);
                    break;
                case SELECT:
                    selectObjectAt(worldPos);
                    break;
            }
        }
        
        private Vector2D screenToWorld(Point screenPoint) {
            return new Vector2D(
                (screenPoint.x - offset.x) / scale,
                (screenPoint.y - offset.y) / scale
            );
        }
        
        private Point worldToScreen(Vector2D worldPoint) {
            return new Point(
                (int)(worldPoint.x * scale + offset.x),
                (int)(worldPoint.y * scale + offset.y)
            );
        }
        
        private void handleWallDrawing(Vector2D worldPos) {
            currentPoints.add(worldPos);
            
            if (currentPoints.size() >= 2) {
                Vector2D start = currentPoints.get(currentPoints.size() - 2);
                Vector2D end = currentPoints.get(currentPoints.size() - 1);
                
                Wall wall = new Wall(start, end);
                currentMap.addWall(wall);
                
                logger.logInfo("Wall added: " + start + " -> " + end);
                repaint();
            }
        }
        
        private void handleSectorDrawing(Vector2D worldPos) {
            currentPoints.add(worldPos);
            repaint();
        }
        
        private void setPlayerPosition(Vector2D worldPos) {
            playerPosition = worldPos;
            currentMap.setPlayerStartPosition(worldPos);
            logger.logInfo("Player position set to: " + worldPos);
            repaint();
        }
        
        private void selectObjectAt(Vector2D worldPos) {
            // TODO: Реализовать выбор объектов
        }
        
        private void finishSector() {
            if (currentPoints.size() < 3) {
                logger.logInfo("Need at least 3 points to create a sector");
                return;
            }
            
            int sectorId = currentMap.getSectors().size() + 1;
            Sector newSector = new Sector(sectorId);
            newSector.setFloorHeight(0);
            newSector.setCeilingHeight(64);
            newSector.setLightLevel(255);
            
            for (int i = 0; i < currentPoints.size(); i++) {
                Vector2D start = currentPoints.get(i);
                Vector2D end = currentPoints.get((i + 1) % currentPoints.size());
                
                Wall wall = new Wall(start, end);
                wall.setTextureId(1);
                newSector.addWall(wall);
                currentMap.addWall(wall);
            }
            
            currentMap.addSector(newSector);
            currentPoints.clear();
            
            logger.logInfo("Sector created with ID: " + sectorId + " (" + newSector.getWalls().size() + " walls)");
            repaint();
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            if (showGrid) {
                drawGrid(g2d);
            }
            
            drawMap(g2d);
            
            drawPlayer(g2d);
            
            drawCurrentPoints(g2d);
        }
        
        private void drawGrid(Graphics2D g2d) {
            g2d.setColor(Color.GRAY);
            g2d.setStroke(new BasicStroke(0.5f));
            
            int width = getWidth();
            int height = getHeight();
            
            for (int x = 0; x < width; x += gridSize) {
                g2d.drawLine(x, 0, x, height);
            }
            
            for (int y = 0; y < height; y += gridSize) {
                g2d.drawLine(0, y, width, y);
            }
            
            g2d.setColor(Color.LIGHT_GRAY);
            g2d.setStroke(new BasicStroke(1.0f));
            
            for (int x = 0; x < width; x += gridSize * 4) {
                g2d.drawLine(x, 0, x, height);
            }
            
            for (int y = 0; y < height; y += gridSize * 4) {
                g2d.drawLine(0, y, width, y);
            }
        }
        
        private void drawMap(Graphics2D g2d) {
            if (currentMap == null) return;
            
            g2d.setColor(Color.WHITE);
            g2d.setStroke(new BasicStroke(2.0f));
            
            for (Wall wall : currentMap.getWalls()) {
                Point start = worldToScreen(wall.getLine().start);
                Point end = worldToScreen(wall.getLine().end);
                
                if (wall == selectedWall) {
                    g2d.setColor(Color.YELLOW);
                    g2d.setStroke(new BasicStroke(3.0f));
                } else if (wall.isSolid()) {
                    g2d.setColor(Color.WHITE);
                    g2d.setStroke(new BasicStroke(2.0f));
                } else {
                    g2d.setColor(Color.CYAN);
                    g2d.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT, 
                                                BasicStroke.JOIN_MITER, 10.0f, 
                                                new float[]{5.0f}, 0.0f));
                }
                
                g2d.drawLine(start.x, start.y, end.x, end.y);
            }
        }
        
        private void drawPlayer(Graphics2D g2d) {
            if (playerPosition == null) return;
            
            Point playerScreen = worldToScreen(playerPosition);
            
            g2d.setColor(Color.GREEN);
            g2d.fillOval(playerScreen.x - 5, playerScreen.y - 5, 10, 10);

            double angle = currentMap.getPlayerStartAngle();
            int dx = (int)(Math.cos(angle) * 20);
            int dy = (int)(Math.sin(angle) * 20);
            g2d.drawLine(playerScreen.x, playerScreen.y, 
                        playerScreen.x + dx, playerScreen.y + dy);
        }
        
        private void drawCurrentPoints(Graphics2D g2d) {
            if (currentPoints.isEmpty()) return;
            
            g2d.setColor(Color.RED);
            g2d.setStroke(new BasicStroke(2.0f));
            
            for (int i = 0; i < currentPoints.size(); i++) {
                Point p = worldToScreen(currentPoints.get(i));
                
                g2d.fillOval(p.x - 4, p.y - 4, 8, 8);
                g2d.setColor(Color.WHITE);
                g2d.drawOval(p.x - 4, p.y - 4, 8, 8);
                g2d.setColor(Color.RED);
                
                if (i > 0) {
                    Point prev = worldToScreen(currentPoints.get(i - 1));
                    g2d.drawLine(prev.x, prev.y, p.x, p.y);
                }
            }
            
            if (!currentPoints.isEmpty() && currentMousePos != null && 
                (currentMode == EditorMode.DRAW_WALL || currentMode == EditorMode.DRAW_SECTOR)) {
                
                Vector2D lastPoint = currentPoints.get(currentPoints.size() - 1);
                Vector2D previewEnd = currentMousePos;
                
                if (diagonalMode && currentMode == EditorMode.DRAW_WALL) {
                    previewEnd = applyDiagonalConstraint(lastPoint, previewEnd);
                }
                
                Point lastScreen = worldToScreen(lastPoint);
                Point previewScreen = worldToScreen(previewEnd);
                
                g2d.setColor(Color.YELLOW);
                g2d.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT, 
                                            BasicStroke.JOIN_MITER, 10.0f, 
                                            new float[]{5.0f, 5.0f}, 0.0f));
                g2d.drawLine(lastScreen.x, lastScreen.y, previewScreen.x, previewScreen.y);
                
                String info = String.format("→ (%.0f, %.0f) Длина: %.1f", 
                                           previewEnd.x, previewEnd.y,
                                           Math.sqrt(Math.pow(previewEnd.x - lastPoint.x, 2) + 
                                                   Math.pow(previewEnd.y - lastPoint.y, 2)));
                g2d.setColor(Color.WHITE);
                g2d.setStroke(new BasicStroke(1.0f));
                g2d.drawString(info, previewScreen.x + 10, previewScreen.y - 10);
                
                g2d.setColor(Color.YELLOW);
                g2d.fillOval(previewScreen.x - 3, previewScreen.y - 3, 6, 6);
            }
            
            if (diagonalMode && currentPoints.size() >= 1 && currentMousePos != null && 
                currentMode == EditorMode.DRAW_WALL) {
                Vector2D lastPoint = currentPoints.get(currentPoints.size() - 1);
                Vector2D previewEnd = applyDiagonalConstraint(lastPoint, currentMousePos);
                
                double dx = previewEnd.x - lastPoint.x;
                double dy = previewEnd.y - lastPoint.y;
                double angle = Math.toDegrees(Math.atan2(dy, dx));
                if (angle < 0) angle += 360;
                
                Point lastScreen = worldToScreen(lastPoint);
                g2d.setColor(Color.CYAN);
                g2d.drawString(String.format("%.0f°", angle), lastScreen.x + 15, lastScreen.y + 15);
            }
        }
    }
    
    private class PropertiesPanel extends JPanel {
        public PropertiesPanel() {
            setPreferredSize(new Dimension(250, 0));
            setBorder(BorderFactory.createTitledBorder("Свойства"));
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        }
        
        public void updateProperties() {
            removeAll();
            
            add(new JLabel("Карта: " + (currentMap != null ? "Загружена" : "Нет")));
            if (currentMap != null) {
                add(new JLabel("Секторов: " + currentMap.getSectors().size()));
                add(new JLabel("Стен: " + currentMap.getWalls().size()));
            }
            
            revalidate();
            repaint();
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new LevelEditor().setVisible(true);
        });
    }
} 