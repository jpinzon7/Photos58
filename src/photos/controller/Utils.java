package photos.controller;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import photos.model.Album;
import photos.model.User;

/**
 * A utility class that contains static fields and methods used throughout the application.
 * Some of the variables are used to avoid repeating the same data search operations.
 * @author Maxim Vyshnevsky and Jorge Pinzon
 */
public class Utils {
    static final File DATA_FILE = new File("data/users.dat"); // The file to store the users
    static List<User> USERS = new ArrayList<>(); // The list of users
    static User CURRENT_USER; // The current user
    static List<Album> CURRENT_ALBUMS; // The current user's albums

    // Save the list of users to the data file
    /**
     * Load the list of users to the data file.
     */
    static void saveUsers() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            oos.writeObject(USERS);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Convert a date obtained from Calendar to a string
    /**
     * Convert an integer date to a string.
     * The integer date is obtained from the Calendar class of the Java Standard Library.
     * The integer date is in the format YYYYMMDD.
     * The string date is in the format MM/DD/YYYY.
     * @param date The date to convert
     * @return The date as a string
     */
    public static String convertIntDateToString(int date) {
        String dateStr = Integer.toString(date);
        String year = dateStr.substring(0, 4);
        String month = dateStr.substring(4, 6);
        String day = dateStr.substring(6, 8);
        String formattedDate = month + "/" + day + "/" + year;
        return formattedDate;
    }
}
