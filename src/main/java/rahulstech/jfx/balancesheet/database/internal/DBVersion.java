package rahulstech.jfx.balancesheet.database.internal;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "db_version")
public class DBVersion {

    @DatabaseField(id = true)
    private int id;

    @DatabaseField(canBeNull = false)
    private long version;

    public DBVersion(int id, long version) {
        this.id = id;
        this.version = version;
    }

    public DBVersion() {}

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
