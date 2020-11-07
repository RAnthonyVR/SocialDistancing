import javax.imageio.ImageIO;
import java.io.IOException;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.*;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;

public class MainTestNoise {
    
    public static void main(String[] args) throws IOException {

        // 1•	Have an reference of an image of the scene before the people arrive.

        // Read the background image (without people)
        BufferedImage scenarioImage = ImageIO.read(new File("images/camera4/emptyClinic.jpg"));
        int height = scenarioImage.getHeight();
        int width = scenarioImage.getWidth();

        // horizontal distance in M (2 Dimentional) to scale from pixels to M
        double horizontal_distance_in_the_room_meters = 3.5; // meters
        double social_distance_meters = 0.5; // meters
        // simple rule of 3
        double social_distance_pixel = (width * social_distance_meters) / horizontal_distance_in_the_room_meters;

        int social_distance_pixelscale = (int)social_distance_pixel;

        System.out.println("Social distance in pixels: " + social_distance_pixelscale);

        // Receives an image update from the camera
        BufferedImage currentImage = ImageIO.read(new File("images/camera4/8.jpg"));

        // The actual number of threads that will process the image
        int numberOfThreads = 4;

        // Assign a piece of the image to pe processed
        int assignedHeight = height / numberOfThreads;
        // difference should be zero
        int difference = height - (assignedHeight * numberOfThreads);
        
        ImageConverter imageConverter = new ImageConverter(width, height);
        int [] scenarioImageMatrix = imageConverter.ConvertImageToMatrix(scenarioImage, width, height);
        int [] currentImageMatrix = imageConverter.ConvertImageToMatrix(currentImage, width, height);

        int [] resultImageMatrix = new int[height * width];

        // 3•	Detect if someone has arrived or something changes by marking the differences between these images.
        // 4•	Applying a binary filter to crearly mark the differences detected and differentiate between empys spaces and objects.
        ParallelBinarizedChanges pbc = new ParallelBinarizedChanges (width, 0, height, currentImageMatrix, scenarioImageMatrix, assignedHeight, resultImageMatrix);
        ForkJoinPool threadPool = new ForkJoinPool();
        
        // Measure the time taken to binarize the image with the changes
        long beginTimeInMiliseconds = System.currentTimeMillis();
        threadPool.invoke(pbc);
        long finishTimeInMiliseconds = System.currentTimeMillis();

        System.out.println("Total time taken to detect changes and binarize on parallel in miliseconds using "+ numberOfThreads + " threads: " + (finishTimeInMiliseconds - beginTimeInMiliseconds));
        
        //ImageIO.write(currentImage, "jpg", new File("images/camera4/detectedBinarizedChanges.jpg"));

        // reduce noise

        assignedHeight = (height - 1);

        int [] reducedNoiseImageMatrix = new int[height * width];

        ParallelNoiseReducer pnr = new ParallelNoiseReducer (width, height, 0, height, resultImageMatrix, assignedHeight, reducedNoiseImageMatrix);
    
        threadPool.invoke(pnr);

        currentImage = imageConverter.ConvertMatrixToImage(reducedNoiseImageMatrix, currentImage, width, height);
        
        ImageIO.write(currentImage, "jpg", new File("images/camera4/imageWithoutNoise.jpg"));

    }

}