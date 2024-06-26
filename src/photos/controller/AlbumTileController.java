package photos.controller;

import static photos.controller.Utils.CURRENT_USER;

import java.io.IOException;

import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import photos.model.Album;
import photos.model.Photo;
import javafx.beans.value.ChangeListener;


/**
 * This class controls the album tile.
 * Allows the user to delete, rename, and open the album.
 * It also displays the album's thumbnail, name, number of photos, and earliest and latest dates.
 * It is used in the User Scene to display the user's albums.
 * @author Maxim Vyshnevsky and Jorge Pinzon
 */
public class AlbumTileController {
    @FXML
    private ImageView albumThumbnail; // The thumbnail for the album
    @FXML
    private Label albumName; // The label for the album name
    @FXML
    private Label numberPhotos; // The label for the number of photos in the album
    @FXML
    private Label earlyDate; // The label for the earliest date in the album
    @FXML
    private Label lateDate; // The label for the latest date in the album
    @FXML
    private TextField renameField; // The text field to rename the album

    private Album album; // The album this tile represents
    private Node albumTileNode; // The node for the album tile
    private ScrollPane scrollPane; // The scroll pane for the album tile

    /**
     * Initializes the album tile with the album's information.
     * Sets the album's thumbnail, name, number of photos, earliest date, and latest date.
     * @param album
     * @param albumTileNode
     * @param scrollPane
     */
    public void initialize(Album album, Node albumTileNode, ScrollPane scrollPane) {
        this.album = album;
        this.albumTileNode = albumTileNode;
        this.scrollPane = scrollPane;
        setAlbumName(album.getName());
        setNumberPhotos(album.getPhotos().size());
        setEarlyDate(album.getEarlyDate());
        setLateDate(album.getLateDate());

        // If the album has photos, set the thumbnail to the first photo, otherwise it is a default image
        if (album.getPhotos().size() > 0) {
            setAlbumThumbnail(album.getPhotos().get(0).getURIPath());
        }
    }

    /**
     * Sets the album thumbnail to the image at the given path.
     * @param path
     */
    public void setAlbumThumbnail(String path) {
        albumThumbnail.setImage(new Image(path));
    }

    /**
     * Sets the album name to the given name.
     * @param albumName
     */
    public void setAlbumName(String albumName) {
        this.albumName.setText(albumName);
    }

    /**
     * Sets the number of photos in the album to the given number.
     * @param numberPhotos
     */
    public void setNumberPhotos(int numberPhotos) {
        this.numberPhotos.setText(Integer.toString(numberPhotos));
    }

    /**
     * Sets the earliest date in the album to the given date.
     * @param earlyDate
     */
    public void setEarlyDate(String earlyDate) {
        this.earlyDate.setText(earlyDate);
    }

    /**
     * Sets the latest date in the album to the given date.
     * @param lateDate
     */
    public void setLateDate(String lateDate) {
        this.lateDate.setText(lateDate);
    }

    /**
     * Deletes the album from the user.
     * Removes all the photos from the album.
     * If the photos are not in any other album, removes them from the user.
     * Removes the album tile from the TilePane.
     * Changes the scrollbar position to the same position before deleting the album.
     */
    public void deleteAlbum() {
        // Remove all the photos from the album
        // If they are not in any other album, remove them from the user
        for (Photo photo : album.getPhotos()) {
            photo.removeAlbum(album);
            if (photo.getAlbums().isEmpty()) {
                CURRENT_USER.removePhotoFromUser(photo);
            }
        }
        // Remove the album from the user
        CURRENT_USER.getAlbums().remove(album);
        Utils.saveUsers();

        double oldScrollPos = scrollPane.getVvalue();

        // Remove the album tile from the TilePane
        ((Pane) albumTileNode.getParent()).getChildren().remove(albumTileNode);

        /**
         * ChangeListener for the scrollbar position
         * Whenever deleting an album javafx changes the scrollbar position to the top
         * which is annoying
         * This listener will keep the scrollbar position at the same place
         */
        ChangeListener<Number> listener = new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                if (scrollPane.heightProperty().get() != 0) {
                    scrollPane.setVvalue(oldScrollPos);
                }
                scrollPane.vvalueProperty().removeListener(this);
            }
        };

        scrollPane.vvalueProperty().addListener(listener);
    }

    /**
     * Renames the album to the text in the rename field.
     * Checks if an album with the same name already exists.
     * If it does, displays a warning dialog.
     * Otherwise, sets the new name for the album and saves the users.
     */
    public void renameAlbum() {
        String newAlbumNameText = renameField.getText();

        // Check if an album with the same name already exists
        if (Utils.USERS.stream()
            .flatMap(user -> user.getAlbums().stream())
            .anyMatch(album -> album.getName().equals(newAlbumNameText))) {

            Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle("Warning Dialog");
            alert.setHeaderText(null);
            alert.setContentText("An album with this name already exists.");

            alert.showAndWait();
            return;
        }
        // Get the new name from the text field and set it as the new name for the album
        album.setName(renameField.getText());
        Utils.saveUsers();
        albumName.setText(album.getName());
    }

    /**
     * Opens the album in the Album Scene.
     */
    public void openAlbum() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/photos/view/AlbumScene.fxml"));
            Parent root = loader.load();
            Scene albumScene = new Scene(root);

            Stage stage = (Stage) albumName.getScene().getWindow();

            AlbumSceneController albumSceneController = loader.getController();
            albumSceneController.initialize(album);

            stage.setScene(albumScene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
