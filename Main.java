import javax.imageio.ImageIO;
import java.io.IOException;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.*;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

public class Main {
    
    public static void main(String[] args) throws IOException {

        // Initial configuration, (entering the horizontal distance of the room)
        System.out.println("Initial configuration");
        Scanner distanceScanner = new Scanner(System.in);
        System.out.println("Please enter the horizontal distance of the room in meters"); // Horizontal distance in meters (2 dimentional) to scale from pixels length to meters
        double horizontal_distance_in_the_room_meters = distanceScanner.nextDouble(); // tested with 3.5 meters
        System.out.println("Please enter the social distance of the room in meters (recommended 1 meter)");
        double social_distance_meters = distanceScanner.nextDouble(); // meters
        int numberOfThreads;
        boolean runToCompareWithSequential = false;

        int numberOfImages;

        // Images path
        String relativeFolderPath;
        String emptyscenarioImageName;

        // PC with 2 cores
        numberOfThreads = 2;

        /* 
        TESTS configurations
        TEST1 
        Horizontal distance of the room: 5
        Horizaontal space tolerance: 0.5 */
        relativeFolderPath = "images/Scene1Theater/";
        emptyscenarioImageName = "emptyTheaterWaitingLine.jpg";
        numberOfImages = 5;

        /*
        TEST2
        Horizontal distance of the room: 8
        Horizaontal space tolerance: 1

        relativeFolderPath = "images/Scene2Hospital/";
        emptyscenarioImageName = "emptyHospitalWaitingLine.jpg";
        numberOfImages = 5;

        TEST2
        Horizontal distance of the room: 3
        Horizaontal space tolerance: 0.35
        relativeFolderPath = "images/Scene3Elevator/";
        emptyscenarioImageName = "emptyElevator.jpg";
        numberOfImages = 6;
        */

        String emptyscenarioImagePath = relativeFolderPath + emptyscenarioImageName;

        
        SocialDistanceDetector socialDistanceDetector = new SocialDistanceDetector (horizontal_distance_in_the_room_meters, social_distance_meters, emptyscenarioImagePath, runToCompareWithSequential, numberOfThreads);

        for (int i = 1; i <= numberOfImages; i++) {
            String currentImagePath = relativeFolderPath + Integer.toString(i) + ".jpg";
            System.out.println(currentImagePath);
            socialDistanceDetector.setCurrentImageName(currentImagePath);
            socialDistanceDetector.detectDistance();
        }
        
       
    }

}