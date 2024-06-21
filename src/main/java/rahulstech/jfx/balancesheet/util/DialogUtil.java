package rahulstech.jfx.balancesheet.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import rahulstech.jfx.balancesheet.controller.ProgressDialogController;

@SuppressWarnings("SameParameterValue")
public class DialogUtil {

    public static Stage showIndeterminateProgressDialog(Stage owner, String title, String message) {
        ViewLauncher loader = new ViewLauncher.Builder()
                .setFxml("progress_dialog.fxml")
                .setOwnerWindow(owner)
                .setTitle(title)
                .setStageModality(Modality.APPLICATION_MODAL)
                .setStageStyle(StageStyle.UTILITY)
                .build();
        loader.load();
        ProgressDialogController controller = loader.getController();
        controller.setMessage(message);
        controller.setIndeterminate(true);
        return loader.getWindow();
    }

    public static void alertError(Stage owner, String title, String message) {
        showAlert(owner, Alert.AlertType.ERROR,title,message);
    }

    public static void alertConfirmation(Stage owner, String title, String message,
                                         String positiveButtonText, Runnable runIfYes,
                                         String negativeButtonText, Runnable runIfNo) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(message);
        alert.initOwner(owner);
        alert.getButtonTypes().clear();
        alert.getButtonTypes().add(new ButtonType(positiveButtonText, ButtonBar.ButtonData.YES));
        alert.getButtonTypes().add(new ButtonType(negativeButtonText, ButtonBar.ButtonData.NO));
        alert.showAndWait().ifPresent(type->{
            if (type.getButtonData().equals(ButtonBar.ButtonData.YES)) {
                if (null != runIfYes) {
                    runIfYes.run();
                }
            }
            else {
                if (null != runIfNo)
                    runIfNo.run();
            }
        });
    }

    private static void showAlert(Stage owner, Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(message);
        alert.initOwner(owner);
        alert.show();
    }


}
