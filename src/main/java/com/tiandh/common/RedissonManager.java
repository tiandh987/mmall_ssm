package com.tiandh.common;

import com.tiandh.util.PropertiesUtil;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.config.Config;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @Auther: lenovo
 * @Date: 2018/11/16 13:35
 * @Description: Redisson初始化
 */
@Component
@Slf4j
public class RedissonManager {

    //注意导入的包：org.redisson.config.Config
    private Config config = new Config();

    private Redisson redisson = null;

    public Redisson getRedisson() {
        return redisson;
    }

    //redis1.host
    private static String redis1Host = PropertiesUtil.getProperty("redis1.host");
    //redis1.port
    private static Integer redis1Port = Integer.parseInt(PropertiesUtil.getProperty("redis1.port"));
    //redis2.host
    private static String redis2Host = PropertiesUtil.getProperty("redis2.host");
    //redis2.port
    private static Integer redis2Port = Integer.parseInt(PropertiesUtil.getProperty("redis2.port"));

    @PostConstruct //使用@PostConstruct注解，在构造器方法之后执行，可替代静态块
    private void init(){
        //Redisson 2.9.0 版本不支持一致性算法
        try {
            config.useSingleServer().setAddress(new StringBuilder().append(redis1Host).append(":").append(redis1Port).toString());

            redisson = (Redisson) Redisson.create(config);

            log.info("初始化Redisson结束");
        } catch (Exception e) {
            log.error("redisson init error", e);
        }
    }
}
