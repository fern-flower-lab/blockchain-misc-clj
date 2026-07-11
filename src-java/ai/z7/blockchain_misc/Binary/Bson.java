package ai.z7.blockchain_misc.Binary;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.undercouch.bson4jackson.BsonFactory;

import java.io.IOException;
import java.io.UncheckedIOException;

public class Bson {

    private static final ObjectMapper MAPPER = new ObjectMapper(new BsonFactory());

    public static <T> T decodeObject(byte[] bson, Class<T> type) {
        try {
            return MAPPER.readValue(bson, type);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to decode BSON.", e);
        }
    }

    public static byte[] encodeObject(Object obj) throws JsonProcessingException {
        return MAPPER.writeValueAsBytes(obj);
    }
}
