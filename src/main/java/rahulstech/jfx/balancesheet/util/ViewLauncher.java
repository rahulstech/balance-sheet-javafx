package rahulstech.jfx.balancesheet.util;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import rahulstech.jfx.balancesheet.controller.Controller;

public class ViewLauncher {

    private Stage ownerWindow;
    private boolean showInDifferentWindow;
    private Stage window;
    private StageStyle stageStyle;
    private Modality stageModality;
    private String title;
    private Scene scene;
    private String fxml;
    private Parent root;
    private Controller controller;
    private ViewLoader viewLoader;
    private double height;
    private double width;

    private ViewLauncher(Builder builder) {
        this.showInDifferentWindow = builder.showInDifferentWindow;
        this.ownerWindow = builder.ownerWindow;
        this.window = builder.window;
        this.stageStyle = builder.stageStyle;
        this.stageModality = builder.stageModality;
        this.title = builder.title;
        this.scene = builder.scene;
        this.fxml = builder.fxml;
        this.controller = builder.controller;
        this.viewLoader = builder.viewLoader;
        this.height = builder.height;
        this.width = builder.width;
    }

    public ViewLauncher load() {
        ViewLoader viewLoader;
        if (this.viewLoader == null) {
            viewLoader = new ViewLoader();
        }
        else {
            viewLoader = this.viewLoader;
        }
        viewLoader.setFxml(fxml);
        if (this.controller != null) {
            viewLoader.setController(this.controller);
        }
        Controller controller = viewLoader.load().getController();
        Parent root = controller.getRoot();

        Stage ownerWindow = this.ownerWindow;
        Stage window;
        if (showInDifferentWindow) {
            if (null != this.window) {
                window = this.window;
            }
            else {
                window = new Stage();
            }
        }
        else {
            window = ownerWindow;
        }
        if (ownerWindow != window) {
            window.initOwner(ownerWindow);
        }
        if (null != this.stageModality) {
            window.initModality(this.stageModality);
        }
        if (null != this.stageStyle) {
            window.initStyle(this.stageStyle);
        }
        window.setTitle(this.title);

        Scene scene;
        if (null == this.scene) {
            scene = new Scene(root);
        }
        else {
            scene = this.scene;
            scene.setRoot(root);
        }
        window.setScene(scene);
        if (height > 0) {
            window.setHeight(height);
        }
        if (width > 0) {
            window.setWidth(width);
        }
        viewLoader.setWindow(window);
        viewLoader.setScene(scene);

        this.root = root;
        this.controller = controller;
        this.window = window;
        this.scene = scene;
        this.viewLoader = viewLoader;
        return this;
    }

    public static class Builder {
        private String title;
        private Stage ownerWindow;
        private boolean showInDifferentWindow = true;
        private Stage window;
        private Modality stageModality;
        private StageStyle stageStyle;
        private Scene scene;
        private String fxml;
        private Controller controller;
        private ViewLoader viewLoader;
        private double height = -1;
        private double width = -1;

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder setOwnerWindow(Stage ownerWindow) {
            this.ownerWindow = ownerWindow;
            return this;
        }

        public Builder setWindow(Stage window) {
            this.window = window;
            return this;
        }

        public Builder setShowInDifferentWindow(boolean showInDifferentWindow) {
            this.showInDifferentWindow = showInDifferentWindow;
            return this;
        }

        public Builder setStageModality(Modality stageModality) {
            this.stageModality = stageModality;
            return this;
        }

        public Builder setStageStyle(StageStyle stageStyle) {
            this.stageStyle = stageStyle;
            return this;
        }

        public Builder setScene(Scene scene) {
            this.scene = scene;
            return this;
        }

        public Builder setFxml(String fxml) {
            this.fxml = fxml;
            return this;
        }

        public Builder setController(Controller controller) {
            this.controller = controller;
            return this;
        }

        public Builder setViewLoader(ViewLoader viewLoader) {
            this.viewLoader = viewLoader;
            return this;
        }

        public Builder setHeight(double height) {
            this.height = height;
            return this;
        }

        public Builder setWidth(double width) {
            this.width = width;
            return this;
        }

        public ViewLauncher build() {
            return new ViewLauncher(this);
        }
    }

    // Getters for the ViewLoader attributes
    public String getTitle() {
        return title;
    }

    public Stage getOwnerWindow() {
        return ownerWindow;
    }

    public Stage getWindow() {
        return window;
    }

    public boolean isShowInDifferentWindow() {
        return showInDifferentWindow;
    }

    public Modality getStageModality() {
        return stageModality;
    }

    public StageStyle getStageStyle() {
        return stageStyle;
    }

    public Scene getScene() {
        return scene;
    }

    public String getFxml() {
        return fxml;
    }

    public Parent getRoot() {
        return root;
    }

    public double getHeight() {
        return height;
    }

    public double getWidth() {
        return width;
    }

    @SuppressWarnings("unchecked")
    public <T extends Controller> T getController() {
        return (T) controller;
    }
}

