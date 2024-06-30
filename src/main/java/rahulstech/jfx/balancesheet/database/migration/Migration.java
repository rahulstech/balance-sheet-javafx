package rahulstech.jfx.balancesheet.database.migration;

import com.j256.ormlite.support.ConnectionSource;

import java.sql.SQLException;

@SuppressWarnings("ALL")
public abstract class Migration {

    private final long from;
    private final long to;

    public Migration(long from, int to) {
        this.from = from;
        this.to = to;
    }

    public long getFrom() {
        return from;
    }

    public long getTo() {
        return to;
    }

    public abstract void migrate(final  ConnectionSource source) throws SQLException;
}
