package photos;

import java.io.Serializable;

public class Photo implements Serializable {
    private String filePath;

    public Photo(String filePath) {
        this.filePath = filePath;
    }

    public String getFilePath() {
        return filePath;
    }
}