package playwave.dao.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javafx.event.Event;
import playwave.PlaylistController;
import playwave.PlaylistManagementController;
import playwave.dao.database.pool.ConnectionPool;
import playwave.entities.Playlist;
import playwave.entities.Track;

public class PlaylistDao implements Dao<Playlist, String> {

    /* Singleton pattern */
    private static PlaylistDao instance;
    private ResultSet rs = null;

    private PlaylistManagementController playlistManagementController;

    private PlaylistController playlistController;

    static {
        instance = new PlaylistDao();
    }

    private PlaylistDao() {
    }

    public static PlaylistDao getInstance() {
        return instance;
    }

    /**
     *
     *
     * @param entity
     * @return
     * @throws SQLException
     */
    @Override
    public int create(Playlist entity) throws SQLException {
        int result;
        String sql = "INSERT INTO playlist (name, creationDate, icon) VALUES (?, NOW(), 'playwave/images/brand.png');";
        //Creating a Playlist with its name, creation date and the default icon.
        try ( Connection conn = ConnectionPool.getConnection();  PreparedStatement pstm = conn.prepareStatement(sql)) {
            pstm.setString(1, entity.getName());
            result = pstm.executeUpdate();
        }
        return result;
    }

    /**
     *
     *
     * @param playlist
     * @return
     * @throws SQLException
     */
    @Override
    public List<Playlist> findAll(Playlist playlist) throws SQLException {
        List<Playlist> playlists = new ArrayList();
        String playlistQuery = "SELECT * FROM playlist";

        try ( Connection conn = ConnectionPool.getConnection();  PreparedStatement playlistPstm = conn.prepareStatement(playlistQuery)) {
            rs = playlistPstm.executeQuery(playlistQuery);
            while (rs.next()) {
                //Getting data from all playlists found.
                Playlist p = new Playlist();
                p.setName(rs.getString(1));
                if (rs.getDate(2) != null) {
                    p.setCreationDate(rs.getDate(2).toLocalDate());
                }
                p.setObservations(rs.getString(3));
                p.setIcon(rs.getString(4));
                //Setting all tracks from every playlist to an ArrayList.
                p.setTrackList(TrackDao.getInstance().findAll(p));
                playlists.add(p);
            }
        }
        return playlists;
    }

    /**
     *
     *
     * @param playlist
     * @param event
     * @return
     * @throws SQLException
     */
    @Override
    public int update(Playlist playlist, Event event) throws SQLException {
        int result = 0;
        playlistController = new PlaylistController();
        if (event.getSource() == playlistController.getEditPlaylistImageButton()) {
            String sql = "UPDATE playlist SET icon = ? WHERE name = ?";
            try ( Connection conn = ConnectionPool.getConnection();  PreparedStatement pstm = conn.prepareStatement(sql)) {
                pstm.setString(1, playlistController.getPlaylistImage().toString());
                pstm.setString(2, playlist.getName());
                result = pstm.executeUpdate();
            }
        }

        if (event.getSource() == playlistController.getEditPlaylistNameButton()) {
            String sql = "UPDATE playlist SET name = ? WHERE name = ?";
            try ( Connection conn = ConnectionPool.getConnection();  PreparedStatement pstm = conn.prepareStatement(sql)) {
                pstm.setString(1, playlist.getName());
                pstm.setString(2, "");
                result = pstm.executeUpdate();
            }
        }

        if (event.getSource() == playlistController.getEditPlaylistObservations()) {
            String sql = "UPDATE playlist SET observations = ? WHERE name = ?";
            try ( Connection conn = ConnectionPool.getConnection();  PreparedStatement pstm = conn.prepareStatement(sql)) {
                pstm.setString(1, playlist.getObservations());
                pstm.setString(2, playlist.getName());
                result = pstm.executeUpdate();
            }
        }
        return result;
    }

    /**
     *
     *
     * @param entity
     * @param K
     * @return
     * @throws SQLException
     */
    @Override
    public int delete(Playlist entity, String K) throws SQLException {

        int result;
        String sql = "DELETE FROM playlist WHERE name = ?";
        try ( Connection conn = ConnectionPool.getConnection();  PreparedStatement pstm = conn.prepareStatement(sql)) {
            pstm.setString(1, entity.getName());
            result = pstm.executeUpdate();
        }

        return result;
    }

    /**
     *
     *
     * @param entity
     * @param newName
     * @return
     * @throws SQLException
     */
    public int updateName(Playlist entity, String newName) throws SQLException {
        int result;
        //The generic sql query and the different options to modify an existing playlist
        String sql = "UPDATE playlist SET name = ? WHERE name = ?";
        try ( Connection conn = ConnectionPool.getConnection();  PreparedStatement pstm = conn.prepareStatement(sql)) {
            pstm.setString(1, newName);
            pstm.setString(2, entity.getName());
            result = pstm.executeUpdate();
        }

        String urtsql = "UPDATE trackplaylist SET playlistName = ? WHERE playlistName = ?"; //urtsql = Update relational table sql
        try ( Connection conn = ConnectionPool.getConnection();  PreparedStatement upstm = conn.prepareStatement(urtsql)) {
            upstm.setString(1, newName);
            upstm.setString(2, entity.getName());
            upstm.executeUpdate();
        }
        return result;
    }

    /**
     *
     *
     * @param name
     * @return
     * @throws SQLException
     */
    public Playlist findByName(String name) throws SQLException {
        Playlist p = new Playlist();
        List<Track> trackList;
        String sql = "SELECT * FROM playlist WHERE name = ?";

        try ( Connection conn = ConnectionPool.getConnection();  PreparedStatement pstm = conn.prepareStatement(sql)) {
            pstm.setString(1, name);
            rs = pstm.executeQuery();
            while (rs.next()) {
                //Getting data from the playlist with the name to search.
                p.setName(rs.getString(1));
                if (rs.getDate(2) != null) {
                    p.setCreationDate(rs.getDate(2).toLocalDate());
                }
                p.setObservations(rs.getString(3));
                p.setIcon(rs.getString(4));
                trackList = TrackDao.getInstance().findAll(p);
                //Setting all tracks from every playlist to an ArrayList.
                p.setTrackList(trackList);
            }
        }
        return p;
    }
}
