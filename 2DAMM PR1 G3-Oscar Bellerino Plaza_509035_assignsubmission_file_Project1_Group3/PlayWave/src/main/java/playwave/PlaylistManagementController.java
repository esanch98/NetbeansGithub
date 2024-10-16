package playwave;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import playwave.dao.database.PlaylistDao;
import playwave.dao.database.TrackDao;
import playwave.entities.Playlist;
import playwave.entities.Track;

public class PlaylistManagementController implements Initializable {

    @FXML
    private Button deletePlaylistButton;
    @FXML
    private Button addPlaylistButton;
    @FXML
    private HBox buttonContainer;

    private HomeController homeController;
    private PlaylistManagementController playlistManagementController;
    private PlaylistListItemController playlistListItemController;

    private Playlist selectedPlaylist;

    private Track selectedTrack;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
    }

    public void expand() {
        if (!deletePlaylistButton.isManaged()) {
            buttonContainer.setPrefWidth(90);
            deletePlaylistButton.setVisible(true);
            deletePlaylistButton.setManaged(true);
        }
    }

    public void contract() {
        if (deletePlaylistButton.isManaged()) {
            buttonContainer.setPrefWidth(Button.USE_COMPUTED_SIZE);
            deletePlaylistButton.setVisible(false);
            deletePlaylistButton.setManaged(false);
        }
    }

    public void setSelectedPlaylist(Playlist selectedPlaylist) {
        this.selectedPlaylist = selectedPlaylist;
    }

    public Playlist getPlaylist(){
        return this.selectedPlaylist;
    }
    
    /***
     * 
     * @param event 
     */
    @FXML
    private void deletePlaylist(MouseEvent event) {
        int result = 0;
        Optional<ButtonType> resultOptional = showDeletePlaylistConfirmationAlert(selectedPlaylist.getName());
        if (resultOptional.isPresent() && resultOptional.get().getText().equals("Delete")) {
            try {
                result = PlaylistDao.getInstance().delete(selectedPlaylist, "");
            } catch (SQLException sqle) {
                System.out.println(sqle.getMessage());
            }
        }
        
        if (result > 0) {
            Alert deleteAlert = new Alert(Alert.AlertType.INFORMATION);
            deleteAlert.setTitle("Playlist Deleted");
            deleteAlert.setHeaderText(null);
            deleteAlert.setContentText("The playlist has been deleted successfully.");
            deleteAlert.show();
            for (Track t : selectedPlaylist.getTrackList()) {
                try {
                    TrackDao.getInstance().delete(t, selectedPlaylist.getName());
                } catch (SQLException ex) {
                    System.out.println(ex.getMessage());
                }
            }
            
            this.selectedPlaylist = null;
            contract();
            homeController.updateObservablePlaylistList();
            homeController.updatePlaylistListGraphicInterface();
            homeController.showVinylDiscView();
        } else {
            System.out.println("No playlist deleted");
        }
    }

    /***
     * 
     * @param playlistName
     * @return 
     */
    public Optional<ButtonType> showDeletePlaylistConfirmationAlert(String playlistName) {

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Are you sure that you want to delete " + playlistName + " playlist?");
        alert.setContentText("This action cannot be reversed.");
        ButtonType buttonTypeYes = new ButtonType("Delete");
        ButtonType buttonTypeNo = new ButtonType("Cancel");
        alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo);

        return alert.showAndWait();
    }

    /***
     * 
     * @param event
     * @throws IOException 
     */
    @FXML
    private void createPlaylist(MouseEvent event) throws IOException {
        homeController.showCreatePlaylistMenu();
    }

    public void setHomeController(HomeController homeController) {
        this.homeController = homeController;
    }

    public void setPlaylistManagementController(PlaylistManagementController playlistManagementController) {
        this.playlistManagementController = playlistManagementController;
    }
}
