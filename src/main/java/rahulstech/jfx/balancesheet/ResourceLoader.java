package rahulstech.jfx.balancesheet;

import javafx.fxml.FXMLLoader;
import javafx.scene.image.Image;

import java.util.Objects;

@SuppressWarnings({"CallToPrintStackTrace","unused"})
public class ResourceLoader {

    private static final String FXML_PREFIX = "/layouts/";
    private static final String IMAGE_PREFIX = "/images/";
    private static final String CSS_PREFIX = "/styles/";

    // Method to load FXML files
    public static FXMLLoader loadFXML(String fileName) {
        try {
            return new FXMLLoader(ResourceLoader.class.getResource(FXML_PREFIX + fileName));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Method to load images
    public static Image loadImage(String fileName) {
        try {
            return new Image(Objects.requireNonNull(ResourceLoader.class.getResourceAsStream(IMAGE_PREFIX + fileName)));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getCssFileExternalForm(String fileName) {
        try {
            return Objects.requireNonNull(ResourceLoader.class.getResource(CSS_PREFIX + fileName)).toExternalForm();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
