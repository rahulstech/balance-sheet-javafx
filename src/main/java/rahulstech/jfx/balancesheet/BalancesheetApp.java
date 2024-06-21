package rahulstech.jfx.balancesheet;

import javafx.application.Application;
import javafx.stage.Stage;
import rahulstech.jfx.balancesheet.database.BalancesheetDb;
import rahulstech.jfx.balancesheet.util.ViewLauncher;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SuppressWarnings("ALL")
public class BalancesheetApp extends Application {

    private static BalancesheetApp INSTANCE;

    private ExecutorService executor;

    private Stage appWindow;

    public ExecutorService getAppExecutor() {
        if (null == executor) {
            executor = Executors.newCachedThreadPool();
        }
        return executor;
    }

    public Stage getAppWindow() {
        return appWindow;
    }

    public BalancesheetApp() {
        INSTANCE = this;
    }

    public static BalancesheetApp getInstance() {
        return INSTANCE;
    }

    @Override
    public void init() throws Exception {
        super.init();
        getAppExecutor().execute(BalancesheetDb::getInstance);
    }

    @Override
    public void start(Stage stage) {
        appWindow = stage;
        ViewLauncher viewLauncher = new ViewLauncher.Builder()
                .setOwnerWindow(stage)
                .setTitle("Balance Sheet")
                .setFxml("dashboard.fxml")
                .setShowInDifferentWindow(false)
                .setStyleSheet("theme.css","colors.css")
                .build();
        viewLauncher.load();
        viewLauncher.getWindow().setMaximized(true);
        viewLauncher.getWindow().show();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        BalancesheetDb.getInstance().closeSilently();
        if (null != executor) {
            executor.shutdownNow();
        }
    }

    public static void main(String[] args) {
        launch();
    }
}