/*
* Ricardo Antonio Vázquez Rodríguez A01209245
* Final Project
* SocialDistance (main) class
*/

package socialdistance;

import java.io.IOException;
import java.util.Scanner;

/**
 * This is the main class of the program
 */
public class SocialDistance {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        
        // Initial configuration, (entering the horizontal distance of the room)
        System.out.println("Initial configuration");
        Scanner distanceScanner = new Scanner(System.in);
        System.out.println("Please enter the horizontal distance of the room in meters"); // Horizontal distance in meters (2 dimentional) to scale from pixels length to meters
        double horizontal_distance_in_the_room_meters = distanceScanner.nextDouble(); // tested with 3.5 meters
        System.out.println("Please enter the social distance of the room in meters (recommended 1 meter)");
        double social_distance_meters = distanceScanner.nextDouble(); // meters
        
        int numberOfDivisions; // this value divides the image 
        boolean runToCompareWithSequential = true; // set to true to run sequential implementation and compare the running times

        int numberOfImages = 1;
        
        // Images path
        String relativeFolderPath = "";
        String emptyscenarioImageName = "";

        numberOfDivisions = 8;
        
        int testCase = 4;
        
        //TESTS configurations
        //TEST1 
        //Horizontal distance of the room: 5
        //Horizontal space tolerance: 0.5
        if (testCase == 1) {
            relativeFolderPath = "images/Scene1Theater/";
            emptyscenarioImageName = "emptyTheaterWaitingLine.jpg";
            numberOfImages = 5;
        }
        
        
        //TEST2
        //Horizontal distance of the room: 8
        //Horizontal space tolerance: 1
        else if (testCase == 2) {
            relativeFolderPath = "images/Scene2Hospital/";
            emptyscenarioImageName = "emptyHospitalWaitingLine.jpg";
            numberOfImages = 5;
        }
        
        
        //TEST3
        //Horizontal distance of the room: 3
        //Horizontal space tolerance: 0.35
        else if (testCase == 3) {
            relativeFolderPath = "images/Scene3Elevator/";
            emptyscenarioImageName = "emptyElevator.jpg";
            numberOfImages = 6;
        }
        
        else if (testCase == 4) {
            relativeFolderPath = "images/DistanceTests/Test_4(Sequence_with_people)/";
            emptyscenarioImageName = "emptyClinic.jpg";
            numberOfImages = 6;
        }
        
        
        // concatenate path and empty space image
        String emptyscenarioImagePath = relativeFolderPath + emptyscenarioImageName;
        
        // create interface to show running tests
        Interface jframe = new Interface(emptyscenarioImagePath);
        
        // instantiate SocialDistanceDetector, which contains all the logic of the program
        SocialDistanceDetector socialDistanceDetector = new SocialDistanceDetector (horizontal_distance_in_the_room_meters, social_distance_meters, emptyscenarioImagePath, runToCompareWithSequential, numberOfDivisions, jframe);

        // show the jframe, until it is closed by the user.
        jframe.notmain();
        jframe.setVisible(true);
        
        // read all the image in the sequence (from 1 to ... n) and detect the distance
        for (int i = 1; i <= numberOfImages; i++) {
            String currentImagePath = relativeFolderPath + Integer.toString(i) + ".jpg";
            System.out.println(currentImagePath);
            socialDistanceDetector.setCurrentImageName(currentImagePath);
            socialDistanceDetector.detectDistance();
        }
        
    }

}
