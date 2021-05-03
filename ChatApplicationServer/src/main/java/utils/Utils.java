package utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

/**
 * @author Rodrigo Machado <a href="mailto:rodrigo.domingos@pucrs.br">rodrigo.domingos@pucrs.br</a>
 */

public class Utils {

    private Utils() {

    }

    public static <T> String toJson(T object) {
        try {
            return new Gson().toJson(object);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static <T> T parseJson(String json, Class<T> clazz) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();

            return objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            return null;
        }
    }

    public static String sanitizeNickName(String nickName) {
        return nickName != null ?
                nickName.trim().toLowerCase() :
                null;
    }
}
