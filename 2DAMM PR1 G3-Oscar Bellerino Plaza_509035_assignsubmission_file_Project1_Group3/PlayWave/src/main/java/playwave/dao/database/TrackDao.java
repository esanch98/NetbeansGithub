package playwave.dao.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javafx.event.Event;
import playwave.HomeController;
import playwave.PlaylistManagementController;
import playwave.dao.database.pool.ConnectionPool;
import playwave.entities.Playlist;
import playwave.entities.Track;

public class TrackDao implements Dao<Track, String> {

    /* Singleton pattern */
    private static final TrackDao instance;
    private ResultSet rs = null;
    private HomeController homeController;

    private PlaylistManagementController controller;

    static {
        instance = new TrackDao();
    }

    private TrackDao() {
    }

    public static TrackDao getInstance() {
        return instance;
    }

    public void setHomeController(HomeController homeController) {
        this.homeController = homeController;
    }
    
    /**
     *
     *
     * @param entity
     * @return
     * @throws SQLException
     */
    @Override
    public int create(Track entity) throws SQLException {
        int result;
        String sql = "INSERT INTO track (id, title, path) VALUES (?,?,?);";
        try ( Connection conn = ConnectionPool.getConnection();  PreparedStatement pstm = conn.prepareStatement(sql)) {
            pstm.setString(1, entity.getId());
            pstm.setString(2, entity.getTitle());
            pstm.setString(3, entity.getPath());
            result = pstm.executeUpdate();
        }
        return result;
    }

    /**
     * 
     *
     * @param entity
     * @return
     * @throws SQLException
     */
    @Override
    public List<Track> findAll(Playlist entity) throws SQLException {

        List<Track> trackList = new ArrayList<>();
        String sql = "SELECT * FROM track INNER JOIN trackplaylist ON track.id = trackplaylist.trackId WHERE trackplaylist.playlistName = ?";

        try ( Connection conn = ConnectionPool.getConnection();  PreparedStatement trackPstm = conn.prepareStatement(sql)) {
            trackPstm.setString(1, entity.getName());
            rs = trackPstm.executeQuery();

            //Getting data from tracks from every playlist.
            while (rs.next()) {
                Track track = new Track();
                track.setId(rs.getString(1));
                track.setTitle(rs.getString(2));
                track.setPath(rs.getString(3));
                track.setArtist(rs.getString(4));
                track.setIcon(rs.getString(5));
                track.setObservations(rs.getString(6));
                trackList.add(track);
            }
            //Setting all tracks from every playlist to an ArrayList.
            entity.setTrackList(trackList);
        }
        return trackList;
    }

    /**
     *
     *
     * @param entity
     * @param event
     * @return
     * @throws SQLException
     */
    @Override
    public int update(Track entity, Event event) throws SQLException {
        int result;
        String sql = "UPDATE track SET rating = ? WHERE id = ?";
        try ( Connection conn = ConnectionPool.getConnection();  PreparedStatement pstm = conn.prepareStatement(sql)) {
            pstm.setDouble(1, homeController.getRating());
            pstm.setString(2, entity.getId());
            result = pstm.executeUpdate();
        }
        return result;
    }

    /**
     * 
     *
     * @param entity
     * @param playlistName
     * @return
     * @throws SQLException
     */
    @Override
    public int delete(Track entity, String playlistName) throws SQLException {
        int result;
        String trackPlaylistSql = "DELETE FROM trackplaylist WHERE playlistName = ? AND trackId = ?";
        String countTrackSql = "SELECT COUNT(*) FROM trackplaylist WHERE trackId = ?";
        String deleteTrackSql = "DELETE FROM track WHERE id = ?";

        try ( Connection conn = ConnectionPool.getConnection();  PreparedStatement trackPlaylistPstm = conn.prepareStatement(trackPlaylistSql);  PreparedStatement countTrackPstm = conn.prepareStatement(countTrackSql);  PreparedStatement deleteTrackPstm = conn.prepareStatement(deleteTrackSql)) {
            /* Delete track from playlist */
            trackPlaylistPstm.setString(1, playlistName);
            trackPlaylistPstm.setString(2, entity.getId());
            result = trackPlaylistPstm.executeUpdate();

            /* Check if it's the last register of the track on the database */
            countTrackPstm.setString(1, entity.getId());
            ResultSet countResult = countTrackPstm.executeQuery();
            countResult.next();
            int trackCount = countResult.getInt(1);

            /* If it's the last one, also delete track from track table */
            if (trackCount == 0) {
                deleteTrackPstm.setString(1, entity.getId());
                result += deleteTrackPstm.executeUpdate();
            }
        }

        return result;
    }

    /**
     *
     *
     * @param title
     * @return
     * @throws SQLException
     */
    public Track findByName(String title) throws SQLException {
        Track t = new Track();
        String sql = "SELECT * FROM track WHERE title = ?";

        try ( Connection conn = ConnectionPool.getConnection();  PreparedStatement pstm = conn.prepareStatement(sql)) {
            pstm.setString(1, title);
            rs = pstm.executeQuery();
            while (rs.next()) {
                //Getting data from the playlist with the name to search.
                t.setId(rs.getString(1));
                t.setTitle(rs.getString(2));
                t.setPath(rs.getString(3));
                t.setArtist(rs.getString(4));
                t.setIcon(rs.getString(5));
                t.setObservations(rs.getString(6));
            }
        }
        return t;
    }

    /**
     *
     *
     * @param t
     * @return
     * @throws SQLException
     */
    public Track findById(Track t) throws SQLException {
        String sql = "SELECT * FROM track WHERE id = ?";
        Track resultTrack = null;

        try ( Connection conn = ConnectionPool.getConnection();  PreparedStatement pstm = conn.prepareStatement(sql)) {
            pstm.setString(1, t.getId());
            rs = pstm.executeQuery();
            while (rs.next()) {
                // Creating a new Track object
                resultTrack = new Track();
                resultTrack.setId(rs.getString(1));
                resultTrack.setTitle(rs.getString(2));
                resultTrack.setPath(rs.getString(3));
                resultTrack.setArtist(rs.getString(4));
                resultTrack.setIcon(rs.getString(5));
                resultTrack.setObservations(rs.getString(6));
            }
        }
        return resultTrack;
    }

    /**
     *
     *
     * @param trackId
     * @param playlistName
     * @return
     * @throws SQLException
     */
    public int assignTrackToPlaylist(String trackId, String playlistName) throws SQLException {
        int result;
        String sql = "INSERT INTO trackplaylist (trackId, playlistName) VALUES (?,?)";

        try ( Connection conn = ConnectionPool.getConnection();  PreparedStatement pstm = conn.prepareStatement(sql)) {
            pstm.setString(1, trackId);
            pstm.setString(2, playlistName);
            result = pstm.executeUpdate();
        }
        return result;
    }
}
