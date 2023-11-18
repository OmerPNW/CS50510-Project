import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Scanner;
public class OptimalPath {
    

    private static List<String> findOptimalPath(Map<String, City> cityMap, String startCity, String goalCity) {
        Map<String, String> cameFrom = new HashMap<>();
        Map<String, Float> gScore = new HashMap<>();
        PriorityQueue<String> openSet = new PriorityQueue<>(Comparator.comparingDouble(city -> (gScore.getOrDefault(city, Float.MAX_VALUE))));

        for (String city : cityMap.keySet()) {
            gScore.put(city, Float.MAX_VALUE);
        }

        gScore.put(startCity, 0f);

        openSet.add(startCity);
        System.out.println("-----START-----");
        while (!openSet.isEmpty()) {
            String currentCity = openSet.poll();


            if (currentCity.equals(goalCity)) {
                return reconstructPath(cameFrom, currentCity);
            }

            for (CityConnectionStruct neighbor : cityMap.get(currentCity).connections) {
                if (cityMap.get(neighbor.name) != null){
                    float tentativeGScore = gScore.get(currentCity) + costBetweenCities(currentCity, neighbor.name, cityMap);
                    
                    if (tentativeGScore < gScore.get(neighbor.name)) {
                        cameFrom.put(neighbor.name, currentCity);
                        gScore.put(neighbor.name, tentativeGScore);
                        if (!openSet.contains(neighbor.name)) {
                            openSet.add(neighbor.name);
                        }
                    }
                }
            }
        }

        return Collections.emptyList();
    }


    private static float costBetweenCities(String city, String connectedCity, Map<String , City> cityMap) {
        // just distance based
        City cityObject = cityMap.get(city);
        for (CityConnectionStruct c : cityObject.connections){
            if (c.name.equals(connectedCity)) return c.distance;
        }
        return Float.MAX_VALUE;
    }

    private static List<String> reconstructPath(Map<String, String> cameFrom, String currentCity) {
        List<String> path = new ArrayList<>();
        while (currentCity != null) {
            path.add(currentCity);
            currentCity = cameFrom.get(currentCity);
        }
        Collections.reverse(path);
        return path;
    }


    public static void main(String[] args) throws IOException{


        String citiesCsvPath = "Cities.txt" ;
        String connectionCsvPath = "Connections.txt" ;


        try{
            Map<String, String[]> weatherData = LDP.loadIndividualCityData(citiesCsvPath);
            Map<String, String[]> connectionData = LDP.loadCityConnectionData(connectionCsvPath);
            HashMap<String, City> citiesDS = BuildCityObjects.twoWayBuild(weatherData, connectionData);
            String inputCommand = "";
            while (!inputCommand.equals("quit")){
                System.out.println("Please input command");
                Scanner myObj = new Scanner(System.in);  // Create a Scanner object
                inputCommand = StringStandardize.standardizeString(myObj.nextLine());
                if (inputCommand.equals("path")){
                    System.out.println("Please Enter source city");
                    myObj = new Scanner(System.in);
                    String sourceCity = StringStandardize.standardizeString(myObj.nextLine());
                    System.out.println("Please Enter Destination City");
                    myObj = new Scanner(System.in);
                    String destinationCity = StringStandardize.standardizeString(myObj.nextLine());
                    System.out.println((findOptimalPath( citiesDS, sourceCity, destinationCity)));
                }
            }
        }


        catch(Exception e){
            e.printStackTrace();
        }

    }
}
