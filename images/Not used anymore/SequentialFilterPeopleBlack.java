/*
 *		Example file for Java Lab 3
 */

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Grey {

    public static void main(String[] args) throws IOException {

        BufferedImage img = ImageIO.read(new File("QueueColor.jpg"));
        int numRows = img.getHeight();
        int numCols = img.getWidth();

        long beginTimeInMiliseconds = System.currentTimeMillis();

        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numCols; j++) {
                int rgb = img.getRGB(j, i);

                int red = rgb & 0xFF;
                int green = (rgb >> 8) & 0xFF;
                int blue = (rgb >> 16) & 0xFF;

                // set people black
                float L = (float) (0 * (float) red + 0 * (float) green + 0 * (float) blue);

                int color;

                //System.out.println("red: " + red + " green: " + green + " blue: " + blue);

                if ((red == 255) && (green == 255) && (blue == 255)) {
                    
                    color = 215 * (int) L / 255;
                    color = (color << 8) | 215 * (int) L / 255;
                    color = (color << 8) | 215 * (int) L / 255;

                    img.setRGB(j, i, color);
                }
            }
        }
        long finishTimeInMiliseconds = System.currentTimeMillis();
        ImageIO.write(img, "jpg", new File("class.jpg"));
        System.out.print("Finished");

        

        System.out.print("Total time taken in miliseconds: " + (finishTimeInMiliseconds - beginTimeInMiliseconds));
    }
}
