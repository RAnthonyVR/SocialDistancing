import javax.imageio.ImageIO;
import java.io.IOException;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.*;

import java.awt.Color;

public class SequentialBinarizedChanges {
    
    private int [] initialImage;
    private int [] currentImage;
    private int imageWidth;
    private int imageHeight;

    public SequentialBinarizedChanges (int width, int height) {
        this.imageWidth = width;
        this.imageHeight = height;
    }

    public int compute (int imageWidth, int imageHeight, int[] initialImage, int[] currentImageMatrix, int []resultImageMatrix, BufferedImage currentImage) {

        for (int row = 0; row < imageHeight; row++) {
            for (int column = 0; column < imageWidth; column++) {

                int rgb = initialImage [column + (row * imageWidth)];
                int rgb2 = currentImageMatrix [column + (row * imageWidth)];

                int red = rgb & 0xFF;
                int green = (rgb >> 8) & 0xFF;
                int blue = (rgb >> 16) & 0xFF;

                int red2 = rgb2 & 0xFF;
                int green2 = (rgb2 >> 8) & 0xFF;
                int blue2 = (rgb2 >> 16) & 0xFF;

                float l = (float) ((0.2126 * (float) red) + (0.7152 * (float) green) + (0.0722 * (float) blue));
                
                int color;
                color = 0;
                
                Color black = Color.black;
                Color white = Color.white;


                if ((Math.abs(red - red2) > 50) || (Math.abs(green - green2) > 50) || (Math.abs(blue - blue2) > 50)) {
                    // System.out.println("" + rgb + rgb2);
                    resultImageMatrix[column + (row * imageWidth)] = Color.BLACK.getRGB();
                } else {

                    color = 255;
                    color = (color << 8) | 255;
                    color = (color << 8) | 255;

                    resultImageMatrix[column + (row * imageWidth)] = Color.WHITE.getRGB();
                }
            }
            
        }
        return 0;
    }

}