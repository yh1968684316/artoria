package com.github.kahlkn.artoria.beans;

import com.github.kahlkn.artoria.converter.ConvertUtils;
import com.github.kahlkn.artoria.converter.Converter;
import com.github.kahlkn.artoria.exception.UncheckedException;
import com.github.kahlkn.artoria.logging.Logger;
import com.github.kahlkn.artoria.logging.LoggerFactory;
import com.github.kahlkn.artoria.reflect.ReflectUtils;
import com.github.kahlkn.artoria.util.Assert;
import com.github.kahlkn.artoria.util.ClassUtils;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Map;

/**
 * Bean tools.
 * @author Kahle
 */
public class BeanUtils extends BeanHandler {
    private static Logger log = LoggerFactory.getLogger(BeanUtils.class);
    private static final Converter CONVERTER = new Converter() {
        @Override
        public Object convert(Object source, Class<?> target) {
            return ConvertUtils.convert(source, target);
        }
    };

    private static BeanCopier beanCopier;
    private static Class<? extends BeanMap> beanMapClass;
    private static Constructor<? extends BeanMap> beanMapConstructor;

    static {
        ClassLoader loader = ClassUtils.getDefaultClassLoader();
        boolean hasCglib = ClassUtils.isPresent("net.sf.cglib.beans.BeanCopier", loader);
        BeanUtils.setBeanCopier(hasCglib ? new CglibBeanCopier() : new JdkBeanCopier());
        BeanUtils.setBeanMapClass(hasCglib ? CglibBeanMap.class : JdkBeanMap.class);
    }

    public static BeanCopier getBeanCopier() {
        return beanCopier;
    }

    public static void setBeanCopier(BeanCopier beanCopier) {
        Assert.notNull(beanCopier,
                "Parameter \"beanCopier\" must not null. ");
        BeanUtils.beanCopier = beanCopier;
        log.info("Set bean copier: " + beanCopier.getClass().getName());
    }

    public static Class<? extends BeanMap> getBeanMapClass() {
        return beanMapClass;
    }

    public static void setBeanMapClass(Class<? extends BeanMap> beanMapClass) {
        Assert.notNull(beanMapClass,
                "Parameter \"beanMapClass\" must not null. ");
        Assert.state(beanMapClass != BeanMap.class,
                "Parameter \"beanMapClass\" must not \"BeanMap.class\". ");
        try {
            BeanUtils.beanMapClass = beanMapClass;
            BeanUtils.beanMapConstructor = beanMapClass.getConstructor();
            log.info("Set bean map class: " + beanMapClass.getName());
        }
        catch (Exception e) {
            throw new UncheckedException(e);
        }
    }

    public static BeanMap createBeanMap() {
        try {
            return beanMapConstructor.newInstance();
        }
        catch (Exception e) {
            throw new UncheckedException(e);
        }
    }

    public static BeanMap createBeanMap(Object bean) {
        BeanMap map = BeanUtils.createBeanMap();
        map.setBean(bean);
        return map;
    }

    public static Object clone(Object obj) {
        try {
            Class<?> clazz = obj.getClass();
            Object clone = ReflectUtils.newInstance(clazz);
            BeanUtils.copy(obj, clone);
            return clone;
        }
        catch (Exception e) {
            throw new UncheckedException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static void copy(Object from, Map to) {
        Assert.notNull(from, "Parameter \"from\" must not null. ");
        Assert.notNull(to, "Parameter \"to\" must not null. ");
        BeanMap map = BeanUtils.createBeanMap(from);
        to.putAll(map);
    }

    public static void copy(Map from, Object to) {
        BeanUtils.copy(from, to, CONVERTER);
    }

    public static void copy(Map from, Object to, Converter cvt) {
        Assert.notNull(from, "Parameter \"from\" must not null. ");
        Assert.notNull(to, "Parameter \"to\" must not null. ");
        BeanMap map = BeanUtils.createBeanMap(to);
        map.setConverter(cvt);
        map.putAll(from);
    }

    public static void copy(Object from, Object to) {
        beanCopier.copy(from, to, null, CONVERTER);
    }

    public static void copy(Object from, Object to, Converter cvt) {
        beanCopier.copy(from, to, null, cvt);
    }

    public static void copy(Object from, Object to, List<String> ignore) {
        beanCopier.copy(from, to, ignore, CONVERTER);
    }

    public static void copy(Object from, Object to, List<String> ignore, Converter cvt) {
        beanCopier.copy(from, to, ignore, cvt);
    }

}
