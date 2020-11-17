/*
* Ricardo Antonio Vázquez Rodríguez A01209245
* Final Project
* SequentialNoiseReducer class
*/

package socialdistance;

import java.util.Arrays;

/* Using median filter to reduce image noise, for each pixel in the image it gets the 8 neightbours, sorts them and assigns the value in the middle as a result. */

public class SequentialNoiseReducer {
    // attributes
    private int beginRowRGB; //assigned rows (complete)
    private int endRowRGB;
    private int [] currentImage; // array (image) with noise
    private int [] resultImageMatrix; // resulting array (image without noise)
    private int imageWidth;
    private int assignedHeight;
    private int [] kernel; // neightbours and current pixel
    private int medianValue;
    private int imageHeight; // images dimentions

    /* Constructor that receives the image with noise as an array, as well as the corresponding dimentions*/
    public SequentialNoiseReducer (int width, int height, int beginRowRGB, int endRowRGB, int [] currentImage, int assignedHeight, int [] resultImageMatrix) {
        this.imageWidth = width;
        this.imageHeight = height;
        this.beginRowRGB = beginRowRGB;
        this.endRowRGB = endRowRGB;
        this.assignedHeight = assignedHeight;
        this.currentImage = currentImage;
        this.resultImageMatrix = resultImageMatrix;
        this.kernel = new int [9]; // 3 X 3 matrix
    }

    // getting the median value for each pixel of the image
    protected void compute() {
        // Iterate through the complete image
        for (int row = beginRowRGB; row < endRowRGB; row++) {
            for (int column = 0; column < imageWidth; column++) { // exclude left and right columns of the image
                // if it is a pixel on the edge of the image, copy the original one
                if ((row == 0) || (column == 0) || (row == (imageHeight - 1)) || (column == (imageWidth - 1))) {
                    resultImageMatrix[column + (row * imageWidth)] = currentImage [column + (row * imageWidth)];
                } else { // else get the median of them
                    int kernel_index = 0;
                    // for each pixel save the neighbours in the kernel (3 X 3), sort and get value in the middle
                    for (int kernel_row = (row - 1); kernel_row <= (row + 1); kernel_row++) {
                        for (int kernel_column = (column - 1); kernel_column <= (column + 1); kernel_column++) {
                            kernel[kernel_index] = currentImage [kernel_column + (kernel_row * imageWidth)];
                            kernel_index ++;
                        }
                    }
                    // sort in ascending order
                    Arrays.sort(kernel);
                    // get the value in the middle (index 5 is in the middle of a 9 indexed kernel)
                    medianValue = kernel[5];
                    // Assign it to the result
                    resultImageMatrix[column + (row * imageWidth)] = medianValue;
                }                
            }
        }

    }

}