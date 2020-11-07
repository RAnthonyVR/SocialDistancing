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
        BufferedImage currentImage = ImageIO.read(new File("images/camera4/6.jpg"));

        // The actual number of threads that will process the image
        int numberOfThreads = 1;

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
            /*
            for (int i = 0; i < peopleHorizontalEndBoundaries.size(); i++) {
                normPeopleHorizontalStartBoundaries.add(peopleHorizontalStartBoundaries.get(j));
                if (!peopleHorizontalEndBoundaries.isEmpty()) { //one person arriving at the extremes
                    while ((j < peopleHorizontalStartBoundaries.size()) && (peopleHorizontalEndBoundaries.get(i) >= peopleHorizontalStartBoundaries.get(j))) {
                        j++;
                    }
                }
            }
            normPeopleHorizontalEndBoundaries = peopleHorizontalEndBoundaries;*/
            
        } else if (peopleHorizontalStartBoundaries.size() == peopleHorizontalEndBoundaries.size()) {
            normPeopleHorizontalStartBoundaries = peopleHorizontalStartBoundaries;
            normPeopleHorizontalEndBoundaries = peopleHorizontalEndBoundaries;
        }

        /*
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
                        alreadyAddedStart = false;
                        j++;
                    }
                } else {
                    normPeopleHorizontalStartBoundaries.add(peopleHorizontalStartBoundaries.get(i));
                }
            }
            normPeopleHorizontalEndBoundaries = peopleHorizontalEndBoundaries;
            /*
            for (int i = 0; i < peopleHorizontalEndBoundaries.size(); i++) {
                normPeopleHorizontalStartBoundaries.add(peopleHorizontalStartBoundaries.get(j));
                if (!peopleHorizontalEndBoundaries.isEmpty()) { //one person arriving at the extremes
                    while ((j < peopleHorizontalStartBoundaries.size()) && (peopleHorizontalEndBoundaries.get(i) >= peopleHorizontalStartBoundaries.get(j))) {
                        j++;
                    }
                }
            }
            normPeopleHorizontalEndBoundaries = peopleHorizontalEndBoundaries;*/
            /*
        } else if (peopleHorizontalStartBoundaries.size() == peopleHorizontalEndBoundaries.size()) {
            normPeopleHorizontalStartBoundaries = peopleHorizontalStartBoundaries;
            normPeopleHorizontalEndBoundaries = peopleHorizontalEndBoundaries;
        }*/

        System.out.println("Start");
        for (int i = 0; i < normPeopleHorizontalStartBoundaries.size(); i++) {
            System.out.println(normPeopleHorizontalStartBoundaries.get(i));
        }

        System.out.println("End");
        for (int i = 0; i < normPeopleHorizontalEndBoundaries.size(); i++) {
            System.out.println(normPeopleHorizontalEndBoundaries.get(i));
        }

        int numberOfPeople;

        // Case that someone is at the extremes, entering the room, or just one person is there
        /*
        if (Math.max(peopleHorizontalStartBoundaries.size(), peopleHorizontalEndBoundaries.size()) == 1) {
            numberOfPeople = 1;
        } else if (normPeopleHorizontalStartBoundaries.size() > normPeopleHorizontalEndBoundaries.size()) {

        } else {*/
            numberOfPeople = Math.max(normPeopleHorizontalStartBoundaries.size(), normPeopleHorizontalEndBoundaries.size());
        //}
        System.out.println("\nNumber of people in the room: " + numberOfPeople + "\n");

        if (numberOfPeople > 1) {
            System.out.println("\nDistances between them: ");
            for (int i = 0; i < numberOfPeople - 1; i++) {
                System.out.println(normPeopleHorizontalStartBoundaries.get(i + 1) - normPeopleHorizontalEndBoundaries.get(i));
            }
        }
        
        
    }

}