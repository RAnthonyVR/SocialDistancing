/*
* Ricardo Antonio Vázquez Rodríguez A01209245
* Final Project
* ParallelBinarizedChanges class
*/

package socialdistance;

import java.util.concurrent.*;

/* ParallelBinarizedChanges class that extends RecursiveAction to be called by forkjoin (void ForkJoinTasks) 
   It receives a section of the two images (each divided vertically by rows) and if the difference between two images in a pixel is lower than tolerance
   sets it black, else white */

public class ParallelBinarizedChanges extends RecursiveAction {
    
    // attributes
    private int beginRowRGB; //assigned rows
    private int endRowRGB;
    private int [] initialImage;
    private int [] currentImage; // arrays (images) to substract
    private int [] resultImageMatrix; // resulting array
    private int imageWidth; 
    private int assignedHeight; // images dimentions

    /* Constructor that receives the two images to get the difference between them (with and whitout people) as arrays, as well as their corresponding dimentions and tasks to compute*/
    public ParallelBinarizedChanges (int width, int beginRowRGB, int endRowRGB, int [] currentImage, int [] initialImage, int assignedHeight, int [] resultImageMatrix) {
        this.imageWidth = width;
        this.beginRowRGB = beginRowRGB;
        this.endRowRGB = endRowRGB;
        this.initialImage = initialImage;
        this.assignedHeight = assignedHeight;
        this.currentImage = currentImage;
        this.resultImageMatrix = resultImageMatrix;
    }

    // method in which if an image is has been divided small enough, the task is executed (getting differences and turning pixels black or white)
    protected void computeDirectly() {

        // Just parse assigned rows buy all columns
        for (int row = beginRowRGB; row < endRowRGB; row++) {
            for (int column = 0; column < imageWidth; column++) {
                // get the rgb value of each pixel in the image
                int rgb = initialImage [column + (row * imageWidth)];
                int rgb2 = currentImage [column + (row * imageWidth)];
                
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

                // tolerance value, if a color of the pixel (RGB) between the two images changes more than this value, then it sets it black, else white
                int threshold = 30;

                // drastic changes (black)
                if ((Math.abs(red - red2) > threshold) || (Math.abs(green - green2) > threshold) || (Math.abs(blue - blue2) > threshold)) {
                    // System.out.println("" + rgb + rgb2);
                    resultImageMatrix[column + (row * imageWidth)] = color;
                } else { // small or no changes at all (white)
                    // insert white (255) to all RGB values, to set RGB value, left shift and joing with bitwise OR
                    color = 255; //blue
                    color = (color << 8) | 255; // green
                    color = (color << 8) | 255; // red

                    // set the RGB value to the resulting matrix
                    resultImageMatrix[column + (row * imageWidth)] = color;
                }
            }
        }
    }

    /* Compute method called by the fork, it compares the assigned values with a section to be divided, if it reaches that value, 
        it computes directly the execution, else, a new thread is created to divide the tasks */
    @Override
    protected void compute() {
        // If the assgined part/section of the image is divided to the value specified (threshold), compute directly
        if ((endRowRGB - beginRowRGB) <= assignedHeight) {
            computeDirectly();
        }
        // else divide the image into more sections and assign it to a new element.
        else {
            // divide the rows into two more elements
            int newAssignedHeight = (endRowRGB - beginRowRGB) / 2;
            
            // create and set their intervals
            ParallelBinarizedChanges t1 = new ParallelBinarizedChanges(imageWidth, beginRowRGB, (beginRowRGB + newAssignedHeight), currentImage, initialImage, assignedHeight, resultImageMatrix);
            ParallelBinarizedChanges t2 = new ParallelBinarizedChanges(imageWidth, (beginRowRGB + newAssignedHeight), endRowRGB, currentImage, initialImage, assignedHeight, resultImageMatrix);
            
            // both compute their assigned values, wait for the completion of created thread
            t2.fork();
            t1.compute();
            t2.join();
        }
    }   

}