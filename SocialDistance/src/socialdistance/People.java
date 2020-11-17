/*
* Ricardo Antonio Vázquez Rodríguez A01209245
* Final Project
* People class
*/

package socialdistance;

import java.util.ArrayList;

/* Shared object between threads in socialDistanceDetector to save the end and star values in a dinamic size array
 uses synchronized as the element is shared between threads*/

public class People {
    // lists with the start and end boundaries
    private ArrayList<Integer> peopleStartHorizontalBoundaries;
    private ArrayList<Integer> peopleEndHorizontalBoundaries;

    //private Integer numberOfPeople;

    // Constructor instantiates two new lists
    public People () {
        peopleStartHorizontalBoundaries = new ArrayList<Integer>();
        peopleEndHorizontalBoundaries = new ArrayList<Integer>();
    }

    // add an element to the start array, synchronized to avoid consistency errors
    public synchronized void addHorizontalStartBoundary (int boundary) {
        this.peopleStartHorizontalBoundaries.add(boundary);
    }

    // add an element to the end array, synchronized to avoid consistency errors
    public synchronized void addHorizontalEndBoundary (int boundary) {
        this.peopleEndHorizontalBoundaries.add(boundary);
    }

    // get and element from the start array, synchronized to avoid consistency errors
    public synchronized ArrayList<Integer> getHorizontalStartBoundaries () {
        return this.peopleStartHorizontalBoundaries;
    }

    // get and element from the end array, synchronized to avoid consistency errors
    public synchronized ArrayList<Integer> getHorizontalEndBoundaries () {
        return this.peopleEndHorizontalBoundaries;
    }

}