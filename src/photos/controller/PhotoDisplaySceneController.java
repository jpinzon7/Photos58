package photos.controller;

import static photos.controller.Utils.convertIntDateToString;

import java.io.IOException;
import java.util.List;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.TilePane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import photos.model.Album;
import photos.model.Photo;
import photos.model.Tag;

/**
 * Controller for the photo display scene.
 * @author Maxim Vyshnevsky and Jorge Pinzon
 */
public class PhotoDisplaySceneController {
    @FXML
    ImageView photoView; // The image view for the photo
    @FXML
    Button leftButton; // The button to go to the previous photo
    @FXML
    Button rightButton; // The button to go to the next photo
    @FXML
    Label dateLabel; // The label for the date the photo was taken
    @FXML
    Label captionLabel; // The label for the caption of the photo
    @FXML
    ScrollPane tagScrollPane; // The scroll pane for the tags
    @FXML
    TilePane tagTilePane; // The tile pane for the tags
    @FXML
    AnchorPane anchorPane; // The anchor pane for the scene

    private Album album;
    private int photoIndex;
    private Photo photo;

    private Stage popupStage; // The stage for the pop-up windows

    
    /** 
     * @param album
     * @param photoIndex
     */
    // Runs after the user clicks on a photo
    public void initialize(Album album, int photoIndex) {
        this.album = album;
        this.photoIndex = photoIndex;
        checkLeftButtonDisable(photoIndex);
        checkRightButtonDisable(photoIndex);
        setupPicture();
    }

    // Runs during the initialization of the scene
    /**
     * Shows the tags for the photo.
     * For each tag, a TagTile is created and added to the tile pane.
     */
    public void showTags() {
        List<Tag> tags = photo.getTags();
        for (Tag tag : tags) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/photos/view/TagTile.fxml"));
                Node tagNode = loader.load();
                TagTileController controller = loader.getController();
                controller.initialize(tag.getTagName(), tag.getTagValue(), photo, tagNode, tagScrollPane);
                tagTilePane.getChildren().add(tagNode);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Runs when the user initializes the scene or goes to the next or previous photo
    /**
     * Sets up the picture.
     * Displays the photo, and the date, caption, and tags for the photo.
     */
    public void setupPicture() {
        photo = album.getPhotos().get(photoIndex);
        photoView.setImage(new Image(photo.getURIPath()));
        dateLabel.setText("Date: " + convertIntDateToString(photo.getDateTaken()));
        captionLabel.setText(photo.getCaption());
        tagTilePane.getChildren().clear();
        showTags();
    }

    // Runs when the user clicks on the next photo button
    // Increases the photo index and shows the picture at that index, checks if the right button should be disabled
    /**
     * Goes to the next picture in the album.
     * Increases the photo index and shows the picture at that index.
     * Checks if the right button should be disabled.
     */
    public void nextPicture() {
        photoIndex++;
        leftButton.setDisable(false);
        checkRightButtonDisable(photoIndex);
        setupPicture();
    }

    // Runs when the user clicks on the previous photo button
    // Decreases the photo index and shows the picture at that index, checks if the left button should be disabled
    /**
     * Goes to the previous picture in the album.
     * Decreases the photo index and shows the picture at that index.
     * Checks if the left button should be disabled.
     */
    public void previousPicture() {
        photoIndex--;
        rightButton.setDisable(false);
        checkLeftButtonDisable(photoIndex);
        setupPicture();
    }

    
    /** 
     * Checks if the first photo in the album is being displayed.
     * If it is, disables the previous photo button.
     * @param photoIndex
     */
    public void checkLeftButtonDisable(int photoIndex) {
        if (photoIndex == 0) {
            leftButton.setDisable(true);
        }
    }

    /**
     * Checks if the last photo in the album is being displayed.
     * If it is, disables the next photo button.
     * @param photoIndex
     */
    public void checkRightButtonDisable(int photoIndex) {
        if (photoIndex == album.getPhotos().size() - 1) {
            rightButton.setDisable(true);
        }
    }

    // Runs when the user clicks on the new caption button
    /**
     * Opens a pop-up window to enter a new caption for the photo.
     * Updates the caption label with the new caption.
     */
    public void newCaption() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/photos/view/NewCaptionScene.fxml"));
            popupStage = new Stage();

            Parent root = loader.load();

            NewCaptionSceneController controller = loader.getController();
            controller.initialize(photo);

            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.initStyle(StageStyle.UNDECORATED);
            popupStage.setScene(new Scene(root));
            popupStage.showAndWait();

            captionLabel.setText(photo.getCaption());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Runs when the user clicks on the new tag button
    /**
     * Opens a pop-up window to enter a new tag for the photo.
     * Updates the tag tile pane with the new tag.
     */
    public void newTag() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/photos/view/NewTagScene.fxml"));
            popupStage = new Stage();

            Parent root = loader.load();

            NewTagSceneController controller = loader.getController();
            controller.initialize(photo);

            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.initStyle(StageStyle.UNDECORATED);
            popupStage.setScene(new Scene(root));
            popupStage.showAndWait();

            tagTilePane.getChildren().clear();
            showTags();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Runs when the user clicks on the move/copy button
    /**
     * Opens a pop-up window to move or copy the photo to another album.
     * Updates the album with the new photo.
     */
    public void moveCopy() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/photos/view/MoveCopyScene.fxml"));
            popupStage = new Stage();

            Parent root = loader.load();

            MoveCopySceneController controller = loader.getController();
            controller.initialize(photo, album, anchorPane);

            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.initStyle(StageStyle.UNDECORATED);
            popupStage.setScene(new Scene(root));
            popupStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Runs when the user clicks on the Go Back button
    /**
     * Goes back to the album scene.
     */
    public void goBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/photos/view/AlbumScene.fxml"));
            Parent root = loader.load();
            AlbumSceneController controller = loader.getController();
            controller.initialize(album);
            Scene scene = new Scene(root);
            Stage stage = (Stage) photoView.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Runs when the user clicks on the Exit button, exits the program
    /**
     * Exits the program.
     */
    public void exitProgram() {
        System.exit(0);
    }
}
