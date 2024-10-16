package playwave;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Rectangle;
import playwave.dao.database.PlaylistDao;
import playwave.entities.Playlist;

public class PlaylistListItemController implements Initializable {

    @FXML
    private HBox playlistListItem;
    @FXML
    private ImageView playlistImage;
    @FXML
    private Label playlistName;
    @FXML
    private Label numberOfTracks;

    private PlaylistManagementController playlistManagementController;
    private PlaylistListItemController playlistListItemController;
    private HomeController homeController;
    private Playlist playlist;
    private PlaylistDao PlaylistDao;

    private boolean playlistSelected;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        int cornerRadius = 10;
        Rectangle trackCoverClip = new Rectangle(playlistImage.getFitWidth(), playlistImage.getFitHeight());
        trackCoverClip.setArcWidth(cornerRadius * 2);
        trackCoverClip.setArcHeight(cornerRadius * 2);
        playlistImage.setClip(trackCoverClip);
    }

    /***
     * 
     * @param event
     * @throws IOException 
     */
    @FXML
    private void playlistSelected(MouseEvent event) throws IOException {
        playlistListItem.setStyle("-fx-background-color: #505050;");
        String selectedPlaylistName = playlistName.getText();
        try {
            playlist = PlaylistDao.getInstance().findByName(selectedPlaylistName);
            playlistManagementController.setSelectedPlaylist(playlist);
            homeController.showPlaylistView(playlist);
        } catch (SQLException sqle) {
            System.out.println(sqle.getMessage());
        }

        playlistManagementController.expand();
    }

    public String getNumberOfTracks() {
        return numberOfTracks.getText();
    }

    public PlaylistManagementController getPlaylistManagementController() {
        return playlistManagementController;
    }

    public void setPlaylistManagementController(PlaylistManagementController playlistManagementController) {
        this.playlistManagementController = playlistManagementController;
    }

    public boolean setPlaylistSelected() {
        return true;
    }
    
    public void setPlaylist(Playlist playlist){
        this.playlist = playlist;
    }
    
    public void setPlaylistName(String name) {
        playlistName.setText(name);
    }

    public Label getPlaylistName() {
        return playlistName;
    }

    public void setNumberOfTracks(int number) {
        numberOfTracks.setText(String.valueOf(number) + " tracks");
    }
    
    public void updateNumberOfTracks() {
        setNumberOfTracks(Integer.parseInt(getNumberOfTracks().substring(0,1)) + 1);
    }

    public void setPlaylistImage(String path) {
        if (path == null || path.isEmpty()) {
            Image image = new Image("playwave/images/brand.png");
            playlistImage.setImage(image);
        } else {
            Image image = new Image(path);
            playlistImage.setImage(image);
        }
    }

    public HomeController getHomeController() {
        return homeController;
    }

    public void setHomeController(HomeController homeController) {
        this.homeController = homeController;
    }
}
