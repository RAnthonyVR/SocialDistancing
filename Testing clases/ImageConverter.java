import javax.imageio.ImageIO;
import java.io.IOException;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.*;

import java.awt.Color;

/* Class that transforms an image to a matrix or vicersa in order to be computed directly, performs reading operations */

public class ImageConverter {
    
    private int width;
    private int height;

    public ImageConverter (int width, int height) {
        this.width = width;
        this.height = height;
    }
    
    public int[] ConvertImageToMatrix (BufferedImage image, int width, int height) {
        int[] matrixedImage = new int[ height * width]; //= image.getRGB(0, 0, width, height, null, width);
        int rgb;
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                rgb = image.getRGB(col, row);
                matrixedImage [col + (row * width)] = rgb;
            }
        }
        return matrixedImage;
    }

    public BufferedImage ConvertMatrixToImage (int[] matrixedImage, BufferedImage image, int width, int height) {
        //BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        //image.setRGB(0, 0, this.width, this.height, matrixedImage, this.width);
        int rgb;

        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                rgb = matrixedImage [col + (row * width)];
                image.setRGB(col, row, rgb);
            }
        }
        return image;
    }

    public void PrintImageMatrix (int[] matrixedImage){
        int rgb = matrixedImage [0];
        System.out.print("WHITE: "+rgb +"\n");

        int red = rgb & 0xFF;
        int green = (rgb >> 8) & 0xFF;
        int blue = (rgb >> 16) & 0xFF;
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                rgb = matrixedImage [col + (row * width)];
                // white
                if (rgb == -1) {
                    System.out.print(0);
                } else { //black
                    System.out.print(1);
                }
            }
            System.out.print("\n");
        }
    }

}