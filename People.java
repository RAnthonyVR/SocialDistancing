import javax.imageio.ImageIO;
import java.io.IOException;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.*;
import java.io.FileWriter;
import java.util.ArrayList;

public class People {

    private ArrayList<Integer> peopleStartHorizontalBoundaries;
    private ArrayList<Integer> peopleEndHorizontalBoundaries;

    private Integer numberOfPeople;

    public People () {
        peopleStartHorizontalBoundaries = new ArrayList<Integer>();
        peopleEndHorizontalBoundaries = new ArrayList<Integer>();
    }

    public void addHorizontalStartBoundary (int boundary) {
        this.peopleStartHorizontalBoundaries.add(boundary);
    }

    public void addHorizontalEndBoundary (int boundary) {
        this.peopleEndHorizontalBoundaries.add(boundary);
    }

    public ArrayList<Integer> getHorizontalStartBoundaries () {
        return this.peopleStartHorizontalBoundaries;
    }

    public ArrayList<Integer> getHorizontalEndBoundaries () {
        return this.peopleEndHorizontalBoundaries;
    }

}