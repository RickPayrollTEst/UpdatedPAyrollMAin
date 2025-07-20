package util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Enhanced Configuration Manager for MotorPH Payroll System
 * Handles all application configuration settings
 * 
 * @author Student Name
 * @version 2.0
 */
public class ConfigurationManager {
    private static final Logger LOGGER = Logger.getLogger(ConfigurationManager.class.getName());
    private static ConfigurationManager instance;
    private Properties properties;
    
    private static final String CONFIG_FILE = "application.properties";
    
    private ConfigurationManager() {
        loadConfiguration();
    }
    
    public static synchronized ConfigurationManager getInstance() {
        if (instance == null) {
            instance = new ConfigurationManager();
        }
        return instance;
    }
    
    private void loadConfiguration() {
        properties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (input != null) {
                properties.load(input);
                LOGGER.info("✅ Configuration loaded successfully from " + CONFIG_FILE);
            } else {
                LOGGER.warning("⚠️ Configuration file not found, using defaults");
                loadDefaultConfiguration();
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "❌ Error loading configuration", e);
            loadDefaultConfiguration();
        }
    }
    
    private void loadDefaultConfiguration() {
        // Database defaults
        properties.setProperty("db.host", "localhost");
        properties.setProperty("db.port", "3306");
        properties.setProperty("db.name", "aoopdatabase_payroll");
        properties.setProperty("db.username", "root");
        properties.setProperty("db.password", "admin");
        properties.setProperty("db.driver", "com.mysql.cj.jdbc.Driver");
        
        // Application defaults
        properties.setProperty("app.name", "MotorPH Payroll System");
        properties.setProperty("app.version", "2.0");
        properties.setProperty("logging.level", "INFO");
        
        LOGGER.info("🔧 Default configuration loaded");
    }
    
    // Database Configuration Methods
    public String getDatabaseHost() {
        return getProperty("db.host", "localhost");
    }
    
    public int getDatabasePort() {
        return getIntProperty("db.port", 3306);
    }
    
    public String getDatabaseName() {
        return getProperty("db.name", "aoopdatabase_payroll");
    }
    
    public String getDatabaseUsername() {
        return getProperty("db.username", "root");
    }
    
    public String getDatabasePassword() {
        return getProperty("db.password", "admin");
    }
    
    public String getDatabaseDriver() {
        return getProperty("db.driver", "com.mysql.cj.jdbc.Driver");
    }
    
    public String getDatabaseUrl() {
        return String.format("jdbc:mysql://%s:%d/%s?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true",
                getDatabaseHost(), getDatabasePort(), getDatabaseName());
    }
    
    // JasperReports Configuration
    public String getJasperReportsPath() {
        return getProperty("jasper.reports.path", "src/reports/templates/");
    }
    
    public String getJasperOutputPath() {
        return getProperty("jasper.output.path", "reports/output/");
    }
    
    public String getJasperCompilePath() {
        return getProperty("jasper.compile.path", "src/reports/compiled/");
    }
    
    // Business Rules Configuration
    public int getWorkingDaysPerMonth() {
        return getIntProperty("payroll.working.days.per.month", 22);
    }
    
    public int getWorkingHoursPerDay() {
        return getIntProperty("payroll.working.hours.per.day", 8);
    }
    
    public double getOvertimeMultiplier() {
        return getDoubleProperty("payroll.overtime.multiplier", 1.25);
    }
    
    public int getLateGracePeriodMinutes() {
        return getIntProperty("payroll.late.grace.period.minutes", 15);
    }
    
    public int getMaxLeaveDaysPerYear() {
        return getIntProperty("payroll.max.leave.days.per.year", 15);
    }
    
    // Security Configuration
    public int getPasswordMinLength() {
        return getIntProperty("security.password.min.length", 8);
    }
    
    public int getSessionTimeoutMinutes() {
        return getIntProperty("security.session.timeout.minutes", 60);
    }
    
    public int getMaxLoginAttempts() {
        return getIntProperty("security.max.login.attempts", 3);
    }
    
    // Application Configuration
    public String getApplicationName() {
        return getProperty("app.name", "MotorPH Payroll System");
    }
    
    public String getApplicationVersion() {
        return getProperty("app.version", "2.0");
    }
    
    public String getApplicationAuthor() {
        return getProperty("app.author", "Student Name");
    }
    
    public boolean isDebugMode() {
        return getBooleanProperty("app.debug", false);
    }
    
    // Logging Configuration
    public String getLoggingLevel() {
        return getProperty("logging.level", "INFO");
    }
    
    public String getLoggingFilePath() {
        return getProperty("logging.file.path", "logs/motorph.log");
    }
    
    // Helper methods
    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
    
    public int getIntProperty(String key, int defaultValue) {
        try {
            return Integer.parseInt(properties.getProperty(key, String.valueOf(defaultValue)));
        } catch (NumberFormatException e) {
            LOGGER.warning("Invalid integer property: " + key + ", using default: " + defaultValue);
            return defaultValue;
        }
    }
    
    public double getDoubleProperty(String key, double defaultValue) {
        try {
            return Double.parseDouble(properties.getProperty(key, String.valueOf(defaultValue)));
        } catch (NumberFormatException e) {
            LOGGER.warning("Invalid double property: " + key + ", using default: " + defaultValue);
            return defaultValue;
        }
    }
    
    public boolean getBooleanProperty(String key, boolean defaultValue) {
        return Boolean.parseBoolean(properties.getProperty(key, String.valueOf(defaultValue)));
    }
    
    // Reload configuration
    public void reloadConfiguration() {
        LOGGER.info("🔄 Reloading configuration...");
        loadConfiguration();
    }
    
    // Get all properties for debugging
    public Properties getAllProperties() {
        return new Properties(properties);
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("📋 MotorPH Configuration Summary:\n");
        sb.append("================================\n");
        sb.append("Database: ").append(getDatabaseUrl()).append("\n");
        sb.append("Application: ").append(getApplicationName()).append(" v").append(getApplicationVersion()).append("\n");
        sb.append("Debug Mode: ").append(isDebugMode()).append("\n");
        sb.append("Working Days/Month: ").append(getWorkingDaysPerMonth()).append("\n");
        sb.append("Overtime Multiplier: ").append(getOvertimeMultiplier()).append("\n");
        sb.append("================================");
        return sb.toString();
    }
}