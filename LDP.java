import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LDP {

    final static String[] inorderCityHeaders = {"State", "Source_city", "Postal Code", "Weather", "Sea_level/m"} ;
    final static String[] inorderConnectionsHeaders = {"State", "City", "Destination", "Distance/km", "Time Taken/min"} ;

    public static Map<String, String[]> loadIndividualCityData(String csvPath) throws IOException, Exception{
        Map<String, String[]> weatherData = new HashMap<>();
        try (CSVReader weatherReader = new CSVReader(new FileReader(csvPath))){
            String[] headers = weatherReader.readNext(); // no need to have header data itself
            for( int i=0; i< inorderCityHeaders.length; i++){
                if (!StringStandardize.standardizeString(headers[i]).equals(StringStandardize.standardizeString(inorderCityHeaders[i]))){
                    System.out.println("CSV headers not as expected");
                    throw new Exception("CSV headers not as expected");
                }
            }            // Parse weather CSV and store in a map
            List<String[]> weatherRecords = weatherReader.readAll();
            if (weatherRecords == null || weatherRecords.size() == 0) {
                throw new Exception("Weather data not found");
            }
            for (String[] weatherFields : weatherRecords) {
                String key = StringStandardize.standardizeString(weatherFields[1]); // City
                if (key.equals("")){
                    // System.out.println("skipping");
                    continue ; // skip empty row
                }
                if (!isNumber(weatherFields[2]) || !isNumber(weatherFields[4])){
                    throw new Exception("Encountered non numeric data in a numeric column in City Data");
                }
                weatherData.put(key, weatherFields);
            }
            weatherReader.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return weatherData;
    }

    public static Map<String, String[]> loadCityConnectionData(String csvPath) throws IOException, Exception{
        Map<String, String[]> connectionData = new HashMap<>();
        try (CSVReader connectionReader = new CSVReader(new FileReader(csvPath))){
            String[] headers = connectionReader.readNext(); // no need to have header data itself
            for( int i=0; i< inorderConnectionsHeaders.length; i++){
                if (!StringStandardize.standardizeString(headers[i]).equals(StringStandardize.standardizeString(inorderConnectionsHeaders[i]))){
                    System.out.println("CSV headers not as expected: Found " + headers[i] + "   Expected : " + inorderConnectionsHeaders[i]);
                    throw new Exception("CSV headers not as expected");
                }
            }
            // Parse weather CSV and store in a map
            List<String[]> connectionRecords = connectionReader.readAll();
            for (String[] connectionFields : connectionRecords) {
                String key = StringStandardize.standardizeString(connectionFields[1]) + "," + StringStandardize.standardizeString(connectionFields[2]); // City + Destination City
                if (key.equals(",")){
                    // System.out.println("skipping");
                    continue ; // skip empty row
                }
                if (!isNumber(connectionFields[3]) || !isNumber(connectionFields[4])){
                    System.out.println(key);
                    throw new Exception("Encountered non numeric data in a numeric column in Connections Data");
                }
                connectionData.put(key, connectionFields);
            }
            connectionReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return connectionData;
    }



    public static void saveInnerJoinCsv(String outputCsvPath, Map<String, String[]> weatherData, Map<String, String[]> connectionData){
                // inner join and create csv for display purposes
        try (CSVWriter outputCSVWriter = new CSVWriter(new FileWriter(outputCsvPath))) {
            String[] header = new String[] { "State", "City", "Postal Code", "Weather", "Sea level height", 
            "Destination City", "Distance", "Time taken" };
            outputCSVWriter.writeNext(header);

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
                    outputCSVWriter.writeNext(outputLine);
                }

            }

        } catch (IOException e) {
            e.printStackTrace();
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
        // only got here if we didn't return false
        return true;
    }

    public static void main(String[] args){
        String citiesCsvPath = "HW6_weather.csv";
        String connectionCsvPath = "HW6_Data_City_Connections.csv";

        String outputCsvPath = "output.csv";
        try{
            Map<String, String[]> weatherData = loadIndividualCityData(citiesCsvPath);
            Map<String, String[]> connectionData = loadCityConnectionData(connectionCsvPath);

            // saveInnerJoinCsv(outputCsvPath, weatherData, connectionData);
        }
        catch(Exception e){
            e.printStackTrace();
        }

    }
}
