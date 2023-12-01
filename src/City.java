package src;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;
import java.util.logging.Logger;
import java.util.Map;
import java.util.Date;


public class City {
    final String name;
    final String state;
    final int seaLevel;
    final String postCode;
    final float lat;
    final float lngt;
    final TreeMap<Long, List<String>> weatherData;
    LinkedList<CityConnectionStruct> connections;


    City(String name, String state, String postCode, int seaLevel, String weatherDataString, float lat, float lngt){
        this.name = name;
        this.state = state;
        this.seaLevel = seaLevel;
        this.postCode = postCode;
        this.lat = lat;
        this.lngt = lngt;
        this.weatherData = WeatherParse.parseWeatherData(weatherDataString);
        this.connections = new LinkedList<>();
    }
    City(String name, String state, String postCode, int seaLevel, String weatherDataString){
        this(name, state, postCode, seaLevel, weatherDataString,-400, -400);
    }

    // copy constructor
    City(City city){
        this.name = city.name;
        this.state = city.state;
        this.seaLevel = city.seaLevel;
        this.postCode = city.postCode;
        this.lat = city.lat;
        this.lngt = city.lngt;
        this.weatherData = city.weatherData;
        this.connections = new LinkedList<>();
        for (CityConnectionStruct c: city.connections){
            this.connections.add(new CityConnectionStruct(c));
        }
    }

    public void printString(String message, Logger logger){
        if (logger == null) System.out.println(message);
        else logger.info(message);
    }


    public void printObject(Logger logger){
        printString("----CITY INFO ----", logger);
        printString("Name : "+ this.name, logger);
        printString("State : "+ this.state, logger);
        printString("Post Code : " + this.postCode, logger);
        printString("Sea Level in m: " + String.valueOf(this.seaLevel), logger);
        printString("Weather Conditions", logger);
        printString("--Weather Conditions --", logger);
        for (Map.Entry<Long, List<String>> entry : weatherData.entrySet()) {
            long timestamp = entry.getKey();
            List<String> conditions = entry.getValue();
            printString("Date : " + new Date(timestamp), logger);
            printString("Weather Conditions: " + conditions, logger);
        }
        printString("--End Weather Conditions--", logger);
        printString("City Connections", logger);
        for (CityConnectionStruct connection: connections){
            connection.printObject(logger);
        }
        printString("----END-----", logger);
        printString("", logger);
        printString("", logger);
        printString("", logger);
    }

    public void printObject(){
        printObject(null);
    }
}
