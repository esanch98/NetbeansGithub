package playwave.dao.database;

import java.sql.SQLException;
import java.util.List;
import javafx.event.Event;
import playwave.entities.Playlist;

/**
 * 
 * @author Oscar
 * @param <T>
 * @param <K> 
 */
public interface Dao<T, K> {
    int create(T entity) throws SQLException;
    List<T> findAll(Playlist p) throws SQLException;
    int update(T entity, Event K) throws SQLException;
    int delete(T entity, K String) throws SQLException;
}
