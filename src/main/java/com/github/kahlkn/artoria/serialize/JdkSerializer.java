package com.github.kahlkn.artoria.serialize;

import com.github.kahlkn.artoria.util.Assert;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;

/**
 * JDK serializer.
 * @author Kahle
 */
public class JdkSerializer implements Serializer<Object> {

    @Override
    public void serialize(Object object, OutputStream outputStream) throws IOException {
        Assert.notNull(object, "Parameter \"object\" must not null. ");
        Assert.notNull(outputStream, "Parameter \"outputStream\" must not null. ");
        Assert.isInstanceOf(Serializable.class, object, JdkSerializer.class.getSimpleName()
                + " requires a Serializable payload but received an object of type ["
                + object.getClass().getName() + "]. ");
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
        objectOutputStream.writeObject(object);
        objectOutputStream.flush();
    }

}
