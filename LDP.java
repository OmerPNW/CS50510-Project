import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LDP {
    
    static Logger logger = Logger.getLogger(LDP.class.getName());
    

    final static String[] inorderCityHeaders = {"State", "Source_city", "Postal Code", "Weather", "Sea_level/m"} ;
    final static String[] inorderConnectionsHeaders = {"State", "City", "Destination", "Distance/km", "Time Taken/min"} ;

    public static Map<String, String[]> loadIndividualCityData(String csvPath) throws IOException, Exception{
        Map<String, String[]> weatherData = new HashMap<>();
        try {
            // Create a Scanner to read from the file
            Scanner scanner = new Scanner(new File(csvPath));


            String[] headers = scanner.nextLine().split(","); // no need to have header data itself
            for( int i=0; i< inorderCityHeaders.length; i++){
                if (!StringStandardize.standardizeString(headers[i]).equals(StringStandardize.standardizeString(inorderCityHeaders[i]))){
                    System.out.println("CITY CSV headers not as expected");
                    logger.severe("-----CITY CSV ISSUE----");
                    logger.severe("CSV headers not as expected . Got "+ headers[i] + " Expected :" + inorderCityHeaders[i]);
                    return null;
                }
            }

            logger.info("---- CITY CSV INFO -----");
            logger.info("City CSV Headers are correct");

            // Read and process each row
            int rowNum = 0;
            while (scanner.hasNextLine()) {
                String row = scanner.nextLine();
                long count_quotes = row.chars().filter(ch -> ch == '"').count();
                while (count_quotes % 2 != 0){
                    row += "\n" + scanner.nextLine() ; 
                    count_quotes = row.chars().filter(ch -> ch == '"').count();
                }


                String[] weatherFields = new String[inorderCityHeaders.length];
                for (int i =0 ; i<weatherFields.length;i++){
                    weatherFields[i] = "";
                }
                int index = 0;
                boolean open_quotes = false;
                for (char c : row.toCharArray()){
                    // System.out.print(c);
                    if (c == '"') {
                        open_quotes = !open_quotes;
                        continue;
                    }
                    if (open_quotes) weatherFields[index] += c;
                    else if (c != ',')
                        weatherFields[index] += c;
                    else index += 1;
                    if (index > inorderCityHeaders.length - 1) break;
                }

                String key = StringStandardize.standardizeString(weatherFields[1]); // City
                if (key.equals("")){
                    logger.warning("Empty .Found row with no city name in row "+ String.valueOf(rowNum + 1) + ". Skipping row");
                    continue ; // skip empty row
                }
                if (!isNumber(weatherFields[2]) || !isNumber(weatherFields[4])){

                    logger.severe("Encountered non numeric data in a numeric column in City Data at row " + String.valueOf(rowNum + 1));



                    return null;
                }
                weatherData.put(key, weatherFields);
            }
            
            scanner.close();

            if (weatherData.size() == 0){
                logger.severe("-----CITY CSV ISSUE----");
                logger.severe("No data in CSV");
                logger.severe("-----END------");
                return null;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.info("CSV Reading Completed");
        logger.info("----END----\n");
        return weatherData;
    }

    public static Map<String, ArrayList<String>> loadContributions(String csvPath) throws IOException{

        try {

            Scanner scanner = new Scanner(new File(csvPath));
            String line = scanner.nextLine();
            String[] headers = line.split(",");
            HashMap<String, ArrayList<String>> studentToCity = new HashMap<>();
            final String[] expectedHeaders = {"City","Student ID"};

            for( int i=0; i< headers.length; i++){
                if (!StringStandardize.standardizeString(headers[i]).equals(StringStandardize.standardizeString(expectedHeaders[i]))){
                    System.out.println("CSV headers not as expected: Found " + headers[i] + "   Expected : " + expectedHeaders[i]);
                    logger.severe("-----Contributibutions CSV ISSUE----");
                    logger.severe("CSV headers not as expected . Got "+ headers[i] + " Expected :" + inorderConnectionsHeaders[i]); 
                    return null;
                }
            }

            int rowNum = 0;

            while( scanner.hasNextLine()){
                rowNum += 1;
                String[] fields = scanner.nextLine().split(",");
                if (fields.length < 2){
                    logger.warning("Empty .Found row with missing data . Row num "+ String.valueOf(rowNum + 1) +". Skipping");
                    continue ; // skip empty row
                }
                String key = StringStandardize.standardizeString(fields[0]);
                if (key.equals("")){
                    logger.warning("Empty . Found row with no city name in row "+ String.valueOf(rowNum + 1) + ". Skipping");
                    continue ; // skip empty row
                }
                final String stndName = StringStandardize.standardizeString(fields[1]);
                if (studentToCity.get(stndName)==null){
                    studentToCity.put(stndName, new ArrayList<>());
                }
                ArrayList<String> newCityList = studentToCity.get(stndName);
                newCityList.add(StringStandardize.standardizeString(fields[0]));
                studentToCity.put(stndName, newCityList);
                // back connection

                // studentToCity.put(StringStandardize.standardizeString(fields[0]),  new ArrayList<String>() {{add(stndName);}});
            }
            return studentToCity;

        }
        catch (Exception e){
            e.printStackTrace();
        }
        return null;

            

    }


    public static Map<String, String> loadInverseContributions(String csvPath) throws IOException{

        try {

            Scanner scanner = new Scanner(new File(csvPath));
            String line = scanner.nextLine();
            String[] headers = line.split(",");
            HashMap<String, String> cityToStudent = new HashMap<>();
            final String[] expectedHeaders = {"City","Student ID"};

            for( int i=0; i< headers.length; i++){
                if (!StringStandardize.standardizeString(headers[i]).equals(StringStandardize.standardizeString(expectedHeaders[i]))){
                    System.out.println("CSV headers not as expected: Found " + headers[i] + "   Expected : " + expectedHeaders[i]);
                    logger.severe("-----Contributibutions CSV ISSUE----");
                    logger.severe("CSV headers not as expected . Got "+ headers[i] + " Expected :" + inorderConnectionsHeaders[i]); 
                    return null;
                }
            }

            int rowNum = 0;

            while( scanner.hasNextLine()){
                rowNum += 1;
                String[] fields = scanner.nextLine().split(",");
                if (fields.length < 2){
                    logger.warning("Empty .Found row with missing data . Row num "+ String.valueOf(rowNum + 1) + ". Skipping");
                    continue ; // skip empty row
                }
                String key = StringStandardize.standardizeString(fields[0]);
                if (key.equals("")){
                    logger.warning("Empty .Found row with no city name in row "+ String.valueOf(rowNum + 1) + ". Skipping");
                    continue ; // skip empty row
                }
                final String cityName = StringStandardize.standardizeString(fields[0]);
                cityToStudent.put(cityName, key);
            }
            return cityToStudent;

        }
        catch (Exception e){
            e.printStackTrace();
        }
        return null;

            

    }

    public static Map<String, String[]> loadCityConnectionData(String csvPath) throws IOException, Exception{
        Map<String, String[]> connectionData = new HashMap<>();
        try {

            Scanner scanner = new Scanner(new File(csvPath));
            String line = scanner.nextLine();
            String[] headers = line.split(",");


            for( int i=0; i< inorderConnectionsHeaders.length; i++){
                if (!StringStandardize.standardizeString(headers[i]).equals(StringStandardize.standardizeString(inorderConnectionsHeaders[i]))){
                    System.out.println("CSV headers not as expected: Found " + headers[i] + "   Expected : " + inorderConnectionsHeaders[i]);
                    logger.severe("-----CONNECTIONS CSV ISSUE----");
                    logger.severe("CSV headers not as expected . Got "+ headers[i] + " Expected :" + inorderConnectionsHeaders[i]); 
                    return null;
                }
            }

            logger.info("---- CONNECTIONS CSV INFO -----");
            logger.info("CONNECTIONS CSV Headers are correct");
            // Parse weather CSV and store in a map
            int rowNum = 0;
            while( scanner.hasNextLine()){
                rowNum += 1;
                String[] connectionFields = scanner.nextLine().split(",");
                if (connectionFields.length < 2){
                    logger.warning("Empty .Found row with missing data . Row num "+ String.valueOf(rowNum + 1) + ". Skipping");
                    continue ; // skip empty row
                }
                String key = StringStandardize.standardizeString(connectionFields[1]) + "," + StringStandardize.standardizeString(connectionFields[2]); // City + Destination City
                if (key.equals(",")){
                    logger.warning("Empty .Found row with no city name in row "+ String.valueOf(rowNum + 1) + ". Skipping");
                    continue ; // skip empty row
                }
                if (!isNumber(connectionFields[3]) || !isNumber(connectionFields[4])){
                    logger.severe("Encountered non numeric data in a numeric column in City Data at row " + String.valueOf(rowNum + 1));
                    return null;
                }
                connectionData.put(key, connectionFields);
            }

            scanner.close();

            if (connectionData.size() == 0){
                logger.severe("-----CONNECTIONS CSV ISSUE----");
                logger.severe("No data in CSV");
                logger.severe("-----END------");
                return null;
            }
            logger.info("CSV Reading Completed");
            logger.info("----END----\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return connectionData;
    }

    public static void verifyContributors(String contributorsName ,Map<String, String[]> weatherData, Map<String, ArrayList<String>> studentMap, 
     Map<String, String> cityToStudentMap) throws Exception{

        if (contributorsName.equals("ALL")){
            logger.info("------ Data contribution check ------");
            ArrayList<String> contributedCitiesAll = new ArrayList<>();
            for (Map.Entry<String, ArrayList<String>> mp : studentMap.entrySet()){
                contributedCitiesAll.addAll(mp.getValue());
            }
            ArrayList<String> submittedCitiesAll = new ArrayList<>();
            for (Map.Entry<String, String[]> mp : weatherData.entrySet()){
                submittedCitiesAll.add(mp.getKey());
            }
            boolean missing = false;
            for (String s: contributedCitiesAll){
                if (!submittedCitiesAll.contains(s)){
                    logger.info("Missing city : " + s + " from "+ cityToStudentMap.get(s));
                    missing = true;
                }
            }
            if (!missing){
                logger.info("DATA VERIFIED!!!! Everyone has chipped in and completed their part");
            }
            logger.info("------ END ------");
            // Collections.sort(contributedCitiesAll);
            // Collections.sort(submittedCitiesAll);
            // if (submittedCitiesAll.equals(contributedCitiesAll)){
            //     logger.info("----- Student Contributions");
            // }
        }
        else{
            logger.info("------ Data contribution check ------");
            ArrayList<String> contributedCitiesExpected = studentMap.get(StringStandardize.standardizeString(contributorsName));
            if (contributedCitiesExpected == null){
                logger.severe(contributorsName + " name is not found among the contributors. Please recheck your input");
                throw new Exception(" name is not found among the contributors. Please recheck your input");
            }
            ArrayList<String> submittedCities = new ArrayList<>();
            for (Map.Entry<String, String[]> mp : weatherData.entrySet()){
                submittedCities.add(mp.getKey());
            }

            boolean missing = false;
            for (String s: contributedCitiesExpected){
                if (!submittedCities.contains(s)){
                    logger.info("Missing city : " + s + " from student : "+ contributorsName);
                    missing = true;
                }
            }
            if (!missing){
                logger.info("DATA VERIFIED!!!! All the data from your side is present!");
            }
            logger.info("------ END ------");
        }



    }


    public static void saveInnerJoinCsv(String outputCsvPath, Map<String, String[]> weatherData, Map<String, String[]> connectionData){
                // inner join and create csv for display purposes
        try {
            logger.info("-------INNER JOIN CSV CREATION--------");

            File outputCsvFile = new File(outputCsvPath);
            FileWriter fileWriter = new FileWriter(outputCsvFile);
            String[] header = new String[] { "State", "City", "Postal Code", "Weather", "Sea level height", 
            "Destination City", "Distance", "Time taken" };
            fileWriter.write(String.join(",", header));

            // Perform inner join and write to output CSV
            for (Map.Entry<String, String[]> connectionEntry : connectionData.entrySet()) {
                String[] keySplits = connectionEntry.getKey().split(",");
                String key = StringStandardize.standardizeString(keySplits[0]);
                // System.out.println(key);
                if (weatherData.containsKey(key)) {
                    String[] connectionFields = connectionEntry.getValue();
                    String[] weatherFields = weatherData.get(key);
                    String[] outputLine = new String[] {
                        connectionFields[0], connectionFields[1], weatherFields[2], weatherFields[3], weatherFields[4],
                        connectionFields[2], connectionFields[3], connectionFields[4]
                    };
                    fileWriter.write(String.join(",",outputLine));
                }

            }
            logger.info("File is at :" + outputCsvPath);
            logger.info("-------END-------\n");
            fileWriter.close();

        } catch (IOException e) {
            logger.severe("-----INNER JOIN FILE CREATION ERROR ----");
            logger.severe(e.getStackTrace().toString());
            logger.severe("------END-------- \n");
        }
    }


    public static boolean isNumber(String s) {

        try { 

            Float.parseFloat(s); 
        } catch(NumberFormatException e) { 

            return false; 
        } catch(NullPointerException e) {

            return false;
        }

        return true;
    }






    public static void main(String[] args) throws IOException{



        /*
         * Data files, input * output
         */
        String citiesCsvPath = "Cities.txt" ;
        String connectionCsvPath = "Connections.txt" ;
        String contributionsCsvPath = "contributions.txt" ;

        // output inner join csv. Only vaild if createInnerJoin is true
        String outputCsvPath = "output.txt";

        
        /*
         Parameters to set/ Especially for the grader
         */


        /* "ALL" means the datasets must have all the cities mentioned. Put the FULL NAME of the 
        person from the list contributors.csv to see whether individual has provided all the data.
        e.g "Muhammad Omer Raza" lower cases and upper case and spaces dont matter here */
        final String contributorsName = "ALL"; 
        final boolean showDetailedLogs = true;
        final boolean createInnerJoin = true;
        // recommend to turn it off so that error logs either in file or console can be easily found. Check info.log for detailed logs
        final boolean getScreenLogs = true; 









        /*
         * Setting up loggers
         */
        FileHandler fileHandlerInfo = new FileHandler("all_info.txt");
        FileHandler fileHandleError = new FileHandler("all_error.txt");
        // Set the log level (e.g., INFO)
        fileHandlerInfo.setLevel(Level.INFO);
        fileHandleError.setLevel(Level.SEVERE);
        // Add the FileHandler to the logger
        logger.addHandler(fileHandlerInfo);
        logger.addHandler(fileHandleError);
        logger.setUseParentHandlers(getScreenLogs);
        CustomRecordFormatter formatter = new CustomRecordFormatter();  
        // If issue with using formatter from then uncomment this line and comment the above
        // SimpleFormatter formatter = new SimpleFormatter();
        fileHandlerInfo.setFormatter(formatter);
        fileHandleError.setFormatter(formatter);




        try{
            Map<String, ArrayList<String>> studentMap = loadContributions(contributionsCsvPath);
            Map<String, String> cityToStudentMap = loadInverseContributions(contributionsCsvPath);
            Map<String, String[]> weatherData = loadIndividualCityData(citiesCsvPath);
            verifyContributors(contributorsName, weatherData, studentMap, cityToStudentMap);
            Map<String, String[]> connectionData = loadCityConnectionData(connectionCsvPath);
            if (createInnerJoin) saveInnerJoinCsv(outputCsvPath, weatherData, connectionData);
            HashMap<String, City> citiesDS = BuildCityObjects.twoWayBuild(weatherData, connectionData);
            if (showDetailedLogs){
                logger.info("-------- DESCRIBE ALL CITIES & CONNECTIONS -----");
                for (Map.Entry<String, City> cityDS: citiesDS.entrySet()){
                    City c = cityDS.getValue();
                    c.printObject(logger);
                }
                logger.info("-------END--------------");
            }

            BuildCityObjects.checkConnectivity(citiesDS, logger);
        }


        catch(Exception e){
            e.printStackTrace();
        }

    }
}
