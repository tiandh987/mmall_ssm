import com.tiandh.common.RedisPool;
import com.tiandh.util.RedisPoolUtil;
import org.junit.Ignore;
import org.junit.Test;
import redis.clients.jedis.Jedis;

/**
 * @Auther: lenovo
 * @Date: 2018/10/28 23:09
 * @Description:
 */

public class TestJedis {
    @Ignore
    @Test
    public void testJedis(){
        Jedis jedis = RedisPool.getJedis();
        jedis.set("keyTest", "valueTest");
        RedisPool.returnResource(jedis);
        System.out.println("program is end");
    }

    @Test
    public void testRedisAPI(){
        RedisPoolUtil.set("keyTest", "valueTest");

        String value = RedisPoolUtil.get("keyTest");

        RedisPoolUtil.setex("keyEx", 60*2, "valueEx");

        RedisPoolUtil.expire("keyTest", 60*10);

        RedisPoolUtil.del("keyTest");
    }
}
