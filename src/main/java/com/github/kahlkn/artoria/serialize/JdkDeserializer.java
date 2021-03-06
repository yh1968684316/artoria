package com.github.kahlkn.artoria.serialize;

import com.github.kahlkn.artoria.exception.UncheckedException;
import com.github.kahlkn.artoria.util.Assert;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;

/**
 * JDK deserializer.
 * @author Kahle
 */
public class JdkDeserializer implements Deserializer<Object> {

    @Override
    public Object deserialize(InputStream inputStream) throws IOException {
        Assert.notNull(inputStream, "Parameter \"inputStream\" must not null. ");
        try {
            ObjectInputStream ois = new ObjectInputStream(inputStream);
            return ois.readObject();
        }
        catch (ClassNotFoundException e) {
            throw new UncheckedException(e);
        }
    }

}
