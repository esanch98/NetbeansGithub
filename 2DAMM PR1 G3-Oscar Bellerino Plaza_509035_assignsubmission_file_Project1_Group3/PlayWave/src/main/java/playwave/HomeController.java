package playwave;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.shape.Rectangle;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import playwave.dao.database.PlaylistDao;
import playwave.dao.database.TrackDao;
import playwave.dao.files.DefaultSettings;
import playwave.dao.files.Settings;
import playwave.entities.Playlist;
import playwave.entities.Track;
import playwave.logic.FXMLManager;
import playwave.logic.PlaylistLogic;
import playwave.logic.SearchTracks;

public class HomeController implements Initializable {

    private final Settings settings = Settings.getInstance();
    private final FXMLManager fxmlManager = FXMLManager.getInstance();
    private final TrackDao trackDao = TrackDao.getInstance();
    private Parent playlistManagementView;
    private Parent createPlaylistView;
    private Parent vinylDiscView;
    private Parent playlistView;
    private Parent playlistListItemView;
    private Parent playlistTrackView;
    private PlaylistManagementController playlistManagementController;
    private CreatePlaylistController createPlaylistController;
    private VinylDiscController vinylDiscController;
    private PlaylistController playlistController;
    private TrackFoundController trackFoundController;
    private Thread searchThread;
    private volatile boolean isSearchPaused = true;
    private SearchTracks searchTask;
    private Thread trackNavigationThread;
    private PlaylistListItemController playlistListItemController;
    private PlaylistTrackController playlistTrackController;
    private PlaylistLogic playlistLogic;
    private Track selectedTrack;
    private Thread listenerThread;

    private ObservableList<Playlist> playlistObservableList;

    private Path defaultDirectory;
    private double rating;
    private MediaPlayer player;
    private Media media;
    private Duration lastPlayPosition;

    private ImageView trackImage;
    @FXML
    private VBox tracksFound;
    @FXML
    private ImageView trackCover;
    @FXML
    private Label trackName;
    @FXML
    private Label playlistName;
    @FXML
    private Button searchButton;
    @FXML
    private Button stopSearchButton;
    @FXML
    private Button defaultDirectoryButton;
    @FXML
    private Button trackRating1;
    @FXML
    private Button trackRating2;
    @FXML
    private Button trackRating3;
    @FXML
    private Button trackRating4;
    @FXML
    private Button trackRating5;
    @FXML
    private Button fastBackwardButton;
    @FXML
    private Button stopButton;
    @FXML
    private Button playPauseButton;
    @FXML
    private Button fastForwardButton;
    @FXML
    private Button settingsButton;
    @FXML
    private VBox playlistManagement;
    @FXML
    private VBox playlistList;
    @FXML
    private TextField searchFilesTextField;
    @FXML
    private TextField defaultDirectoryTextField;
    @FXML
    private ScrollPane tracksFoundScrollPane;
    @FXML
    private Button playPauseSearchButton;
    @FXML
    private SplitPane splitPane;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        /* Track cover image radius application */
        int cornerRadius = 10;
        Rectangle trackCoverClip = new Rectangle(trackCover.getFitWidth(), trackCover.getFitHeight());
        trackCoverClip.setArcWidth(cornerRadius * 2);
        trackCoverClip.setArcHeight(cornerRadius * 2);
        trackCover.setClip(trackCoverClip);

