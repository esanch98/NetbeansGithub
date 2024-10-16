package playwave;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import playwave.dao.database.PlaylistDao;
import playwave.entities.Playlist;

public class CreatePlaylistController implements Initializable {

    @FXML
    private TextField playlistName;
    @FXML
    private Button cancelButton;
    @FXML
    private Button confirmButton;

    private HomeController homeController;

    private PlaylistManagementController playlistManagementController;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
    }

    /**
     * *
     *
     * @param event
     * @throws IOException
     */
    @FXML
    private void cancelPlaylistCreation(MouseEvent event) throws IOException {
        homeController.showPlaylistManagementMenu();
    }

    /**
     * *
     *
     * @param event
     * @throws IOException
     */
    @FXML
    private void confirmPlaylistCreation(MouseEvent event) throws IOException {

        Playlist newPlaylist = new Playlist(playlistName.getText());
        if (newPlaylist.getName().length() > 0) {
            try {
                PlaylistDao.getInstance().create(newPlaylist);
            } catch (SQLException sqle) {
                sqle.getMessage();
            }

            homeController.showNewPlaylistItem(newPlaylist);
            homeController.showPlaylistManagementMenu();
        }
    }

    public HomeController getHomeController() {
        return homeController;
    }

    public void setHomeController(HomeController homeController) {
        this.homeController = homeController;
    }

    public void setPlaylistManagementController(PlaylistManagementController playlistManagementController) {
        this.playlistManagementController = playlistManagementController;
    }
}
