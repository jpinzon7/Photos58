package photos.controller;

import static photos.controller.Utils.DATA_FILE;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import photos.model.User;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import static photos.controller.Utils.USERS;
import static photos.controller.Utils.saveUsers;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;

/**
 * This class is the controller for the Admin Scene.
 * It handles all the UI and logic for the admin scene.
 * Allows admin to create and delete users, and shows the list of all current users.
 * @author Maxim Vyshnevsky and Jorge Pinzon
 */

public class AdminSceneController {
    @FXML
    private Label userLabel;
    @FXML
    private ListView<String> userList; // ListView to display the list of users
    @FXML
    private TextField newUserField; // TextField to input the name of a new user


    /**
     * Initializes the admin scene with the username of the admin.
     * Shows the list of all current users.
     * If the file exists and is not empty, reads the list of users from it and stores it in the users list.
     * @param username
     */
    public void initialize(String username) {
        userLabel.setText("Welcome, " + username + "!");
        userList.getItems().clear();
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
        loadUsers();
    }

    /**
     * Saves the list of users to the file.
     * If the file does not exist, it is created.
     */
    private void loadUsers() {
        userList.getItems().clear();
        for (User user : USERS) {
            userList.getItems().add(user.getUsername());
        }
    }

    /**
     * Creates a new user with the username entered in the text field.
     * If a user with the same username already exists, shows a warning dialog.
     * Otherwise, adds the new user to the list of users and saves the list of users to the file.
     */
    public void createUser() {
        String newUsername = newUserField.getText();

        // Check if a user with the same username already exists
        if (USERS.stream().anyMatch(user -> user.getUsername().equals(newUsername))) {
            Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle("Warning Dialog");
            alert.setHeaderText(null);
            alert.setContentText("A user with this username already exists.");

            alert.showAndWait();
            return;
        }

        USERS.add(new User(newUsername));
        saveUsers();
        loadUsers();
    }

    /**
     * Deletes the user selected in the list of users.
     * If no user is selected, does nothing.
     * Otherwise, removes the user from the list of users and saves the list of users to the file.
     */
    public void deleteUser() {
        String selectedUsername = userList.getSelectionModel().getSelectedItem();

        if (selectedUsername != null) {
            USERS.removeIf(user -> user.getUsername().equals(selectedUsername));
            saveUsers();
            loadUsers();
        }
    }

   /**
    * Logs out the admin and goes back to the login scene.
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

    /**
     * Exits the program safely
     */
    public void exitProgram() {
        System.exit(0);
    }
}

