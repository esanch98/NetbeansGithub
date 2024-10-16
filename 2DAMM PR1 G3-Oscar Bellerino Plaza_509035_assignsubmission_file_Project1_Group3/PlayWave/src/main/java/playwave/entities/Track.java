package playwave.entities;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Track {
    private String id;
    private String title;
    private String path;
    private String artist;
    private String icon;
    private String observations;
    private double rating;
    
    public String getTitle() {
        return this.title;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPath() {
        return this.path;
    }

    public String getArtist() {
        return this.artist;
    }

    public String getIcon() {
        return this.icon;
    }

    public String getObservations() {
        return this.observations;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public void setObservations(String observations) {
        this.observations = observations;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public Track() {
    }
    
    public Track(String title, String path, String artist, String icon, String observations) {
        this.id = getChecksumString(path);
        this.title = title;
        this.path = path;
        this.artist = artist;
        this.icon = icon;
        this.observations = observations;
    }
    
    private byte[] getChecksum(String trackPath) {
        try {
            InputStream input = new FileInputStream(trackPath);

            byte[] buffer = new byte[1024];
            MessageDigest complete = MessageDigest.getInstance("MD5");
            int numRead;
            /* Read data */
            do {
                numRead = input.read(buffer);
                if (numRead > 0) {
                    complete.update(buffer, 0, numRead);
                }
            } while (numRead != -1);

            input.close();
            /* Return bytes */
            return complete.digest();
        } catch (NoSuchAlgorithmException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public String getChecksumString(String trackPath) {
        /* Convert bytes into string */
        byte[] b = getChecksum(trackPath);
        StringBuilder resultado = new StringBuilder();

        for (byte unByte : b) {
            resultado.append(Integer.toString((unByte & 0xff) + 0x100, 16).substring(1));
        }
        return resultado.toString();
    }
}
