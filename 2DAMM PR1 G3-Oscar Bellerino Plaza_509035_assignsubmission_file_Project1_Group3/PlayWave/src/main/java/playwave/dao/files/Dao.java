package playwave.dao.files;

/**
 * 
 * @author Oscar
 */
public interface Dao {
    void createDefaultSettings();
    void updateSettings(String key, String value);
    String getPropertyValue(String key);
}
