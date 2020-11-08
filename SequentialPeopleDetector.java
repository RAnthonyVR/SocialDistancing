import javax.imageio.ImageIO;
import java.io.IOException;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.*;

import java.awt.Color;

public class SequentialPeopleDetector {
    
    private int beginColumnRGB;
    private int endColumnRGB;
    private int[] currentImageMatrix;
    private int imageHeight;
    private int imageWidth;
    private int assignedWidth;
    private People people;

    public SequentialPeopleDetector (int beginColumnRGB, int endColumnRGB, int[] currentImageMatrix, int assignedWidth, int height, int width, People people) {
        this.beginColumnRGB = beginColumnRGB;
        this.endColumnRGB = endColumnRGB;
        this.currentImageMatrix = currentImageMatrix;
        this.imageHeight = height;
        this.assignedWidth = assignedWidth;
        this.imageWidth = width;
        this.people = people;
    }

    public void compute() {

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