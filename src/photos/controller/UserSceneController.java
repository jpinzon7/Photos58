package photos.controller;

import static photos.controller.Utils.DATA_FILE;
import static photos.controller.Utils.USERS;
import static photos.controller.Utils.saveUsers;
import static photos.controller.Utils.CURRENT_USER;
import static photos.controller.Utils.CURRENT_ALBUMS;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.TilePane;
import javafx.stage.Stage;
import photos.model.Album;
import photos.model.Photo;
import photos.model.User;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;

/**
 * The controller class for the User Scene.
 * @author Maxim Vyshnevsky and Jorge Pinzon
 */
public class UserSceneController {
    @FXML
    private Label userLabel; // The label at the top of the page
    @FXML
    private TilePane albumPane; // The TilePane to display the albums
    @FXML
    private Label albumName; // The label for the album name
    @FXML
    private TextField newAlbumName; // The text field for the new album name
    @FXML
    private ScrollPane scrollPane; // The scroll pane for the TilePane
    @FXML
    private TextField tagName1Field;
    @FXML
    private TextField tagValue1Field;
    @FXML
    private TextField tagName2Field;
    @FXML
    private TextField tagValue2Field;
    @FXML
    private ChoiceBox<String> operatorChoiceBox;
    @FXML
    private Button searchButton;
    @FXML
    private DatePicker startDatePicker;
    @FXML
    private DatePicker endDatePicker;
    @FXML
    private Button dateSearchButton;

    

