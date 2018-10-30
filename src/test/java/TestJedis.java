import com.tiandh.common.RedisPool;
import org.junit.Test;
import redis.clients.jedis.Jedis;

/**
 * @Auther: lenovo
 * @Date: 2018/10/28 23:09
 * @Description:
 */

public class TestJedis {
    @Test
    public void testJedis(){
        Jedis jedis = RedisPool.getJedis();
        jedis.set("keyTest", "valueTest");
        RedisPool.returnResource(jedis);
        System.out.println("program is end");
    }
}
