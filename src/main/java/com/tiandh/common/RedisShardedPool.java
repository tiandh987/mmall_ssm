package com.tiandh.common;

import com.tiandh.util.PropertiesUtil;
import redis.clients.jedis.*;
import redis.clients.util.Hashing;
import redis.clients.util.Sharded;

import java.util.ArrayList;
import java.util.List;

/**
 * @Auther: lenovo
 * @Date: 2018/11/5 11:34
 * @Description: 分布式Redis连接池
 */
public class RedisShardedPool {
    //sharded jedis连接池
    private static ShardedJedisPool pool;
    //最大连接数
    private static Integer maxTotal = Integer.parseInt(PropertiesUtil.getProperty("redis.max.total", "20"));
    //在JedisPool中最大的Idle状态的（空闲的）Jedis实例的个数
    private static Integer maxIdle = Integer.parseInt(PropertiesUtil.getProperty("redis.max.idle", "10"));
    //在JedisPool中最小的Idle状态的（空闲的）Jedis实例的个数
    private static Integer minIdle = Integer.parseInt(PropertiesUtil.getProperty("redis.min.idle", "2"));
    //在borrow一个Jedis实例的时候，是否进行验证操作。
    //如果赋值为true，则得到的Jedis实例肯定是可用的
    private static Boolean testOnBorrow = Boolean.parseBoolean(PropertiesUtil.getProperty("redis.test.borrow", "true"));
    //在return一个Jedis实例的时候，是否进行验证操作
    //如果赋值为true，则放回JedisPool的Jedis实例肯定是可用的
    private static Boolean testOnReturn = Boolean.parseBoolean(PropertiesUtil.getProperty("redis.test.return", "false"));
    //redis1.host
    private static String redis1Host = PropertiesUtil.getProperty("redis1.host");
    //redis1.port
    private static Integer redis1Port = Integer.parseInt(PropertiesUtil.getProperty("redis1.port"));
    //redis2.host
    private static String redis2Host = PropertiesUtil.getProperty("redis2.host");
    //redis2.port
    private static Integer redis2Port = Integer.parseInt(PropertiesUtil.getProperty("redis2.port"));

    //初始化Redis连接池，该方法只会被调用一次（通过静态代码块调用）
    private static void initPool(){
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(maxTotal);
        config.setMaxIdle(maxIdle);
        config.setMinIdle(minIdle);
        config.setTestOnBorrow(testOnBorrow);
        config.setTestOnReturn(testOnReturn);
        //当连接耗尽时，是否阻塞，true阻塞直到超时，false会抛出异常。默认为true
        config.setBlockWhenExhausted(true);

        JedisShardInfo info1 = new JedisShardInfo(redis1Host, redis1Port, 1000*2);
        //如果redis有密码，调用此方法
        //info1.setPassword("password");
        JedisShardInfo info2 = new JedisShardInfo(redis2Host, redis2Port, 1000*2);

        List<JedisShardInfo> jedisShardInfoList = new ArrayList<>();
        jedisShardInfoList.add(info1);
        jedisShardInfoList.add(info2);

        //初始化ShardedJedisPool, 超时时间：1000*2 单位为毫秒
        //Hashing.MURMUR_HASH 对应一致性算法
        pool = new ShardedJedisPool(config, jedisShardInfoList, Hashing.MURMUR_HASH, Sharded.DEFAULT_KEY_TAG_PATTERN);

    }

    //当类被加载时，调用Redis连接池初始化方法
    static {
        initPool();
    }

    //从连接池中返回一个Jedis实例
    public static ShardedJedis getJedis(){
        return pool.getResource();
    }

    //将Jedis放回连接池中
    public static void returnResource(ShardedJedis jedis){
        pool.returnResource(jedis);
    }

    //将损坏的连接放入BrokenResource
    public static void returnBrokenResource(ShardedJedis jedis){
        pool.returnBrokenResource(jedis);
    }
}
