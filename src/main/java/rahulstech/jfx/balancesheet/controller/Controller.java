package rahulstech.jfx.balancesheet.controller;

import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import rahulstech.jfx.balancesheet.BalancesheetApp;
import rahulstech.jfx.balancesheet.util.ViewLauncher;
import rahulstech.jfx.balancesheet.util.ViewLoader;

import java.net.URL;
import java.util.ResourceBundle;

@SuppressWarnings("ALL")
public class Controller implements Initializable {

    private Stage window;

    private Scene scene;

    public Parent getRoot() {
        return root;
    }

    public void setRoot(Parent root) {
        this.root = root;
    }

    public Stage getWindow() {
        return window;
    }

    public void setWindow(Stage window) {
        this.window = window;
    }

    public Scene getScene() {
        return scene;
    }

    public void setScene(Scene scene) {
        this.scene = scene;
    }

    public ResourceBundle getResource() {
        return resource;
    }

    private Parent root;

    private ResourceBundle resource;

    @Override
    public final void initialize(URL url, ResourceBundle resourceBundle) {
        this.resource = resourceBundle;
        onInitialize(resourceBundle);
    }

    public ViewLoader getViewLoader() {
        ViewLoader loader = new ViewLoader();
        loader.setWindow(window);
        loader.setScene(scene);
        return loader;
    }

    public ViewLauncher.Builder getViewLauncherBuilder() {
        ViewLauncher.Builder builder = new ViewLauncher.Builder();
        builder.setViewLoader(getViewLoader());
        builder.setOwnerWindow(getWindow());
        return builder;
    }

    protected void onInitialize(ResourceBundle res) {}

    public BalancesheetApp getApp() {
        return BalancesheetApp.getInstance();
    }
}
