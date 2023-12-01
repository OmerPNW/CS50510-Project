package src;
import java.text.Normalizer;

public class utils {

    public static String standardizeString(String input) {
        // Remove accents
        if (input == null) return "";
        String normalizedString = Normalizer.normalize(input, Normalizer.Form.NFD);
        normalizedString = normalizedString.replaceAll("\\p{M}", "");

        // Convert to lowercase
        normalizedString = normalizedString.toLowerCase();

        // Remove punctuations, symbols, spaces, underscores, and hyphens
        normalizedString = normalizedString.replaceAll("[^a-zA-Z0-9]", "");

        return normalizedString;
    }

    public static String standardizeCityKey(String state, String city){
        return standardizeString(state) + "__" + standardizeString(city);
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
}
