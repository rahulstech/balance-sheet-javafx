package rahulstech.jfx.balancesheet.database;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.misc.TransactionManager;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import rahulstech.jfx.balancesheet.database.dao.*;
import rahulstech.jfx.balancesheet.database.entity.*;
import rahulstech.jfx.balancesheet.database.exception.DatabaseException;
import rahulstech.jfx.balancesheet.database.internal.DBVersion;
import rahulstech.jfx.balancesheet.database.migration.Migration;
import rahulstech.jfx.balancesheet.util.Log;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

@SuppressWarnings("ALL")
public class BalancesheetDb {

    private static final String TAG = BalancesheetDb.class.getSimpleName();

    private static final long DB_VERSION = 2;

    private static final String DB_FILE_NAME = "balance_sheet.db3";

    private static final File DEFAULT_DB_DIR = new File(".");

    private static File DB_DIR;

    private static File DB_FILE = null;

    private static final Class<?>[] ENTITIES = new Class<?>[]{
            Account.class, Category.class,
            TransactionHistory.class, HistoryCategory.class, Budget.class
    };

    private final List<Migration> MIGRATIONS = Collections.emptyList();

    private final List<DBCallback> CALLBACKS;

    private static BalancesheetDb INSTANCE = null;

    private ConnectionSource connectionSource;

    private AccountDao accountDao;

    private TransactionHistoryDao transactionHistoryDao;

    private ChartDao chartDao;

    private CategoryDao categoryDao;

    private BudgetDao budgetDao;

    private BalancesheetDb() {
        CALLBACKS = Arrays.asList(
                ADD_DEFAULT_CATEGORIES
        );
        initialize(null==DB_DIR ? DEFAULT_DB_DIR : DB_DIR);
    }

    public synchronized static BalancesheetDb getInstance() {
        if (null==INSTANCE) {
            INSTANCE = new BalancesheetDb();
        }
        return INSTANCE;
    }

    public AccountDao getAccountDao() {
        return accountDao;
    }

    public TransactionHistoryDao getTransactionHistoryDao() {
        return transactionHistoryDao;
    }

    public ChartDao getChartDao() {
        return chartDao;
    }

    public CategoryDao getCategoryDao() {
        return categoryDao;
    }

    public BudgetDao getBudgetDao() {
        return budgetDao;
    }

    public long getCurrentDBVersion() {
        return DB_VERSION;
    }

    public <T> T inTransaction(Callable<T> callable) {
        return DaoUtil.callWithoutExceptionHandling(()->TransactionManager.callInTransaction(connectionSource,callable));
    }

    public void closeSilently() {
        try {
            if (null != connectionSource) {
                connectionSource.close();
            }
        }
        catch (Exception ignore) {}
        finally {
            connectionSource = null;
            INSTANCE = null;
        }
    }

    public void deleteDatabase() {
        try{
            closeSilently();
            Log.info(TAG,"deleting database file=\""+DB_FILE+"\"");
            if (DB_FILE.exists()) {
                boolean deleted = DB_FILE.delete();
                Log.info(TAG,"database file=\""+DB_FILE+"\" deleted="+deleted);
            }
        }
        catch (Exception ex) {
            Log.error(TAG,"deteleDatabase",ex);
        }
        finally {
            DB_FILE = null;
        }
    }

    public static void setDatabaseDirectory(File db_dir) {
        DB_DIR = db_dir;
    }

    private void initialize(File dir) {
        ConnectionSource source = null;
        boolean initialized = false;
        try {
            // set ormlite log level
            com.j256.ormlite.logger.Level logLevel = com.j256.ormlite.logger.Level.ERROR;
            com.j256.ormlite.logger.Logger.setGlobalLogLevel(logLevel);

            // TODO: update logging mechanism so that ormlite logs are added to app log file

            // create connection source
            File db_file = new File(dir,DB_FILE_NAME);
            source = newConnectionSource(db_file);

            // create internal table
            DBVersion version = createVersionTable(source);
            long newVersion = getCurrentDBVersion();

            if (null==version) {
                // Create all tables in one transaction
                createTables(source, ENTITIES);

                // call callback onCreate methods
                callDBCallback_onCreate(source, CALLBACKS);

                // add the version
                updateDBVersion(source,newVersion);
            }
            else if (version.getVersion()!=getCurrentDBVersion()) {
                // apply migration if required
                applyMigrations(source,version.getVersion(),newVersion, MIGRATIONS);

                // update the version
                updateDBVersion(source,newVersion);
            }

            // create table dao
            createDaos(source);

            // call callback onOpen methods
            callDBCallback_onOpen(source,CALLBACKS);

            setDatabaseFile(db_file);
            setConnectionSource(source);
            initialized = true;
        }
        catch (Exception ex) {
            throw new DatabaseException(ex);
        }
        finally {
            if (!initialized && null!=source) {
                source.closeQuietly();
            }
        }
    }

