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
    4.1•Reduce image noise with median filter
    5•	Analyzing the distances between the new objects in the scene to calculate the distance between them.
 */

public class Main {
    
    public static void main(String[] args) throws IOException {

        // 1•	Have an reference of an image of the scene before the people arrive.

        // Read the background image (without people)
        BufferedImage scenarioImage = ImageIO.read(new File("images/WaitingRoomQueueCamera/WaitingRoominGreen.jpg"));
        int height = scenarioImage.getHeight();
        int width = scenarioImage.getWidth();

        // Initial configuration, (entering the horizontal distance of the room)
        System.out.println("Initial configuration");
        Scanner distanceScanner = new Scanner(System.in);

        // horizontal distance in meters (2 Dimentional) to scale from pixels length to meters
        System.out.println("Please enter the horizontal distance of the room in meters");
        double horizontal_distance_in_the_room_meters = 3.5; //distanceScanner.nextDouble(); // 3.5 meters
        double social_distance_meters = 1; // meters

        // simple rule of 3 to get the social distance to pixels in the image (number of pixels must be integer)
        double social_distance_pixel = (width * social_distance_meters) / horizontal_distance_in_the_room_meters;
        int social_distance_pixelscale = (int)social_distance_pixel;

        // Receives an image update from the camera
        BufferedImage currentImage = ImageIO.read(new File("images/WaitingRoomQueueCamera/test3.jpg"));

        int cores = Runtime.getRuntime().availableProcessors();
        System.out.println("Cores: " + cores);
        
        // The actual number of threads that will process the image
        int numberOfThreads = 2;

        // Assign a piece of the image to pe processed
        int assignedHeight = height / numberOfThreads;
        // The difference should be zero
        int difference = height - (assignedHeight * numberOfThreads);

        //System.out.println("\nassignedHeight: " + assignedHeight + "\n");

        // Transform the both readed images to matrices to be processed, as well as allocating memory space for the resulting matrix
        ImageConverter imageConverter = new ImageConverter(width, height);
        int [] scenarioImageMatrix = imageConverter.ConvertImageToMatrix(scenarioImage, width, height);
        int [] currentImageMatrix = imageConverter.ConvertImageToMatrix(currentImage, width, height);
        int [] resultImageMatrix = new int[height * width];

        // 3•	Detect if someone has arrived or something changes by marking the differences between these images.
        // 4•	Applying a binary filter to crearly mark the differences detected and differentiate between empty spaces and objects.

        // Instantiate parallel class that subtracts the images and applies a binary filter, using a thread pool and fork join
        ParallelBinarizedChanges pbc = new ParallelBinarizedChanges (width, 0, height, currentImageMatrix, scenarioImageMatrix, assignedHeight, resultImageMatrix);
        ForkJoinPool threadPool = new ForkJoinPool();
        
        // Measure the time taken to detech changes and binarize the image in a parallel way (printed at the end)
        double beginTimeInMiliseconds = System.currentTimeMillis();
        threadPool.invoke(pbc);
        double finishTimeInMiliseconds = System.currentTimeMillis();
        double parallelTimeToDetectChangesAndBinarize = (finishTimeInMiliseconds - beginTimeInMiliseconds);

        //System.out.println("\nUsing: " + numberOfThreads + " thread " + parallelTimeToDetectChangesAndBinarize + "\n");

        // Calculate sequential time to compare time between sequential and parallel version
        
        SequentialBinarizedChanges sbc = new SequentialBinarizedChanges(width, height);
        beginTimeInMiliseconds = System.currentTimeMillis();
        sbc.compute (width, height, currentImageMatrix, scenarioImageMatrix, resultImageMatrix, currentImage);
        finishTimeInMiliseconds = System.currentTimeMillis();
        double sequentialTimeToDetectChangesAndBinarize = (finishTimeInMiliseconds - beginTimeInMiliseconds);

        // TESTING BY WRITTING THE RESULT ON AN IMAGE
        //currentImage = imageConverter.ConvertMatrixToImage(resultImageMatrix, currentImage, width, height);
        //ImageIO.write(currentImage, "jpg", new File("images/camera4/detectedBinarizedChanges.jpg"));

        // 4.1 Reduce image noise with median filter

        // Invoke parallel implementation
        assignedHeight = (height - 1)/numberOfThreads;
        int [] reducedNoiseImageMatrix = new int[height * width]; // Resulting matrix
        ParallelNoiseReducer pnr = new ParallelNoiseReducer (width, height, 0, height, resultImageMatrix, assignedHeight, reducedNoiseImageMatrix);
        beginTimeInMiliseconds = System.currentTimeMillis();
        threadPool.invoke(pnr);
        finishTimeInMiliseconds = System.currentTimeMillis();
        double parallelTimeNoiseReduction = (finishTimeInMiliseconds - beginTimeInMiliseconds);

        // Invoke sequential implementation
        SequentialNoiseReducer snr = new SequentialNoiseReducer (width, height, 0, height, resultImageMatrix, height, reducedNoiseImageMatrix);
        beginTimeInMiliseconds = System.currentTimeMillis();
        snr.compute();
        finishTimeInMiliseconds = System.currentTimeMillis();
        double sequentialTimeNoiseReduction = (finishTimeInMiliseconds - beginTimeInMiliseconds);

        currentImage = imageConverter.ConvertMatrixToImage(reducedNoiseImageMatrix, currentImage, width, height);
        
        // TESTING BY WRITTING THE RESULT ON AN IMAGE
        ImageIO.write(currentImage, "jpg", new File("images/WaitingRoomQueueCamera/imageWithoutNoise.jpg"));

        // 5•	Analyzing the distances between the new objects in the scene to calculate the distance between them.

        //imageConverter.PrintImageMatrix(testresultImageMatrix);

        int assignedWidth = width / numberOfThreads;

        // System.out.println("assignedWidth: " + assignedWidth);

        People people = new People();
        
        ParallelPeopleDetector ppd = new ParallelPeopleDetector(0, width, reducedNoiseImageMatrix, assignedWidth, height, width, people);
        beginTimeInMiliseconds = System.currentTimeMillis();
        threadPool.invoke(ppd);
        finishTimeInMiliseconds = System.currentTimeMillis();
        double parallelTimePeopleDetector = (finishTimeInMiliseconds - beginTimeInMiliseconds);

        SequentialPeopleDetector spd = new SequentialPeopleDetector(0, width, reducedNoiseImageMatrix, width, height, width, people);
        beginTimeInMiliseconds = System.currentTimeMillis();
        spd.compute();
        finishTimeInMiliseconds = System.currentTimeMillis();
        double sequentialTimePeopleDetector = (finishTimeInMiliseconds - beginTimeInMiliseconds);
        
        // JOIN OF THE RESULTS

        double beginTimeInMilisecondsJOIN = System.currentTimeMillis();

        ArrayList<Integer> peopleHorizontalStartBoundaries = people.getHorizontalStartBoundaries();
        ArrayList<Integer> peopleHorizontalEndBoundaries = people.getHorizontalEndBoundaries();

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

        // Print the results (number of people dected, distance bounds, social distance and processing times)

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

        double finishTimeInMilisecondsJOIN = System.currentTimeMillis();

        System.out.println("\nTime to join: " + (finishTimeInMilisecondsJOIN - beginTimeInMilisecondsJOIN) + "\n");

        System.out.print("\n\n");

        // Print processing times
        System.out.println("\nPROCESING TIME INFORMATION:\n");
        // Changes between images and binarization
        System.out.println("Total time taken to detect changes and binarize on parallel using "+ numberOfThreads + " threads: " + parallelTimeToDetectChangesAndBinarize + " miliseconds");
        System.out.println("Total time taken to detect changes and binarize on sequential: " + sequentialTimeToDetectChangesAndBinarize + " miliseconds" + "\n");

        System.out.println("Total time taken to reduce noise on parallel using "+ numberOfThreads + " threads: " + parallelTimeNoiseReduction + " miliseconds");
        System.out.println("Total time taken to reduce noise on sequential sequential: " + sequentialTimeNoiseReduction + " miliseconds" + "\n");

        System.out.println("Total time taken to detect people on parallel using "+ numberOfThreads + " threads: " + parallelTimePeopleDetector + " miliseconds");
        System.out.println("Total time taken to detect people on sequential: " + sequentialTimePeopleDetector + " miliseconds" + "\n");

        double totalTimeParallel = (parallelTimeToDetectChangesAndBinarize + parallelTimeNoiseReduction + parallelTimePeopleDetector );
        double totalTimeSequential = (sequentialTimeToDetectChangesAndBinarize + sequentialTimeNoiseReduction + sequentialTimePeopleDetector);
        System.out.println("Addition of times parallel: " + totalTimeParallel + "\n");
        System.out.println("Addition of times sequential: " + totalTimeSequential  + "\n");

        System.out.println("Saved time: " + (totalTimeSequential-totalTimeParallel));

        System.out.print("\n\n");
    }

}