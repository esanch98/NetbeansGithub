package playwave;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import playwave.dao.database.PlaylistDao;
import playwave.dao.database.TrackDao;
import playwave.entities.Playlist;
import playwave.entities.Track;
import playwave.logic.FXMLManager;

public class PlaylistController {

    private PlaylistManagementController playlistManagementController;
    private Playlist playlist;
    private HomeController homeController;
    private PlaylistController playlistController;
    private PlaylistListItemController playlistListItemController;
    PlaylistTrackController playlistTrackController;
    private FXMLManager fxmlManager = FXMLManager.getInstance();
    private TrackFoundController trackFoundController;
    private int indexOfplaylistList;
    private ObservableList<Track> observable;

    @FXML
    private ImageView playlistImage;
    @FXML
    private Label playlistTitle;
    @FXML
    private TextArea playlistObservations;
    @FXML
    private VBox trackList;
    @FXML
    private HBox playlistInfo;
    @FXML
    private Button editPlaylistImageButton;
    @FXML
    private Button editPlaylistNameButton;
    @FXML
    private Label numberOfTracks;
    @FXML
    private VBox playlistPanel;
    @FXML
    private Button exitButton;
    @FXML
    private Button confirmNameButton;
    @FXML
    private TextField playlistNameTextField;
    
    public void initialize(URL url, ResourceBundle rb) {

        int cornerRadius = 10;
        Rectangle trackCoverClip = new Rectangle(playlistImage.getFitWidth(), playlistImage.getFitHeight());
        trackCoverClip.setArcWidth(cornerRadius * 2);
        trackCoverClip.setArcHeight(cornerRadius * 2);
        playlistImage.setClip(trackCoverClip);
    }

    public ImageView getPlaylistImage() {
        return playlistImage;
    }

    public void setPlaylistImage(ImageView playlistImage) {
        this.playlistImage = playlistImage;
    }

    public Label getPlaylistTitle() {
        return playlistTitle;
    }

    public void setPlaylistTitle(Label playlistTitle) {
        this.playlistTitle = playlistTitle;
    }

    public TextArea getPlaylistObservations() {
        return playlistObservations;
    }

    public void setPlaylistObservations(TextArea playlistObservations) {
        this.playlistObservations = playlistObservations;
    }

    public Playlist getPlaylist() {
        return playlist;
    }

    public PlaylistListItemController getPlaylistListItemController() {
        return playlistListItemController;
    }

    public void setPlaylistListItemController(PlaylistListItemController playlistListItemController) {
        this.playlistListItemController = playlistListItemController;
    }

    public void setPlaylist(Playlist playlist) {
        this.playlist = playlist;
    }

    public void setPlaylistManagementController(PlaylistManagementController playlistManagementController) {
        this.playlistManagementController = playlistManagementController;
    }

    public void setHomeController(HomeController homeController) {
        this.homeController = homeController;
    }

    public HomeController getHomeController() {
        return homeController;
    }

    public Button getEditPlaylistImageButton() {
        return editPlaylistImageButton;
    }

    public void setEditPlaylistImageButton(Button editPlaylistImageButton) {
        this.editPlaylistImageButton = editPlaylistImageButton;
    }

    public Button getEditPlaylistNameButton() {
        return editPlaylistNameButton;
    }

    public void setEditPlaylistNameButton(Button editPlaylistNameButton) {
        this.editPlaylistNameButton = editPlaylistNameButton;
    }

    public TextArea getEditPlaylistObservations() {
        return playlistObservations;
    }

    public void setEditPlaylistObservations(TextArea editPlaylistObservations) {
        this.playlistObservations = editPlaylistObservations;
    }

    private void getPlaylistTitleText() {
        playlistTitle.setText(playlist.getName());
    }

    private void getNumberOfTracks() {
        numberOfTracks.setText(playlist.getNumberOfTracks().get() + " tracks");
    }

    public int getIndexOfplaylistList() {
        indexOfplaylistList = homeController.getPlaylistObservableList().indexOf(playlist);
        return indexOfplaylistList;
    }

    public void setPlaylistController(PlaylistController playlistController) {
        this.playlistController = playlistController;
    }

    private ImageView getImage() {
        String defaultImage = "/playwave/images/brand.png";
        if (playlist.getIcon() == null || playlist.getIcon().isEmpty()) {
            playlistImage = new ImageView(new Image(getClass().getResource(defaultImage).toExternalForm()));
        } else {
            playlistImage = new ImageView(new Image(playlist.getIcon()));
        }
        return playlistImage;
    }

    /***
     * 
     * @param playlist 
     */
    public void updatePlaylistInformation(Playlist playlist) {
        this.playlist = playlist;
        playlistTitle.setText(playlist.getName());
    }

