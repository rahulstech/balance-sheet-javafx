package rahulstech.jfx.balancesheet;

import javafx.application.Application;
import javafx.stage.Stage;
import rahulstech.jfx.balancesheet.database.BalancesheetDb;
import rahulstech.jfx.balancesheet.util.ViewLauncher;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BalancesheetApp extends Application {

    private static ExecutorService executor;

    private static Stage appWindow;

    public static ExecutorService getAppExecutor() {
        if (null == executor) {
            executor = Executors.newCachedThreadPool();
        }
        return executor;
    }

    public static Stage getAppWindow() {
        return appWindow;
    }

    @Override
    public void init() throws Exception {
        super.init();
        getAppExecutor().execute(BalancesheetDb::getInstance);
    }

    @Override
    public void start(Stage stage) throws IOException {
        appWindow = stage;
        ViewLauncher viewLauncher = new ViewLauncher.Builder()
                .setOwnerWindow(stage)
                .setTitle("Balance Sheet")
                .setFxml("dashboard.fxml")
                .setHeight(400)
                .setWidth(600)
                .setShowInDifferentWindow(false)
                .build();
        viewLauncher.load();
        viewLauncher.getWindow().show();

        /*ViewLauncher viewLauncher = new ViewLauncher.Builder()
                .setOwnerWindow(stage)
                .setFxml("monthly_category_chart.fxml")
                .setShowInDifferentWindow(false)
                .build();
        viewLauncher.load();
        viewLauncher.getWindow().show();*/
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