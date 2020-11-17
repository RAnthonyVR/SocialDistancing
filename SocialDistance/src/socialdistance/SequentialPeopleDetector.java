/*
* Ricardo Antonio Vázquez Rodríguez A01209245
* Final Project
* SequentialPeopleDetector class
*/

package socialdistance;

//Algorithm:
    /*
    1. Start boundaries: Swipe from top to bottom if there is a black pixel then there is a person starting boundary) 
     (if it happens again on the next column then it is still the same person), include previous column to get context of the boundaries
    2. End boundaries: Else if there was a black pixel and then only white pixels in a column, a space is represented.
    */

// sets the start and end boundaries of people in an image
public class SequentialPeopleDetector {
    
    // attributes
    private int beginColumnRGB;
    private int endColumnRGB;
    private int[] currentImageMatrix; //array of binarized image to get the value
    private int imageHeight;
    private int imageWidth;
    private int assignedWidth;
    private People people; // shared object to save the boundaries

    /* Constructor that receives the binarized image to calculate the start and end boundaries as an array, as well as the corresponding dimentions*/
    public SequentialPeopleDetector (int beginColumnRGB, int endColumnRGB, int[] currentImageMatrix, int assignedWidth, int height, int width, People people) {
        this.beginColumnRGB = beginColumnRGB;
        this.endColumnRGB = endColumnRGB;
        this.currentImageMatrix = currentImageMatrix;
        this.imageHeight = height;
        this.assignedWidth = assignedWidth;
        this.imageWidth = width;
        this.people = people;
    }

    // getting start and end boundaries
    public void compute() {
        // boolean value to keep track if a start bound has been set or not
        boolean previousColumnHadSomething = false;
        
        // Parse the complete columns of the image, for each iterate the rows until finding a value in it
        for (int column = beginColumnRGB; column < endColumnRGB; column++) {
            // starting row
            int row = 0;
            // get the rgb value of each pixel in the image
            int rgb = currentImageMatrix [column + (row * imageWidth)]; 
            /* get the color value for each RGB, to do so, shift to the beggining position of each color, and use 0xFF mask 
                 this allows to ignore other bits in case shift affected other values */
            int red = rgb & 0xFF;
            int green = (rgb >> 8) & 0xFF;
            int blue = (rgb >> 16) & 0xFF;

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

            // if there is something there (the row isn't completelly white)
            if ((row != imageHeight) && !previousColumnHadSomething) {
                previousColumnHadSomething = true;
                //this.people.addHorizontalStartBoundary(column);
                //System.out.println("ID: " + this.id + " Starting column: " + column);
            } else if ((row == imageHeight) && previousColumnHadSomething){
                previousColumnHadSomething = false;
                //this.people.addHorizontalEndBoundary(column);
                //System.out.println("ID: " + this.id + " Ending column: " + column);
            }

        }
    }

}