        /* Load of external resources */
        try {
            /* Load views and its controllers */
            FXMLLoader playlistManagementLoader = fxmlManager.loadFXML(PlaylistManagementController.class, "PlaylistManagement");
            playlistManagementView = playlistManagementLoader.load();
            playlistManagementController = playlistManagementLoader.getController();

            FXMLLoader createPlaylistLoader = fxmlManager.loadFXML(CreatePlaylistController.class, "CreatePlaylist");
            createPlaylistView = createPlaylistLoader.load();
            createPlaylistController = createPlaylistLoader.getController();

            FXMLLoader vinylDiscLoader = fxmlManager.loadFXML(VinylDiscController.class, "VinylDisc");
            vinylDiscView = vinylDiscLoader.load();
            vinylDiscController = vinylDiscLoader.getController();

            FXMLLoader playlistLoader = fxmlManager.loadFXML(PlaylistController.class, "Playlist");
            playlistView = playlistLoader.load();
            playlistController = playlistLoader.getController();

            FXMLLoader playlistTrackLoader = fxmlManager.loadFXML(PlaylistTrackController.class, "PlaylistTrack");
            playlistTrackView = playlistTrackLoader.load();
            playlistTrackController = playlistTrackLoader.getController();

            /* Set controllers on external controllers */
            playlistManagementController.setHomeController(this);
            createPlaylistController.setHomeController(this);
            playlistController.setHomeController(this);
            CreatePlaylistController createPlaylistController = new CreatePlaylistController();
            createPlaylistController.setPlaylistManagementController(playlistManagementController);
            playlistController.setPlaylistManagementController(playlistManagementController);
            vinylDiscController.setHomeController(this);
            trackDao.setHomeController(this);
            playlistLogic = PlaylistLogic.getInstance();
            playlistLogic.setHomeController(this);

            initializePlaylistList();

            /* Inject views */
            playlistManagement.getChildren().add(0, playlistManagementView);
            splitPane.getItems().add(1, vinylDiscView);

            /* Load of values from settings.properties */
            defaultDirectory = getDefaultDirectory();
            updateDefaultDirectoryDisplay(defaultDirectory);

            String leftDividerPosition = settings.getPropertyValue(DefaultSettings.LEFT_DIVIDER_POSITION.name());
            String rightDividerPosition = settings.getPropertyValue(DefaultSettings.RIGHT_DIVIDER_POSITION.name());
            if (isNumeric(leftDividerPosition) && isNumeric(rightDividerPosition)) {
                splitPane.getDividers().get(0).setPosition(Double.parseDouble(leftDividerPosition));
                splitPane.getDividers().get(1).setPosition(Double.parseDouble(rightDividerPosition));
            } else {
                splitPane.getDividers().get(0).setPosition(Double.parseDouble(DefaultSettings.LEFT_DIVIDER_POSITION.getValue()));
                splitPane.getDividers().get(1).setPosition(Double.parseDouble(DefaultSettings.RIGHT_DIVIDER_POSITION.getValue()));
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * *
     *
     * @throws IOException
     */
    public void showCreatePlaylistMenu() throws IOException {
        playlistManagement.getChildren().remove(0);
        playlistManagement.getChildren().add(0, createPlaylistView);
    }

    /**
     * *
     *
     * @throws IOException
     */
    public void showPlaylistManagementMenu() throws IOException {
        playlistManagement.getChildren().remove(0);
        playlistManagement.getChildren().add(0, playlistManagementView);
    }

    /**
     * *
     *
     * @param playlist
     * @throws IOException
     */
    public void showPlaylistView(Playlist playlist) throws IOException {
        // Set the playlist in PlaylistController
        playlistController.setPlaylist(playlist);

        // Call the method to show the PlaylistController screen
        playlistController.showScreen();
        splitPane.getItems().remove(1);
        splitPane.getItems().add(1, playlistView);
    }

    public void showVinylDiscView() {
        splitPane.getItems().remove(1);
        splitPane.getItems().add(1, vinylDiscView);
    }

    public double getLeftDividerPosition() {
        return splitPane.getDividers().get(0).getPosition();
    }

    public double getRightDividerPosition() {
        return splitPane.getDividers().get(1).getPosition();
    }

    public String getDefaultDirectoryTextDisplay() {
        return defaultDirectoryTextField.getText();
    }

    public ObservableList<Playlist> getPlaylistObservableList() {
        return playlistObservableList;
    }

    public void setTrackData() {
        playPauseButton.setDisable(false);
        selectedTrack = playlistLogic.getTrack();
        if (player != null) {
            player.stop();
            disableTrackNavigationButtons();
        } else {
            media = new Media(new File(selectedTrack.getPath()).toURI().toString());
            player = new MediaPlayer(media);
        }
        lastPlayPosition = Duration.ZERO;
        trackName.setText(selectedTrack.getTitle());
        playlistName.setText(playlistController.getPlaylistTitle().getText());
    }

    /**
     * *
     *
     * @param button
     * @param isPaused
     */
    public void setPlayPauseIcon(Button button, boolean isPaused) {
        double width = button.getPrefWidth();
        double height = button.getPrefHeight();
        Image playIcon = new Image("playwave/icons/play.png");
        Image pauseIcon = new Image("playwave/icons/pause.png");
        ImageView icon = new ImageView();

        if (isPaused) {
            icon.setImage(playIcon);
        } else {
            icon.setImage(pauseIcon);
        }

        icon.setFitWidth(width);
        icon.setFitHeight(height);
        button.setGraphic(icon);
    }

    /**
     * *
     *
     * @param isManaged
     */
    public void manageStopSearchButton(boolean isManaged) {
        stopSearchButton.setManaged(isManaged);
        stopSearchButton.setVisible(isManaged);
    }

    /**
     * *
     *
     * @param string
     * @return
     */
    private boolean isNumeric(String string) {
        try {
            Double.parseDouble(string);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /* Initializes a thread that runs when something changes */
    private void initializePlaylistList() {
        playlistObservableList = FXCollections.observableArrayList();
        updateObservablePlaylistList();
        updatePlaylistListGraphicInterface();
        /* Binds the UI directly to the observable list */
        playlistObservableList.addListener((ListChangeListener<Playlist>) change -> {
            while (change.next()) {
                if (change.wasUpdated()) {
                    Platform.runLater(() -> {
                        updatePlaylistListGraphicInterface();
                    });
                }
                if (change.wasRemoved()) {
                    Platform.runLater(() -> {
                        updatePlaylistListGraphicInterface();
                    });
                }
            }
        });
    }

    /* Shows in the left part of the screen the list of all playlists */
    public void updateObservablePlaylistList() {
        playlistObservableList.clear();

        Playlist pl = new Playlist();
        try {
            playlistObservableList.addAll(PlaylistDao.getInstance().findAll(pl));
        } catch (SQLException sqle) {
            System.out.println(sqle.getMessage());
        }
    }

    /**
     * *
     *
     * @param pl
     */
    public void addItemToObservablePlaylistList(Playlist pl) {
        playlistObservableList.clear();

        try {
            playlistObservableList.addAll(PlaylistDao.getInstance().findAll(pl));
        } catch (SQLException sqle) {
            System.out.println(sqle.getMessage());
        }
    }

    public void updatePlaylistListGraphicInterface() {
        playlistList.getChildren().clear(); //Clear the existing list

        for (Playlist p : playlistObservableList) {
            try {
                FXMLLoader playlistListLoader = fxmlManager.loadFXML(PlaylistListItemController.class, "PlaylistListItem");
                Parent playlistListItemView = playlistListLoader.load();
                PlaylistListItemController playlistListItemController = playlistListLoader.getController();
                playlistListItemController.setHomeController(this);
                playlistListItemController.setPlaylistManagementController(playlistManagementController);

                playlistListItemController.setPlaylistName(p.nameProperty().get());
                playlistListItemController.setNumberOfTracks(p.getNumberOfTracks().get());
                playlistListItemController.setPlaylistImage(p.getIcon());

                playlistList.getChildren().add(playlistListItemView);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * *
     *
     * @param playlist
     */
    public void showNewPlaylistItem(Playlist playlist) {
        addItemToObservablePlaylistList(playlist);
        updatePlaylistListGraphicInterface();
    }

    private Path getDefaultDirectory() {
        Path defaultDir = Paths.get(settings.getPropertyValue(DefaultSettings.DEFAULT_DIRECTORY.name()));
        if (!Files.exists(defaultDir)) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Warning");
            alert.setHeaderText("Default directory not found");
            alert.setContentText("When loading default directory from settings.properties, the directory\n"
                    + defaultDir.toString() + " was not found");
            alert.initModality(Modality.APPLICATION_MODAL);
            alert.showAndWait();
            defaultDir = Paths.get(System.getProperty("user.home"));
        }

        return defaultDir;
    }

    private void setDefaultDirectory(String path) {
        defaultDirectory = Paths.get(path);
    }

    /**
     * *
     *
     * @param path
     */
    private void updateDefaultDirectoryDisplay(Path path) {
        defaultDirectoryTextField.setText(path.toString());
    }

    /**
     * *
     *
     * @param event
     */
    @FXML
    private void stopSearch(MouseEvent event) {
        if (searchThread != null) {
            manageStopSearchButton(false);
            searchThread.stop();
        }
    }

    /**
     * *
     *
     * @param event
     * @throws IOException
     */
    @FXML
    private void playPauseSearch(MouseEvent event) throws IOException {
        if (searchFilesTextField.getText().length() > 0) { //Start if the input text for the search is not empty
            // Set the button and search state to the opposite value
            isSearchPaused = !isSearchPaused;
            setPlayPauseIcon(playPauseSearchButton, isSearchPaused);

            // Start, pause or stop the search files thread
            if (searchThread == null || !searchThread.isAlive()) { //If there's no search thread active, then start a new one
                searchTask = new SearchTracks(this, defaultDirectoryTextField, searchFilesTextField, tracksFound, playPauseSearchButton);
                searchThread = new Thread(searchTask);
                searchThread.start();
            } else { //If there's a thread active, toggle its state between paused and running
                if (searchTask != null) {
                    searchTask.togglePause();
                }
            }
        } else { //Warn the user to input a text to start the search
            Tooltip tooltip = new Tooltip("Input a text for the search");
            Bounds boundsInScreen = searchButton.localToScreen(searchButton.getBoundsInLocal());
            tooltip.show(searchButton, boundsInScreen.getMinX() + searchButton.getWidth(), boundsInScreen.getMinY() - tooltip.getHeight());
            PauseTransition delay = new PauseTransition(Duration.seconds(3));
            delay.setOnFinished(e -> tooltip.hide());
            delay.play();
        }
    }

    /**
     * *
     *
     * @param event
     */
    @FXML
    private void trackNavigation(MouseEvent event) {

        if (event.getSource() == playPauseButton) {

            if (player.getStatus() == MediaPlayer.Status.UNKNOWN || player.getStatus() == MediaPlayer.Status.DISPOSED
                    || player.getStatus() == MediaPlayer.Status.PAUSED || player.getStatus() == MediaPlayer.Status.STOPPED
                    || player.getStatus() == MediaPlayer.Status.READY) {
                enableTrackNavigationButtons();
                if (player.getStatus() == MediaPlayer.Status.UNKNOWN || player.getStatus() == MediaPlayer.Status.DISPOSED) {
                    media = new Media(new File(selectedTrack.getPath()).toURI().toString());
                    player = new MediaPlayer(media);
                }
                if (lastPlayPosition != null) {
                    if (lastPlayPosition != Duration.ZERO) {
                        player.seek(lastPlayPosition);
                    }
                }
                player.play();
            } else if (player.getStatus() == MediaPlayer.Status.PLAYING) {
                setPlayPauseIcon(playPauseButton, true);
                player.pause();
                lastPlayPosition = player.getCurrentTime();
            }
        } else if (event.getSource() == stopButton) {
            setPlayPauseIcon(playPauseButton, true);
            player.stop();
        } else if (event.getSource() == fastForwardButton) {
            Duration currentPosition = player.getCurrentTime();
            Duration newPosition = currentPosition.add(Duration.seconds(10));
            player.seek(newPosition);
            lastPlayPosition = newPosition;
        } else if (event.getSource() == fastBackwardButton) {
            Duration currentPosition = player.getCurrentTime();
            Duration newPosition = currentPosition.subtract(Duration.seconds(10));
            player.seek(newPosition);
            lastPlayPosition = newPosition;
        }
    }

    private void enableTrackNavigationButtons() { //Visualize pause button
        setPlayPauseIcon(playPauseButton, false);
        stopButton.setDisable(false);
        fastForwardButton.setDisable(false);
        fastBackwardButton.setDisable(false);
    }

    private void disableTrackNavigationButtons() { //Visualize play button
        setPlayPauseIcon(playPauseButton, true);
        stopButton.setDisable(true);
        fastForwardButton.setDisable(true);
        fastBackwardButton.setDisable(true);
    }

    /**
     * *
     *
     * @param event
     */
    @FXML
    private void chooseDefaultDirectory(MouseEvent event) {
        /* Open directory chooser */
        DirectoryChooser directoryChooser = new DirectoryChooser();
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        directoryChooser.setTitle("Choose default directory");
        File selectedDirectory = directoryChooser.showDialog(stage);

        /* Sets the default directory to the new chosen directory */
        if (selectedDirectory != null) {
            String selectedPathString = selectedDirectory.getPath();
            setDefaultDirectory(selectedPathString);
            updateDefaultDirectoryDisplay(Paths.get(selectedPathString));
        }
    }

    /**
     * *
     *
     * @param event
     */
    @FXML
    private void updateRating(MouseEvent event) {
        Image emptyStarIcon = new Image("playwave/icons/emptyStar.png");
        Image fullStarIcon = new Image("playwave/icons/fullStar.png");
        Image halfStarIcon = new Image("playwave/icons/halfStar.png");
        ImageView icon = new ImageView();
        List<Button> ratingButtons = Arrays.asList(trackRating1, trackRating2, trackRating3, trackRating4, trackRating5);
        Button button = (Button) event.getSource();
        ImageView eventButtonImageView = (ImageView) button.getGraphic();
        String eventButtonCurrentImageUrl = eventButtonImageView.getImage().getUrl();
        double width = eventButtonImageView.getFitWidth();
        double height = eventButtonImageView.getFitHeight();

        if (selectedTrack != null) {
            /* Update clicked button */
            if (eventButtonCurrentImageUrl.equals(fullStarIcon.getUrl())) {
                icon.setImage(halfStarIcon);
            } else if (eventButtonCurrentImageUrl.equals(halfStarIcon.getUrl())) {
                icon.setImage(emptyStarIcon);
            } else if (eventButtonCurrentImageUrl.equals(emptyStarIcon.getUrl())) {
                icon.setImage(fullStarIcon);
            }

            icon.setFitWidth(width);
            icon.setFitHeight(height);
            button.setGraphic(icon);

            /* Update next buttons from clicked button */
            int buttonIndex = ratingButtons.indexOf(button);
            for (int i = 0; i < buttonIndex; i++) {
                ImageView previousImageView = (ImageView) ratingButtons.get(i).getGraphic();
                previousImageView.setImage(fullStarIcon);
            }

            /* Update previous buttons from clicked button*/
            for (int i = buttonIndex + 1; i < ratingButtons.size(); i++) {
                ImageView nextImageView = (ImageView) ratingButtons.get(i).getGraphic();
                nextImageView.setImage(emptyStarIcon);
            }

            rating = 0.0;
            for (Button ratingButton : ratingButtons) {
                ImageView ratingButtonImageView = (ImageView) ratingButton.getGraphic();
                String ratingButtonCurrentImage = ratingButtonImageView.getImage().getUrl();
                if (ratingButtonCurrentImage.equals(fullStarIcon.getUrl())) {
                    rating += 1;
                } else if (ratingButtonCurrentImage.equals(halfStarIcon.getUrl())) {
                    rating += 0.5;
                }
            }

            vinylDiscController.updateRatingFromTrackNavigation(rating);
            selectedTrack.setRating(rating);
            try {
                trackDao.update(selectedTrack, event);
            } catch (SQLException ex) {
                System.out.println(ex.getMessage());
            }
        } else {
            Tooltip tooltip = new Tooltip("There is no track selected");
            tooltip.show(button, event.getScreenX(), event.getScreenY());
            PauseTransition delay = new PauseTransition(Duration.seconds(3));
            delay.setOnFinished(e -> tooltip.hide());
            delay.play();
        }
    }

    public Track getSelectedTrack() {
        return selectedTrack;
    }

    /**
     * *
     *
     * @param rating
     */
    public void updateRatingFromTrackNavigation(double rating) {
        Image emptyStarIcon = new Image("playwave/icons/emptyStar.png");
        Image fullStarIcon = new Image("playwave/icons/fullStar.png");
        Image halfStarIcon = new Image("playwave/icons/halfStar.png");

        ImageView[] starImageViews = {(ImageView) trackRating1.getGraphic(), (ImageView) trackRating2.getGraphic(),
            (ImageView) trackRating3.getGraphic(), (ImageView) trackRating4.getGraphic(), (ImageView) trackRating5.getGraphic()};

        for (ImageView starImageView : starImageViews) {
            if (rating >= 1) {
                starImageView.setImage(fullStarIcon);
                rating -= 1;
            } else if (rating > 0) {
                starImageView.setImage(halfStarIcon);
                rating = 0;
            } else {
                starImageView.setImage(emptyStarIcon);
            }
        }
    }

    public double getRating() {
        return rating;
    }

    /**
     * *
     *
     * @param event
     */
    @FXML
    private void openSettingsFile(MouseEvent event) {
        try {
            if (!settings.settingsFileExists()) {
                settings.createDefaultSettings();
            }
            String path = settings.getSettingsPath() + System.getProperty("file.separator") + "settings.properties";
            System.out.println(path);
            ProcessBuilder p = new ProcessBuilder();
            p.command("cmd.exe", "/c", path);
            p.start();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
