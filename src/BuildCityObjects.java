package src;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;


public class BuildCityObjects {


    // flags
    private static final boolean checkOuterCityDisconnectedFlag = false;


    public static void main(String[] args) throws IOException{
        String citiesCsvPath = "data/Cities.txt";
        String connectionCsvPath = "data/Connections_test.txt";

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
        HashMap<String, City> cities;
        try{
            weatherData = LDP.loadIndividualCityData(citiesCsvPath);
            connectionData = LDP.loadCityConnectionData(connectionCsvPath);
            cities = twoWayBuild(weatherData, connectionData);

            for (Map.Entry<String, City> city : cities.entrySet()){
                System.out.println(city.getKey());
                city.getValue().printObject();
            }

            // System.out.println(checkConnectivity(cities));

        }
        catch(Exception e){
            e.printStackTrace();
        }
    }


    public static HashMap<String, City> oneWayBuild(Map<String, String[]> weatherData, Map<String, String[]> connectionData){
        HashMap<String, City> cities = new HashMap<>();

        for (Map.Entry<String, String[]> weatherEntry : weatherData.entrySet()) {
                String key = weatherEntry.getKey();
                String[] values = weatherEntry.getValue();
                int seaLevel;
                float lat;
                float lngt;
                try {
                   seaLevel = Integer.parseInt(values[4]);

                } catch(NumberFormatException ex) {
                    
                    seaLevel = 0;

                    ex.printStackTrace();
                }
                try {
                   lat = Float.parseFloat(values[5]);
                   lngt = Float.parseFloat(values[6]);
                } catch(NumberFormatException ex) {
                    
                    lat = -400;
                    lngt = -400;
                    System.out.println("No lat lng found. Assuming invalid vals");
                }        
                cities.put(key, new City(values[1],values[0], values[2], seaLevel, values[3], lat, lngt));
            }
        for (Map.Entry<String, String[]> connectionEntry : connectionData.entrySet()) {
            String[] keySplits = connectionEntry.getKey().split(",");
            String key = keySplits[0];

            String[] connectionCity = connectionEntry.getValue();
            City city = cities.get(key);

            if (city != null){

                String state = connectionCity[2];
                if (utils.standardizeString(state).equals(""))
                    state = city.state;
                city.connections.add(new CityConnectionStruct(connectionCity[3], state,
                 Float.parseFloat(connectionCity[4]), Integer.parseInt(connectionCity[5])));
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
                    String connkey = utils.standardizeCityKey(c.state, c.name);
                    // String connkey = StringStandardize.standardizeString(c.state) + "__" + StringStandardize.standardizeString(c.name);
                    City connCity = cities.get(connkey);
                    if (connCity != null){
                        boolean isPresent = false;
                        for (CityConnectionStruct backConnC : connCity.connections){
                            String backConnCKey = utils.standardizeCityKey(backConnC.state, backConnC.name);
                            // String backConnCKey = StringStandardize.standardizeString(backConnC.state) + "__" + StringStandardize.standardizeString(backConnC.name);
                            if (backConnCKey.equals(key)) isPresent = true;
                        }
                        if (!isPresent){
                            connCity.connections.add(new CityConnectionStruct(city.name, city.state, c.distance, c.timeTaken));
                        }
                    }
                }
            }

        return cities;

    }

    public static HashMap<String, City> makeDistancesEqual(HashMap<String, City> citiesDS){
        for (Map.Entry<String, City> cityEntry : citiesDS.entrySet()) {
                String key = cityEntry.getKey();
                City city = cityEntry.getValue();
                for (CityConnectionStruct c: city.connections){
                    String connkey = utils.standardizeCityKey(c.state, c.name);
                    // String connkey = StringStandardize.standardizeString(c.state) + "__" + StringStandardize.standardizeString(c.name);
                    City connCity = citiesDS.get(connkey);
                    if (connCity != null){
                        for (CityConnectionStruct backConnC : connCity.connections){
                            String backConnCKey = utils.standardizeCityKey(backConnC.state, backConnC.name);
                            // String backConnCKey = StringStandardize.standardizeString(backConnC.state) + "__" + StringStandardize.standardizeString(backConnC.name);
                            if (backConnCKey.equals(key)) c.distance = backConnC.distance;

                        }

                    }
                }
            }

        return citiesDS;

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
                    String connkey = utils.standardizeCityKey(connectCity.state, connectCity.name);
                    // String connkey = StringStandardize.standardizeString(connectCity.state) + "__" + StringStandardize.standardizeString(connectCity.name);
                    if (!cityNames.contains(connkey) && !queue.contains(connkey)){
                        queue.add(connkey);
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
        float totalDistance = 0;
        int totalConnections = 0;
        for (Map.Entry<String, City> cityEntry: cityMap.entrySet()){
            City city = cityEntry.getValue();
            totalCities +=1;
            System.out.println();
            System.out.println("####");
            System.out.print(city.name + " ( " + city.state + " ) :");
            for (CityConnectionStruct cityConnectionStruct : city.connections){
                String connkey = utils.standardizeCityKey(cityConnectionStruct.state, cityConnectionStruct.name);
                // String connkey = StringStandardize.standardizeString(cityConnectionStruct.state) + "__" + StringStandardize.standardizeString(cityConnectionStruct.name);
                if (cityMap.get(connkey)!= null){
                    String connCityState = cityMap.get(connkey).state ;
                    if (connCityState.equals(city.state)) System.out.print(" --- " + cityConnectionStruct.distance/1.6   + " miles ---> " + cityConnectionStruct.name + " ,");
                    else System.out.print(" --- " + cityConnectionStruct.distance/1.6   + " miles ---> " + cityConnectionStruct.name + "(" + connCityState + ") ," );
                    totalDistance += cityConnectionStruct.distance;
                }
            }
            totalConnections += city.connections.size();

        }
        System.out.println();
        System.out.println("Total Cities are : " + totalCities);
        System.out.println("Total Connections are : " + totalConnections + " Theoretical max (directed) are " + (totalCities * totalCities - totalCities));
        System.out.println("Avg connections per city are : " + (totalConnections * 1.0f/totalCities));
        System.out.println("Total Cumulative Distance is : " + totalDistance/1.6 + " miles");


    }

    public static HashMap<String, City> copyCityMap(HashMap<String, City> cityMap){

        HashMap<String, City> newCityMap = new HashMap<>();
        for (Map.Entry<String,City> me: cityMap.entrySet()) newCityMap.put(me.getKey(), new City(me.getValue()));
        return newCityMap;
    }
}
