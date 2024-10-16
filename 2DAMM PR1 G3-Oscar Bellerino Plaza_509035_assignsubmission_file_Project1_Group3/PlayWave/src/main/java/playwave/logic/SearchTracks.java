package playwave.logic;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import playwave.App;
import playwave.HomeController;
import playwave.TrackFoundController;

public class SearchTracks implements Runnable {

    private final ArrayList<String> mp3FilesPath = new ArrayList<>();
    private final HomeController homeController;
    private final TextField defaultDirectoryTextField;
    private final TextField searchFilesTextField;
    private final VBox tracksFound;
    private final Button playPauseSearchButton;
    private volatile boolean paused = false;
    private final Object pauseLock = new Object();

    public SearchTracks(HomeController homeController, TextField defaultDirectoryTextField, TextField searchFilesTextField, VBox tracksFound, Button playPauseSearchButton) {
        this.homeController = homeController;
        this.defaultDirectoryTextField = defaultDirectoryTextField;
        this.searchFilesTextField = searchFilesTextField;
        this.tracksFound = tracksFound;
        this.playPauseSearchButton = playPauseSearchButton;
    }

    @Override
    public void run() {
        processFiles(Paths.get(defaultDirectoryTextField.getText()), searchFilesTextField.getText());

        Platform.runLater(() -> {
            try {
                showResults();
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }

            homeController.setPlayPauseIcon(playPauseSearchButton, true);
        });
    }

    public void togglePause() {
        paused = !paused;
        homeController.manageStopSearchButton(true);
        if (!paused) {
            homeController.manageStopSearchButton(false);
            synchronized (pauseLock) {
                pauseLock.notify();
            }
        }
    }

    /**
     *
     * @param path
     * @param keyword
     */
    public void processFiles(Path path, String keyword) {
        Stream<Path> directoryContent;
        try {
            directoryContent = Files.list(path);
            directoryContent.forEach(node -> {
                if (paused) {
                    synchronized (pauseLock) {
                        try {
                            pauseLock.wait();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                }

                Path newPath = (Path) node;
                File file = newPath.toFile();
                if (file.isDirectory()) {
                    processFiles(newPath, keyword);
                } else if (isMP3File(file) && matchesSearchPattern(file.getName(), keyword)) {
                    mp3FilesPath.add(node.toString());
                }
            });
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     *
     * @param fileName
     * @param searchPattern
     * @return
     */
    private boolean matchesSearchPattern(String fileName, String searchPattern) {
        int extensionIndex = fileName.lastIndexOf(".");
        String fileNameWithoutExtension = (extensionIndex >= 0) ? fileName.substring(0, extensionIndex) : fileName;

        String regex = searchPattern.replace("*", ".+").replace("?", ".?");
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(fileNameWithoutExtension.toLowerCase());

        return matcher.find();
    }

    /**
     *
     * @param file
     * @return
     */
    private boolean isMP3File(File file) {
        try ( FileInputStream fileInputStream = new FileInputStream(file.getPath())) {
            byte[] header = new byte[3];
            int bytesRead = fileInputStream.read(header);
            return bytesRead == 3 && new String(header, "ISO-8859-1").equals("ID3");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     *
     * @throws IOException
     */
    private void showResults() throws IOException {
        tracksFound.getChildren().clear(); //Removes all the results showed from the last search

        if (mp3FilesPath.isEmpty()) { //Checks if search has returned 0 matches
            Label noTracksFound = new Label("No tracks found");
            noTracksFound.setStyle("-fx-text-fill: #e2e2e2; -fx-font-family: 'Gill Sans MT'; -fx-font-size: 16;");
            tracksFound.getChildren().add(noTracksFound);
        } else {
            for (String mp3FilePath : mp3FilesPath) { //Shows all the matches found
                FXMLLoader trackFoundLoader = new FXMLLoader(App.class.getResource("TrackFound.fxml"));
                Parent trackFound = trackFoundLoader.load();
                TrackFoundController trackFoundController = trackFoundLoader.getController();
                trackFoundController.getTrackTitle().setText(Paths.get(mp3FilePath).getFileName().toString());
                trackFoundController.getTrackPath().setText(mp3FilePath);
                tracksFound.getChildren().add(trackFound);
            }
        }
        mp3FilesPath.clear(); //Removes all the results on the array to prepare for the next search
    }
}
