package playwave;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import playwave.entities.Track;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.input.MouseEvent;
import playwave.dao.database.TrackDao;
import playwave.logic.PlaylistLogic;

public class PlaylistTrackController {

    private Track track;
    private PlaylistController playlistController;
    private HomeController homeController;
    private TrackDao trackDao = TrackDao.getInstance();
    private PlaylistLogic playlistLogic = PlaylistLogic.getInstance();

    @FXML
    private HBox playlistTrack;

    @FXML
    private Label trackObservations;

    @FXML
    private ImageView trashIcon;

    @FXML
    private ImageView folderIcon;

    private ImageView clockIcon;
    @FXML
    private Label trackTitle;
    @FXML
    private HBox trackInfo;
    @FXML
    private Label trackPath;
    @FXML
    private Button deleteTrackButton;

    public void setTrack(Track track) {
        this.track = track;
        playlistLogic.setTrack(this.track);
    }

    public HBox getPlaylistTrack() {
        return playlistTrack;
    }

    public Track getTrack() {
        return track;
    }

    public PlaylistController getPlaylistController() {
        return playlistController;
    }

    public void setPlaylistController(PlaylistController playlistController) {
        this.playlistController = playlistController;
    }

    public HomeController getHomeController() {
        return homeController;
    }

    public void setHomeController(HomeController homeController) {
        this.homeController = homeController;
    }

    public void initialize(URL url, ResourceBundle rb) throws IOException {
    }

    public void setProperties() {
        trackTitle.setText(track.getTitle());
        trackPath.setText(track.getPath());
        trackObservations.setText(track.getObservations());
    }

    public void showTrack() {
        getTitle();
        getPath();
        getFolderIcon();
        getClockIcon();
        getTrashIcon();
        getTrackObservations();
    }

    private Label getPath() {
        trackPath.setText(track.getPath());
        return trackPath;
    }

    private Label getTitle() {
        trackTitle.setText(track.getTitle() + " - " + track.getArtist());
        return trackTitle;
    }

    private ImageView getFolderIcon() {
        String FolderIconSource = "/icons/folder.png";
        folderIcon = new ImageView(new Image(FolderIconSource));
        return folderIcon;
    }

    private ImageView getClockIcon() {
        String ClockIconSource = "/icons/clock.png";
        clockIcon = new ImageView(new Image(ClockIconSource));
        return clockIcon;
    }

    private ImageView getTrashIcon() {
        String trashIconSource = "/icons/trash.png";
        trashIcon = new ImageView(new Image(trashIconSource));
        return trashIcon;
    }

    private Label getTrackObservations() {
        trackObservations.setText(track.getObservations());
        return trackObservations;
    }

    /***
     * 
     * @param event 
     */
    @FXML
    private void deleteTrack(MouseEvent event) {
        int result = 0;
        Optional<ButtonType> resultOptional = showDeleteTrackConfirmationAlert(track.getTitle());
        if (resultOptional.isPresent() && resultOptional.get().getText().equals("Delete")) {
            try {
                result = trackDao.delete(track, playlistController.getPlaylist().getName());
            } catch (SQLException ex) {
                System.out.println(ex.getMessage());
            }
        }

        if (result > 0) {
            Alert deleteAlert = new Alert(Alert.AlertType.INFORMATION);
            deleteAlert.setTitle("Track Deleted");
            deleteAlert.setHeaderText(null);
            deleteAlert.setContentText("The track has been deleted successfully.");
            deleteAlert.showAndWait();
            playlistController.getTrackList().getChildren().remove(playlistTrack);
            playlistController.updateNumberOfTracksDelete(); //This line do the job but is not ideal
        } else {
            System.out.println("No track deleted");
        }
    }

    /***
     * 
     * @param trackTitle
     * @return 
     */
    public Optional<ButtonType> showDeleteTrackConfirmationAlert(String trackTitle) {

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Are you sure that you want to delete " + trackTitle + " track?");
        alert.setContentText("This action cannot be reversed.");
        ButtonType buttonTypeYes = new ButtonType("Delete");
        ButtonType buttonTypeNo = new ButtonType("Cancel");
        alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo);

        return alert.showAndWait();
    }

    /***
     * 
     * @param event 
     */
    @FXML
    private void selectedTrack_onMouseClicked(MouseEvent event) {
        playlistTrack.setStyle("-fx-background-color: #3e3e3e");
        playlistLogic.setTrackData();
    }
}
