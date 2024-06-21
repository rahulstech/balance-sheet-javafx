package rahulstech.jfx.balancesheet.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import rahulstech.jfx.balancesheet.json.model.DataModel;
import rahulstech.jfx.balancesheet.database.type.Currency;

import java.io.*;
import java.time.LocalDate;

public class JsonHelper {

    public static DataModel readJsonFile(File jsonFile) {
        Gson gson = createGson();
        try (FileReader reader = new FileReader(jsonFile)) {
            return gson.fromJson(reader, DataModel.class);
        }
        catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private static Gson createGson() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(LocalDate.class, (JsonDeserializer<LocalDate>)
                (json, type, jsonDeserializationContext) -> LocalDate.parse(json.getAsString()));
        gsonBuilder.registerTypeAdapter(boolean.class, (JsonDeserializer<Boolean>)
                (json, type, jsonDeserializationContext) -> Boolean.parseBoolean(json.getAsString()));
        gsonBuilder.registerTypeAdapter(Currency.class, (JsonDeserializer<Currency>)
                (json, type, jsonDeserializationContext) -> Currency.from(json.getAsString()));
        return gsonBuilder
                .create();
    }
 }