    private ConnectionSource newConnectionSource(File db_file) throws Exception {
        String db_file_path = db_file.toString();
        String DATABASE_URL = "jdbc:sqlite:"+db_file_path;
        return new JdbcConnectionSource(DATABASE_URL);
    }

    private void setDatabaseFile(File db_file) {
        this.DB_FILE = db_file;
    }

    private void setConnectionSource(ConnectionSource source) {
        this.connectionSource = source;
    }

    @SuppressWarnings("unchecked")
    private DBVersion createVersionTable(final  ConnectionSource source) throws Exception {
        TableUtils.createTableIfNotExists(source,DBVersion.class);
        return ((Dao<DBVersion,Long>) DaoManager.createDao(source,DBVersion.class)).queryBuilder().queryForFirst();
    }

    private void createTables(final ConnectionSource source, final Class<?>[] entities) throws Exception {
       TransactionManager.callInTransaction(source, ()-> {
            for (Class<?> entity : entities) {
                if (1!=TableUtils.createTableIfNotExists(source,entity)) {
                    throw new SQLException("unable to create entity="+entity);
                }
            }
            return null;
        });
    }

    private void createDaos(final ConnectionSource source) throws Exception {
        this.accountDao = new AccountDao(source);
        this.transactionHistoryDao = new TransactionHistoryDao(source);
        this.chartDao = new ChartDao(source);
        this.categoryDao = new CategoryDao(source);
        this.budgetDao = new BudgetDao(source);
    }

    private void applyMigrations(final ConnectionSource source, long oldVersion, long newVersion, final List<Migration> migrations) throws Exception {
        TransactionManager.callInTransaction(source,()->{
            if (migrations.isEmpty()) {
                throw new SQLException("db version changed from="+oldVersion+" to="+newVersion+" but to migration performed");
            }
            boolean isMigrationPerformed = false;
            for (Migration migration : migrations) {
                if (oldVersion==migration.getFrom() && newVersion==migration.getTo()) {
                    migration.migrate(source);
                    isMigrationPerformed = true;
                }
            }
            if (!isMigrationPerformed) {
                throw new SQLException("db version changed from="+oldVersion+" to="+newVersion+" but to migration performed");
            }
            return null;
        });
    }

    private void updateDBVersion(final ConnectionSource source, long newVersion) throws Exception {
        Dao<DBVersion,Long> versionDao = DaoManager.createDao(source, DBVersion.class);
        DBVersion version = new DBVersion(1,newVersion);
        versionDao.createOrUpdate(version);
    }

    private void callDBCallback_onCreate(final ConnectionSource source, final List<DBCallback> callbacks) throws Exception {
        if (callbacks.isEmpty()) {
            return;
        }
        for (DBCallback callback : callbacks) {
            callback.onCrate(source);
        }
    }

    private void callDBCallback_onOpen(final ConnectionSource source, final List<DBCallback> callbacks) throws Exception {
        if (callbacks.isEmpty()) {
            return;
        }
        for (DBCallback callback : callbacks) {
            callback.onOpen(source);
        }
    }

    private DBCallback ADD_DEFAULT_CATEGORIES = new DBCallback() {
        final String[] CATEGORIES = new String[]{
                "Income","Expense", "Stock & Mutual Fund", "Bank Interest", "Food & Snacks", "Recharge & Bills", "Fuel", "Repairing",
                "Entertainment", "Books & Studies", "Stationaries", "Courses & Fees", "Travelling & Vehicles", "Donation", "Medicines",
                "Beauty & Cosmetics", "Tax & Charges"
        };

        @Override
        public void onCrate(ConnectionSource source) throws Exception {
            if (2==getCurrentDBVersion()) {
                Dao<Category,Long> dao = DaoManager.createDao(source, Category.class);
                List<Category> categories = new ArrayList<>();
                long id = 1;
                for (String name : CATEGORIES) {
                    Category category = new Category();
                    category.setId(id++);
                    category.setName(name);
                    categories.add(category);
                }
                dao.create(categories);
            }
        }

        @Override
        public void onOpen(ConnectionSource source) throws Exception {}
    };
}

