package rahulstech.jfx.balancesheet.view;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.StringConverter;

@SuppressWarnings("ALL")
public class TextAreaPopupTableCell<S,T> extends TableCell<S,T> {

    private static final String DEFAULT_TITLE = "Edit Cell";

    private final StringConverter<T> converter;

    private Stage popupStage;

    private String title;

    public TextAreaPopupTableCell(StringConverter<T> converter) {
        this(converter,DEFAULT_TITLE);
    }

    public TextAreaPopupTableCell(StringConverter<T> converter, String title) {
        this.converter = converter;
        this.title = title;
    }

    @Override
    public void startEdit() {
        super.startEdit();
        showPopup();
    }

    @Override
    protected void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
            setText(null);
        }
        else {
            setText(converter.toString(item));
        }
    }

    private void showPopup() {
        if (null==popupStage) {
            popupStage = new Stage(StageStyle.UTILITY);
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.initOwner(getScene().getWindow());
            popupStage.setResizable(false);
            popupStage.setTitle(title);
            Parent root = getPopupContent(popupStage);

            Scene scene = new Scene(root);
            scene.getStylesheets().addAll(getScene().getStylesheets());
            popupStage.setScene(scene);
        }
        popupStage.show();
    }

    private Parent getPopupContent(Stage popupStage) {
        TextArea popupTextArea = new TextArea(converter.toString(getItem()));
        popupTextArea.setPrefWidth(300);
        popupTextArea.setPrefHeight(80);
        popupTextArea.setWrapText(true);

        Button saveButton = new Button("Save");
        saveButton.getStyleClass().add("base-button");
        saveButton.setOnAction(event -> {
            commitEdit(converter.fromString(popupTextArea.getText()));
            popupStage.close();
        });

        Button cancelButton = new Button("Cancel");
        cancelButton.getStyleClass().add("base-button");
        cancelButton.setOnAction(event -> popupStage.close());

        VBox vbox = new VBox(popupTextArea, new HBox(10, saveButton, cancelButton));
        vbox.setSpacing(10);
        vbox.setPadding(new javafx.geometry.Insets(10));
        return vbox;
    }
}
