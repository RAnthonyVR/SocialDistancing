# Social Distance Detector Alarm System 🦠

Programming languages course project.

Final Project.

Instituto Tecnológico de Monterrey Campus Querétaro

August – December 2020

Social Distance Detector Alarm System

Teacher: Benjamín Valdés Aguirre PhD

Ricardo Antonio Vázquez Rodríguez 
A01209245

## About the project

"A distance detector alarm system can help make sure that people are keeping a safe distance from each other while waiting in line at an enclosed space. For this problem to be solved it is necessary to reduce the time it takes to process images and alert as fast as possible if someone is not respecting the stablished safe social distance, hence a parallel paradigm is needed. This means that instead of having a sequential way of analyzing the images with a single thread; multiple threads can be used to work at the same time on different tasks or sections and divide the overall time taken to do the tasks. This solution is intended to prevent more than 2 parties of people from getting too close to each other."

The source code can be found in: [SocialDistance/src/socialdistance](https://github.com/RAnthonyVR/SocialDistancing/tree/master/SocialDistance/src/socialdistance/images) (socialdistance is the package)

Interface example:

![people close in an elevator example](https://github.com/RAnthonyVR/SocialDistancing/blob/master/Images%20examples%20for%20README/Elevator.png)

## Documentation

A report that contains the documentation of the project can be found on this repo in [Report (Documentation)](https://github.com/RAnthonyVR/SocialDistancing/blob/master/Report%20(Documentation)/Report.pdf)

## Tests

The implementation has been extensively tested with several different configurations and scenarios, the corresponding documentation can be found in [Tests](https://github.com/RAnthonyVR/SocialDistancing/tree/master/Tests)

## Setup instructions

1.	Clone GitHub repository on the terminal:

  git clone https://github.com/RAnthonyVR/SocialDistancing.git

2.	Have Java 1.8 installed.

3.	Open the project on NetBeans (I personally use NetBeans 8.2 RC):
  3.1 Select Open Project
 
  3.2 Select the project from the file browser and click open
 
  3.3 Run the project by clicking on the play button

4.	Insert the desired input.

5.	The program will show information on the output shell and will open a JFrame to view the tests.

Test configurations: The suggested inputs for the tests are the following:

Test case 1: It is expected at all images respect social distance
Initial configuration
Please enter the horizontal distance of the room in meters
5
Please enter the social distance of the room in meters (recommended 1 meter)
0.5
Please enter the test case (1 to 4)
1

Test case 2: It is expected at not all images respect social distance (images 4-5)
Initial configuration
Please enter the horizontal distance of the room in meters
8
Please enter the social distance of the room in meters (recommended 1 meter)
1
Please enter the test case (1 to 4)
2

Test case 3: It is expected at not all images respect social distance (images 2-5)
Initial configuration
Please enter the horizontal distance of the room in meters
3
Please enter the social distance of the room in meters (recommended 1 meter)
0.35
Please enter the test case (1 to 4)
3

Test case 4: It is expected to detect 1 person
Initial configuration
Please enter the horizontal distance of the room in meters
1
Please enter the social distance of the room in meters (recommended 1 meter)
2
Please enter the test case (1 to 4)
4

6.	Closing the project

  6.1 The program will close until the jFrame has been closed.

## License

Registered a GNU license.
