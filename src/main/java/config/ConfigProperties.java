package config;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class ConfigProperties {

    public static String getProperty(String propertyName) {
        Properties properties = new Properties();
        try {
            FileReader reader = new FileReader("src/main/resources/application.properties");
            properties.load(reader);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties.getProperty(propertyName);
    }
}
