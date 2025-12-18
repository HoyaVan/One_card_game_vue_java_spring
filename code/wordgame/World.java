package wordgame;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

public class World {
    final Map<String, Country> countries;

    public World() {
        this.countries = new HashMap<>();
    }

    public void addCountry(final Country country)
    {
        countries.put(country.getName(), country); // Key is the country name
    }

    public static World loadWorldMap(final String directoryPath) {
        File directory = new File(directoryPath);
        World world = new World();

        if (directory.exists() && directory.isDirectory()) {
            File[] fileList = directory.listFiles();

            if (fileList != null && fileList.length > 0) {

                for (File file : fileList) {
                    // Extracted method to parse a file and add countries
                    parseAndAddCountriesFromFile(file, world);
                }

            } else {
                System.out.println("No files in the directory.");
            }
        } else {
            System.out.println("Can't find the directory.");
        }
        return world;
    }

    private static void parseAndAddCountriesFromFile(final File file, final World world) {
        try (BufferedReader reader = Files.newBufferedReader(file.toPath())) {
            String line;
            String countryName = null;
            String capitalCityName = null;
            List<String> facts = new ArrayList<>();

            while ((line = reader.readLine()) != null) {
                // If line contains a colon, it indicates the start of a new country
                if (line.contains(":")) {
                    // If we have already parsed a country, create and add it to the list
                    if (countryName != null) {
                        world.addCountry(new Country(countryName, capitalCityName, facts.toArray(new String[0])));
                        facts.clear(); // Reset facts for the next country
                    }

                    // Split the line to get the country name and capital city name
                    String[] parts = line.split(":", 2);
                    countryName = parts[0].trim();
                    capitalCityName = parts[1].trim();
                } else if (!line.isBlank()) {
                    // Add facts (non-blank lines) to the list
                    facts.add(line.trim());
                }
            }

            // Add the last country after exiting the loop
            if (countryName != null) {
                world.addCountry(new Country(countryName, capitalCityName, facts.toArray(new String[0])));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("World:\n");

        // Iterate through the map entries and append country details
        countries.forEach((name, country) -> {
            sb.append("Country Name: ").append(name).append("\n");
            sb.append(country.toString()).append("\n\n");
        });

        return sb.toString();
    }

    // Retrieve all countries
    public Map<String, Country> getCountries()
    {
        return new HashMap<>(countries); // Return a copy to preserve encapsulation
    }
}
