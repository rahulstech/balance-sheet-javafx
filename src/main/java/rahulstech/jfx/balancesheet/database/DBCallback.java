package rahulstech.jfx.balancesheet.database;

import com.j256.ormlite.support.ConnectionSource;

public interface DBCallback {

    void onCrate(ConnectionSource source) throws Exception;

    void onOpen(ConnectionSource source) throws Exception;
}
