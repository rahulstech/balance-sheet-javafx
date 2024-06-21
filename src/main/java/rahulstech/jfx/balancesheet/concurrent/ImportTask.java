package rahulstech.jfx.balancesheet.concurrent;

import javafx.concurrent.Task;
import rahulstech.jfx.balancesheet.json.JsonHelper;
import rahulstech.jfx.balancesheet.json.model.DataModel;

import java.io.File;

@SuppressWarnings("ALL")
public class ImportTask extends Task<DataModel> {

    private final File jsonFile;

    public ImportTask(File jsonFile) {
        this.jsonFile = jsonFile;
    }

    @Override
    protected DataModel call() throws Exception {
        return JsonHelper.readJsonFile(jsonFile);
    }
}
