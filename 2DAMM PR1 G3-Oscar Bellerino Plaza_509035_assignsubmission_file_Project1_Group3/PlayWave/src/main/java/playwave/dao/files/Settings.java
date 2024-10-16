package playwave.dao.files;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.Properties;
import playwave.HomeController;

public class Settings implements Dao {

    private final String settingsPath;
    private final Properties settings = new Properties();

    /* Singleton pattern */
    private static Settings instance;

    static {
        instance = new Settings();
    }

    public static Settings getInstance() {
        if (instance == null) {
            instance = new Settings();
        }

        return instance;
    }

    private Settings() {
        this.settingsPath = getSettingsPath();
    }

    /**
     *
     *
     * @return
     */
    public String getSettingsPath() {
        String settingsFilePath = null;
        try {
            String jarFilePath = new File(HomeController.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getPath();
            int jarFileIndex = jarFilePath.lastIndexOf(System.getProperty("file.separator"));
            settingsFilePath = (new File(jarFilePath).canWrite() ? jarFilePath.substring(0, jarFileIndex) : System.getProperty("user.home"));
        } catch (URISyntaxException e) {
            System.out.println(e.getMessage());
        }

        return settingsFilePath;
    }

    private File getSettingsFile(String path) {
        return new File(path + System.getProperty("file.separator") + "settings.properties");
    }

    public boolean settingsFileExists() {
        return getSettingsFile(settingsPath).exists();
    }

    /**
     *
     * @param propertyName
     * @return
     */
    private boolean propertyExists(String propertyName) {
        for (DefaultSettings setting : DefaultSettings.values()) {
            if (setting.name().equals(propertyName)) {
                return true;
            }
        }
        return false;
    }

    /**
     *
     *
     */
    @Override
    public void createDefaultSettings() {
        if (settingsFileExists()) {
            getSettingsFile(settingsPath).delete();
        }

        try ( OutputStream output = new FileOutputStream(getSettingsFile(settingsPath))) {
            settings.setProperty(DefaultSettings.WINDOW_WIDTH.name(), DefaultSettings.WINDOW_WIDTH.getValue());
            settings.setProperty(DefaultSettings.WINDOW_HEIGHT.name(), DefaultSettings.WINDOW_HEIGHT.getValue());
            settings.setProperty(DefaultSettings.LEFT_DIVIDER_POSITION.name(), DefaultSettings.LEFT_DIVIDER_POSITION.getValue());
            settings.setProperty(DefaultSettings.RIGHT_DIVIDER_POSITION.name(), DefaultSettings.RIGHT_DIVIDER_POSITION.getValue());
            settings.setProperty(DefaultSettings.DEFAULT_DIRECTORY.name(), DefaultSettings.DEFAULT_DIRECTORY.getValue());

            settings.store(output, null);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     *
     * @param key
     * @param value
     */
    @Override
    public void updateSettings(String key, String value) {
        try ( InputStream input = new FileInputStream(getSettingsFile(settingsPath))) {
            settings.load(input);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        for (String property : settings.stringPropertyNames()) {
            if (propertyExists(property)) {
                try ( OutputStream output = new FileOutputStream(getSettingsFile(settingsPath))) {
                    settings.setProperty(key, value);
                    settings.store(output, null);
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
            } else {
                createDefaultSettings();
            }
        }
    }

    /**
     *
     * @param key
     * @return
     */
    @Override
    public String getPropertyValue(String key) {
        try ( InputStream input = new FileInputStream(getSettingsFile(settingsPath))) {
            settings.load(input);
            return settings.getProperty(key);
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }
}
