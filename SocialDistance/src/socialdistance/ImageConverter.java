/*
* Ricardo Antonio Vázquez Rodríguez A01209245
* Final Project
* ImageConverter class
*/

package socialdistance;

import java.awt.image.BufferedImage;

/* Class that transforms an image to a matrix or vicersa in order to be computed directly, performs reading operations */

public class ImageConverter {
    // image dimentions
    private int width;
    private int height;

    // Constructor
    public ImageConverter (int width, int height) {
        this.width = width;
        this.height = height;
    }
    
    /* Method that transforms an image (BufferedImage into a matrix)
    Parameters: BufferedImage image, int width, int height
    Returns: array with the RGB values of the image*/
    public int[] ConvertImageToMatrix (BufferedImage image, int width, int height) {
        // create array
        int[] matrixedImage = new int[ height * width];
        int rgb;
        // iterate through the image, get the RGB value and assign it to the array.
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                rgb = image.getRGB(col, row);
                matrixedImage [col + (row * width)] = rgb;
            }
        }
        return matrixedImage;
    }

    /* Method that transfors a matrix into an image
    Parameters: int[] matrixedImage, BufferedImage image, int width, int height
    Returns: BufferedImage*/
    public BufferedImage ConvertMatrixToImage (int[] matrixedImage, BufferedImage image, int width, int height) {

        int rgb;

        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                rgb = matrixedImage [col + (row * width)];
                image.setRGB(col, row, rgb);
            }
        }
        return image;
    }

}