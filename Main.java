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
        int numberOfThreads = 2;
        boolean runToCompareWithSequential = false;

        // Images path
        String emptyscenarioImagePath = "images/WaitingRoomQueueCamera/WaitingRoominGreen.jpg";
        
        SocialDistanceDetector socialDistanceDetector = new SocialDistanceDetector (horizontal_distance_in_the_room_meters, social_distance_meters, emptyscenarioImagePath, runToCompareWithSequential, numberOfThreads);
        socialDistanceDetector.setCurrentImageName("images/WaitingRoomQueueCamera/test3.jpg");
        socialDistanceDetector.detectDistance();
       
    }

}