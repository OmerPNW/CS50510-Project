import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LDP {
    public static Map<String, String[]> loadIndividualCityData(String csvPath) throws IOException{
        Map<String, String[]> weatherData = new HashMap<>();
        try (CSVReader weatherReader = new CSVReader(new FileReader(csvPath))){
            weatherReader.readNext();
            // Parse weather CSV and store in a map
            List<String[]> weatherRecords = weatherReader.readAll();
            for (String[] weatherFields : weatherRecords) {
                String key = weatherFields[1]; // City
                weatherData.put(key, weatherFields);
            }
            weatherReader.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return weatherData;
    }

    public static Map<String, String[]> loadCityConnectionData(String csvPath) throws IOException{
        Map<String, String[]> connectionData = new HashMap<>();
        try (CSVReader connectionReader = new CSVReader(new FileReader(csvPath))){
            connectionReader.readNext(); // no need to have header data itself

            // Parse weather CSV and store in a map
            List<String[]> connectionRecords = connectionReader.readAll();
            for (String[] connectionFields : connectionRecords) {
                String key = connectionFields[1] + "," + connectionFields[2]; // City + Destination City
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
                String key = keySplits[0];
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
    public static void main(String[] args){
        String weatherCsvPath = "weather.csv";
        String connectionCsvPath = "travel.csv";
        String outputCsvPath = "output.csv";
        try{
            Map<String, String[]> weatherData = loadIndividualCityData(weatherCsvPath);
            Map<String, String[]> connectionData = loadCityConnectionData(connectionCsvPath);

            saveInnerJoinCsv(outputCsvPath, weatherData, connectionData);
        }
        catch(Exception e){
            e.printStackTrace();
        }

    }
}
