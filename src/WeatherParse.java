package src;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


public class WeatherParse {
    public static void main(String[] args) {
        // example data
        String weatherData = "10 November 12 am to 11 November 12 am : Cloudy, Light rain\n" +
                "11 November 12 am to 13 November 12 am : Cloudy\n" +
                "13 November 12 am to 16 November 12 am : Cloudy, Light rain";

        TreeMap<Long, List<String>> weatherDataMap = parseWeatherData(weatherData);

        System.out.println(getNearestWeather(27324000000l, weatherDataMap));

        for (Map.Entry<Long, List<String>> entry : weatherDataMap.entrySet()) {
            long timestamp = entry.getKey();
            List<String> conditions = entry.getValue();
            System.out.println("Timestamp: " + timestamp);
            System.out.println("Weather Conditions: " + conditions);
        }
    }

    public static TreeMap<Long, List<String>> parseWeatherData (String weatherData) {

        TreeMap<Long, List<String>> weatherDataMap = new TreeMap<>();
        
        String[] lines = weatherData.split("\n");
        
        for (String line : lines) {
            String[] parts = line.split(":");
            if (parts.length != 2) {
                
                continue; // Skip invalid lines
            }

            String timeRange = parts[0];
            String conditions = parts[1];
            String[] timeTokens = timeRange.split("to");
            
            if (timeTokens.length != 2) {
                continue; 
            }

            String startTime = timeTokens[0];
            
            try {
                Date startDate = parseWeatherDateTime(startTime);

                long startEpoch = startDate.getTime();
                weatherDataMap.computeIfAbsent(startEpoch, arrayList -> new ArrayList<>()).addAll(parseConditions(conditions));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        return weatherDataMap;
    }

    
    // Function to get the weather conditions nearest in time to the specified timestamp
    public static List<String> getNearestWeather(long timestamp, TreeMap<Long, List<String>> weatherMap) {
        if (weatherMap.isEmpty()) {
            return null;
        }

        // If the specified timestamp is less than the first timestamp in the TreeMap, return the conditions for the first timestamp
        if (timestamp < weatherMap.firstKey()) {
            return weatherMap.firstEntry().getValue();
        }

        // Get the entry with a timestamp less than or equal to the specified timestamp
        Long nearestTimestamp = weatherMap.floorKey(timestamp);

        // If no entry with a timestamp less than or equal to the specified timestamp is found, return null
        if (nearestTimestamp == null) {
            return null;
        }

        // Return the weather conditions for the nearest timestamp
        return weatherMap.get(nearestTimestamp);
    }

    public static List<String> parseConditions(String conditions) {
        String[] conditionTokens = conditions.split(",");
        List<String> conditionTokensList = List.of(conditionTokens);
        conditionTokensList.forEach( condition -> condition.trim());
        return conditionTokensList;
    }

    public static Date parseWeatherDateTime(String weatherDateTime) throws ParseException{
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM hh a");
        Date date = dateFormat.parse(weatherDateTime);
        date.setYear(Year.now().getValue() - 1900);
        return date;
    }

        public static Date parseWeatherDateTimeNew(String weatherDateTime) throws ParseException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM hh a");
        LocalDateTime localDateTime = LocalDateTime.parse(weatherDateTime, formatter);
        
        localDateTime = localDateTime.withYear(java.time.Year.now().getValue());

        Date date = java.util.Date.from(localDateTime.atZone(java.time.ZoneId.systemDefault()).toInstant());

        return date;
    }
}
