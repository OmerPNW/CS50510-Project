package src;
import java.util.logging.Logger;

public class CityConnectionStruct {
    final String name;
    final String state;
    float distance;
    final int timeTaken;
    String meta;
    CityConnectionStruct(String name, String state, float distance, int timeTaken){
        this.name = name;
        this.state = state;
        this.distance = distance;
        this.timeTaken = timeTaken;

    }
    // copy constructor
    CityConnectionStruct(CityConnectionStruct c){
        this.name = c.name;
        this.state = c.state;
        this.distance = c.distance;
        this.timeTaken = c.timeTaken;
        this.meta = c.meta;
    }

    public void printString(String message, Logger logger){
        if (logger == null) System.out.println(message);
        else logger.info(message);
    }

    public void printObject(Logger logger){
        printString("---Connection Info---", logger);
        printString("Connected City Name : "+ name + " in state of "+ state, logger);

        printString("Distance (forward) in km :" + String.valueOf(distance), logger);
        printString("Avg time taken (forward) in min :" + timeTaken, logger);
    }
}
