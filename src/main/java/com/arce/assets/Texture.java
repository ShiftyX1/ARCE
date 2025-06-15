package com.arce.assets;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class Texture {
    private BufferedImage image;
    private String name;
    private int width;
    private int height;
    
    public Texture(String name, String filepath) throws IOException {
        this.name = name;
        this.image = ImageIO.read(new File(filepath));
        this.width = image.getWidth();
        this.height = image.getHeight();
    }
    
    public Texture(String name, BufferedImage image) {
        this.name = name;
        this.image = image;
        this.width = image.getWidth();
        this.height = image.getHeight();
    }
    
    public Color getPixel(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return Color.MAGENTA;
        }
        return new Color(image.getRGB(x, y));
    }
    
    public Color getPixelUV(double u, double v) {
        u = u - Math.floor(u);
        v = v - Math.floor(v);
        
        int x = (int) (u * width);
        int y = (int) (v * height);
        
        x = Math.max(0, Math.min(width - 1, x));
        y = Math.max(0, Math.min(height - 1, y));
        
        return new Color(image.getRGB(x, y));
    }
    
    public int getPixelRGB(double u, double v) {
        u = u - Math.floor(u);
        v = v - Math.floor(v);
        
        int x = (int) (u * width);
        int y = (int) (v * height);
        
        x = Math.max(0, Math.min(width - 1, x));
        y = Math.max(0, Math.min(height - 1, y));
        
        return image.getRGB(x, y);
    }
    
    public static Texture createDefault() {
        BufferedImage defaultImage = new BufferedImage(64, 64, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = defaultImage.createGraphics();
        
        for (int x = 0; x < 64; x += 8) {
            for (int y = 0; y < 64; y += 8) {
                boolean isEven = ((x / 8) + (y / 8)) % 2 == 0;
                g.setColor(isEven ? Color.LIGHT_GRAY : Color.DARK_GRAY);
                g.fillRect(x, y, 8, 8);
            }
        }
        
        g.dispose();
        return new Texture("default", defaultImage);
    }
    
    public static Texture createSolid(String name, Color color, int size) {
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setColor(color);
        g.fillRect(0, 0, size, size);
        g.dispose();
        return new Texture(name, image);
    }
    
    public static Texture createBrick(String name) {
        BufferedImage image = new BufferedImage(64, 64, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        
        g.setColor(new Color(180, 100, 80));
        g.fillRect(0, 0, 64, 64);
        
        g.setColor(new Color(120, 60, 40));
        g.drawLine(0, 16, 64, 16);
        g.drawLine(0, 48, 64, 48);
        
        g.drawLine(16, 0, 16, 16);
        g.drawLine(48, 0, 48, 16);
        g.drawLine(32, 16, 32, 48);
        g.drawLine(16, 48, 16, 64);
        g.drawLine(48, 48, 48, 64);
        
        g.dispose();
        return new Texture(name, image);
    }
    
    public void dispose() {
        if (image != null) {
            image.flush();
            image = null;
        }
    }
    
    public BufferedImage getImage() { return image; }
    public String getName() { return name; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    
    @Override
    public String toString() {
        return String.format("Texture(%s, %dx%d)", name, width, height);
    }
}