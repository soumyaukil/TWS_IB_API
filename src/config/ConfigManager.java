package config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class ConfigManager {

    private static ConfigManager instance;

    private Properties prop;

    private ConfigManager() {
        prop = new Properties();
    }

    /// Get singleton instance
    public static ConfigManager getInstance() {
        if(instance == null) {
            instance = new ConfigManager();
        }
        return instance;
    }

    public void load() throws FileNotFoundException, IOException {
        prop.load(new FileInputStream("/home/ubuntu/TWS_IB/src/bin/config.properties"));
    }

    public String getAsString(String key) {
        return prop.getProperty(key);
    }

    public int getAsInt(String key) {
        return Integer.parseInt(prop.getProperty(key));
    }

    public double getAsDouble(String key) {
        return Double.parseDouble(prop.getProperty(key));
    }
}
