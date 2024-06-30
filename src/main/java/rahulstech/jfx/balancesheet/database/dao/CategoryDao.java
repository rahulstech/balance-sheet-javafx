package rahulstech.jfx.balancesheet.database.dao;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.misc.TransactionManager;
import com.j256.ormlite.support.ConnectionSource;
import rahulstech.jfx.balancesheet.database.entity.Category;
import rahulstech.jfx.balancesheet.database.exception.DatabaseException;

import java.sql.SQLException;
import java.util.List;

import static rahulstech.jfx.balancesheet.database.dao.DaoUtil.callWithoutExceptionHandling;

@SuppressWarnings("ALL")
public class CategoryDao {

    private final ConnectionSource source;
    private Dao<Category, Long> categoryDao;

    public CategoryDao(ConnectionSource connectionSource) throws SQLException {
        this.source = connectionSource;
        categoryDao = DaoManager.createDao(connectionSource, Category.class);
    }

    // Insert method
    public Category insertOrUpdate(Category category) {
        return callWithoutExceptionHandling(()->{
            if (1!=categoryDao.createOrUpdate(category).getNumLinesChanged()) {
                throw new DatabaseException("category not created");
            }
            return category;
        });
    }

    public boolean deleteCategories(List<Category> categories) {
        if (null == categories || categories.isEmpty()) {
            return false;
        }
        return  callWithoutExceptionHandling(()-> TransactionManager.callInTransaction(source,()->{
            if (categories.size()!=categoryDao.delete(categories)){
                throw new DatabaseException("fail to delete multiple categories");
            }
            return true;
        }));
    }

    // Query method to return all categories containing the given phrase
    public List<Category> getAll() {
        return  callWithoutExceptionHandling(()-> categoryDao.queryForAll());
    }
}
