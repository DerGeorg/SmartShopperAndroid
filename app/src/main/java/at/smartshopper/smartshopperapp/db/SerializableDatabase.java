package at.smartshopper.smartshopperapp.db;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

public class SerializableDatabase implements JsonSerializer<Database> {

    @Override
    public JsonElement serialize(Database src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject object = new JsonObject();
        return null;
    }
}
