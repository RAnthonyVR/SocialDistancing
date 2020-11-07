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
    private int width;
    private People people;

    public SequentialPeopleDetector (int beginColumnRGB, int endColumnRGB, int[] currentImageMatrix, int width, int height, People people) {
        this.currentImageMatrix = currentImageMatrix;
        this.imageHeight = height;
        this.width = width;
        this.people = people;
    }

    public void compute() {

        boolean previousColumnHadSomething = false;

        for (int column = 0; column < width; column++) {
            
            int row = 0;

            int rgb = currentImageMatrix [column + (row * width)]; 
            //System.out.println("RGB " + rgb);
            int red = rgb & 0xFF;
            int green = (rgb >> 8) & 0xFF;
            int blue = (rgb >> 16) & 0xFF;


            
            while ((red == 255) && (green == 255) && (blue == 255) && (row < imageHeight)) { //(red == 255) && (green == 255) && (blue == 255)
                
                rgb = currentImageMatrix [column + (row * width)]; 

                red = rgb & 0xFF;
                green = (rgb >> 8) & 0xFF;
                blue = (rgb >> 16) & 0xFF;

                row++;

                System.out.println("RGB WHITE " + rgb);
            }

            //System.out.println("Row: " + row);
            //System.out.println("Height: " + imageHeight);
            //System.out.println("RGB " + rgb);

            // if there is something there (the row isn't completelly white)
            if ((row != imageHeight) && !previousColumnHadSomething) {
                previousColumnHadSomething = true;
                //this.people.addHorizontalStartBoundary(column);
                //System.out.println("Starting column: " + column);
                
            } else if ((row == imageHeight) && previousColumnHadSomething){
                previousColumnHadSomething = false;
                //this.people.addHorizontalEndBoundary(column);
                //System.out.println("Ending column: " + column);
            }

        }
    }

}