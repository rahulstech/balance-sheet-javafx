package rahulstech.jfx.balancesheet.controller;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
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

    private ObjectProperty<Stage> windowProperty = new SimpleObjectProperty<>();

    private ObjectProperty<Scene> sceneProperty = new SimpleObjectProperty<>();

    private ObjectProperty<Parent> rootProperty = new SimpleObjectProperty<>();

    private ResourceBundle resource;

    public ObjectProperty<Parent> rootProperty() {
        return rootProperty;
    }

    public Parent getRoot() {
        return rootProperty.getValue();
    }

    public void setRoot(Parent root) {
        rootProperty.setValue(root);
    }

    public ObjectProperty<Stage> stageProperty() {
        return windowProperty;
    }

    public Stage getWindow() {
        return windowProperty.getValue();
    }

    public void setWindow(Stage window) {
        windowProperty.setValue(window);
    }

    public ObjectProperty<Scene> sceneProperty() {
        return sceneProperty;
    }

    public Scene getScene() {
        return sceneProperty.getValue();
    }

    public void setScene(Scene scene) {
        sceneProperty.setValue(scene);
    }

    public ResourceBundle getResource() {
        return resource;
    }

    @Override
    public final void initialize(URL url, ResourceBundle resourceBundle) {
        this.resource = resourceBundle;
        onInitialize(resourceBundle);
    }

    public ViewLoader getViewLoader() {
        ViewLoader loader = new ViewLoader();
        loader.setWindow(getWindow());
        loader.setScene(getScene());
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
