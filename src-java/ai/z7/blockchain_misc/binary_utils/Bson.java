package ai.z7.blockchain_misc.binary_utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.undercouch.bson4jackson.BsonFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class Bson {

    public static <T> T decodeObject(byte[] BSON, Class<T> _class) {
        T t = null;
        ObjectMapper mapper = new ObjectMapper(new BsonFactory());

        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(BSON);
            t = (T) mapper.readValue(bais, _class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return t;
    }

    public static byte[] encodeObject(Object obj) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper(new BsonFactory());
        return mapper.writeValueAsBytes(obj);
    }
}
