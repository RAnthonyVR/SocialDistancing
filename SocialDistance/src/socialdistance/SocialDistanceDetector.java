/*
* Ricardo Antonio Vázquez Rodríguez A01209245
* Final Project
* SocialDistanceDetector class
*/

package socialdistance;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.awt.image.BufferedImage;
import java.util.concurrent.*;
import java.util.ArrayList;
import java.util.Collections;

/*
Logic:
    1•	Have an reference of an image of the scene before the people arrive.
    2•  Receives an image to process in a sequential manner.
    3•	Detect if someone has arrived or something changes by marking the differences between these images.
    4•	Applying a binary filter to creaarly mark the differences detected and differentiate between empys spaces and objects.
    5•  Reduce image noise with median filter
    6•	Analyzing the distances between the new objects in the scene to calculate the distance between them.
 */

public class SocialDistanceDetector {

    // attributes
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
    private Interface jframe;

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

    /* Constructor that receives the initial configuration from the main class (Social distance) */
    public SocialDistanceDetector (double horizontal_distance_in_the_room_meters, double social_distance_meters, String emptyscenarioImagePath, boolean runToCompareWithSequential, int numberOfThreads, Interface jframe) {
        this.horizontal_distance_in_the_room_meters = horizontal_distance_in_the_room_meters;
        this.social_distance_meters = social_distance_meters;
        this.emptyscenarioImagePath = emptyscenarioImagePath;
        this.numberOfThreads = numberOfThreads;
        this.runToCompareWithSequential = runToCompareWithSequential;
        this.jframe = jframe;
    }

    /* Method to set/update the image to be compared witth the one without people in it
    Parameters: String (the path to the image to be compared)
    Returns: nothing*/
    public void setCurrentImageName (String currentImagePath) {
        this.currentImagePath = currentImagePath;
    }

    /* Method to call parallel implementation to detect the differences between two images and binarize it, called from detectDistance method
    Parameters and Returns: void */
    public void detectChangesAndBinarize () {
        // Instantiate parallel class that subtracts the images and applies a binary filter, using a thread pool and fork join
        int assignedHeight = height / numberOfThreads; // Assign a piece of the image to pe processed
        ParallelBinarizedChanges pbc = new ParallelBinarizedChanges (width, 0, height, currentImageMatrix, scenarioImageMatrix, assignedHeight, resultImageMatrix);
        // Measure the time taken to detect changes and binarize the image in a parallel way (printed at the end)
        beginTimeInMiliseconds = System.currentTimeMillis();
        this.threadPool = new ForkJoinPool(); // threadpool using fork join calls compute method
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
        // Testing the results by writing them on an image
        // currentImage = imageConverter.ConvertMatrixToImage(resultImageMatrix, currentImage, width, height);
        // ImageIO.write(currentImage, "jpg", new File("images/camera4/detectedBinarizedChanges.jpg"));
    }

    /* Method to call parallel implementation to reduce the image of a binarized image, called from detectDistance method
    Parameters and Returns: void */
    public void reduceImageNoise () {
        // Invoke parallel implementation
        int assignedHeight = (height - 1) / numberOfThreads;
        reducedNoiseImageMatrix = new int[height * width]; // Resulting matrix
        ParallelNoiseReducer pnr = new ParallelNoiseReducer (width, height, 0, height, resultImageMatrix, assignedHeight, reducedNoiseImageMatrix);
        beginTimeInMiliseconds = System.currentTimeMillis();
        threadPool.invoke(pnr); // threadpool using fork join calls compute method
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

        // Testing the results by writing them on an image
        /*
        currentImage = imageConverter.ConvertMatrixToImage(reducedNoiseImageMatrix, currentImage, width, height);
        try {
            ImageIO.write(currentImage, "jpg", new File("images/WaitingRoomQueueCamera/imageWithoutNoise.jpg"));
        } catch (IOException ioe) {
            System.out.println("Couldn't write image withou noise");
        }*/
    }

    /* Method just in case to remove extra noise that we want to ignore, setting a tolerance 
    Parameters: void
    Returns: void*/
    public void removeNoiseFromBoundaries () {
        int pixelnumtolerance = 10; // set tolerance in pixels
        // iterate the array
        for (int i = 0; i < normPeopleHorizontalEndBoundaries.size(); i++) {
            // if the boundary occupies a value lower than the specified pixels, remove from the list
            if ((normPeopleHorizontalEndBoundaries.get(i) - normPeopleHorizontalStartBoundaries.get(i)) < pixelnumtolerance) {
                normPeopleHorizontalStartBoundaries.remove(i);
                normPeopleHorizontalEndBoundaries.remove(i);
            }
            
        }
    }

