import javax.imageio.ImageIO;
import java.io.IOException;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.*;
import java.io.FileWriter;

import java.awt.Color;

public class ParallelPeopleDetector extends RecursiveAction {
    
    private int beginColumnRGB;
    private int endColumnRGB;
    private int [] currentImageMatrix;
    private int imageHeight;
    private int imageWidth;
    private int assignedWidth;
    private int id;
    private People people;

    //Algorithm:

    /*First option
    1. Count the number of people in the room (swipe from top to bottom if at least 300 or threshold pixels are nearby it is a person) 
     (if it happens again on the next column then it is still the same person)
    2. Optional (more precise) get width of a person (to know the distance between them) Min start position if there is an empty ron between them start checking for other min start position
    3. Check distance between them
    */

    /* Other option
        1. Count the number of people (swipe from left to right counting nearby pixels)
        2. Save max width of persons ()
    */

    public ParallelPeopleDetector (int beginColumnRGB, int endColumnRGB, int [] currentImageMatrix, int assignedWidth, int height, int width, int id, People people) {

        this.beginColumnRGB = beginColumnRGB;
        this.endColumnRGB = endColumnRGB;
        this.currentImageMatrix = currentImageMatrix;
        this.imageHeight = height;
        this.imageWidth = width;
        this.assignedWidth = assignedWidth;
        this.id = id;
        this.people = people;
    }

    protected void computeDirectly() {

        boolean previousColumnHadSomething = false;

        for (int column = beginColumnRGB; column < endColumnRGB; column++) {
            
            int row = 0;

            int rgb = currentImageMatrix [column + (row * imageWidth)]; 

            int red = rgb & 0xFF;
            int green = (rgb >> 8) & 0xFF;
            int blue = (rgb >> 16) & 0xFF;

            
            while ((red == 255) && (green == 255) && (blue == 255) && (row < imageHeight)) {

                rgb = currentImageMatrix [column + (row * imageWidth)];

                red = rgb & 0xFF;
                green = (rgb >> 8) & 0xFF;
                blue = (rgb >> 16) & 0xFF;

                row++;
            }

            // if there is something there (the row isn't completelly white)
            if ((row != imageHeight) && !previousColumnHadSomething) {
                previousColumnHadSomething = true;
                this.people.addHorizontalStartBoundary(column);
                System.out.println("ID: " + this.id + " Starting column: " + column);
            } else if ((row == imageHeight) && previousColumnHadSomething){
                previousColumnHadSomething = false;
                this.people.addHorizontalEndBoundary(column);
                System.out.println("ID: " + this.id + " Ending column: " + column);
            }

        }
    }

    protected void compute() {
        // if it divided small enough
        if ((endColumnRGB - beginColumnRGB) <= assignedWidth) {
            computeDirectly();
        }
        // else divide into smaller chunks
        else {
            int newassignedWidth = (endColumnRGB - beginColumnRGB) / 2;

            ParallelPeopleDetector t1 = new ParallelPeopleDetector(beginColumnRGB, (beginColumnRGB + newassignedWidth), currentImageMatrix, assignedWidth, imageHeight, imageWidth, this.id + 1, people);
            ParallelPeopleDetector t2 = new ParallelPeopleDetector((beginColumnRGB + newassignedWidth), endColumnRGB, currentImageMatrix, assignedWidth, imageHeight, imageWidth, this.id + 2, people);

            invokeAll(t1, t2);
        }
    }   

}