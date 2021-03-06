package com.github.kahlkn.artoria.converter;

import com.github.kahlkn.artoria.logging.Logger;
import com.github.kahlkn.artoria.logging.LoggerFactory;
import com.github.kahlkn.artoria.util.Assert;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Convert tools.
 * @author Kahle
 */
public class ConvertUtils {
    private static Logger log = LoggerFactory.getLogger(ConvertUtils.class);

    private static final Map<Class<?>, Converter> CONVERTERS;

    static {
        CONVERTERS = new ConcurrentHashMap<Class<?>, Converter>();
        ConvertUtils.register(Date.class, new DateConverter());
        ConvertUtils.register(String.class, new StringConverter());
        ConvertUtils.register(Number.class, new NumberConverter());
        ConvertUtils.register(Object.class, new ObjectConverter());
    }

    public static Converter unregister(Class<?> clazz) {
        Assert.notNull(clazz, "Clazz must is not null. ");
        Converter remove = CONVERTERS.remove(clazz);
        log.info("Unregister: " + clazz.getName() + " >> " + remove.getClass().getName());
        return remove;
    }

    public static void register(Class<?> clazz, Converter converter) {
        Assert.notNull(clazz, "Clazz must is not null. ");
        Assert.notNull(converter, "Converter must is not null. ");
        CONVERTERS.put(clazz, converter);
        log.info("Register: " + clazz.getName() + " >> " + converter.getClass().getName());
    }

    public static Object convert(Object source, Class<?> target) {
        if (source == null) { return null; }
        Class<?> clazz = source.getClass();
        for (Class<?> cClass : CONVERTERS.keySet()) {
            if (cClass.isAssignableFrom(clazz)) {
                Converter converter = CONVERTERS.get(cClass);
                source = converter.convert(source, target);
            }
        }
        return source;
    }

}
