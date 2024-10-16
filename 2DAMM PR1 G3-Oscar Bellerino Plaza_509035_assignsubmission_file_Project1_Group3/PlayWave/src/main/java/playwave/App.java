package playwave;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.Locale;
import javafx.scene.image.Image;
import playwave.dao.files.DefaultSettings;
import playwave.dao.files.Settings;

public class App extends Application {
    private static Scene scene;
    private HomeController homeController;

    /***
     * 
     * @param stage
     * @throws IOException 
     */
    @Override
    public void start(Stage stage) throws IOException {
        Settings settings = Settings.getInstance();
        if (!settings.settingsFileExists()) settings.createDefaultSettings();
        
        FXMLLoader loader = new FXMLLoader(App.class.getResource("Home.fxml"));
        Parent root = loader.load();
        homeController = loader.getController();
        
        scene = new Scene(root, Integer.parseInt(settings.getPropertyValue(DefaultSettings.WINDOW_WIDTH.name())), Integer.parseInt(settings.getPropertyValue(DefaultSettings.WINDOW_HEIGHT.name())));
        stage.setScene(scene);
        stage.sizeToScene();
        stage.setTitle("Play Wave");
        Image appIcon = new Image("playwave/icons/appIcon.png");
        stage.getIcons().add(appIcon);
        
        stage.setOnCloseRequest(e -> {
            settings.updateSettings(DefaultSettings.WINDOW_WIDTH.name(), String.format("%.0f", scene.widthProperty().get()));
            settings.updateSettings(DefaultSettings.WINDOW_HEIGHT.name(), String.format("%.0f", scene.heightProperty().get()));
            settings.updateSettings(DefaultSettings.LEFT_DIVIDER_POSITION.name(), String.format(Locale.US, "%.2f", homeController.getLeftDividerPosition()));
            settings.updateSettings(DefaultSettings.RIGHT_DIVIDER_POSITION.name(), String.format(Locale.US, "%.2f", homeController.getRightDividerPosition()));
            settings.updateSettings(DefaultSettings.DEFAULT_DIRECTORY.name(), homeController.getDefaultDirectoryTextDisplay());
        });
        
        stage.show();
    }

    /***
     * 
     * @param fxml
     * @throws IOException 
     */
    static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    /***
     * 
     * @param fxml
     * @return
     * @throws IOException 
     */
    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void launcher() {
        launch();
    }
}