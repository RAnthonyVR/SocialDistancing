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
    5•  Reduce image noise with median filter
    6•	Analyzing the distances between the new objects in the scene to calculate the distance between them.
 */

public class SocialDistanceDetector {

    private double horizontal_distance_in_the_room_meters;
    private double social_distance_meters;
    private String emptyscenarioImagePath;
    private String currentImagePath;
    private int numberOfThreads; // The actual number of threads that will process the image
    private BufferedImage scenarioImage;
    private BufferedImage currentImage;
    private ForkJoinPool threadPool; // used for parallelism
    private boolean runToCompareWithSequential;
    private int width;
    private int height;
    private People people;
    private ImageConverter imageConverter;

    //images matrixes
    private int [] scenarioImageMatrix;
    private int [] currentImageMatrix;
    private int [] resultImageMatrix;
    private int [] reducedNoiseImageMatrix;

    // Comparison times
    private double beginTimeInMiliseconds;
    private double finishTimeInMiliseconds;
    private double parallelTimeToDetectChangesAndBinarize;
    private double sequentialTimeToDetectChangesAndBinarize;
    private double parallelTimePeopleDetector;
    private double sequentialTimePeopleDetector;
    private double parallelTimeNoiseReduction;
    private double sequentialTimeNoiseReduction;

    // people boundaries
    private ArrayList<Integer> peopleHorizontalStartBoundaries;
    private ArrayList<Integer> peopleHorizontalEndBoundaries;
    private ArrayList<Integer> normPeopleHorizontalStartBoundaries;
    private ArrayList<Integer> normPeopleHorizontalEndBoundaries;

    public SocialDistanceDetector (double horizontal_distance_in_the_room_meters, double social_distance_meters, String emptyscenarioImagePath, boolean runToCompareWithSequential, int numberOfThreads) {
        this.horizontal_distance_in_the_room_meters = horizontal_distance_in_the_room_meters;
        this.social_distance_meters = social_distance_meters;
        this.emptyscenarioImagePath = emptyscenarioImagePath;
        this.threadPool = new ForkJoinPool();
        this.numberOfThreads = numberOfThreads;
        this.runToCompareWithSequential = runToCompareWithSequential;
    }

    public void setCurrentImageName (String currentImagePath) {
        this.currentImagePath = currentImagePath;
    }

    public void detectChangesAndBinarize () {
        // Instantiate parallel class that subtracts the images and applies a binary filter, using a thread pool and fork join
        int assignedHeight = height / numberOfThreads; // Assign a piece of the image to pe processed
        ParallelBinarizedChanges pbc = new ParallelBinarizedChanges (width, 0, height, currentImageMatrix, scenarioImageMatrix, assignedHeight, resultImageMatrix);
        // Measure the time taken to detect changes and binarize the image in a parallel way (printed at the end)
        beginTimeInMiliseconds = System.currentTimeMillis();
        threadPool.invoke(pbc);
        finishTimeInMiliseconds = System.currentTimeMillis();
        parallelTimeToDetectChangesAndBinarize = (finishTimeInMiliseconds - beginTimeInMiliseconds);

        // Calculate sequential time to compare time between sequential and parallel version
        sequentialTimeToDetectChangesAndBinarize = 0;
        if (runToCompareWithSequential) {
            SequentialBinarizedChanges sbc = new SequentialBinarizedChanges(width, height);
            beginTimeInMiliseconds = System.currentTimeMillis();
            sbc.compute (width, height, currentImageMatrix, scenarioImageMatrix, resultImageMatrix, currentImage);
            finishTimeInMiliseconds = System.currentTimeMillis();
            sequentialTimeToDetectChangesAndBinarize = (finishTimeInMiliseconds - beginTimeInMiliseconds);
        }
        // TESTING RESULTS BY WRITTING THE RESULT ON AN IMAGE
        // currentImage = imageConverter.ConvertMatrixToImage(resultImageMatrix, currentImage, width, height);
        // ImageIO.write(currentImage, "jpg", new File("images/camera4/detectedBinarizedChanges.jpg"));
    }

