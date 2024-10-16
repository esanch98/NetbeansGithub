package playwave.entities;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;

public class Playlist {

    private String name;
    private int numberOfTracks;
    private LocalDate creationDate;
    private String observations;
    private String icon;
    private List<Track> trackList;

    // <editor-fold defaultstate="colapsed" desc="Getters and Setters"> 
    public String getName() {
        return this.name;
    }

    public StringProperty nameProperty() {
        StringProperty nameP = new SimpleStringProperty(name);
        return nameP;
    }

    public IntegerProperty getNumberOfTracks() {
        IntegerProperty intProperty = new SimpleIntegerProperty(this.trackList.size());
        return intProperty;
    }

    public LocalDate getCreationDate() {
        return this.creationDate;
    }

    public String getObservations() {
        return this.observations;
    }

    public String getIcon() {
        return this.icon;
    }

    public List<Track> getTrackList() {
        return trackList;
    }

    public Track getTrack(int i) {
        return trackList.get(i);
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setNumberOfTracks() {
        this.numberOfTracks = this.trackList.size();
    }

    public void setCreationDate(LocalDate creationDate) {
        this.creationDate = creationDate;
    }

    public void setObservations(String observations) {
        this.observations = observations;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public void setTrackList(List<Track> trackList) {
        this.trackList = trackList;
    }

    /**
     *
     * @param track
     */
    public void addTrack(Track track) {
        this.trackList.add(track);
    }

    /**
     *
     * @param track
     * @return
     */
    public boolean checkTrackWithinPlaylist(Track track) {

        for (Track t : trackList) {
            if (t.getId().equals(track.getId())) {
                return true;
            }
        }

        return false;
    }

    public Playlist() {
        this.trackList = FXCollections.observableArrayList();
    }

    public Playlist(String name) {
        this.name = name;
        this.creationDate = LocalDate.now();
        this.trackList = FXCollections.observableArrayList();
    }

    public Playlist(String name, String observations, String icon) {
        this(name);
        this.observations = observations;
        this.icon = icon;
    }

    /**
     *
     * @return
     */
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 89 * hash + Objects.hashCode(this.name);
        return hash;
    }

    /**
     *
     * @param obj
     * @return
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Playlist other = (Playlist) obj;
        return Objects.equals(this.name, other.name);
    }
}
