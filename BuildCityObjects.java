import java.util.Map;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;


public class BuildCityObjects {


    // flags
    private static final boolean checkOuterCityDisconnectedFlag = false;


    public static void main(String[] args) throws IOException{
        String citiesCsvPath = "HW6_weather.csv";
        String connectionCsvPath = "HW6_Data_City_Connections.csv";

        // Create a FileHandler that writes log messages to a file
        FileHandler fileHandlerInfo = new FileHandler("info.log");
        FileHandler fileHandleError = new FileHandler("error.log");

        // Set the log level (e.g., INFO)
        fileHandlerInfo.setLevel(Level.INFO);
        fileHandleError.setLevel(Level.SEVERE);

        // Add the FileHandler to the logger
        // logger.addHandler(fileHandlerInfo);
        // logger.addHandler(fileHandleError);
        CustomRecordFormatter formatter = new CustomRecordFormatter();  
        // If issue with using formatter from then uncomment this line and comment the above
        // SimpleFormatter formatter = new SimpleFormatter();
        fileHandlerInfo.setFormatter(formatter);
        fileHandleError.setFormatter(formatter);
        
        Map<String, String[]> weatherData;
        Map<String, String[]> connectionData;
        HashMap<String, City> cities = new HashMap<>();
        try{
            weatherData = LDP.loadIndividualCityData(citiesCsvPath);
            connectionData = LDP.loadCityConnectionData(connectionCsvPath);
            cities = twoWayBuild(weatherData, connectionData);

            // for (Map.Entry<String, City> city : cities.entrySet()){
            //     System.out.println(city.getKey());
            //     city.getValue().printObject();
            // }

            // System.out.println(checkConnectivity(cities));

        }
        catch(Exception e){
            e.printStackTrace();
        }
    }


    public static HashMap<String, City> oneWayBuild(Map<String, String[]> weatherData, Map<String, String[]> connectionData){
        HashMap<String, City> cities = new HashMap<>();

        for (Map.Entry<String, String[]> weatherEntry : weatherData.entrySet()) {
                String key = StringStandardize.standardizeString(weatherEntry.getKey());
                String[] values = weatherEntry.getValue();
                int seaLevel;
                try {
                   seaLevel = Integer.parseInt(values[4]);
                } catch(NumberFormatException ex) {
                   seaLevel = 0;
                }                
                cities.put(key, new City(values[1],values[0], values[2], seaLevel, values[3]));
            }
        for (Map.Entry<String, String[]> connectionEntry : connectionData.entrySet()) {
            String[] keySplits = connectionEntry.getKey().split(",");
            String key = StringStandardize.standardizeString(keySplits[0]);
            String[] connectionCity = connectionEntry.getValue();
            City city = cities.get(key);
            if (city != null){
                city.connections.add(new CityConnectionStruct(StringStandardize.standardizeString(connectionCity[2]),
                 Float.parseFloat(connectionCity[3]), Integer.parseInt(connectionCity[4])));
            }
        }
        return cities;

    }


    public static HashMap<String, City> twoWayBuildEx(Map<String, String[]> weatherData, Map<String, String[]> connectionData){
        HashMap<String, City> cities = new HashMap<>();

        for (Map.Entry<String, String[]> weatherEntry : weatherData.entrySet()) {
                String key = StringStandardize.standardizeString(weatherEntry.getKey());
                String[] values = weatherEntry.getValue();
                int seaLevel;
                try {
                   seaLevel = Integer.parseInt(values[4]);
                } catch(NumberFormatException ex) {
                   seaLevel = 0;
                }    
                if (cities.get(key) == null) cities.put(key, new City(values[1],values[0], values[2], seaLevel, values[3]));
            }
        for (Map.Entry<String, String[]> connectionEntry : connectionData.entrySet()) {
            String[] keySplits = connectionEntry.getKey().split(",");
            String key = StringStandardize.standardizeString(keySplits[0]);
            String[] connectionCity = connectionEntry.getValue();
            City city = cities.get(key);
            if (city != null){
                String connectionCityKey = StringStandardize.standardizeString(connectionCity[2]);
                boolean isPresent = false;
                for (CityConnectionStruct c : city.connections){
                    if (c.name.equals(connectionCityKey)) isPresent=true;
                }
                if (!isPresent) city.connections.add(new CityConnectionStruct(connectionCityKey, Float.parseFloat(connectionCity[3]), Integer.parseInt(connectionCity[4])));

                // create back connection as well
                if (cities.get(connectionCityKey) == null){
                    String[] values = weatherData.get(connectionCityKey);
                    final int seaLevel = Integer.parseInt(values[4]) ;
                    cities.put(key, new City(values[1],values[0], values[2], seaLevel, values[3]));
                }
                City backConnCity = cities.get(connectionCityKey);
                for (CityConnectionStruct c : backConnCity.connections){
                    if (c.name.equals(key)) isPresent=true;
                }
                if (!isPresent) backConnCity.connections.add(new CityConnectionStruct(connectionCityKey, Float.parseFloat(connectionCity[3]), Integer.parseInt(connectionCity[4])));

            }
        }
        return cities;

    }

