package utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

/**
 * @author Rodrigo Machado <a href="mailto:rodrigo.domingos@pucrs.br">rodrigo.domingos@pucrs.br</a>
 */

public class Utils {

    private Utils() {

    }

    //Converte um objeto Java em um JSON
    public static <T> String toJson(T object) {
        return new Gson().toJson(object);
    }

    //Converte um JSON em objeto Java
    public static <T> T parseJson(String json, Class<T> clazz) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();

            return objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            return null;
        }
    }
}