    // Runs after the user logs in
    // If the user has already logged in and just going back to this page, do not
    // run
    /** 
     * Initializes the User Scene.
     * Sets the user label, initializes the operator choice box, and disables the second tag fields.
     * If the data file exists, reads the list of users from it and stores it in the users list.
     * If the user is not in the list of users, adds them.
     * Updates the current user and current user's albums.
     * Loads the albums into the TilePane.
     * @param username
     */
    public void initialize(String username) {
        userLabel.setText("Welcome, " + username + "!"); // Top of the page greeting
        operatorChoiceBox.getItems().addAll("AND", "OR", "");
        operatorChoiceBox.setValue("");
        tagName2Field.setDisable(true);
        tagValue2Field.setDisable(true);

        // If the file exists and is not empty, read the list of users from it and store
        // it in the users list
        if (DATA_FILE.exists() && DATA_FILE.length() > 0) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(DATA_FILE))) {
                @SuppressWarnings("unchecked")
                List<User> readUsers = (List<User>) ois.readObject();
                USERS = readUsers;
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        // If the user is not in the list of users, add them
        if (USERS.stream().noneMatch(user -> user.getUsername().equals(username))) {
            System.out.println("Adding new user...");
            USERS.add(new User(username));
            // Write the user to the data file
            saveUsers();
        }

        // Print the list of users names
        System.out.println("Stored users: {");
        for (User user : USERS) {
            System.out.println(user.getUsername());
        }
        System.out.println("}");

        // Update the current user
        CURRENT_USER = USERS.stream().filter(u -> u.getUsername().equals(username)).findFirst().get();

        // Print the current user's name
        System.out.println("CurrentUser: " + CURRENT_USER.getUsername());

        // Update the current user's albums
        CURRENT_ALBUMS = CURRENT_USER.getAlbums();

        // Print the current user's albums
        System.out.println("User has " + CURRENT_ALBUMS.size() + " albums");
        System.out.println("Albums: {");
        for (Album album : CURRENT_ALBUMS) {
            System.out.println(album.getName());
            // System.out.println(album.getEarlyDate());
            // System.out.println(album.getLateDate());
        }
        System.out.println("}");

        // Load the albums into the TilePane
        for (Album album : CURRENT_ALBUMS) {
            tileMaker(album);
        }

        operatorChoiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if ("AND".equals(newValue) || "OR".equals(newValue)) {
                tagName2Field.setDisable(false);
                tagValue2Field.setDisable(false);
            } else {
                tagName2Field.setDisable(true);
                tagValue2Field.setDisable(true);
            }
        });
    }

    // If user clicks on create album button
    /**
     * Creates a new album.
     * This method is called when the create album button is pressed.
     */
    public void createAlbum() {
        String newAlbumNameText = newAlbumName.getText();

        // Check if an album with the same name already exists and display alert
        if (CURRENT_USER.getAlbums().stream().anyMatch(album -> album.getName().equals(newAlbumNameText))) {
            Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle("Warning Dialog");
            alert.setHeaderText(null);
            alert.setContentText("An album with this name already exists.");
            alert.showAndWait();
            return;
        }

        // Create a new album with the name from the text field
        Album album = new Album(newAlbumName.getText());
        // Add the album to the user's list of albums
        CURRENT_USER.addAlbum(album);

        // Add the album to the TilePane
        tileMaker(album);

        // Update the data file
        saveUsers();
    }

    

    // Create a tile for the album
    // Tiles are used to display the albums in the TilePane
    /** 
     * Creates a tile for the album.
     * This method is called when the user logs in or creates a new album.
     * @param album
     */
    public void tileMaker(Album album) {
        System.out.println("Loading album: " + album.getName());
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/photos/view/AlbumTile.fxml"));
            Node albumTile = loader.load();
            AlbumTileController albumTileController = loader.getController();
            albumTileController.initialize(album, albumTile, scrollPane);
            albumPane.getChildren().add(albumTile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Searches for photos with the tags entered in the text fields.
     * If the operator is AND, searches for photos with both tags.
     * If the operator is OR, searches for photos with either tag.
     * If the operator is empty, searches for photos with the first tag.
     */
    @FXML
    public void searchPhotos() {
        String tagType1 = tagName1Field.getText();
        String tagValue1 = tagValue1Field.getText();
        String tagType2 = tagName2Field.getText();
        String tagValue2 = tagValue2Field.getText();
        String operator = operatorChoiceBox.getValue();
        HashMap<String, Photo> photosMap = CURRENT_USER.getPhotosMap();

        List<Photo> matchingPhotos = new ArrayList<>();
        for (Photo photo : photosMap.values()) {
            boolean matchesTag1 = photo.hasTag(tagType1, tagValue1);
            boolean matchesTag2 = photo.hasTag(tagType2, tagValue2);

            if (operator.equals("") && matchesTag1) {
                // Disable tagName2Field and tagValue2Field
                matchingPhotos.add(photo);
            }

            if ((operator.equals("AND") && matchesTag1 && matchesTag2) ||
                    (operator.equals("OR") && (matchesTag1 || matchesTag2))) {
                matchingPhotos.add(photo);
            }
        }
        if (matchingPhotos.isEmpty()) {
            Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle("Warning Dialog");
            alert.setHeaderText(null);
            alert.setContentText("No search results found.");
            alert.showAndWait();
        } else {
            switchToSearchScene(matchingPhotos);
        }
        
    }

    /**
     * Searches for photos with the dates entered in the date pickers.
     */
    @FXML
    public void searchPhotosByDate() {
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();
        HashMap<String, Photo> photosMap = CURRENT_USER.getPhotosMap();

        List<Photo> matchingPhotos = new ArrayList<>();
        for (Photo photo : photosMap.values()) {
            int dateTaken = photo.getDateTaken();
            int year = dateTaken / 10000;
            int month = (dateTaken % 10000) / 100;
            int day = dateTaken % 100;
            LocalDate photoDate = LocalDate.of(year, month, day);
            if ((photoDate.isAfter(startDate) || photoDate.isEqual(startDate)) &&
                    (photoDate.isBefore(endDate) || photoDate.isEqual(endDate))) {
                {
                    matchingPhotos.add(photo);
                }
            }
        }
        if (matchingPhotos.isEmpty()) {
            Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle("Warning Dialog");
            alert.setHeaderText(null);
            alert.setContentText("No search results found.");
            alert.showAndWait();
        } else {
            switchToSearchScene(matchingPhotos);
        }


    }

    /**
     * Switches to the search scene.
     * This method is called when the search button is pressed.
     * @param searchResults
     */
    private void switchToSearchScene(List<Photo> searchResults) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/photos/view/SearchScene.fxml"));
            Parent root = loader.load();

            // Get the SearchSceneController and pass the search results to it
            SearchSceneController controller = loader.getController();
            controller.setSearchResults(searchResults);

            // Switch the scene
            Stage stage = (Stage) searchButton.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // If user clicks on logout button
    // Go back to the login scene
    /**
     * Logs out the user.
     * This method is called when the logout button is pressed.
     */
    public void logout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/photos/view/LoginScene.fxml"));
            Parent root = loader.load();
            Scene loginScene = new Scene(root);

            Stage stage = (Stage) userLabel.getScene().getWindow();
            stage.setScene(loginScene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // If user clicks on exit button, exit the program
    /**
     * Exits the program.
     * This method is called when the exit button is pressed.
     */
    public void exitProgram() {
        System.exit(0);
    }
}