    public void reduceImageNoise () {
        // Invoke parallel implementation
        int assignedHeight = (height - 1)/numberOfThreads;
        reducedNoiseImageMatrix = new int[height * width]; // Resulting matrix
        ParallelNoiseReducer pnr = new ParallelNoiseReducer (width, height, 0, height, resultImageMatrix, assignedHeight, reducedNoiseImageMatrix);
        beginTimeInMiliseconds = System.currentTimeMillis();
        threadPool.invoke(pnr);
        finishTimeInMiliseconds = System.currentTimeMillis();
        parallelTimeNoiseReduction = (finishTimeInMiliseconds - beginTimeInMiliseconds);

        // Invoke sequential implementation
        sequentialTimeNoiseReduction = 0;
        if (runToCompareWithSequential) {
            SequentialNoiseReducer snr = new SequentialNoiseReducer (width, height, 0, height, resultImageMatrix, height, reducedNoiseImageMatrix);
            beginTimeInMiliseconds = System.currentTimeMillis();
            snr.compute();
            finishTimeInMiliseconds = System.currentTimeMillis();
            sequentialTimeNoiseReduction = (finishTimeInMiliseconds - beginTimeInMiliseconds);
        }

        //  WRITTING THE RESULT ON AN IMAGE
        /*
        currentImage = imageConverter.ConvertMatrixToImage(reducedNoiseImageMatrix, currentImage, width, height);
        try {
            ImageIO.write(currentImage, "jpg", new File("images/WaitingRoomQueueCamera/imageWithoutNoise.jpg"));
        } catch (IOException ioe) {
            System.out.println("Couldn't write image withou noise");
        }*/
        
    }

    public void removeNoiseFromBoundaries () {
        int pixelnumtolerance = 10;
        //System.out.println("RESTAS:");
        for (int i = 0; i < normPeopleHorizontalEndBoundaries.size(); i++) {
            //System.out.println("ENTRO CON "+ normPeopleHorizontalStartBoundaries.get(i) + " " + normPeopleHorizontalEndBoundaries.get(i) + " ");
            if ((normPeopleHorizontalEndBoundaries.get(i) - normPeopleHorizontalStartBoundaries.get(i)) < 10) {
                normPeopleHorizontalStartBoundaries.remove(i);
                normPeopleHorizontalEndBoundaries.remove(i);
            }
            
        }
    }

    public void joinObtainedBoundaries() {
        peopleHorizontalStartBoundaries = people.getHorizontalStartBoundaries();
        peopleHorizontalEndBoundaries = people.getHorizontalEndBoundaries();
        Collections.sort(peopleHorizontalStartBoundaries);
        Collections.sort(peopleHorizontalEndBoundaries);
        normPeopleHorizontalStartBoundaries = new ArrayList<Integer>();
        normPeopleHorizontalEndBoundaries = new ArrayList<Integer>();
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
    }

    public void getPeopleBoundaries () {
        int assignedWidth = width / numberOfThreads;
        people = new People();
        ParallelPeopleDetector ppd = new ParallelPeopleDetector(0, width, reducedNoiseImageMatrix, assignedWidth, height, width, people);
        beginTimeInMiliseconds = System.currentTimeMillis();
        threadPool.invoke(ppd);
        finishTimeInMiliseconds = System.currentTimeMillis();
        parallelTimePeopleDetector = (finishTimeInMiliseconds - beginTimeInMiliseconds);
        
        sequentialTimePeopleDetector = 0;
        if (runToCompareWithSequential) {
            SequentialPeopleDetector spd = new SequentialPeopleDetector(0, width, reducedNoiseImageMatrix, width, height, width, people);
            beginTimeInMiliseconds = System.currentTimeMillis();
            spd.compute();
            finishTimeInMiliseconds = System.currentTimeMillis();
            sequentialTimePeopleDetector = (finishTimeInMiliseconds - beginTimeInMiliseconds);
        }
    }

    // Print the results (number of people dected, distance bounds and social distance results)
    public void printPeopleObtainedInformation () {
        removeNoiseFromBoundaries();
        int numberOfPeople = Math.max(normPeopleHorizontalStartBoundaries.size(), normPeopleHorizontalEndBoundaries.size());

        System.out.println("\nOBTAINED INFORMATION: ");
        System.out.println("* Number of people in the room: " + numberOfPeople + "\n");
        System.out.print("Starts in pixels: ");
        for (int i = 0; i < normPeopleHorizontalStartBoundaries.size(); i++) {
            System.out.print(normPeopleHorizontalStartBoundaries.get(i) + " ");
        }
        System.out.print("\n");
        System.out.print("Ends in pixels: ");
        for (int i = 0; i < normPeopleHorizontalEndBoundaries.size(); i++) {
            System.out.print(normPeopleHorizontalEndBoundaries.get(i) + " ");
        }
        if (numberOfPeople > 1) {
            System.out.println("\nDistances between them in pixels: ");
            for (int i = 0; i < numberOfPeople - 1; i++) {
                System.out.print(normPeopleHorizontalStartBoundaries.get(i + 1) - normPeopleHorizontalEndBoundaries.get(i) + " ");
            }
        }
        System.out.print("\n");

        if (numberOfPeople > 1) {
            System.out.println("\nDistances between them in meters: ");
            for (int i = 0; i < numberOfPeople - 1; i++) {
                double distanceInPixels = normPeopleHorizontalStartBoundaries.get(i + 1) - normPeopleHorizontalEndBoundaries.get(i);
                double distanceInMeters = (horizontal_distance_in_the_room_meters * distanceInPixels) / width;
                System.out.print(String.format("\n%.2f", distanceInMeters) + " ");
                if (distanceInMeters >= social_distance_meters) {
                    System.out.print(" = Respecting social distance\n");
                } else {
                    System.out.print(" = ALERT! Not respecting social distance\n");
                    // sound alert
                    // Consulted: https://www.youtube.com/watch?v=8NUSbY_7Joc
                    int a = 7;
                    char beep = (char)a;
                    System.out.print(beep);
                }
            }
        }
    }

