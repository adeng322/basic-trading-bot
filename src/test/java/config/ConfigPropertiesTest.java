package config;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ConfigPropertiesTest {

    private final String acceptLanguage = "accept-language";
    private final String contentType = "content-type";
    private final String noSuchPropertyExists = "property-doesnt-exist";

    @Test
    public void testGetProperty() {
        assertEquals(ConfigProperties.getProperty(acceptLanguage), "nl-NL,en;q=0.8");
        assertEquals(ConfigProperties.getProperty(contentType), "application/json");
        assertNull(ConfigProperties.getProperty(noSuchPropertyExists));
    }
}
