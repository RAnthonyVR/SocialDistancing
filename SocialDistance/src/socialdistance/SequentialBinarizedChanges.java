/*
* Ricardo Antonio Vázquez Rodríguez A01209245
* Final Project
* SequentialBinarizedChanges class
*/

package socialdistance;

import java.awt.image.BufferedImage;
import java.awt.Color;

/* SequentialBinarizedChanges class that is used to compare with parallel implementation using 1 thread (main)
It receives a section of the two images and if the difference between two images in a pixel is lower than tolerance
   sets it black, else white*/

public class SequentialBinarizedChanges {
    // attributes
    private int [] initialImage;
    private int [] currentImage;
    private int imageWidth;
    private int imageHeight;

    /* Constructor that receives the corresponding dimentions */
    public SequentialBinarizedChanges (int width, int height) {
        this.imageWidth = width;
        this.imageHeight = height;
    }

    // method in which the task is executed (getting differences and turning pixels black or white)
    public int compute (int imageWidth, int imageHeight, int[] initialImage, int[] currentImageMatrix, int []resultImageMatrix, BufferedImage currentImage) {
        
        // Pase the complete image pixel by pixel
        for (int row = 0; row < imageHeight; row++) {
            for (int column = 0; column < imageWidth; column++) {
                // get the rgb value of each pixel in the image
                int rgb = initialImage [column + (row * imageWidth)];
                int rgb2 = currentImageMatrix [column + (row * imageWidth)];
                /* get the color value for each RGB, to do so, shift to the beggining position of each color, and use 0xFF mask 
                 this allows to ignore other bits in case shift affected other values */
                int red = rgb & 0xFF;
                int green = (rgb >> 8) & 0xFF;
                int blue = (rgb >> 16) & 0xFF;

                int red2 = rgb2 & 0xFF;
                int green2 = (rgb2 >> 8) & 0xFF;
                int blue2 = (rgb2 >> 16) & 0xFF;
                // set the color to black (0)
                int color = 0;

                 // drastic changes (black)
                if ((Math.abs(red - red2) > 50) || (Math.abs(green - green2) > 50) || (Math.abs(blue - blue2) > 50)) {
                    // System.out.println("" + rgb + rgb2);
                    resultImageMatrix[column + (row * imageWidth)] = color;
                } else { // small or no changes at all (white)
                    // insert white (255) to all RGB values, to set RGB value, left shift and joing with bitwise OR
                    color = 255; //blue
                    color = (color << 8) | 255; // green
                    color = (color << 8) | 255; //red
                    // set the RGB value to the resulting matrix
                    resultImageMatrix[column + (row * imageWidth)] = color;
                }
            }
            
        }
        return 0;
    }

}