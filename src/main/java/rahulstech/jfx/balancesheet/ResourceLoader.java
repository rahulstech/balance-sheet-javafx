package rahulstech.jfx.balancesheet;

import javafx.fxml.FXMLLoader;
import javafx.scene.image.Image;

import java.io.IOException;

public class ResourceLoader {

    private static final String FXML_PREFIX = "/layouts/";
    private static final String IMAGE_PREFIX = "/images/";
    private static final String CSS_PREFIX = "/styles/";


    // Method to load FXML files
    public static FXMLLoader loadFXML(String fileName) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(ResourceLoader.class.getResource(FXML_PREFIX + fileName));
            return fxmlLoader;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Method to load images
    public static Image loadImage(String fileName) {
        try {
            Image image = new Image(ResourceLoader.class.getResourceAsStream(IMAGE_PREFIX + fileName));
            return image;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getCssFileExternalForm(String fileName) {
        try {
            return ResourceLoader.class.getResource(CSS_PREFIX+fileName).toExternalForm();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