    public static HashMap<String, City> twoWayBuild(Map<String, String[]> weatherData, Map<String, String[]> connectionData){
        HashMap<String, City> cities = oneWayBuild(weatherData, connectionData);
        for (Map.Entry<String, City> cityEntry : cities.entrySet()) {
                String key = cityEntry.getKey();
                City city = cityEntry.getValue();
                for (CityConnectionStruct c: city.connections){
                    City connCity = cities.get(c.name);
                    if (connCity != null){
                        boolean isPresent = false;
                        for (CityConnectionStruct backConnC : connCity.connections){
                            if (backConnC.name.equals(key)) isPresent = true;
                        }
                        if (!isPresent){
                            connCity.connections.add(new CityConnectionStruct(key, c.distance, c.timeTaken));
                        }
                    }
                }
            }

        return cities;

    }

    // Verification check that all cities have a path from one to another
    // cities must not be of size 0
    public static boolean checkConnectivity(HashMap<String, City> cities, Logger logger){

        ArrayList<String> cityNames = new ArrayList<>();
        ArrayList<String> queue = new ArrayList<>();
        String cityKey = cities.entrySet().iterator().next().getKey();
        queue.add(cityKey);
        logger.info("----VERIFYING CONNECTIVITY-----");
        while (queue.size() > 0){
            String dequeueKey = queue.remove(0);
            City city = cities.get(dequeueKey);
            if (city != null){
                cityNames.add(dequeueKey);
                // System.out.println(dequeueKey);
                for (CityConnectionStruct connectCity : city.connections) {
                    if (!cityNames.contains(connectCity.name) && !queue.contains(connectCity.name)){
                        queue.add(connectCity.name);
                    }
                }
            }
            else if (checkOuterCityDisconnectedFlag){
                logger.info("City not in records : " + dequeueKey );
                return false;
            }
            // System.out.println(cityNames);

            // System.out.println(cityNames);
        }
        // System.out.println(cityNames);
        // System.out.println(cities.keySet());
        // System.out.println(cityNames.size());
        // System.out.println(cities.keySet().size());
        if (cityNames.size() == cities.size()){
            logger.info("Completed. All cities have path to each other!!!");
            logger.info("-----END-----");
            return true;
        }
        else {
            ArrayList<String> difference = new ArrayList<>(cities.keySet()) ;
            difference.removeAll(cityNames);

            logger.info("Some set of cities are cut off from the other set of cities." +" Total cities are " + cities.size()+ 
            ".\n Connected cities start from " + cityNames.get(0) +". The cities reachable from this starting point though any path are " + cityNames.size()
            + "\n" + "Unreachable cities are " + difference );
            
            logger.info("-----END-----\n");
            return false;
        }

    }
    public static void printAdjacencyList(HashMap<String, City> cityMap){
        int totalCities = 0;
        int totalConnections = 0;
        for (Map.Entry<String, City> cityEntry: cityMap.entrySet()){
            City city = cityEntry.getValue();
            totalCities +=1;
            System.out.println();
            System.out.println("####");
            System.out.print(city.name + " ( " + city.state + " ) :");
            for (CityConnectionStruct cityConnectionStruct : city.connections){
                if (cityMap.get(cityConnectionStruct.name)!= null){
                    String connCityState = cityMap.get(cityConnectionStruct.name).state ;
                    if (connCityState.equals(city.state)) System.out.print(" --- " + cityConnectionStruct.distance   + " km ---> " + cityConnectionStruct.name + " ,");
                    else System.out.print(" --- " + cityConnectionStruct.distance   + " km ---> " + cityConnectionStruct.name + "(" + connCityState + ") ," );
                }
            }
            totalConnections += city.connections.size();

        }
        System.out.println();
        System.out.println("Total Cities are : " + totalCities);
        System.out.println("Total Connections are : " + totalConnections + " Theoretical max (directed) are " + (totalCities * totalCities - totalCities));
        System.out.println("Avg connections per city are : " + (totalConnections * 1.0f/totalCities));

    }
}

class CityConnectionStruct {
    final String name;
    final float distance;
    final int timeTaken;
    CityConnectionStruct(String name, float distance, int timeTaken){
        this.name = name;
        this.distance = distance;
        this.timeTaken = timeTaken;

    }

    public void printString(String message, Logger logger){
        if (logger == null) System.out.println(message);
        else logger.info(message);
    }

    public void printObject(Logger logger){
        printString("---Connection Info---", logger);
        printString("Connected City Name : "+ name, logger);
        printString("Distance (forward) in km :" + String.valueOf(distance), logger);
        printString("Avg time taken (forward) in min :" + timeTaken, logger);
    }
}

class City {
    final String name;
    final String state;
    final int seaLevel;
    final String postCode;
    final TreeMap<Long, List<String>> weatherData;
    LinkedList<CityConnectionStruct> connections;


    City(String name, String state, String postCode, int seaLevel, String weatherDataString){
        this.name = name;
        this.state = state;
        this.seaLevel = seaLevel;
        this.postCode = postCode;
        this.weatherData = WeatherParse.parseWeatherData(weatherDataString);
        this.connections = new LinkedList<>();

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
