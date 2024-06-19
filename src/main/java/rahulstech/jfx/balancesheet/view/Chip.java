package rahulstech.jfx.balancesheet.view;
import javafx.animation.FadeTransition;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.util.Duration;
import rahulstech.jfx.balancesheet.ResourceLoader;

public class Chip extends HBox {

    private Label label;
    private Button closeButton;
    private ChipCloseListener closeListener;

    public Chip(String text) {
        // Initialize the label with the chip text
        label = new Label(text);

        // Initialize the close button
        closeButton = new Button("X");
        closeButton.setOnAction(e->handleCloseButtonClick());

        // Add label and close button to the HBox
        this.getChildren().addAll(label, closeButton);

        // Apply styles to the chip
        this.getStyleClass().add("chip");
        label.getStyleClass().add("chip-label");
        closeButton.getStyleClass().add("chip-close-button");

        getStylesheets().add(ResourceLoader.getCssFileExternalForm("chip_styles.css"));

        // Ensure the chip's size is just enough to hold its content
        this.setMinWidth(USE_COMPUTED_SIZE);
        this.setMinHeight(USE_COMPUTED_SIZE);
        this.setMaxWidth(USE_PREF_SIZE);
        this.setMaxHeight(USE_PREF_SIZE);
    }

    public Label getLabel() {
        return label;
    }

    public Button getCloseButton() {
        return closeButton;
    }

    private void handleCloseButtonClick() {
        // Add fade-out animation
        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), this);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e ->{
            ((Pane) this.getParent()).getChildren().remove(this);
            if (closeListener != null) {
                closeListener.onChipClose(this);
            }
        });
        fadeOut.play();
    }

    // Interface for listening to the close button click event
    public interface ChipCloseListener {
        void onChipClose(Chip chip);
    }

    // Setter for the close listener
    public void setCloseListener(ChipCloseListener listener) {
        this.closeListener = listener;
    }
}
