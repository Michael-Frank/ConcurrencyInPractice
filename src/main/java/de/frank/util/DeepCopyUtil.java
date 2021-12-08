package de.frank.util;


import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.util.TokenBuffer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.experimental.UtilityClass;

import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * Utility's for deep cloning/copy of an nested object. Utilizes serialization.
 */
@UtilityClass
public class DeepCopyUtil {

    /**
     * ObjectMapper is thread safe
     */
    private static final ObjectMapper MAPPER = new JsonMapper()
            .registerModule(new JavaTimeModule())
            .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
            .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS,false);

    /**
     * Deep-clone the provided object by serializing and de-serializing it into an intermediate representation
     *
     * @param v   the object to be cloned
     * @param <V> type of object to be cloned
     * @return a deep-clone of the the provided object
     */
    @SuppressWarnings("unchecked")
    public static <V> V clone(V v) {
        //TokenBuffer is Jackson's internal intermediate representation and faster then toBytes or toString
        TokenBuffer tokens = new TokenBuffer(MAPPER, false);
        try {

            //Q: Hey! lets use a cached ObjectReader and ObjectWriter, jackson says they are thread safe!
            //A: Thread-Safe != Concurrent. Its faster While a shared ObjectReader/Writer slightly increases
            //performance in
            //low-load situations, it hurts performance in a contended situation (many threads using the same
            //reader/writer object)
            MAPPER.writeValue(tokens, v);
            return (V) MAPPER.readValue(tokens.asParser(), v.getClass());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}
