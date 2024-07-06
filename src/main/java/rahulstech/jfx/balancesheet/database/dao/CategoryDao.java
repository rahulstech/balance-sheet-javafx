package rahulstech.jfx.balancesheet.database.dao;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.misc.TransactionManager;
import com.j256.ormlite.support.ConnectionSource;
import rahulstech.jfx.balancesheet.database.entity.Category;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import static rahulstech.jfx.balancesheet.database.dao.DaoUtil.callWithoutExceptionHandling;

@SuppressWarnings("ALL")
public class CategoryDao extends BaseDaoImpl<Category,Long> {

    public CategoryDao(ConnectionSource connectionSource) throws SQLException {
        super(connectionSource,Category.class);
    }

    // Insert method
    public void insertOrUpdate(Collection<Category> categories) throws Exception {
        TransactionManager.callInTransaction(getConnectionSource(),()->{
            for (Category category : categories) {
                createOrUpdate(category);
            }
            return null;
        });
    }

    public boolean deleteCategories(List<Category> categories) {
        return  callWithoutExceptionHandling(()-> {
            delete(categories);
            return true;
        });
    }

    // Query method to return all categories containing the given phrase
    public List<Category> getAll() {
        return  callWithoutExceptionHandling(()-> queryForAll());
    }
}
