import javax.imageio.ImageIO;
import java.io.IOException;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.*;
import java.util.Arrays;

/* Using median filter to reduce image noice */

public class SequentialNoiseReducer {
    
    private int beginRowRGB;
    private int endRowRGB;
    private int [] currentImage;
    private int [] resultImageMatrix;
    private int imageWidth;
    private int assignedHeight;
    private int [] kernel;
    private int medianValue;


    public SequentialNoiseReducer (int width, int beginRowRGB, int endRowRGB, int [] currentImage, int assignedHeight, int [] resultImageMatrix) {
        this.imageWidth = width;
        this.beginRowRGB = beginRowRGB;
        this.endRowRGB = endRowRGB;
        this.assignedHeight = assignedHeight;
        this.currentImage = currentImage;
        this.resultImageMatrix = resultImageMatrix;
        this.kernel = new int [9]; // 3 X 3 matrix
    }

    protected void compute() {

        System.out.println("HOLA");
        
        // Iterate through the complete image
        for (int row = beginRowRGB; row < endRowRGB; row++) {
            for (int column = 1; column < (imageWidth - 1); column++) { // exclude left and right columns of the image

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