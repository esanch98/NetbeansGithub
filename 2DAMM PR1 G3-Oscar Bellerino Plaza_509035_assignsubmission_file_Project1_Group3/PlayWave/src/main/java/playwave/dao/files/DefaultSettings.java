package playwave.dao.files;

import java.nio.file.Paths;

public enum DefaultSettings {
    WINDOW_WIDTH("640"),
    WINDOW_HEIGHT("400"),
    LEFT_DIVIDER_POSITION("0.14"),
    RIGHT_DIVIDER_POSITION("0.86"),
    DEFAULT_DIRECTORY(Paths.get(System.getProperty("user.home")).toString());
    
    private final String value;
    
    private DefaultSettings(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
}
