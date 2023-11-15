import java.util.Map;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;
import java.util.ArrayList;
import java.util.HashMap;


public class BuildCityObjects {

    // flags
    private static final boolean checkOuterCityDisconnectedFlag = false;





    public static void main(String[] args){
        String citiesCsvPath = "cities_3.csv";
        String connectionCsvPath = "connections_3.csv";


        
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

            System.out.println(checkConnectivity(cities));

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
                cities.put(key, new City(values[2], seaLevel, values[3]));
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

    public static HashMap<String, City> twoWayBuild(Map<String, String[]> weatherData, Map<String, String[]> connectionData){
        HashMap<String, City> cities = oneWayBuild(weatherData, connectionData);
        for (Map.Entry<String, City> cityEntry : cities.entrySet()) {
                String key = cityEntry.getKey();
                String name = key.split(",")[0];
                City city = cityEntry.getValue();
                if (city.connections.size() < 1){
                    for (Map.Entry<String, City> connectCityEntry : cities.entrySet()) {
                        City connectCity = connectCityEntry.getValue();
                        for ( CityConnectionStruct cityConnectionStruct: connectCity.connections){
                            if (name.equals(cityConnectionStruct.name)){
                                city.connections.add(cityConnectionStruct);
                            }
                        }
                    }
                }
            }

        return cities;

    }

    // Verification check that all cities have a path from one to another
    // cities must not be of size 0
    public static boolean checkConnectivity(HashMap<String, City> cities){

        ArrayList<String> cityNames = new ArrayList<>();
        ArrayList<String> queue = new ArrayList<>();
        String cityKey = cities.entrySet().iterator().next().getKey();
        queue.add(cityKey);

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
                System.out.println("City not in records");
                return false;
            }
            // System.out.println(cityNames);
        }

        if (cityNames.size() == cities.size()){
            return true;
        }
        else {
            return false;
        }
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

    public void printObject(){
        System.out.println("Connection Info");
        System.out.println(name);
        System.out.println(distance);
        System.out.println(timeTaken);
    }
}

class City {
    final int seaLevel;
    final String postCode;
    final TreeMap<Long, List<String>> weatherData;
    LinkedList<CityConnectionStruct> connections;


    City(String postCode, int seaLevel, String weatherDataString){
        this.seaLevel = seaLevel;
        this.postCode = postCode;
        this.weatherData = WeatherParse.parseWeatherData(weatherDataString);
        this.connections = new LinkedList<>();

    }

    public void printObject(){
        System.out.println("Post Code : " + this.postCode);
        System.out.println("Sea Level : " + String.valueOf(this.seaLevel));
        System.out.println("Weather Conditions");
        for (Map.Entry<Long, List<String>> entry : weatherData.entrySet()) {
            long timestamp = entry.getKey();
            List<String> conditions = entry.getValue();
            System.out.println("Timestamp: " + timestamp);
            System.out.println("Weather Conditions: " + conditions);
        }
        System.out.println("City Connections");
        for (CityConnectionStruct connection: connections){
            connection.printObject();
        }
        System.out.println();
        System.out.println();
        System.out.println();

    }
}
