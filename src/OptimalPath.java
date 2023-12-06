package src;
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
import java.util.Random;
import java.util.Scanner;
import java.io.File;
import java.util.Date;

import javax.swing.*;


public class OptimalPath {
    
    // criteria for Cost Calcualtion. Param to set
    private static int costCalculationCriteria = 0 ;


    private static int riskThreshold = 1000 ;
    private static int riskFactorScale = 20;
    // km per litre of an average US car (Sorry Matt XD) even tho the name literally says "mile"age
    private static float gasMileage = 16.061f; // 38 miles per gallon
    

    /*(assuming an avg of 70 kph or 43.75 mph speed on highways)
    indicates min to km concersion, e.g 1 min '=' 7/6 km for cost related purposes
    */
    private static float timeToDistanceRatio = 7f/6;


    public static Map<String, ArrayList<Integer>> loadWeatherRiskFactor(String csvPath){

        try {
            Scanner scanner = new Scanner(new File(csvPath));
            scanner.nextLine(); // discard headers. No need to check this small file which wont change much apart form num values
            HashMap<String, ArrayList<Integer>> weatherRisk = new HashMap<>();

            while( scanner.hasNextLine()){
                String[] fields = scanner.nextLine().split(",");
                weatherRisk.put(utils.standardizeString(fields[0]), new ArrayList<Integer>() {{
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

    private static WrapperOutput findOptimalPathDjikstra(Map<String, City> cityMap, Map<String, ArrayList<Integer>> weatherRiskMap, String startState,String startCity,
    String goalState, String goalCity, long startTime, boolean debug) {
        Map<String, String> cameFrom = new HashMap<>();
        Map<String, CostStruct> gScore = new HashMap<>();


        PriorityQueue<String> openSet = new PriorityQueue<>(Comparator.comparingDouble(stateCity -> (gScore.get(stateCity).getCostStructValue(riskThreshold))));

        for (String city : cityMap.keySet()) {
            gScore.put(city, new CostStruct(Float.MAX_VALUE, Integer.MAX_VALUE));
        }
        String startKey = utils.standardizeCityKey(startState, startCity);
        // String startKey = StringStandardize.standardizeString(startState) + "__" + StringStandardize.standardizeString(startCity);
        String goalKey = utils.standardizeCityKey(goalState, goalCity);
        // String goalKey = StringStandardize.standardizeString(goalState) + "__" + StringStandardize.standardizeString(goalCity);

        gScore.put(startKey, new CostStruct(0, 0, startTime));

        openSet.add(startKey);
        while (!openSet.isEmpty()) {
            String currentCityKey = openSet.poll();


            if (currentCityKey.equals(goalKey)) {
                return new WrapperOutput(reconstructPath(cameFrom, currentCityKey), gScore);
            }
            for (CityConnectionStruct neighbor : cityMap.get(currentCityKey).connections) {
                String neighborKey = utils.standardizeCityKey(neighbor.state, neighbor.name);
                // String neighborKey = StringStandardize.standardizeString(neighbor.state) + "__" + StringStandardize.standardizeString(neighbor.name);

                if (cityMap.get(neighborKey) != null){
                    
                    CostStruct currentCityGScore = gScore.get(currentCityKey);
                    // add func is NOT SYMMETRICAL. Please see implementation for details
                    CostStruct tentativeGScore = currentCityGScore.add(costBetweenCities(currentCityKey, neighborKey, cityMap, weatherRiskMap, currentCityGScore.startTime));
                    if (tentativeGScore.getCostStructValue(riskThreshold) < gScore.get(neighborKey).getCostStructValue(riskThreshold)) {
                        cameFrom.put(neighborKey, currentCityKey);
                        gScore.put(neighborKey, tentativeGScore);
                        if (!openSet.contains(neighborKey)) {
                            openSet.add(neighborKey);
                        }
                    }
                }
                else if (debug) System.out.println(neighborKey + " not found");
            }
        }

        return null;
    }


    private static WrapperOutput findOptimalPathBellmanFord(Map<String, City> cityMap, Map<String, ArrayList<Integer>> weatherRiskMap, String startState, String startCity,
                                                         String goalState, String goalCity, long startTime, boolean debug) {
        Map<String, String> cameFrom = new HashMap<>();
        Map<String, CostStruct> gScore = new HashMap<>();

        for (String city : cityMap.keySet()) {
            gScore.put(city, new CostStruct(Float.MAX_VALUE, Integer.MAX_VALUE));
        }

        String startKey = utils.standardizeCityKey(startState, startCity);
        String goalKey = utils.standardizeCityKey(goalState, goalCity);

        gScore.put(startKey, new CostStruct(0, 0, startTime));

        // V-1 iterations
        for (int i = 0; i < cityMap.size() - 1; i++) {
            for (String currentCityKey : cityMap.keySet()) {
                CostStruct currentCityGScore = gScore.get(currentCityKey);

                for (CityConnectionStruct neighbor : cityMap.get(currentCityKey).connections) {
                    String neighborKey = utils.standardizeCityKey(neighbor.state, neighbor.name);

                    if (cityMap.get(neighborKey) != null) {
                        CostStruct tentativeGScore = currentCityGScore.add(costBetweenCities(currentCityKey, neighborKey, cityMap, weatherRiskMap, currentCityGScore.startTime));
                        if (tentativeGScore.getCostStructValue(riskThreshold) < gScore.get(neighborKey).getCostStructValue(riskThreshold)) {
                            cameFrom.put(neighborKey, currentCityKey);
                            gScore.put(neighborKey, tentativeGScore);
                        }
                    } else if (debug) {
                        System.out.println(neighborKey + " not found");
                    }
                }
            }
        }

        // Check for negative cycles and throw an error if so
        for (String currentCityKey : cityMap.keySet()) {
            CostStruct currentCityGScore = gScore.get(currentCityKey);

            for (CityConnectionStruct neighbor : cityMap.get(currentCityKey).connections) {
                String neighborKey = utils.standardizeCityKey(neighbor.state, neighbor.name);
                if (cityMap.get(neighborKey) != null) {
                    CostStruct tentativeGScore = currentCityGScore.add(costBetweenCities(currentCityKey, neighborKey, cityMap, weatherRiskMap, currentCityGScore.startTime));
                    if (tentativeGScore.getCostStructValue(riskThreshold) < gScore.get(neighborKey).getCostStructValue(riskThreshold)) {
                        // Negative cycle detected
                        throw new RuntimeException("Negative cycle detected");
                    }
                }
            }
        }

        return new WrapperOutput(reconstructPath(cameFrom, goalKey), gScore);
    }


    public static CostStruct costBetweenCities(String cityKey, String connectedCityKey, Map<String , City> cityMap, Map<String, ArrayList<Integer>> weatherRiskMap, long startTime) {
        City cityObject = cityMap.get(cityKey);
        CityConnectionStruct ccs = null;
        for (CityConnectionStruct c : cityObject.connections){
            String connKey = utils.standardizeCityKey(c.state, c.name);
            // String connKey = StringStandardize.standardizeString(c.state) + "__" + StringStandardize.standardizeString(c.name);
            if (connKey.equals(connectedCityKey)) ccs = c;
        }
        String connKey = utils.standardizeCityKey(ccs.state, ccs.name);
        // String connKey = StringStandardize.standardizeString(ccs.state) + "__" + StringStandardize.standardizeString(ccs.name);
        List<String> currentCityWeather = WeatherParse.getNearestWeather(startTime, cityObject.weatherData);
        int totalSafetyRisk = 0 , totalTimeRisk = 0;
        for(int i=0;i < currentCityWeather.size(); i++){
            List<Integer> risks = weatherRiskMap.get(utils.standardizeString(currentCityWeather.get(i)));
            if (risks != null){
                totalSafetyRisk += risks.get(0);
                totalTimeRisk += risks.get(1); 
            }
            // else{
            //     System.out.println("Weather Condition not Found. Assuming no risk for ");
            //     System.out.println(currentCityWeather.get(i));
            // }
        }

        // TODO: use haversteins distance based on lat/lng . Coz google will show road distance(including slope), and 
        // not just ground level distance // also distance grad to be modified as its simply adding sea level difference lol
        // can have sealevel diff/dist * sea level diff(as proportionality is scaled by grad diff/steepness)
        int seaLevelDiff = (cityMap.get(connKey).seaLevel - cityObject.seaLevel );
        float distanceGradient = seaLevelDiff * 1.0f / (ccs.distance * 1000) ; 
        long timeTaken = (long)(ccs.timeTaken * 60 * 1000 * (1 + totalTimeRisk * 1.0f / riskFactorScale));
        float adjustedGasMileage = gasMileage * 1.0f/ ((1 + distanceGradient * Math.abs(seaLevelDiff) * 0.05f)); //+ totalTimeRisk * 0.0f/(riskFactorScale*12));
        adjustedGasMileage = Math.max(gasMileage * 0.9f,Math.min((gasMileage*1.1f), adjustedGasMileage));

        // just distance based
        if (costCalculationCriteria == 0){
            // just distance based for now so commenting below
            //float distance = ccs.distance * (1 + distanceGradient);
            return new CostStruct(ccs.distance, totalSafetyRisk, startTime + timeTaken, adjustedGasMileage);
        }
        // just time based
        else if (costCalculationCriteria == 1){
            return new CostStruct(ccs.timeTaken,  totalSafetyRisk, startTime + timeTaken, adjustedGasMileage);
        }
        // distance & time 
        else if (costCalculationCriteria == 2)
        {
            float distance = ccs.distance * (1 + distanceGradient * Math.abs(seaLevelDiff) * 0.05f);
            return new CostStruct(distance + timeToDistanceRatio * ccs.timeTaken, totalSafetyRisk, startTime + timeTaken, adjustedGasMileage);
        }
        else if (costCalculationCriteria == 3){
            return new CostStruct(0, totalSafetyRisk, startTime + timeTaken, adjustedGasMileage);
        }

        // weather (with distance as break tie factor)
        else if (costCalculationCriteria == 4){
            float distance = ccs.distance * (1 + distanceGradient * Math.abs(seaLevelDiff) * 0.05f);
            return new CostStruct(distance + totalSafetyRisk * 15000, totalSafetyRisk, startTime + timeTaken, adjustedGasMileage);
        }
        // weather, distance & time based - full hybrid
        else if (costCalculationCriteria == 5){
            float distance = ccs.distance * (1 + distanceGradient * Math.abs(seaLevelDiff) * 0.05f);
            return new CostStruct(distance + timeToDistanceRatio * timeTaken /(60 * 1000), totalSafetyRisk, startTime + timeTaken, adjustedGasMileage);
        }
        // lowest gas consumption
        else if (costCalculationCriteria == 6){
            return new CostStruct(ccs.distance / adjustedGasMileage, totalSafetyRisk, startTime + timeTaken, adjustedGasMileage);
        }

        else if (costCalculationCriteria == 7){
            // in optimal
            return new CostStruct((float)(Math.random()), totalSafetyRisk, startTime + timeTaken, adjustedGasMileage);
        }
        // if no criteria then just return max cost
        return new CostStruct();
    }

    private static List<String> reconstructPath(Map<String, String> cameFrom, String currentCityKey) {
        List<String> path = new ArrayList<>();
        while (currentCityKey != null) {
            path.add(currentCityKey);
            currentCityKey = cameFrom.get(currentCityKey);
        }
        Collections.reverse(path);
        return path;
    }

    private static ArrayList<String> inputCityKeyNames(int num, HashMap<String, City> cities, Scanner myObj){
        ArrayList<String> cityKeyNames = new ArrayList<>();
        for (int i=0;i<num;i++){
            System.out.println("Please Enter city");
            String City = utils.standardizeString(myObj.nextLine());
            System.out.println("Please Enter the state of "+ City);
            String State = utils.standardizeString(myObj.nextLine());
            String sourceCityKey = utils.standardizeCityKey(State, City);
            // String sourceCityKey = StringStandardize.standardizeString(State) + "__" + StringStandardize.standardizeString(City);
            if (cities.get(sourceCityKey) == null){
                System.out.println(City + " in the state of "+ State + " is not in our repertoire. Rewinding!!!" );
                return null;
            }
            cityKeyNames.add(sourceCityKey);
        }
        return cityKeyNames;
    }

    public static ArrayList<String> selectLimitedConnectedCities(HashMap<String, City> cities, int connectedCitiesNum){

        ArrayList<String> cityNames = new ArrayList<>();
        ArrayList<String> queue = new ArrayList<>();
        int index = new Random().nextInt(connectedCitiesNum);
        String cityKey  = cities.keySet().toArray(new String[cities.size()])[index];
        queue.add(cityKey);
        while (queue.size() > 0 & cityNames.size() != connectedCitiesNum){
            String dequeueKey = queue.remove(0);
            City city = cities.get(dequeueKey);
            if (city != null){
                cityNames.add(dequeueKey);
                // System.out.println(dequeueKey);
                for (CityConnectionStruct connectCity : city.connections) {
                    String connkey = utils.standardizeCityKey(connectCity.state, connectCity.name);
                    //String connkey = StringStandardize.standardizeString(connectCity.state) + "__" + StringStandardize.standardizeString(connectCity.name);
                    if (!cityNames.contains(connkey) && !queue.contains(connkey)){
                        queue.add(connkey);
                    }
                }
            }
        }
        return cityNames;

    }
    
    private static void printPathInfo(WrapperOutput path, HashMap<String, City> citiesDS,Map<String, ArrayList<Integer>> weatherRiskMap, boolean details){
        List<String> cityKeys = path.cityKeys;
        Map<String, CostStruct> gScore = path.gScore;
        float totalDistance = 0;
        float totalTimeTaken = 0;
        int totalPerceivedRisk = 0;
        float expectedGasMileage = 0;

        for (int i=1; i < cityKeys.size(); i++){
            String sourceCityKey = cityKeys.get(i - 1);
            String destinationCityKey = cityKeys.get(i);
            long startTime = gScore.get(sourceCityKey).startTime;
            long currentTime = startTime;
            City sourceCity = citiesDS.get(sourceCityKey);
            City destinationCity = citiesDS.get(destinationCityKey);
            if (details){
                System.out.print("(" + sourceCity.state + ") " + sourceCity.name);
                System.out.print(" ------> ");
                if(!utils.standardizeString(sourceCity.state).equals(utils.standardizeString(destinationCity.state)))
                    System.out.println("(" + destinationCity.state + ") " + destinationCity.name); 
                else System.out.println(destinationCity.name);
            }
            for (CityConnectionStruct c : sourceCity.connections){
                String connKey = utils.standardizeCityKey(c.state, c.name);
                // String connKey = StringStandardize.standardizeString(c.state) + "__" + StringStandardize.standardizeString(c.name);
                if (connKey.equals(destinationCityKey)){
                    int riskVal = (gScore.get(destinationCityKey).risk - gScore.get(sourceCityKey).risk);
                    float timeTaken = (gScore.get(destinationCityKey).startTime - currentTime)/(1000 * 60f);
                    if (details){
                        System.out.print("Distance is ");
                        System.out.print(c.distance / 1.6);
                        System.out.println( " miles");
                        System.out.println("Time start is " + new Date(currentTime));
                        System.out.println("Expected Time taken is " + timeTaken + " min");
                        System.out.println("ETA is " + new Date(gScore.get(destinationCityKey).startTime));
                        System.out.println("Perceived risk due to weather" + WeatherParse.getNearestWeather(startTime, sourceCity.weatherData) 
                        + " is " + riskVal);
                        System.out.println("Expected Gas Mileage in miles/gallon is : " + String.format("%.2f", gScore.get(destinationCityKey).mileage * 3.78541 / 1.6));
                        System.out.println("Expected Gas Consumption is : " + String.format("%.2f",((c.distance / 1.6)/(gScore.get(destinationCityKey).mileage * 3.78541 / 1.6))) + " gallons");
                        System.out.println("Sea level diff " + (citiesDS.get(destinationCityKey).seaLevel - citiesDS.get(sourceCityKey).seaLevel) + " m");
                        System.out.println();
                    }
                    totalTimeTaken += timeTaken;
                    totalDistance += c.distance;
                    totalPerceivedRisk += riskVal;
                    expectedGasMileage += gScore.get(destinationCityKey).mileage * c.distance * 1.0f / 1000;
                    break;
                }
            }
        }
        System.out.println("");
        System.out.println("Path is : ");
        City city = citiesDS.get(cityKeys.get(0));
        String ogState = utils.standardizeString(city.state);
        System.out.print("(" + city.state + ") " + city.name);
        for (int i=1;i < cityKeys.size(); i++){

            System.out.print(" ------> ");
            city = citiesDS.get(cityKeys.get(i));
            if (utils.standardizeString(city.state).equals(ogState))
                System.out.print(city.name);
            else System.out.print("(" + city.state + ")"  + city.name);
        }
        System.out.println();
        System.out.println("Total Distance is " + totalDistance/1.6 + " miles");
        System.out.println("Total Time taken is " + totalTimeTaken + " min");
        System.out.println("Total Perceived risk is (scale "+ riskFactorScale +") " + totalPerceivedRisk + ". Max allowed risk is " + riskThreshold);

        System.out.println("Expected Gas Mileage is : " + String.format("%.2f",expectedGasMileage * 1000/ totalDistance * 3.78541 / 1.6) +  " miles /  gallon");
        System.out.println("Expected Gas Consumption is : " + String.format("%.2f",totalDistance/1.6 / (expectedGasMileage * 1000/ totalDistance * 3.78541 / 1.6)) +  " gallons");



    } 

    private static void printTraversals(ArrayList<String> cityKeyNames, long startTime, boolean prune, HashMap<String, City> cities, Map<String,ArrayList<Integer>> weatherRiskMap){
        if (prune){
            for (String cityKey: cities.keySet() ){
                if (!cityKeyNames.contains(cityKey)) cities.put(cityKey, null);
            }
        }

        System.out.println("Path includes cities :");
        for (String city: cityKeyNames){
            System.out.println(city.split("__")[1]);
        }
        long time = startTime;
        for (int i=0;i< cityKeyNames.size() - 1;i++){
            String startState = cityKeyNames.get(i).split("__")[0];
            String startCity = cityKeyNames.get(i).split("__")[1];
            String endState = cityKeyNames.get(i+1).split("__")[0];
            String endCity = cityKeyNames.get(i+1).split("__")[1];
            WrapperOutput wo = findOptimalPathDjikstra(cities, weatherRiskMap, startState, startCity, endState, endCity, time, false);
            printPathInfo(wo , cities, weatherRiskMap, false);
            System.out.println();
            System.out.println("--------");
            System.out.println();

            time = wo.gScore.get(wo.cityKeys.get(wo.cityKeys.size()-1)).startTime;
        }

    }


    public static void main(String[] args) throws IOException{

        /*
         * Parameters to change
         */
        String citiesCsvPath = "data/Cities_3.txt" ;
        String connectionCsvPath = "data/Connections_4.txt" ;
        String weatherRiskCsvPath = "data/Weather Risk Factor.txt";

        try{
            Map<String, String[]> weatherData = LDP.loadIndividualCityData(citiesCsvPath);
            Map<String, String[]> connectionData = LDP.loadCityConnectionData(connectionCsvPath);
            Map<String, ArrayList<Integer>> weatherRiskMap = loadWeatherRiskFactor(weatherRiskCsvPath);
            HashMap<String, City> citiesDS = BuildCityObjects.twoWayBuild(weatherData, connectionData);
            DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm");
            String inputCommand = "";
            Scanner myObj = null;

            while (!inputCommand.equals("quit")){
                System.out.println("Please input command (visualize / path / mst / traverse / quit)");
                myObj = new Scanner(System.in);  // Create a Scanner object
                inputCommand = utils.standardizeString(myObj.nextLine());


                if (inputCommand.equals("path")){

                        System.out.println("Please Enter source city");
                        String sourceCity = utils.standardizeString(myObj.nextLine());
                        System.out.println("Please Enter the state of "+ sourceCity);
                        String sourceState = utils.standardizeString(myObj.nextLine());
                        String sourceCityKey = utils.standardizeCityKey(sourceState, sourceCity);
                        // String sourceCityKey = StringStandardize.standardizeString(sourceState) + "__" + StringStandardize.standardizeString(sourceCity);
                        if (citiesDS.get(sourceCityKey) == null){
                            System.out.println(sourceCity + " in the state of "+ sourceState + " is not in our repertoire. Rewinding!!!" );
                            continue;
                        }
                        System.out.println("Please Enter destination city");
                        String destinationCity = utils.standardizeString(myObj.nextLine());
                        System.out.println("Please Enter the state of "+ destinationCity);
                        String destinationState = utils.standardizeString(myObj.nextLine());
                        String destinationCityKey = utils.standardizeCityKey(destinationState, destinationCity);
                        // String destinationCityKey = StringStandardize.standardizeString(destinationState) + "__" + StringStandardize.standardizeString(destinationCity);
                        if (citiesDS.get(destinationCityKey) == null){
                            System.out.println(destinationCity + " in the state "+ destinationState + " is not in our repertoire. Rewinding!!!" );
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
                        System.out.println("Please Enter path criterion : (1) shortest distance, (2) Lowest gas consumption (3) Safest (4) Time");
                        String criteria = utils.standardizeString(myObj.nextLine());
                        int criteriaInt = Integer.parseInt(criteria);
                        
                        if (criteriaInt == 1) costCalculationCriteria = 0;
                        else if (criteriaInt == 2) costCalculationCriteria = 6;
                        else if (criteriaInt == 3) costCalculationCriteria = 4;
                        else if (criteriaInt == 4) costCalculationCriteria = 1;
                        else {
                            System.out.println("Criteria not found. Rewinding!!!");
                            continue;
                        }
                        
                        System.out.println("Please enter shortest path algorithm to use : djikstra / bellman ford");
                        String type = utils.standardizeString(myObj.nextLine());
                        if (type.equals("djikstra")){
                            WrapperOutput path = findOptimalPathDjikstra( citiesDS, weatherRiskMap, sourceState, sourceCity, destinationState, destinationCity, startTime, true);
                            printPathInfo(path, citiesDS, weatherRiskMap, true);

                            System.out.println("visualize paths: (y) ?");
                            String response = utils.standardizeString(myObj.nextLine());
                            if (response.equals("y")) {
                                HashMap<String, City> copyCMap = BuildCityObjects.copyCityMap(citiesDS);
                                String[] keys = copyCMap.keySet().toArray(new String[citiesDS.size()]);
                                for (String cityKey : keys){
                                    if (!path.cityKeys.contains(cityKey)){
                                        copyCMap.remove(cityKey);
                                    }
                                }
                                SwingUtilities.invokeAndWait(() -> new MapVisualizeDetailsETA(copyCMap, weatherRiskMap, path));
                            }
                            if (costCalculationCriteria == 4){
                                int totalRiskScore = path.gScore.get(path.cityKeys.get(path.cityKeys.size() - 1)).risk;
                                for (int i =0;i< 12 ; i++){
                                    WrapperOutput wo = findOptimalPathDjikstra( citiesDS, weatherRiskMap, sourceState, sourceCity, destinationState, destinationCity, startTime + i * 60 * 60 *1000 , false);
                                    int otherRiskScore = wo.gScore.get(wo.cityKeys.get(wo.cityKeys.size() - 1)).risk;
                                    if (totalRiskScore > otherRiskScore){
                                        System.out.println("Suggesting alternate safer route START : \n\n");
                                        printPathInfo(wo, citiesDS, weatherRiskMap, true);
                                        System.out.println("Suggesting alternate safer route END : \n\n");
                                        break;
                                    }
                                }
                            }
                            System.out.println("suggest two other non-optimal alts (y)?");
                            response = utils.standardizeString(myObj.nextLine());
                            if (response.equals("y")){
                                int ogccc = costCalculationCriteria;
                                costCalculationCriteria = 7;
                                WrapperOutput wo = findOptimalPathDjikstra( citiesDS, weatherRiskMap, sourceState, sourceCity, destinationState, destinationCity, startTime, false);
                                System.out.println("#### ALT PATH 1");
                                printPathInfo( wo,
                                citiesDS, weatherRiskMap, true);
                                wo = findOptimalPathDjikstra( citiesDS, weatherRiskMap, sourceState, sourceCity, destinationState, destinationCity, startTime, false);
                                System.out.println("#### ALT PATH 2");
                                printPathInfo(wo, 
                                citiesDS, weatherRiskMap, true);
                                costCalculationCriteria = ogccc;
                            }
                        }
                        else if (type.equals("bellmanford")){
                            WrapperOutput path = findOptimalPathBellmanFord( citiesDS, weatherRiskMap, sourceState, sourceCity, destinationState, destinationCity, startTime, false);
                            printPathInfo(path, citiesDS, weatherRiskMap, true);
                            System.out.println("visualize paths: (y) ?");
                            String response = utils.standardizeString(myObj.nextLine());
                            if (response.equals("y")) {
                                HashMap<String, City> copyCMap = BuildCityObjects.copyCityMap(citiesDS);
                                String[] keys = copyCMap.keySet().toArray(new String[citiesDS.size()]);
                                for (String cityKey : keys){
                                    if (!path.cityKeys.contains(cityKey)){
                                        copyCMap.remove(cityKey);
                                    }
                                }
                                SwingUtilities.invokeAndWait(() -> new MapVisualizeDetailsETA(copyCMap, weatherRiskMap, path));
                            }
                            if (costCalculationCriteria == 4){
                                int totalRiskScore = path.gScore.get(path.cityKeys.get(path.cityKeys.size() - 1)).risk;
                                for (int i =0;i< 12 ; i++){
                                    WrapperOutput wo = findOptimalPathDjikstra( citiesDS, weatherRiskMap, sourceState, sourceCity, destinationState, destinationCity, startTime + i * 60 * 60 *1000 , false);
                                    int otherRiskScore = wo.gScore.get(wo.cityKeys.get(wo.cityKeys.size() - 1)).risk;
                                    if (totalRiskScore > otherRiskScore){
                                        System.out.println("Suggesting alternate safer route START : \n\n");
                                        printPathInfo(wo, citiesDS, weatherRiskMap, true);
                                        System.out.println("Suggesting alternate safer route END : \n\n");
                                        break;
                                    }
                                }
                            }
                            System.out.println("suggest two other non-optimal alts (y)?");
                            response = utils.standardizeString(myObj.nextLine());
                            if (response.equals("y")){
                                int ogccc = costCalculationCriteria;
                                costCalculationCriteria = 7;
                                WrapperOutput wo = findOptimalPathDjikstra( citiesDS, weatherRiskMap, sourceState, sourceCity, destinationState, destinationCity, startTime, false);
                                System.out.println("#### ALT PATH 1");
                                printPathInfo( wo,
                                citiesDS, weatherRiskMap, true);
                                wo = findOptimalPathDjikstra( citiesDS, weatherRiskMap, sourceState, sourceCity, destinationState, destinationCity, startTime, false);
                                System.out.println("#### ALT PATH 2"); 
                                printPathInfo(wo, 
                                citiesDS, weatherRiskMap, true);
                                costCalculationCriteria = ogccc;
                            }
                        }
                        else System.out.println("Invalid type specified. Rewinding!!!");

                }


                else if (inputCommand.equals("visualize")){
                    System.out.println("Full details : (y) ?");
                    String response = utils.standardizeString(myObj.nextLine());
                    if (response.equals("y")) {
                        for (HashMap.Entry<String, City> city : citiesDS.entrySet()){
                            city.getValue().printObject();
                        }
                    }
                    else BuildCityObjects.printAdjacencyList(citiesDS);
                    System.out.println();
                    System.out.println();

                }


                else if (inputCommand.equals("mst")){
                    System.out.println("Please enter MST algorithm : (kruskal / prim)");
                    String type = utils.standardizeString(myObj.nextLine());
                    if (type.equals("kruskal")){
                        HashMap<String, City> copyCitiesDS = BuildCityObjects.makeDistancesEqual(BuildCityObjects.copyCityMap(citiesDS));
                        HashMap<String, City> mst = MST.kruskalMST(copyCitiesDS);
                        BuildCityObjects.printAdjacencyList(mst);
                        System.out.println("visualize mst: (y) ?");
                        String response = utils.standardizeString(myObj.nextLine());
                        if (response.equals("y")) {
                            long startTime = new SimpleDateFormat("MM/dd/yyyy HH:mm").parse("11/08/2023 12:00").getTime();
                            SwingUtilities.invokeAndWait(() -> new MapVisualizeDetails(mst, weatherRiskMap, startTime));
                        }
                    }
                    else if (type.equals("prim")){
                        HashMap<String, City> copyCitiesDS = BuildCityObjects.makeDistancesEqual(BuildCityObjects.copyCityMap(citiesDS));
                        HashMap<String, City> mst = MST.primMST(copyCitiesDS);
                        BuildCityObjects.printAdjacencyList(mst);
                        System.out.println("visualize mst: (y) ?");
                        String response = utils.standardizeString(myObj.nextLine());
                        if (response.equals("y")) {
                            long startTime = new SimpleDateFormat("MM/dd/yyyy HH:mm").parse("11/08/2023 12:00").getTime();
                            SwingUtilities.invokeAndWait(() -> new MapVisualizeDetails(mst, weatherRiskMap, startTime));
                        }
                    }
                    else System.out.println("Invalid type specified. Rewinding!!!");
                }
                


                else if (inputCommand.equals("traverse")){
                    System.out.println("random / select");;
                    String response = utils.standardizeString(myObj.nextLine());
                    if (response.equals("random")){
                        System.out.println("Please enter number of cities to traverse");
                        int num = Integer.parseInt(myObj.nextLine());
                        System.out.println("Please Enter Start time in format : MM/dd/yyyy HH:mm"); // e.g 11/08/2023 12:00 any other date in this format is fine but we collected weather data for 8-9 Nov inclusive
                        long startTime = 0;
                        try {
                            startTime = df.parse(myObj.nextLine()).getTime();
                        }
                        catch(ParseException pe){
                            System.out.println("Date Format not adhering to specified format. E.g is 11/08/2023 23:44. Rewinding !!!");
                            continue;
                        }

                        printTraversals(selectLimitedConnectedCities(citiesDS, num), startTime, true, BuildCityObjects.copyCityMap(citiesDS), weatherRiskMap);
                    }
                    else if (response.equals("select")){
                        System.out.println("Please enter number of cities to traverse");
                        int num = Integer.parseInt(myObj.nextLine());
                        System.out.println("Please Enter Start time in format : MM/dd/yyyy HH:mm"); // e.g 11/08/2023 12:00 any other date in this format is fine but we collected weather data for 8-9 Nov inclusive
                        long startTime = 0;
                        try {
                            startTime = df.parse(myObj.nextLine()).getTime();
                        }
                        catch(ParseException pe){
                            System.out.println("Date Format not adhering to specified format. E.g is 11/08/2023 23:44. Rewinding !!!");
                            continue;
                        }
                        printTraversals(inputCityKeyNames(num, citiesDS, myObj), startTime, false, citiesDS, weatherRiskMap);
                    }
                }



                else if (!inputCommand.equals("quit")){
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
    public float mileage;
    // meta data to help with efficient implementation
    long startTime;

    CostStruct(float cost, int risk, long startTime){
        this(cost, risk, startTime, 16.061f);
    }

    CostStruct(float cost, int risk, long startTime, float gasMileage){
        this.cost = cost;
        this.risk = risk;
        this.mileage = gasMileage;
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
        return new CostStruct(this.cost + other.cost, this.risk + other.risk, other.startTime, other.mileage);
    }
}

class WrapperOutput{
    List<String> cityKeys;
    Map<String, CostStruct> gScore;

    WrapperOutput(List<String> cityKeys, Map<String, CostStruct> gScore){
        this.cityKeys = cityKeys;
        this.gScore = gScore;
    }
}