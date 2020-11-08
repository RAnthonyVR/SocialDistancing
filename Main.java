import javax.imageio.ImageIO;
import java.io.IOException;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.*;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

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
        BufferedImage scenarioImage = ImageIO.read(new File("images/camera4/emptyClinic.jpg"));
        int height = scenarioImage.getHeight();
        int width = scenarioImage.getWidth();

        // Initial configuration, (entering the horizontal distance of the room)
        System.out.println("Initial configuration");
        Scanner distanceScanner = new Scanner(System.in);
        // horizontal distance in M (2 Dimentional) to scale from pixels to M
        System.out.println("Please enter the horizontal distance of the room in meters");
        double horizontal_distance_in_the_room_meters = distanceScanner.nextDouble(); // 3.5 meters
        double social_distance_meters = 1; // meters

        // simple rule of 3
        double social_distance_pixel = (width * social_distance_meters) / horizontal_distance_in_the_room_meters;

        int social_distance_pixelscale = (int)social_distance_pixel;

        //System.out.println("Social distance in pixels: " + social_distance_pixelscale);

        // Receives an image update from the camera
        BufferedImage currentImage = ImageIO.read(new File("images/camera4/5.jpg"));

        // The actual number of threads that will process the image
        int numberOfThreads = 2;

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

        System.out.println("\nPROCESING TIME INFORMATION:");
        System.out.println("Total time taken to detect changes and binarize on parallel in miliseconds using "+ numberOfThreads + " threads: " + (finishTimeInMiliseconds - beginTimeInMiliseconds));
        
        // Compare time with sequential version
        /*
        beginTimeInMiliseconds = System.currentTimeMillis();
        SequentialBinarizedChanges sbc = new SequentialBinarizedChanges(width, height);
        sbc.compute (width, height, currentImageMatrix, scenarioImageMatrix, resultImageMatrix, currentImage);
        finishTimeInMiliseconds = System.currentTimeMillis();
        System.out.println("Total time taken to detect changes and binarize on sequential in miliseconds: " + (finishTimeInMiliseconds - beginTimeInMiliseconds));
        */

        //currentImage = imageConverter.ConvertMatrixToImage(resultImageMatrix, currentImage, width, height);
        //ImageIO.write(currentImage, "jpg", new File("images/camera4/detectedBinarizedChanges.jpg"));

        // 4.1 Reduce image noise with median filter

        assignedHeight = (height - 1);

        int [] reducedNoiseImageMatrix = new int[height * width];

        ParallelNoiseReducer pnr = new ParallelNoiseReducer (width, height, 0, height, resultImageMatrix, assignedHeight, reducedNoiseImageMatrix);

        threadPool.invoke(pnr);

        currentImage = imageConverter.ConvertMatrixToImage(reducedNoiseImageMatrix, currentImage, width, height);
        
        ImageIO.write(currentImage, "jpg", new File("images/camera4/imageWithoutNoise.jpg"));

        // 5•	Analyzing the distances between the new objects in the scene to calculate the distance between them.

        //imageConverter.PrintImageMatrix(testresultImageMatrix);

        int assignedWidth = width / numberOfThreads;

        // System.out.println("assignedWidth: " + assignedWidth);

        People people = new People();
        
        ParallelPeopleDetector ppd = new ParallelPeopleDetector(0, width, reducedNoiseImageMatrix, assignedWidth, height, width, 0, people);
        beginTimeInMiliseconds = System.currentTimeMillis();
        threadPool.invoke(ppd);
        finishTimeInMiliseconds = System.currentTimeMillis();

        System.out.println("Total time taken to detect people on parallel in miliseconds using "+ numberOfThreads + " threads: " + (finishTimeInMiliseconds - beginTimeInMiliseconds));
        
        // JOIN OF THE RESULTS

        ArrayList<Integer> peopleHorizontalStartBoundaries = people.getHorizontalStartBoundaries();
        ArrayList<Integer> peopleHorizontalEndBoundaries = people.getHorizontalEndBoundaries();

        /* DEBUGGING
        if (peopleHorizontalStartBoundaries.isEmpty()) {
            System.out.println("peopleHorizontalStartBoundaries vacio");
        }else{
            System.out.println(peopleHorizontalStartBoundaries.size());
            for (int i = 0; i < peopleHorizontalStartBoundaries.size(); i++) {
                System.out.print(peopleHorizontalStartBoundaries.get(i) + " ");
            }
        }
        System.out.print("\n");

        if (peopleHorizontalEndBoundaries.isEmpty()) {
            System.out.println("peopleHorizontalEndBoundaries vacio");
        }else{
            System.out.println(peopleHorizontalEndBoundaries.size());
            for (int i = 0; i < peopleHorizontalEndBoundaries.size(); i++) {
                System.out.print(peopleHorizontalEndBoundaries.get(i) + " ");
            }
        }*/

        Collections.sort(peopleHorizontalStartBoundaries);
        Collections.sort(peopleHorizontalEndBoundaries);

        ArrayList<Integer> normPeopleHorizontalStartBoundaries = new ArrayList<Integer>();
        ArrayList<Integer> normPeopleHorizontalEndBoundaries = new ArrayList<Integer>();

        // Normalized boundaries (join them)
       
        // in case a person was divided by the threads, there is no case in which it is smaller, (start position is always considered)
        
        if (peopleHorizontalStartBoundaries.size() > peopleHorizontalEndBoundaries.size()) {
            int j = 0;
            boolean alreadyAddedStart = false;
            for (int i = 0; i < peopleHorizontalStartBoundaries.size(); i++) {
                if (j < peopleHorizontalEndBoundaries.size())  {
                    if ((peopleHorizontalStartBoundaries.get(i) < peopleHorizontalEndBoundaries.get(j)) && !alreadyAddedStart) {
                        normPeopleHorizontalStartBoundaries.add(peopleHorizontalStartBoundaries.get(i));
                        alreadyAddedStart = true;
                    } else if ((peopleHorizontalStartBoundaries.get(i) >= peopleHorizontalEndBoundaries.get(j)) && alreadyAddedStart) {
                        normPeopleHorizontalStartBoundaries.add(peopleHorizontalStartBoundaries.get(i));
                        alreadyAddedStart = true;
                        j++;
                    }
                } else {
                    normPeopleHorizontalStartBoundaries.add(peopleHorizontalStartBoundaries.get(i));
                }
            }
            normPeopleHorizontalEndBoundaries = peopleHorizontalEndBoundaries;
            
        } else if (peopleHorizontalStartBoundaries.size() == peopleHorizontalEndBoundaries.size()) {
            normPeopleHorizontalStartBoundaries = peopleHorizontalStartBoundaries;
            normPeopleHorizontalEndBoundaries = peopleHorizontalEndBoundaries;
        }

        int numberOfPeople;

        numberOfPeople = Math.max(normPeopleHorizontalStartBoundaries.size(), normPeopleHorizontalEndBoundaries.size());
        
        System.out.println("\nOBTAINED INFORMATION: ");
        System.out.println("Number of people in the room: " + numberOfPeople);

        System.out.println("Starts in pixels: ");
        for (int i = 0; i < normPeopleHorizontalStartBoundaries.size(); i++) {
            System.out.print(normPeopleHorizontalStartBoundaries.get(i) + " ");
        }
        System.out.print("\n");
        System.out.println("Ends in pixels:");
        for (int i = 0; i < normPeopleHorizontalEndBoundaries.size(); i++) {
            System.out.print(normPeopleHorizontalEndBoundaries.get(i) + " ");
        }
        
        if (numberOfPeople > 1) {
            System.out.println("\nDistances between them in pixels: ");
            for (int i = 0; i < numberOfPeople - 1; i++) {
                System.out.print(normPeopleHorizontalStartBoundaries.get(i + 1) - normPeopleHorizontalEndBoundaries.get(i) + " ");
            }
        }

        if (numberOfPeople > 1) {
            System.out.println("\nDistances between them in meters: ");
            for (int i = 0; i < numberOfPeople - 1; i++) {
                double distanceInPixels = normPeopleHorizontalStartBoundaries.get(i + 1) - normPeopleHorizontalEndBoundaries.get(i);
                double distanceInMeters = (horizontal_distance_in_the_room_meters * distanceInPixels) / width;
                System.out.print(String.format("%.2f", distanceInMeters) + " ");
                if (distanceInMeters >= social_distance_meters) {
                    System.out.print(" = Respecting social distance");
                } else {
                    System.out.print("ALERT! Not respecting social distance");
                    // sound alert
                    // Consulted: https://www.youtube.com/watch?v=8NUSbY_7Joc
                    int a = 7;
                    char beep = (char)a;
                    System.out.print(beep);
                }
            }
            
        }
        

        System.out.print("\n\n");
        
    }

}