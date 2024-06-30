package rahulstech.jfx.balancesheet.database.dao;

import rahulstech.jfx.balancesheet.database.exception.DatabaseException;

import java.util.concurrent.Callable;

public class DaoUtil {

    public static <T> T callWithoutExceptionHandling(Callable<T> callable) {
        try {
            return callable.call();
        }
        catch (Exception ex) {
            throw new DatabaseException(ex);
        }
    }
}
