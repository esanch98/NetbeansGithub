package playwave;

import java.net.URL;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import playwave.dao.database.TrackDao;
import playwave.entities.Track;

public class VinylDiscController implements Initializable {
    private double rating;
    private HomeController homeController;
    private Track selectedTrack;
    private TrackDao trackDao = TrackDao.getInstance();
    
    @FXML
    private VBox centralPanel;
    @FXML
    private ImageView trackImage;
    @FXML
    private Label trackTitle;
    @FXML
    private Label trackObservations;
    @FXML
    private Button trackRating1;
    @FXML
    private Button trackRating2;
    @FXML
    private Button trackRating3;
    @FXML
    private Button trackRating4;
    @FXML
    private Button trackRating5;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        /* Track image radius application */
        double radius = Math.min(trackImage.getFitWidth(), trackImage.getFitHeight()) / 2;
        Circle trackImageClip = new Circle(radius, radius, radius);
        trackImage.setClip(trackImageClip);
    }

    public HomeController getHomeController() {
        return homeController;
    }

    public void setHomeController(HomeController homeController) {
        this.homeController = homeController;
    }

    /***
     * 
     * @param rating 
     */
    public void updateRatingFromTrackNavigation(double rating) {
        Image emptyStarIcon = new Image("playwave/icons/emptyStar.png");
        Image fullStarIcon = new Image("playwave/icons/fullStar.png");
        Image halfStarIcon = new Image("playwave/icons/halfStar.png");

        ImageView[] starImageViews = {(ImageView) trackRating1.getGraphic(), (ImageView) trackRating2.getGraphic(), 
            (ImageView) trackRating3.getGraphic(), (ImageView) trackRating4.getGraphic(), (ImageView) trackRating5.getGraphic()};

        for (ImageView starImageView : starImageViews) {
            if (rating >= 1) {
                starImageView.setImage(fullStarIcon);
                rating -= 1;
            } else if (rating > 0) {
                starImageView.setImage(halfStarIcon);
                rating = 0;
            } else {
                starImageView.setImage(emptyStarIcon);
            }
        }
    }

    /***
     * 
     * @param event 
     */
    @FXML
    private void updateRating(MouseEvent event) {
        selectedTrack = homeController.getSelectedTrack();
        Image emptyStarIcon = new Image("playwave/icons/emptyStar.png");
        Image fullStarIcon = new Image("playwave/icons/fullStar.png");
        Image halfStarIcon = new Image("playwave/icons/halfStar.png");
        ImageView icon = new ImageView();
        List<Button> ratingButtons = Arrays.asList(trackRating1, trackRating2, trackRating3, trackRating4, trackRating5);
        Button button = (Button) event.getSource();

        ImageView eventButtonImageView = (ImageView) button.getGraphic();
        String eventButtonCurrentImageUrl = eventButtonImageView.getImage().getUrl();
        double width = eventButtonImageView.getFitWidth();
        double height = eventButtonImageView.getFitHeight();

        if (selectedTrack != null) {
            /* Update clicked button */
            if (eventButtonCurrentImageUrl.equals(fullStarIcon.getUrl())) {
                icon.setImage(halfStarIcon);
            } else if (eventButtonCurrentImageUrl.equals(halfStarIcon.getUrl())) {
                icon.setImage(emptyStarIcon);
            } else if (eventButtonCurrentImageUrl.equals(emptyStarIcon.getUrl())) {
                icon.setImage(fullStarIcon);
            }

            icon.setFitWidth(width);
            icon.setFitHeight(height);
            button.setGraphic(icon);

            /* Update next buttons from clicked button */
            int buttonIndex = ratingButtons.indexOf(button);
            for (int i = 0; i < buttonIndex; i++) {
                ImageView previousImageView = (ImageView) ratingButtons.get(i).getGraphic();
                previousImageView.setImage(fullStarIcon);
            }

            /* Update previous buttons from clicked button*/
            for (int i = buttonIndex + 1; i < ratingButtons.size(); i++) {
                ImageView nextImageView = (ImageView) ratingButtons.get(i).getGraphic();
                nextImageView.setImage(emptyStarIcon);
            }

            rating = 0.0;
            for (Button ratingButton : ratingButtons) {
                ImageView ratingButtonImageView = (ImageView) ratingButton.getGraphic();
                String ratingButtonCurrentImage = ratingButtonImageView.getImage().getUrl();
                if (ratingButtonCurrentImage.equals(fullStarIcon.getUrl())) {
                    rating += 1;
                } else if (ratingButtonCurrentImage.equals(halfStarIcon.getUrl())) {
                    rating += 0.5;
                }
            }

            homeController.updateRatingFromTrackNavigation(rating);
            selectedTrack.setRating(rating);
            try {
                trackDao.update(selectedTrack, event);
            } catch (SQLException ex) {
                System.out.println(ex.getMessage());
            }
        } else {
            Tooltip tooltip = new Tooltip("There is no track selected");
            tooltip.show(button, event.getScreenX(), event.getScreenY());
            PauseTransition delay = new PauseTransition(Duration.seconds(3));
            delay.setOnFinished(e -> tooltip.hide());
            delay.play();
        }
    }
}
