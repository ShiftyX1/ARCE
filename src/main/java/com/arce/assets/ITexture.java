package com.arce.assets;

import java.awt.Color;
import java.awt.image.BufferedImage;

public interface ITexture {
    public Color getPixel(int x, int y);
    public Color getPixelUV(double u, double v);
    public int getWidth();
    public int getHeight();
    public String getName();
    public void dispose();
    public BufferedImage getImage();
}
