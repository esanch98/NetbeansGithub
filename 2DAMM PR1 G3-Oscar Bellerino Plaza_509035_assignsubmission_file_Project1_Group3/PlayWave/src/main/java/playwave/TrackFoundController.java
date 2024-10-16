package playwave;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import playwave.dao.database.PlaylistDao;
import playwave.dao.database.TrackDao;
import playwave.entities.Playlist;
import playwave.entities.Track;
import playwave.logic.FXMLManager;

public class TrackFoundController implements Initializable {

    private final FXMLManager fxmlManager = FXMLManager.getInstance();
    private PlaylistListItemController playlistListItemController;
    private final PlaylistDao playlistDao = PlaylistDao.getInstance();
    private final TrackDao trackDao = TrackDao.getInstance();
    private PlaylistController playlistController;
    private Playlist selectedPlaylist;

    @FXML
    private VBox track;
    @FXML
    private Label trackTitle;
    @FXML
    private Label trackPath;
    @FXML
    private Button addTrackButton;
    @FXML
    private ComboBox<String> selectPlaylistDropDown;

    // This VBox is from the Playlist View
    @FXML
    private VBox trackList;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        selectPlaylistDropDown.setManaged(false);
        selectPlaylistDropDown.setVisible(false);
        try {
            FXMLLoader playlistListItemLoader = fxmlManager.loadFXML(PlaylistListItemController.class, "PlaylistListItem");
            playlistListItemLoader.load();
            playlistListItemController = playlistListItemLoader.getController();
        } catch (IOException ex) {
            Logger.getLogger(TrackFoundController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Label getTrackTitle() {
        return trackTitle;
    }

    public Playlist getPlaylist() {
        return selectedPlaylist;
    }

    public void setPlaylist(Playlist playlist) {
        this.selectedPlaylist = playlist;
    }

    public void setTrackTitle(Label trackTitle) {
        this.trackTitle = trackTitle;
    }

    public Label getTrackPath() {
        return trackPath;
    }

    public void setTrackPath(Label trackPath) {
        this.trackPath = trackPath;
    }

    public PlaylistController setPlaylistController(PlaylistController pc) {
        return this.playlistController = pc;
    }

    /***
     * 
     * @param event
     * @throws SQLException 
     */
    @FXML
    private void addTrackToPlaylist(MouseEvent event) throws SQLException {
        List<Playlist> playlists = playlistDao.findAll(new Playlist());
        double width = 16;
        double height = 16;
        Image plusIcon = new Image("playwave/icons/plus.png");
        Image cancelIcon = new Image("playwave/icons/cancel.png");
        Image checkIcon = new Image("playwave/icons/check.png");
        ImageView icon = (ImageView) addTrackButton.getGraphic();

        if (icon.getImage().getUrl().equals(plusIcon.getUrl())) {
            selectPlaylistDropDown.setManaged(true);
            selectPlaylistDropDown.setVisible(true);
            track.setVisible(false);
            track.setManaged(false);
            icon.setImage(cancelIcon);
            if (!selectPlaylistDropDown.getItems().isEmpty()) {
                selectPlaylistDropDown.getItems().clear();
            }
            if (!playlists.isEmpty()) {
                selectPlaylistDropDown.setPromptText("Select playlist");
                for (Playlist p : playlists) {
                    selectPlaylistDropDown.getItems().add(p.getName());
                }
            } else {
                selectPlaylistDropDown.setPromptText("Create a playlist first");
            }
        } else if (icon.getImage().getUrl().equals(cancelIcon.getUrl())) {
            selectPlaylistDropDown.setManaged(false);
            selectPlaylistDropDown.setVisible(false);
            track.setVisible(true);
            track.setManaged(true);
            icon.setImage(plusIcon);
        } else if (icon.getImage().getUrl().equals(checkIcon.getUrl())) {
            selectPlaylistDropDown.setManaged(false);
            selectPlaylistDropDown.setVisible(false);
            track.setVisible(true);
            track.setManaged(true);
            icon.setImage(plusIcon);

            int playlistIndex = -1;
            for (int i = 0; i < playlists.size(); i++) {
                if (playlists.get(i).getName().equals(selectPlaylistDropDown.getValue())) {
                    playlistIndex = i;
                    break;
                }
            }

            if (playlistIndex == -1) {
                System.out.println("Selected playlist not found");
            } else {
                selectedPlaylist = playlists.get(playlistIndex);
            }

            /* If track hash is not repeated in track table */
            Track newTrack = createTrack();
            if (trackDao.findById(newTrack) != null) {
                if (selectedPlaylist.checkTrackWithinPlaylist(newTrack)) {
                    Tooltip tooltip = new Tooltip("This song is already in the selected playlist");
                    tooltip.show(track, event.getScreenX(), event.getScreenY());
                    PauseTransition delay = new PauseTransition(Duration.seconds(3));
                    delay.setOnFinished(e -> tooltip.hide());
                    delay.play();
                } else {
                    selectedPlaylist.addTrack(newTrack);
                    trackDao.assignTrackToPlaylist(newTrack.getId(), selectedPlaylist.getName());
                    /*if (playlistController != null && playlistController.getPlaylist() == selectedPlaylist) {
                        playlistController.updatePlaylistTracks();
                    } NOT WORKING PROPERLY*/
                }
            } else {
                trackDao.create(newTrack);
                selectedPlaylist.addTrack(newTrack);
                trackDao.assignTrackToPlaylist(newTrack.getId(), selectPlaylistDropDown.getValue());
                /*if (playlistController != null && playlistController.getPlaylist() == selectedPlaylist) {
                    playlistController.updatePlaylistTracks();
                } NOT WORKING PROPERLY*/ 
            }
        }

        icon.setFitWidth(width);
        icon.setFitHeight(height);
        addTrackButton.setGraphic(icon);
    }

    /***
     * 
     * @param event 
     */
    @FXML
    private void playlistSelected(ActionEvent event) {
        double width = 16;
        double height = 16;
        Image checkIcon = new Image("playwave/icons/check.png");
        ImageView icon = new ImageView(checkIcon);
        icon.setFitWidth(width);
        icon.setFitHeight(height);
        addTrackButton.setGraphic(icon);
    }

    /***
     * 
     * @return 
     */
    private Track createTrack() {
        try {
            String mp3FilePath = trackPath.getText();
            AudioFile audioFile = AudioFileIO.read(new File(mp3FilePath));
            Tag tag = audioFile.getTag();

            /* Read metadata */
            String title;
            String artist = null;
            if (tag.getFirst(FieldKey.TITLE) == null) {
                title = trackTitle.getText();
                if (tag.getFirst(FieldKey.ARTIST) != null) {
                    artist = tag.getFirst(FieldKey.ARTIST);
                    title += " - " + artist;
                }
            } else {
                title = tag.getFirst(FieldKey.TITLE);
            }

            String icon = tag.getFirst(FieldKey.COVER_ART) == null ? tag.getFirst(FieldKey.COVER_ART) : null;
            String observations = tag.getFirst(FieldKey.COMMENT) == null ? tag.getFirst(FieldKey.COMMENT) : null;
            return new Track(title, mp3FilePath, artist, icon, observations);
        } catch (CannotReadException | IOException | TagException | ReadOnlyFileException | InvalidAudioFrameException ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
