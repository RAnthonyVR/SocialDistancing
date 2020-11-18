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

The implementation has been extensively tested with several different configurations and scenarios, the corresponding documentation can be found in [Tests](https://github.com/RAnthonyVR/SocialDistancing/blob/master/Tests/Documented%20Tests%20(different%20scenarios).pdf)

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

Test configurations can be found on the report

6.	Closing the project

  6.1 The program will close until the jFrame has been closed.

## License

Registered a GNU license.
