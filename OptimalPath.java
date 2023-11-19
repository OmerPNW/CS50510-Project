import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
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


    private static int riskThreshold = Integer.MAX_VALUE ;
    private static int riskFactorScale = 20;
    // km per litre of an average US car (Sorry Matt XD) even tho the name literally says "mile"age
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
        // not just ground level distance // also distance grad to be modified as its simply adding sea level difference lol
        // can have sealevel diff/dist * sea level diff(as proportionality is scaled by grad diff/steepness)
        float distanceGradient = (cityObject.seaLevel- cityMap.get(ccs.name).seaLevel) / (ccs.distance * 1000) ; 
        long timeTaken = (long)(ccs.timeTaken * 60 * 1000 * (1 + totalTimeRisk * 1.0f / riskFactorScale));

        // just distance based
        if (costCalculationCriteria == 0){
            // just distance based for now so commenting below
            //float distance = ccs.distance * (1 + distanceGradient);
            return new CostStruct(ccs.distance, totalSafetyRisk, startTime + timeTaken);
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
        float totalTimeTaken = 0;
        int totalPerceivedRisk = 0;


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
                    float timeTaken = (gScore.get(destinationCity).startTime - currentTime)/(1000 * 60f);
                    System.out.println("Time start is " + new Date(currentTime));
                    System.out.println("Expected Time taken is " + timeTaken + " min");
                    System.out.println("ETA is " + new Date(gScore.get(destinationCity).startTime));
                    int riskVal = (gScore.get(destinationCity).risk - gScore.get(sourceCity).risk);
                    System.out.println("Perceived risk due to weather" + WeatherParse.getNearestWeather(startTime, citiesDS.get(sourceCity).weatherData) 
                     + " is " + riskVal);
                    totalTimeTaken += timeTaken;
                    totalDistance += c.distance;
                    totalPerceivedRisk += riskVal;
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
        System.out.println("Total Perceived risk is " + totalPerceivedRisk);


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
            Scanner myObj = null;
            while (!inputCommand.equals("quit")){
                System.out.println("Please input command (visualize / path / quit)");
                myObj = new Scanner(System.in);  // Create a Scanner object
                inputCommand = StringStandardize.standardizeString(myObj.nextLine());
                if (inputCommand.equals("path")){
                    System.out.println("Please Enter source city");
                    String sourceCity = StringStandardize.standardizeString(myObj.nextLine());
                    if (citiesDS.get(sourceCity) == null){
                        System.out.println("Source city not in our repertoire. Rewinding!!!" );
                        continue;
                    }
                    System.out.println("Please Enter Destination City");
                    String destinationCity = StringStandardize.standardizeString(myObj.nextLine());
                    if (citiesDS.get(destinationCity) == null){
                        System.out.println("Destination city not in our repertoire. Rewinding!!!" );
                        continue;
                    }
                    System.out.println("Please Enter Start time in format : MM/dd/yyyy HH:mm"); // e.g 11/08/2023 12:00 any other date in this format is fine but we collected weather data for 8-9 Nov inclusive
                    long startTime = 0;
                    try {
                        startTime = df.parse(myObj.nextLine()).getTime();
                    }
                    catch(ParseException pe){
                        System.out.println("Date Format not adhering to specified format. E.g is 11/08/2023 23:44. Rewinding !!!");
                    }
                    WrapperOutput path = findOptimalPath( citiesDS, weatherRiskMap, sourceCity, destinationCity, startTime);
                    printPathInfo(path, citiesDS, weatherRiskMap);
                }
                else if (inputCommand.equals("visualize")){
                    System.out.println("Full details : (y) ?");
                    String response = StringStandardize.standardizeString(myObj.nextLine());
                    if (response.equals("y")) {
                        for (HashMap.Entry<String, City> city : citiesDS.entrySet()){
                            city.getValue().printObject();
                        }
                    }
                    else BuildCityObjects.printAdjacencyList(citiesDS);
                    System.out.println();
                    System.out.println();

                }
                else {
                    System.out.println("Unknown Command");
                }
            }
            myObj.close();
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