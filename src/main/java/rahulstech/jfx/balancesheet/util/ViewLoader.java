package rahulstech.jfx.balancesheet.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import rahulstech.jfx.balancesheet.ResourceLoader;
import rahulstech.jfx.balancesheet.controller.Controller;

public class ViewLoader {

    private Stage window;
    private Scene scene;
    private String fxml;
    private Parent root;
    private Controller controller;

    public ViewLoader() {}

    public ViewLoader load() {
        try {
            FXMLLoader loader = ResourceLoader.loadFXML(fxml);
            if (this.controller != null) {
                loader.setController(this.controller);
            }
            Parent root = loader.load();
            Controller controller = loader.getController();

            controller.setRoot(root);
            controller.setWindow(window);
            controller.setScene(scene);

            this.root = root;
            this.controller = controller;
        }
        catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        return this;
    }

    public String getFxml() {
        return fxml;
    }

    public Parent getRoot() {
        return root;
    }

    @SuppressWarnings("unchecked")
    public <T extends Controller> T getController() {
        return (T) controller;
    }

    public ViewLoader setWindow(Stage window) {
        this.window = window;
        if (null != controller) {
            controller.setWindow(window);
        }
        return this;
    }

    public ViewLoader setScene(Scene scene) {
        this.scene = scene;
        if (null != controller) {
            controller.setScene(scene);
        }
        return this;
    }

    public ViewLoader setFxml(String fxml) {
        this.fxml = fxml;
        return this;
    }

    public ViewLoader setController(Controller controller) {
        this.controller = controller;
        return this;
    }
}
