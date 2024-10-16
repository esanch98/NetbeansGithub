package playwave.logic;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javafx.fxml.FXMLLoader;
import playwave.App;

public class FXMLManager {

    /* Singleton pattern */
    private static FXMLManager instance;
    private final Map<Class<?>, FXMLLoader> fxmlLoaders = new HashMap<>();

    private FXMLManager() {
    }

    public static FXMLManager getInstance() {
        if (instance == null) {
            instance = new FXMLManager();
        }
        return instance;
    }

    /**
     *
     * @param <T>
     * @param controllerType
     * @return
     * @throws IOException
     */
    public <T> T getController(Class<T> controllerType) throws IOException {
        FXMLLoader fxmlLoader = fxmlLoaders.get(controllerType);
        if (fxmlLoader != null) {
            return fxmlLoader.getController();
        } else {
            throw new IOException("No controller found");
        }
    }

    /**
     *
     * @param <T>
     * @param controllerType
     * @param fxml
     * @return
     * @throws IOException
     */
    public <T> FXMLLoader loadFXML(Class<T> controllerType, String fxml) throws IOException {
        if (fxml.endsWith(".fxml")) {
            int extensionIndex = fxml.lastIndexOf(".");
            fxml = fxml.substring(0, extensionIndex);
        }

        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        fxmlLoaders.put(controllerType, fxmlLoader);
        return fxmlLoader;
    }
}