    public void showScreen() {
        trackList.getChildren().clear();
        getPlaylistTitleText();
        getNumberOfTracks();
        getImage();
        confirmNameButton.setVisible(false);
        showPlaylistTracks();
/*        try {
            updatePlaylistTracks();
        } catch (SQLException ex) {
            Logger.getLogger(PlaylistController.class.getName()).log(Level.SEVERE, null, ex);
        }*/
    }

/* It doesn't work properly because it's not updating the view while you are
    inside the playlist and it's not doing anything, it doesn't break anything either
    public void resetObservableListTracks() throws SQLException {
        
        observable = FXCollections.observableArrayList();
        observable.clear();
        observable.addAll(TrackDao.getInstance().findAll(playlist));
        
    }
    
    public void updatePlaylistTracks() throws SQLException {
        resetObservableListTracks();
        observable.addListener((ListChangeListener) change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    Platform.runLater(() -> {
                        trackList.getChildren().clear();
                        showPlaylistTracks();
                    });
                }
                if (change.wasRemoved()) {
                    Platform.runLater(() -> {
                        trackList.getChildren().clear();
                        showPlaylistTracks();
                    });
                }
            }
        });

    }
*/
    public void updateNumberOfTracksDelete() {
        numberOfTracks.setText((playlist.getNumberOfTracks().get() - 1) + " tracks");
    }

    public void showPlaylistTracks() {
        for (int i = 0; i < playlist.getNumberOfTracks().get(); ++i) {
            try {
                FXMLLoader playlistTrackLoader = fxmlManager.loadFXML(PlaylistTrackController.class, "PlaylistTrack");
                Parent playlistTrackView = playlistTrackLoader.load();
                playlistTrackController = playlistTrackLoader.getController();
                playlistTrackController.setTrack(playlist.getTrackList().get(i));
                playlistTrackController.setPlaylistController(this);
                playlistTrackController.setProperties();
                trackList.getChildren().add(playlistTrackView);
            } catch (IOException ex) {
                System.out.println(ex.getMessage());
            }
        }
    }

    public VBox getTrackList() {
        return trackList;
    }

    /***
     * 
     * @param event 
     */
    @FXML
    private void changePlaylistTitle(MouseEvent event) {
        playlistTitle.setVisible(false);
        playlistNameTextField.setText(playlist.getName());
        playlistNameTextField.setStyle("-fx-background-color: #292929");
        playlistNameTextField.setVisible(true);
        playlistTitle.setText("");
        confirmNameButton.setVisible(true);
    }

    /***
     * 
     * @param event
     * @throws SQLException 
     */
    @FXML
    private void confirmName(MouseEvent event) throws SQLException {
        int index = getIndexOfplaylistList();
        String newName = playlistNameTextField.getText();
        PlaylistDao.getInstance().updateName(playlist, newName);
        playlist.setName(newName);
        // Update the label with the new name
        playlistTitle.setText(newName);
        playlistTitle.setVisible(true);
        playlistNameTextField.setVisible(false);
        confirmNameButton.setVisible(false);
        // Update the PlaylistListItem in the list that is in the left part of the screen
        homeController.updateObservablePlaylistList();
        homeController.updatePlaylistListGraphicInterface();
    }
    
    /***
     * 
     * @param event
     * @throws SQLException 
     */
    private void chooseImageButton(MouseEvent event) throws SQLException {
        editPlaylistImageButton.setOnAction(eh -> {
            FileChooser fileChooser = new FileChooser();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            fileChooser.setTitle("Choose image");
            File selectedFile = fileChooser.showOpenDialog(stage);

            if (selectedFile.toString().endsWith(".png") || selectedFile.toString().endsWith(".jpg")) {
                try {
                    playlist.setIcon(selectedFile.toString());
                    PlaylistDao.getInstance().update(playlist, event);
                    getImage();
                } catch (SQLException ex) {
                    Logger.getLogger(PlaylistController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        );
    }

    /***
     * 
     * @param event 
     */
    private void selectTrack(MouseEvent event) {
        PlaylistTrackController controller = new FXMLLoader(App.class.getResource("PlaylistViewItem.fxml")).getController();
        playlistTrackController.getPlaylistTrack().setOnMouseClicked(eh -> {
            Track selectedTrack = controller.getTrack();
            try {
                TrackDao.getInstance().findById(selectedTrack);
            } catch (SQLException ex) {
                Logger.getLogger(PlaylistController.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
    }

    /***
     * 
     * @param event 
     */
    @FXML
    private void exitPlaylist(MouseEvent event) {
        //Playlist is deselected
        if (!playlistTitle.isVisible()) {
            playlistTitle.setVisible(true);
            playlistNameTextField.setText("");
            playlistNameTextField.setVisible(false);
            confirmNameButton.setVisible(false);
        }
        playlistManagementController.contract();
        homeController.showVinylDiscView();
    }

    /***
     * 
     * @param event 
     */
    @FXML
    private void editPlaylistIcon(MouseEvent event) {
        try {
            PlaylistDao.getInstance().update(playlist, event);
        } catch (SQLException sqle) {
            sqle.getMessage();
        }
    }

    /***
     * 
     * @param event 
     */
    @FXML
    private void editPlaylistName(MouseEvent event) {
        try {
            PlaylistDao.getInstance().update(playlist, event);
        } catch (SQLException sqle) {
            sqle.getMessage();
        }
    }

    /***
     * 
     * @param event 
     */
    @FXML
    private void editPlaylistObservations(KeyEvent event) {
        try {
            PlaylistDao.getInstance().update(playlist, event);
        } catch (SQLException sqle) {
            sqle.getMessage();
        }
    }
}
