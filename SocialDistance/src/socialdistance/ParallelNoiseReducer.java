/*
* Ricardo Antonio Vázquez Rodríguez A01209245
* Final Project
* ParallelNoiseReducer class
*/

package socialdistance;

import java.util.concurrent.*;
import java.util.Arrays;

/* Using median filter to reduce image noise, for each pixel in the image it gets the 8 neightbours, sorts them and assigns the value in the middle as a result. */
public class ParallelNoiseReducer extends RecursiveAction {
    
    // attributes
    private int beginRowRGB; //assigned rows
    private int endRowRGB;
    private int [] currentImage; // array (image) with noise
    private int [] resultImageMatrix; // resulting array (image without noise)
    private int imageWidth;
    private int assignedHeight;
    private int [] kernel; // neightbours and current pixel
    private int medianValue;
    private int imageHeight; // images dimentions

    /* Constructor that receives the image with noise as an array, as well as the corresponding dimentions and tasks to compute*/
    public ParallelNoiseReducer (int width, int height, int beginRowRGB, int endRowRGB, int [] currentImage, int assignedHeight, int [] resultImageMatrix) {
        this.imageWidth = width;
        this.imageHeight = height;
        this.beginRowRGB = beginRowRGB;
        this.endRowRGB = endRowRGB;
        this.assignedHeight = assignedHeight;
        this.currentImage = currentImage;
        this.resultImageMatrix = resultImageMatrix;
        this.kernel = new int [9]; // 3 X 3 matrix (9 in 1D linearized array)
    }

    // method in which if a subsection of an image is small enough, the task is executed (getting the median value for each pixel)
    protected void computeDirectly() {        
        // Iterate through the complete image columns but divided in rows
        for (int row = beginRowRGB; row < endRowRGB; row++) {
            for (int column = 0; column < imageWidth; column++) { // exclude left and right columns of the image

                // if it is a pixel on the edge of the image, copy the original one
                if ((row == 0) || (column == 0) || (row == (imageHeight - 1)) || (column == (imageWidth - 1))) {
                    resultImageMatrix[column + (row * imageWidth)] = currentImage [column + (row * imageWidth)];
                } else { // else get the median of the corresponing pixel with its 8 neightbours
                    int kernel_index = 0;
                    // for each pixel save the neighbours in the kernel (3 X 3 = 9), sort and get value in the middle
                    for (int kernel_row = (row - 1); kernel_row <= (row + 1); kernel_row++) {
                        for (int kernel_column = (column - 1); kernel_column <= (column + 1); kernel_column++) {
                            kernel[kernel_index] = currentImage [kernel_column + (kernel_row * imageWidth)];
                            kernel_index ++;
                        }
                    }
                    // sort in ascending order the pixel with its 8 neightbours
                    Arrays.sort(kernel);
                    // get the value in the middle (index 5 is in the middle of a 9 indexed kernel)
                    medianValue = kernel[5];
                    // Assign it to the result
                    resultImageMatrix[column + (row * imageWidth)] = medianValue;
                }                
            }
        }
    }

    /* Compute method called by the fork, it compares the assigned values with a section to be divided, if it reaches that value, 
        it computes directly the execution, else, a new thread is created to divide the tasks */
    @Override
    protected void compute() {
        // if the task (subsection of an image) is small enough than specified value, the thread computes the operations
        if ((endRowRGB - beginRowRGB) <= assignedHeight) {
            computeDirectly();
        }
        else {
            // it the tasks are still bigger tham expected divide them into more threads
            // divide the rows into two more elements
            int newAssignedHeight = (endRowRGB - beginRowRGB) / 2;
            // create the new objects (workers and assign their subtasks limits)
            ParallelNoiseReducer t1 = new ParallelNoiseReducer(imageWidth, imageHeight, beginRowRGB, (beginRowRGB + newAssignedHeight), currentImage, assignedHeight, resultImageMatrix);
            ParallelNoiseReducer t2 = new ParallelNoiseReducer(imageWidth, imageHeight, (beginRowRGB + newAssignedHeight), endRowRGB, currentImage, assignedHeight, resultImageMatrix);
            // both threads compute their assigned values or tasks, recursive method that assigns work to thread threads and executes in order,
            // could also use fork() and join(), doing t2.fork(); t1.compute(); t2.join(); but they are more ideal for Recursive Tasks 
            invokeAll(t1, t2);
        }
    }   

}