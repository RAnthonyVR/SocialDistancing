import javax.imageio.ImageIO;
import java.io.IOException;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.*;

import java.awt.Color;

public class ParallelBinarizedChanges extends RecursiveAction {
    
    private int beginRowRGB;
    private int endRowRGB;
    private int [] initialImage;
    private int [] currentImage;
    private int [] resultImageMatrix;
    private int imageWidth;
    private int assignedHeight;

    public ParallelBinarizedChanges (int width, int beginRowRGB, int endRowRGB, int [] currentImage, int [] initialImage, int assignedHeight, int [] resultImageMatrix) {
        this.imageWidth = width;
        this.beginRowRGB = beginRowRGB;
        this.endRowRGB = endRowRGB;
        this.initialImage = initialImage;
        this.assignedHeight = assignedHeight;
        this.currentImage = currentImage;
        this.resultImageMatrix = resultImageMatrix;
    }

    protected void computeDirectly() {

        for (int row = beginRowRGB; row < endRowRGB; row++) {
            for (int column = 0; column < imageWidth; column++) {
                int rgb = initialImage [column + (row * imageWidth)];
                int rgb2 = currentImage [column + (row * imageWidth)];
                
                int red = rgb & 0xFF;
                int green = (rgb >> 8) & 0xFF;
                int blue = (rgb >> 16) & 0xFF;

                int red2 = rgb2 & 0xFF;
                int green2 = (rgb2 >> 8) & 0xFF;
                int blue2 = (rgb2 >> 16) & 0xFF;
                             
                float l = (float) ((0.2126 * (float) red) + (0.7152 * (float) green) + (0.0722 * (float) blue));
                
                int color;
                color = 0;
                // drastic changes (black)
                if ((Math.abs(red - red2) > 10) || (Math.abs(green - green2) > 10) || (Math.abs(blue - blue2) > 10)) {
                    // System.out.println("" + rgb + rgb2);
                    resultImageMatrix[column + (row * imageWidth)] = color;
                } else { // small or no changes at all (white)

                    color = 255;
                    color = (color << 8) | 255;
                    color = (color << 8) | 255;

                    resultImageMatrix[column + (row * imageWidth)] = color;
                }
            }
        }
    }

    protected void compute() {
        // If it is divided small enough than the threshold
        if ((endRowRGB - beginRowRGB) <= assignedHeight) { //3000
            computeDirectly();
        }
        // Else divide into smaller chunks
        else {
            int newAssignedHeight = (endRowRGB - beginRowRGB) / 2;

            ParallelBinarizedChanges t1 = new ParallelBinarizedChanges(imageWidth, beginRowRGB, (beginRowRGB + newAssignedHeight), currentImage, initialImage, assignedHeight, resultImageMatrix);
            ParallelBinarizedChanges t2 = new ParallelBinarizedChanges(imageWidth, (beginRowRGB + newAssignedHeight), endRowRGB, currentImage, initialImage, assignedHeight, resultImageMatrix);

            invokeAll(t1, t2);
        }
    }   

}