    // Method to join the boundaries calculated
    /* Parameters: void Returns: void */
    public void joinObtainedBoundaries() {
        // get calculated boundaries
        peopleHorizontalStartBoundaries = people.getHorizontalStartBoundaries();
        peopleHorizontalEndBoundaries = people.getHorizontalEndBoundaries();
        //sort them in order
        Collections.sort(peopleHorizontalStartBoundaries);
        Collections.sort(peopleHorizontalEndBoundaries);
        // normalize the boundaries, that means, reducing the repeated starting boundaries
        normPeopleHorizontalStartBoundaries = new ArrayList<Integer>();
        normPeopleHorizontalEndBoundaries = new ArrayList<Integer>();
        // Normalized boundaries (join them)
        // in case a person was divided by the threads, there is no case in which it is smaller, (start position is always considered)
        if (peopleHorizontalStartBoundaries.size() > peopleHorizontalEndBoundaries.size()) {
            int j = 0; // end boundaries iterator
            boolean alreadyAddedStart = false; //boolean value to keep track if an initial start has been set
            // iterate through the start boundaries
            for (int i = 0; i < peopleHorizontalStartBoundaries.size(); i++) {
                // in case the index of end is lower than the ends
                if (j < peopleHorizontalEndBoundaries.size())  {
                    // if start is lower than end add to start arraylist and no previous starts have been stablished
                    if ((peopleHorizontalStartBoundaries.get(i) < peopleHorizontalEndBoundaries.get(j)) && !alreadyAddedStart) {
                        normPeopleHorizontalStartBoundaries.add(peopleHorizontalStartBoundaries.get(i));
                        alreadyAddedStart = true;
                    // if start is greater or equal than end add to start arraylist and one was added previously
                    } else if ((peopleHorizontalStartBoundaries.get(i) >= peopleHorizontalEndBoundaries.get(j)) && alreadyAddedStart) {
                        normPeopleHorizontalStartBoundaries.add(peopleHorizontalStartBoundaries.get(i));
                        alreadyAddedStart = true;
                        j++;
                    }
                // if all ends have been checked (index greater) then add the remaining start boundary
                } else {
                    normPeopleHorizontalStartBoundaries.add(peopleHorizontalStartBoundaries.get(i));
                }
            }
            normPeopleHorizontalEndBoundaries = peopleHorizontalEndBoundaries;
        // if the start and end boundaries are the same, then just set them as the normalized arrays
        } else if (peopleHorizontalStartBoundaries.size() == peopleHorizontalEndBoundaries.size()) {
            normPeopleHorizontalStartBoundaries = peopleHorizontalStartBoundaries;
            normPeopleHorizontalEndBoundaries = peopleHorizontalEndBoundaries;
        }
    }

    /* Method to call parallel implementation to get the people boundaries, called from detectDistance method
    Parameters and Returns: void */
    public void getPeopleBoundaries () {
        int assignedWidth = width / numberOfThreads;
        people = new People();
        ParallelPeopleDetector ppd = new ParallelPeopleDetector(0, width, reducedNoiseImageMatrix, assignedWidth, height, width, people);
        beginTimeInMiliseconds = System.currentTimeMillis();
        threadPool.invoke(ppd); // threadpool using fork join calls compute method
        threadPool.shutdown();
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
        String infoDispleayedOnInterface = "Processed image: " + currentImagePath + "\n";
        infoDispleayedOnInterface += "\nOBTAINED INFORMATION:\n";
        System.out.println("\nOBTAINED INFORMATION: ");
        infoDispleayedOnInterface += "Number of people in the room: " + numberOfPeople + "\n";
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
        boolean noSocialDistance = false;
        if (numberOfPeople > 1) {
            infoDispleayedOnInterface += "\nDistances between them in meters: ";
            System.out.println("\nDistances between them in meters: ");
            
            for (int i = 0; i < numberOfPeople - 1; i++) {
                double distanceInPixels = normPeopleHorizontalStartBoundaries.get(i + 1) - normPeopleHorizontalEndBoundaries.get(i);
                double distanceInMeters = (horizontal_distance_in_the_room_meters * distanceInPixels) / width;
                System.out.print(String.format("\n%.2f", distanceInMeters) + " ");
                infoDispleayedOnInterface += "" + String.format("\n%.2f", distanceInMeters) + " ";
                if (distanceInMeters >= social_distance_meters) {
                    System.out.print(" = Respecting social distance\n");
                    infoDispleayedOnInterface += " = Respecting social distance\n";
                } else {
                    System.out.print(" = ALERT! Not respecting social distance\n");
                    infoDispleayedOnInterface += " = ALERT! Not respecting social distance\n";
                    // sound alert
                    // Consulted: https://www.youtube.com/watch?v=8NUSbY_7Joc
                    int a = 7;
                    char beep = (char)a;
                    System.out.print(beep);
                    noSocialDistance = true;
                }
            }
        }
        //update interface
        jframe.loadImage(currentImagePath);
        jframe.setPeopleInfo(infoDispleayedOnInterface);
        if (noSocialDistance) {
            jframe.enableAlarm();
        }else {
            jframe.disableAlarm();
        }
        jframe.repaint();
    }

    // Method to print processing times, to compare times between sequential and parallel
    /* Parameters: double parallelTimeToDetectChangesAndBinarize, double sequentialTimeToDetectChangesAndBinarize, double parallelTimeNoiseReduction, double sequentialTimeNoiseReduction, double parallelTimePeopleDetector, double sequentialTimePeopleDetector
    Returns: void */
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

    // main method that executes the logic of the program
    public void detectDistance () throws IOException {

        // 1. ----------------------------- Have an reference of an image of the scene before the people arrive. -----------------------------------------
        this.scenarioImage = ImageIO.read(SocialDistance.class.getResource(emptyscenarioImagePath));
        this.currentImage = ImageIO.read(SocialDistance.class.getResource(currentImagePath)); // Receives an image update from the camera
        this.height = scenarioImage.getHeight();
        this.width = scenarioImage.getWidth();

        // 3. --------------- Detect if someone has arrived or something changes by marking the differences between these images. -----------------------
        // 4. -----------Applying a binary filter to crearly mark the differences detected and differentiate between empty spaces and objects. ----------

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