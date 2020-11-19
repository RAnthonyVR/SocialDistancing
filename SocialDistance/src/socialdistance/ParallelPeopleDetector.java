/*
* Ricardo Antonio Vázquez Rodríguez A01209245
* Final Project
* ParallelPeopleDetector class
*/

package socialdistance;

import java.util.concurrent.*;

/* ParallelPeopleDetector class that extends RecursiveAction to be called by forkjoin (void ForkJoinTasks), sets the start and end boundaries of people in the scene*/

//Algorithm:
    /*
    1. Start boundaries: Swipe from top to bottom if there is a black pixel then there is a person starting boundary) 
     (if it happens again on the next column then it is still the same person), include previous column to get context of the boundaries
    2. End boundaries: Else if there was a black pixel and then only white pixels in a column, a space is represented.
    */

public class ParallelPeopleDetector extends RecursiveAction {
    
    // attributes
    private int beginColumnRGB;
    private int endColumnRGB; //assigned rows
    private int [] currentImageMatrix; //array of binarized image to get the value
    private int imageHeight;
    private int imageWidth;
    private int assignedWidth;
    private People people; // shared object to save the boundaries

    /* Constructor that receives the binarized image to calculate the start and end boundaries as an array, as well as the corresponding dimentions and tasks to compute*/
    public ParallelPeopleDetector (int beginColumnRGB, int endColumnRGB, int [] currentImageMatrix, int assignedWidth, int height, int width, People people) {
        this.beginColumnRGB = beginColumnRGB;
        this.endColumnRGB = endColumnRGB;
        this.currentImageMatrix = currentImageMatrix;
        this.imageHeight = height;
        this.imageWidth = width;
        this.assignedWidth = assignedWidth;
        this.people = people;
    }
    
    // method in which if an image has been divided small enough, the task is executed (getting start and end boundaries)
    protected void computeDirectly() {
        // boolean value to keep track if a start bound has been set or not
        boolean previousColumnHadSomething = false;
        
        int row = 0;
        int rgb, red, green, blue;
        int previousCol;
        // check previous column to get context if it is a starting or ending bound
        if (beginColumnRGB != 0) {
            // start from previous column in case it is not the first
            previousCol = beginColumnRGB - 1;
            // get the rgb value of each pixel in the image
            rgb = currentImageMatrix [previousCol + (row * imageWidth)]; 
            /* get the color value for each RGB, to do so, shift to the beggining position of each color, and use 0xFF mask 
                 this allows to ignore other bits in case shift affected other values */
            red = rgb & 0xFF;
            green = (rgb >> 8) & 0xFF;
            blue = (rgb >> 16) & 0xFF;
            
            // if only white pixels in the images, keep parsing through the whole column unitl there is something or it reaches the end
            while ((red == 255) && (green == 255) && (blue == 255) && (row < imageHeight)) {
                // get the rgb value
                rgb = currentImageMatrix [previousCol + (row * imageWidth)];
                // update each color
                red = rgb & 0xFF;
                green = (rgb >> 8) & 0xFF;
                blue = (rgb >> 16) & 0xFF;
                // check the next row in the same column
                row++;
            }
            // if there was something previously to this thread's work, update boolean
            if (row != imageHeight) {
                previousColumnHadSomething = true;
            }
        }
        
        // Parse the assigned columns, for each iterate the rows until finding a value in it
        for (int column = beginColumnRGB; column < endColumnRGB; column++) {
            // starting row
            row = 0;
            // get the rgb value of each pixel in the image
            rgb = currentImageMatrix [column + (row * imageWidth)]; 
            /* get the color value for each RGB, to do so, shift to the beggining position of each color, and use 0xFF mask 
                 this allows to ignore other bits in case shift affected other values */
            red = rgb & 0xFF;
            green = (rgb >> 8) & 0xFF;
            blue = (rgb >> 16) & 0xFF;

            // if only white pixels in the images, keep parsing through the whole column unitl there is something or it reaches the end
            while ((red == 255) && (green == 255) && (blue == 255) && (row < imageHeight)) {
                // get the rgb value
                rgb = currentImageMatrix [column + (row * imageWidth)];
                // update each color
                red = rgb & 0xFF;
                green = (rgb >> 8) & 0xFF;
                blue = (rgb >> 16) & 0xFF;
                // check the next row in the same column
                row++;
            }

            // if there is something there (the row isn't completelly white) and there was nothing there before, save it as a staring value
            if ((row != imageHeight) && !previousColumnHadSomething) {
                previousColumnHadSomething = true;
                this.people.addHorizontalStartBoundary(column);
                //System.out.println(" Starting column: " + column);
            } else if ((row == imageHeight) && previousColumnHadSomething){ // if there was nothing then set it as an ending value
                previousColumnHadSomething = false;
                this.people.addHorizontalEndBoundary(column);
                //System.out.println(" Ending column: " + column);
            }

        }
    }

    /* Compute method called by the fork, it compares the assigned values with a section to be divided, if it reaches that value, 
        it computes directly the execution, else, a new thread is created to divide the tasks */
    @Override
    protected void compute() {
        // if the task (subsection of an image) is small enough than specified value, the thread computes the operations to detect people boundaries
        if ((endColumnRGB - beginColumnRGB) <= assignedWidth) {
            computeDirectly();
        }
        // divide the rows into two more elements recursivelly
        else {
            int newassignedWidth = (endColumnRGB - beginColumnRGB) / 2;
            // create workers and set their intervals
            ParallelPeopleDetector t1 = new ParallelPeopleDetector(beginColumnRGB, (beginColumnRGB + newassignedWidth), currentImageMatrix, assignedWidth, imageHeight, imageWidth, people);
            ParallelPeopleDetector t2 = new ParallelPeopleDetector((beginColumnRGB + newassignedWidth), endColumnRGB, currentImageMatrix, assignedWidth, imageHeight, imageWidth, people);
            // both threads compute their assigned values or tasks, recursive method that assigns work to thread threads and executes in order,
            // could also use fork() and join(), doing t2.fork(); t1.compute(); t2.join(); but they are more ideal for Recursive Tasks 
            invokeAll(t1, t2);
        }
    }   

}