import javax.imageio.ImageIO;
import java.io.IOException;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.*;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;

/*
Logic:
    1•	Have an reference of an image of the scene before the people arrive.
    2•  Receives an image from the camera to process in a sequential manner.
    3•	Detect if someone has arrived or something changes by marking the differences between these images.
    4•	Applying a binary filter to creaarly mark the differences detected and differentiate between empys spaces and objects.
    5•	Analyzing the distances between the new objects in the scene to calculate the distance between them.
 */

public class Main {
    
    public static void main(String[] args) throws IOException {

        // 1•	Have an reference of an image of the scene before the people arrive.

        // Read the background image (without people)
        BufferedImage scenarioImage = ImageIO.read(new File("images/camera1/emptyClinic.jpg"));
        int height = scenarioImage.getHeight();
        int width = scenarioImage.getWidth();

        // horizontal distance in M (2 Dimentional) to scale from pixels to M
        double horizontal_distance_in_the_room_meters = 3.5; // meters
        double social_distance_meters = 1.8; // meters
        // simple rule of 3
        double social_distance_pixel = (width * social_distance_meters) / horizontal_distance_in_the_room_meters;

        int social_distance_pixelscale = (int)social_distance_pixel;

        // Receives an image update from the camera
        BufferedImage currentImage = ImageIO.read(new File("images/camera1/clinic.jpg"));

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
        // 4•	Applying a binary filter to creaarly mark the differences detected and differentiate between empys spaces and objects.
        ParallelBinarizedChanges pbc = new ParallelBinarizedChanges (width, 0, height, currentImageMatrix, scenarioImageMatrix, assignedHeight, resultImageMatrix);
        ForkJoinPool threadPool = new ForkJoinPool();
        
        // Measure the time taken to binarize the image with the changes
        long beginTimeInMiliseconds = System.currentTimeMillis();
        threadPool.invoke(pbc);
        long finishTimeInMiliseconds = System.currentTimeMillis();

        System.out.println("Total time taken to detect changes and binarize on parallel in miliseconds using "+ numberOfThreads + " threads: " + (finishTimeInMiliseconds - beginTimeInMiliseconds));
        /*
        beginTimeInMiliseconds = System.currentTimeMillis();
        SequentialBinarizedChanges sbc = new SequentialBinarizedChanges(width, height);
        sbc.compute (width, height, currentImageMatrix, scenarioImageMatrix, resultImageMatrix, currentImage);
        finishTimeInMiliseconds = System.currentTimeMillis();
        System.out.println("Total time taken to detect changes and binarize on sequential in miliseconds: " + (finishTimeInMiliseconds - beginTimeInMiliseconds));
        */
        currentImage = imageConverter.ConvertMatrixToImage(resultImageMatrix, currentImage, width, height);

        ImageIO.write(currentImage, "jpg", new File("images/camera1/detectedBinarizedChanges.jpg"));

        // 5•	Analyzing the distances between the new objects in the scene to calculate the distance between them.

        //---------TESTING----------------
        BufferedImage testImage = ImageIO.read(new File("images/camera1/threeStars.jpg"));
        width = testImage.getWidth();
        height = testImage.getHeight();
        int [] testresultImageMatrix = imageConverter.ConvertImageToMatrix(testImage, width, height);
        

        BufferedImage getback = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        getback = imageConverter.ConvertMatrixToImage(testresultImageMatrix, getback, width, height);

        ImageIO.write(getback, "jpg", new File("estoSale.jpg"));

        //imageConverter.PrintImageMatrix(testresultImageMatrix);


        int assignedWidth = width / numberOfThreads;
        // System.out.println("assignedWidth: " + assignedWidth);

        People people = new People();

        
        ParallelPeopleDetector ppd = new ParallelPeopleDetector(0, width, testresultImageMatrix, assignedWidth, height, width, 0, people);
        beginTimeInMiliseconds = System.currentTimeMillis();
        threadPool.invoke(ppd);
        finishTimeInMiliseconds = System.currentTimeMillis();

        System.out.println("Total time taken to detect people on parallel in miliseconds using "+ numberOfThreads + " threads: " + (finishTimeInMiliseconds - beginTimeInMiliseconds));
        
        /*
        System.out.println("WIDTH: " + width);
        System.out.println("height: " + height);

        SequentialPeopleDetector spd = new SequentialPeopleDetector(0, width, resultImageMatrix, width, height, people);
        beginTimeInMiliseconds = System.currentTimeMillis();
        spd.compute();
        finishTimeInMiliseconds = System.currentTimeMillis();

        System.out.println("Total time taken to detect people on sequential in miliseconds: " + (finishTimeInMiliseconds - beginTimeInMiliseconds));
        */

        ArrayList<Integer> peopleHorizontalStartBoundaries = people.getHorizontalStartBoundaries();
        ArrayList<Integer> peopleHorizontalEndBoundaries = people.getHorizontalEndBoundaries();

        Collections.sort(peopleHorizontalStartBoundaries);
        Collections.sort(peopleHorizontalEndBoundaries);

        int numberOfPeople = Math.min(peopleHorizontalStartBoundaries.size(), peopleHorizontalEndBoundaries.size());

        System.out.println("\nNumber of people in the room: " + numberOfPeople + "\n");

        /*
        System.out.println("Start\n");
        for (int i = 0; i < peopleHorizontalStartBoundaries.size(); i++) {
            System.out.println(peopleHorizontalStartBoundaries.get(i));
        }
        
        System.out.println("\nEnd\n");
        for (int i = 0; i < peopleHorizontalEndBoundaries.size(); i++) {
            System.out.println(peopleHorizontalEndBoundaries.get(i));
        }*/

        ArrayList<Integer> normPeopleHorizontalStartBoundaries = new ArrayList<Integer>();
        ArrayList<Integer> normPeopleHorizontalEndBoundaries = new ArrayList<Integer>();

        // Normalized boundaries (join them)
        int j = 0;

        // in case a person was divided by the threads
        if (peopleHorizontalStartBoundaries.size() > peopleHorizontalEndBoundaries.size()) {
            for (int i = 0; i < numberOfPeople; i++) {
                normPeopleHorizontalStartBoundaries.add(peopleHorizontalStartBoundaries.get(j));
                while ((j < peopleHorizontalStartBoundaries.size()) && (peopleHorizontalEndBoundaries.get(i) >= peopleHorizontalStartBoundaries.get(j))) {
                    j++;
                }
            }
            normPeopleHorizontalEndBoundaries = peopleHorizontalEndBoundaries;
        }

        
        System.out.println("Start\n");
        for (int i = 0; i < normPeopleHorizontalStartBoundaries.size(); i++) {
            System.out.println(normPeopleHorizontalStartBoundaries.get(i));
        }
        
        System.out.println("\nEnd\n");
        for (int i = 0; i < normPeopleHorizontalEndBoundaries.size(); i++) {
            System.out.println(normPeopleHorizontalEndBoundaries.get(i));
        }

        System.out.println("\nDistances between them:\n");
        for (int i = 0; i < numberOfPeople - 1; i++) {
            System.out.println(normPeopleHorizontalStartBoundaries.get(i + 1) - normPeopleHorizontalEndBoundaries.get(i));
        }
        
    }

}