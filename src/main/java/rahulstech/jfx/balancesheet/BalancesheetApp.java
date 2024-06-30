package rahulstech.jfx.balancesheet;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import rahulstech.jfx.balancesheet.database.BalancesheetDb;
import rahulstech.jfx.balancesheet.util.Log;
import rahulstech.jfx.balancesheet.util.ViewLauncher;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SuppressWarnings("ALL")
public class BalancesheetApp extends Application {

    private static final String DEV_DATA_DIR = "./balancesheet-data-DEV";

    private static final String DATA_DIR = "./balancesheet-data";

    private static final String DATABASE_DIR = "/database";

    private static final String LOG_DIR = "/logs";

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

    public File getDataDirectory() {
        File dir = new File(isDevelopment() ? DEV_DATA_DIR : DATA_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    public File getLogsDirectory() {
        File dir = new File(getDataDirectory(),LOG_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    public File getDatabaseDirectory() {
        File dir = new File(getDataDirectory(),DATABASE_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    public boolean isDevelopment() {
        // TODO: implement method to check development or release version
        return false;
    }

    @Override
    public void init() throws Exception {
        super.init();
        Log.init(getLogsDirectory(),isDevelopment());
        getAppExecutor().execute(()->{
            BalancesheetDb.setDatabaseDirectory(getDatabaseDirectory());
            BalancesheetDb.getInstance();
        });
    }

    @Override
    public void start(Stage stage) {
        appWindow = stage;
        stage.setOnCloseRequest(e->{
            Platform.exit();
            System.exit(0);
        });
        ViewLauncher viewLauncher = new ViewLauncher.Builder()
                .setOwnerWindow(stage)
                .setTitle("Balance Sheet")
                .setFxml("dashboard.fxml")
                .setShowInDifferentWindow(false)
                .setStyleSheet("theme.css")
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
        launch(args);
    }
}