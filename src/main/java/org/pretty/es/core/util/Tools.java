package org.pretty.es.core.util;

import org.pretty.es.core.annotion.ESId;
import org.pretty.es.core.annotion.ESMetaData;

import java.lang.reflect.Field;

public class Tools {

    /**
     * 根据实体类取@ESId标注的值
     *
     * @param object
     * @return
     * @throws Exception
     */
    public static String getESId(Object object) throws Exception {
        Field[] fields = object.getClass().getDeclaredFields();

        for (Field field : fields) {
            field.setAccessible(true);
            ESId esId = field.getAnnotation(ESId.class);
            if (esId != null) {
                Object value = field.get(object);
                if (value != null){
                    return value.toString();
                }
            }
        }
        return null;
    }

    public static MetaData getMetaData(Class<?> clazz) {
        if (clazz.getAnnotation(ESMetaData.class) != null) {
            String indexName = clazz.getAnnotation(ESMetaData.class).indexName();
            String indexType = clazz.getAnnotation(ESMetaData.class).indexType();
            MetaData metaData = new MetaData(indexName, indexType);

            return metaData;
        }
        return null;
    }
}
