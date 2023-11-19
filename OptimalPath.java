import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Scanner;
import java.io.File;
import java.util.Date;


public class OptimalPath {
    
    // criteria for Cost Calcualtion. Param to set
    private static int costCalculationCriteria = 0 ;


    private static int riskThreshold = 100 ;
    private static int riskFactorScale = 20;
    // km per litre of an average US car (Sorry Matt XD)
    private static float gasMileage = 10.9f;
    

    /*(assuming an avg of 70 kph or 43.75 mph speed on highways)
    indicates min to km concersion, e.g 1 min '=' 7/6 km for cost related purposes
    */
    private static float timeToDistanceRatio = 7f/6;


    private static Map<String, ArrayList<Integer>> loadWeatherRiskFactor(String csvPath){

        try {
            Scanner scanner = new Scanner(new File(csvPath));
            String line = scanner.nextLine(); // discard headers. No need to check this small file which wont change much apart form num values
            HashMap<String, ArrayList<Integer>> weatherRisk = new HashMap<>();

            while( scanner.hasNextLine()){
                String[] fields = scanner.nextLine().split(",");
                weatherRisk.put(StringStandardize.standardizeString(fields[0]), new ArrayList<Integer>() {{
                    add(Integer.parseInt(fields[1])); // sfatey risk
                    add(Integer.parseInt(fields[2])); // time risk
                }});
            }
            return weatherRisk;
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    private static WrapperOutput findOptimalPath(Map<String, City> cityMap, Map<String, ArrayList<Integer>> weatherRiskMap, String startCity, String goalCity, long startTime) {
        Map<String, String> cameFrom = new HashMap<>();
        Map<String, CostStruct> gScore = new HashMap<>();


        PriorityQueue<String> openSet = new PriorityQueue<>(Comparator.comparingDouble(city -> (gScore.get(city).getCostStructValue(riskThreshold))));

        for (String city : cityMap.keySet()) {
            gScore.put(city, new CostStruct(Float.MAX_VALUE, Integer.MAX_VALUE));
        }
        gScore.put(startCity, new CostStruct(0, 0, startTime));

        openSet.add(startCity);
        while (!openSet.isEmpty()) {
            String currentCity = openSet.poll();


            if (currentCity.equals(goalCity)) {
                return new WrapperOutput(reconstructPath(cameFrom, currentCity), gScore);
            }
            for (CityConnectionStruct neighbor : cityMap.get(currentCity).connections) {
                if (cityMap.get(neighbor.name) != null){
                    
                    CostStruct currentCityGScore = gScore.get(currentCity);
                    // add func is NOT SYMMETRICAL. Please see implementation for details
                    CostStruct tentativeGScore = currentCityGScore.add(costBetweenCities(currentCity, neighbor.name, cityMap, weatherRiskMap, currentCityGScore.startTime));
                    if (tentativeGScore.getCostStructValue(riskThreshold) < gScore.get(neighbor.name).getCostStructValue(riskThreshold)) {
                        cameFrom.put(neighbor.name, currentCity);
                        gScore.put(neighbor.name, tentativeGScore);
                        if (!openSet.contains(neighbor.name)) {
                            openSet.add(neighbor.name);
                        }
                    }
                }
            }
        }

        return null;
    }





    private static CostStruct costBetweenCities(String city, String connectedCity, Map<String , City> cityMap, Map<String, ArrayList<Integer>> weatherRiskMap, long startTime) {
        City cityObject = cityMap.get(city);
        CityConnectionStruct ccs = null;
        for (CityConnectionStruct c : cityObject.connections){
            if (c.name.equals(connectedCity)) ccs = c;
        }
        List<String> currentCityWeather = WeatherParse.getNearestWeather(startTime, cityObject.weatherData);
        int totalSafetyRisk = 0 , totalTimeRisk = 0;
        for(int i=0;i < currentCityWeather.size(); i++){
            List<Integer> risks = weatherRiskMap.get(StringStandardize.standardizeString(currentCityWeather.get(i)));
            if (risks != null){
                totalSafetyRisk += risks.get(0);
                totalTimeRisk += risks.get(1); 
            }
            else{
                System.out.println("Weather Condition not Found. Assuming no risk for ");
                System.out.println(currentCityWeather.get(i));
            }
        }


        // TODO: use haversteins distance based on lat/lng . Coz google will show road distance(including slope), and 
        // not just ground level distance
        float distanceGradient = (cityObject.seaLevel- cityMap.get(ccs.name).seaLevel) / (ccs.distance * 1000) ; 
        long timeTaken = (long)(ccs.timeTaken * 60 * 1000 * (1 + totalTimeRisk * 1.0f / riskFactorScale));

        // just distance based
        if (costCalculationCriteria == 0){
            float distance = ccs.distance * (1 + distanceGradient);
            return new CostStruct(distance, totalSafetyRisk, startTime + timeTaken);
        }
        // just time based
        else if (costCalculationCriteria == 1){
            return new CostStruct(-1 * ccs.timeTaken);
        }
        // distance & time 
        else if (costCalculationCriteria == 2)
        {
            float distance = ccs.distance * (1 + distanceGradient);
            return new CostStruct(distance + timeToDistanceRatio * ccs.timeTaken, totalSafetyRisk, startTime + timeTaken);
        }
        // just weather based
        else if (costCalculationCriteria == 3){
            return new CostStruct(0, totalSafetyRisk, startTime + timeTaken);
        }

        // weather & distance
        else if (costCalculationCriteria == 4){
            float distance = ccs.distance * (1 + distanceGradient);
            return new CostStruct(distance, totalSafetyRisk, startTime + timeTaken);
        }
        // weather, distance & time based - full hybrid
        else if (costCalculationCriteria == 5){
            float distance = ccs.distance * (1 + distanceGradient);
            return new CostStruct(distance + timeToDistanceRatio * timeTaken /(60 * 1000), totalSafetyRisk, startTime + timeTaken);
        }

        // if no criteria then just return max cost
        return new CostStruct();
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

    private static void printPathInfo(WrapperOutput path, HashMap<String, City> citiesDS,Map<String, ArrayList<Integer>> weatherRiskMap){
        List<String> cityNames = path.cityNames;
        Map<String, CostStruct> gScore = path.gScore;
        float totalDistance = 0;
        int totalTimeTaken = 0;


        for (int i=1; i < cityNames.size(); i++){
            String sourceCity = cityNames.get(i - 1);
            String destinationCity = cityNames.get(i);
            long startTime = gScore.get(sourceCity).startTime;
            long currentTime = startTime;

            System.out.print(sourceCity);
            System.out.print(" ------> ");
            System.out.println(destinationCity); 
            for (CityConnectionStruct c : citiesDS.get(sourceCity).connections){
                if (c.name.equals(destinationCity)){
                    System.out.print("Distance is ");
                    System.out.print(c.distance);
                    System.out.println( " km");
                    float timeTaken = (gScore.get(destinationCity).startTime - currentTime)/(1000 * 60);
                    System.out.println("Time start is " + new Date(currentTime));
                    System.out.println("Time taken is " + timeTaken + " min");
                    System.out.println("Time of arrival is " + new Date(gScore.get(destinationCity).startTime));
                    totalTimeTaken += timeTaken;
                    totalDistance += c.distance;
                    System.out.println();
                    break;
                }
            }
        }
        System.out.println("");
        System.out.println("Path is : ");
        System.out.print(cityNames.get(0));
        for (int i=1;i < cityNames.size(); i++){
            System.out.print(" ------> ");
            System.out.print(cityNames.get(i));
        }
        System.out.println();
        System.out.println("Total Distance is " + totalDistance + " km");
        System.out.println("Total Time taken is " + totalTimeTaken + " min");

    } 

    public static void main(String[] args) throws IOException{


        String citiesCsvPath = "Cities.txt" ;
        String connectionCsvPath = "Connections.txt" ;



        String weatherRiskCsvPath = "Weather Risk Factor.txt";

        try{
            Map<String, String[]> weatherData = LDP.loadIndividualCityData(citiesCsvPath);
            Map<String, String[]> connectionData = LDP.loadCityConnectionData(connectionCsvPath);
            Map<String, ArrayList<Integer>> weatherRiskMap = loadWeatherRiskFactor(weatherRiskCsvPath);
            HashMap<String, City> citiesDS = BuildCityObjects.twoWayBuild(weatherData, connectionData);
            DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm");
            String inputCommand = "";
            while (!inputCommand.equals("quit")){
                System.out.println("Please input command (quit / visualize / path)");
                Scanner myObj = new Scanner(System.in);  // Create a Scanner object
                inputCommand = StringStandardize.standardizeString(myObj.nextLine());
                if (inputCommand.equals("path")){
                    System.out.println("Please Enter source city");
                    myObj = new Scanner(System.in);
                    String sourceCity = StringStandardize.standardizeString(myObj.nextLine());
                    System.out.println("Please Enter Destination City");
                    myObj = new Scanner(System.in);
                    String destinationCity = StringStandardize.standardizeString(myObj.nextLine());
                    System.out.println("Please Enter Start time in format : MM/dd/yyyy HH:mm"); // e.g 11/08/2023 12:00 any other date in this format is fine but we collected weather data for 8-9 Nov inclusive
                    myObj = new Scanner(System.in);
                    long startTime = df.parse(myObj.nextLine()).getTime();
                    WrapperOutput path = findOptimalPath( citiesDS, weatherRiskMap, sourceCity, destinationCity, startTime);
                    printPathInfo(path, citiesDS, weatherRiskMap);
                }
                else {
                    System.out.println("Construction underway");
                }

            }
        }


        catch(Exception e){
            e.printStackTrace();
        }

    }
}

class CostStruct {
    // cost here can be distance, time or any other combination of distance, risk, time 
    public float cost;
    public int risk;
    // meta data to help with efficient implementation
    long startTime;

    CostStruct(float cost, int risk, long startTime){
        this.cost = cost;
        this.risk = risk;
        this.startTime = startTime;
    }

    CostStruct(float cost,int risk){
        // timestamp corresponds to 8 Nov 2023 00:00:00
        this(cost, risk, 1670458260000l);
    }
    CostStruct(float cost){
        this(cost, 0);
    }

    CostStruct(){
        this(Float.MAX_VALUE, Integer.MAX_VALUE);
    }

    public float getCostStructValue(int riskThreshold) {
        if (risk > riskThreshold){
            return Float.MAX_VALUE;
        }
        else{
            return cost;
        }
    }

    public CostStruct add (CostStruct other){
        return new CostStruct(this.cost + other.cost, this.risk + other.risk, other.startTime);
    }
}

class WrapperOutput{
    List<String> cityNames;
    Map<String, CostStruct> gScore;

    WrapperOutput(List<String> cityNames, Map<String, CostStruct> gScore){
        this.cityNames = cityNames;
        this.gScore = gScore;
    }
}