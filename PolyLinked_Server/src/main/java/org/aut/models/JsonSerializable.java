package org.aut.models;

import org.aut.utils.exceptions.NotAcceptableException;
import org.json.JSONObject;
import java.lang.reflect.InvocationTargetException;

public interface JsonSerializable {
    JSONObject toJson();

    static <T extends JsonSerializable> T fromJson(JSONObject jsonObject, Class<T> cls) throws NotAcceptableException {
        try {
            return cls.getConstructor(JSONObject.class).newInstance(jsonObject);
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException e) {
            throw new NotAcceptableException("Json constructor Not Found.");
        }
    }
}
