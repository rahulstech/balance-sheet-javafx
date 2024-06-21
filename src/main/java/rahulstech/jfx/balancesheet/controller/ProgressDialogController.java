package rahulstech.jfx.balancesheet.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

@SuppressWarnings("ALL")
public class ProgressDialogController extends Controller {

    @FXML
    private Label label;

    @FXML
    private ProgressBar progressBar;

    public void setMessage(String message) {
        label.setText(message);
    }

    public void setProgress(double progress) {
        progressBar.setProgress(progress);
    }

    public void setIndeterminate(boolean indeterminate) {
        progressBar.setProgress(indeterminate ? ProgressBar.INDETERMINATE_PROGRESS : 0);
    }
}
