import java.text.Normalizer;

public class StringStandardize {

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

    public static void main(String[] args) {
        String inputString = "Héllõ Wörld! This_is-a_Test-String.";

        String standardizedString = standardizeString(inputString);

        System.out.println("Original String: " + inputString);
        System.out.println("Standardized String: " + standardizedString);
    }
}
