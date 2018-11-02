package com.tiandh.util;

        import lombok.extern.slf4j.Slf4j;
        import org.apache.commons.lang3.StringUtils;
        import org.codehaus.jackson.map.DeserializationConfig;
        import org.codehaus.jackson.map.ObjectMapper;
        import org.codehaus.jackson.map.SerializationConfig;
        import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
        import org.codehaus.jackson.type.JavaType;
        import org.codehaus.jackson.type.TypeReference;

        import java.io.IOException;
        import java.text.SimpleDateFormat;

/**
 * @Auther: lenovo
 * @Date: 2018/11/1 18:29
 * @Description: JsonUtil Json工具类，对象序列化与反序列化
 */
@Slf4j
public class JsonUtil {

    private  static ObjectMapper objectMapper = new ObjectMapper();

    //初始化ObjectMapper
    static {
        //对象的所有字段全部列入
        objectMapper.setSerializationInclusion(Inclusion.ALWAYS);

        //取消默认转换timestamps形式(毫秒数)
        objectMapper.configure(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS, false);

        //忽略空Bean转Json的错误
        objectMapper.configure(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS, false);

        //所有的日期格式都统一格式，即：yyyy-MM-dd HH:mm:ss
        objectMapper.setDateFormat(new SimpleDateFormat(DateTimeUtil.STANDARD_FORMAT));

        //反序列化 忽略在json字符串中存在，而在java对象中不存在对应属性的情况，防止错误
        objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * java对象转为json字符串
     * @param object
     * @param <T>
     * @return
     */
    public static <T> String objectToString(Object object) {
        if (object == null)
            return null;
        try {
            //如果object是String类型，直接返回；不是，调用writeValueAsString()方法
            return object instanceof String ? (String)object : objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            log.error("Parse Object to String error ", e);
            return null;
        }
    }

    /**
     * java对象转为json字符串
     * @param object
     * @param <T>
     * @return 返回的json字符串是进行格式化后的（有空格，换行，方便查看）
     */
    public static <T> String objectToStringPretty(Object object){
        if (object == null)
            return null;
        try {
            return object instanceof String ? (String) object : objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
        } catch (IOException e) {
            log.error("Parse Object to String error", e);
            return null;
        }
    }

    /**
     * json字符串转换为java对象
     * @param string
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T stringToObject(String string, Class<T> clazz) {
        if (StringUtils.isEmpty(string) || clazz == null) {
            return null;
        }
        try {
            return clazz.equals(String.class) ? (T) string : objectMapper.readValue(string, clazz);
        } catch (Exception e) {
            log.error("Parse String to Object error",e );
            return null;
        }
    }

    //************************两个通用性较强的反序列化方法*****************************

    /**
     * json字符串转换为java对象
     * @param string
     * @param typeReference
     * @param <T>
     * @return
     */
    public static <T> T stringToObject(String string, TypeReference<T> typeReference) {
        if (StringUtils.isEmpty(string) || typeReference == null)
            return null;
        try {
            return typeReference.getType().equals(String.class) ? (T) string : objectMapper.readValue(string, typeReference);
        } catch (Exception e) {
            log.error("Parse String to Object error", e);
            return null;
        }
    }

    /**
     * json字符串转换为java对象
     * @param string
     * @param collectionClass
     * @param elementClasses
     * @param <T>
     * @return
     */
    public static <T> T stringToObject(String string, Class<?> collectionClass, Class<?> elementClasses) {
        JavaType javaType = objectMapper.getTypeFactory().constructParametricType(collectionClass,elementClasses);
        try {
            return objectMapper.readValue(string, javaType);
        } catch (Exception e) {
            log.error("Parse String to Object error", e);
            return null;
        }
    }
}