    // Print processing times, compare times between sequential and parallel
    public void printProcessingTimes (double parallelTimeToDetectChangesAndBinarize, double sequentialTimeToDetectChangesAndBinarize, double parallelTimeNoiseReduction, double sequentialTimeNoiseReduction, double parallelTimePeopleDetector, double sequentialTimePeopleDetector ) {
        System.out.println("\nPROCESING TIME INFORMATION:\n");
        System.out.println("Total time taken to detect changes and binarize on parallel using "+ this.numberOfThreads + " threads: " + parallelTimeToDetectChangesAndBinarize + " miliseconds");
        if (this.runToCompareWithSequential) {
            System.out.println("Total time taken to detect changes and binarize on sequential: " + sequentialTimeToDetectChangesAndBinarize + " miliseconds" + "\n");
        }
        System.out.println("Total time taken to reduce noise on parallel using "+ this.numberOfThreads + " threads: " + parallelTimeNoiseReduction + " miliseconds");
        if (this.runToCompareWithSequential) {
            System.out.println("Total time taken to reduce noise on sequential sequential: " + sequentialTimeNoiseReduction + " miliseconds" + "\n");
        }
        System.out.println("Total time taken to detect people on parallel using "+ this.numberOfThreads + " threads: " + parallelTimePeopleDetector + " miliseconds");
        if (this.runToCompareWithSequential) {
            System.out.println("Total time taken to detect people on sequential: " + sequentialTimePeopleDetector + " miliseconds" + "\n");
        }
        if (this.runToCompareWithSequential) {
            double totalTimeParallel = (parallelTimeToDetectChangesAndBinarize + parallelTimeNoiseReduction + parallelTimePeopleDetector );
            double totalTimeSequential = (sequentialTimeToDetectChangesAndBinarize + sequentialTimeNoiseReduction + sequentialTimePeopleDetector);
            System.out.println("Addition of times parallel: " + totalTimeParallel + "\n");
            System.out.println("Addition of times sequential: " + totalTimeSequential  + "\n");
            System.out.println("Saved time: " + (totalTimeSequential-totalTimeParallel));
            System.out.print("\n\n");
        }
    }

    public void detectDistance () throws IOException {

        // 1 ----------------------------- Have an reference of an image of the scene before the people arrive. -----------------------------------------
        this.scenarioImage = ImageIO.read(new File(emptyscenarioImagePath));
        this.currentImage = ImageIO.read(new File(currentImagePath)); // Receives an image update from the camera
        this.height = scenarioImage.getHeight();
        this.width = scenarioImage.getWidth();

        // 3 --------------- Detect if someone has arrived or something changes by marking the differences between these images. -----------------------
        // 4 -----------Applying a binary filter to crearly mark the differences detected and differentiate between empty spaces and objects. ----------

        // Transform the both readed images to matrices to be processed, as well as allocating memory space for the resulting matrix
        imageConverter = new ImageConverter(width, height);
        scenarioImageMatrix = imageConverter.ConvertImageToMatrix(scenarioImage, width, height);
        currentImageMatrix = imageConverter.ConvertImageToMatrix(currentImage, width, height);
        resultImageMatrix = new int[height * width];

        detectChangesAndBinarize ();

        // 5. ---------------------------------------------Reduce image noise with median filter---------------------------------------------------------
        reduceImageNoise();

        // 6. ------------------Analyzing the distances between the new objects in the scene to calculate the distance between them.----------------------
        getPeopleBoundaries();
        // join the results obtained by the threads
        double beginTimeInMilisecondsJOIN = System.currentTimeMillis();
        joinObtainedBoundaries();
        double finishTimeInMilisecondsJOIN = System.currentTimeMillis();
        double timeJOIN = finishTimeInMilisecondsJOIN - beginTimeInMilisecondsJOIN;
        
        // ----------------------------------------------------------Print results------------------------------------------------------------------------

        // Print obtained data
        printPeopleObtainedInformation();
        
        // Print processing times
        printProcessingTimes (parallelTimeToDetectChangesAndBinarize, sequentialTimeToDetectChangesAndBinarize, parallelTimeNoiseReduction, sequentialTimeNoiseReduction, parallelTimePeopleDetector, sequentialTimePeopleDetector );
    }

}