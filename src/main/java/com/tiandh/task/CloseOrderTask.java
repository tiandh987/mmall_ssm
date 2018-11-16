package com.tiandh.task;

import com.tiandh.common.Const;
import com.tiandh.common.RedissonManager;
import com.tiandh.service.IOrderService;
import com.tiandh.util.PropertiesUtil;
import com.tiandh.util.RedisShardedPoolUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.concurrent.TimeUnit;

/**
 * @Auther: lenovo
 * @Date: 2018/11/12 16:18
 * @Description: 定时关单任务
 */
@Component
@Slf4j
public class CloseOrderTask {

    @Autowired
    private IOrderService iOrderService;

    @Autowired
    private RedissonManager redissonManager;

    /**
     * 使用shutdown命令关闭Tomcat服务器，关闭前会调用该方法
     */
    @PreDestroy
    public void delLock(){
        RedisShardedPoolUtil.del(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
    }

    /**
     * 该方法每一分钟执行一次，会关闭两个小时内为付款的订单
     * 适用于单个Tomcat服务器使用
     */
    //@Scheduled(cron = "0 */1 * * * ?") //每一分钟（每个一分钟的整数倍）
    public void closeOrderTaskV1(){
        log.info("关闭订单定时任务启动");
        int hour = Integer.parseInt(PropertiesUtil.getProperty("close.order.task.time.hour", "2"));
        //iOrderService.closeOrder(hour);
        log.info("关闭订单定时任务结束");
    }

    /**
     * 该方法每一分钟执行一次，会关闭两个小时内为付款的订单
     * 缺点：
     *      在多台服务器集群下，当某个进程执行到setnx方法设置了锁，突然发生了关闭服务器（此时并没有对锁设置有效期）的操作，
     *      这种情况下，分布式锁的有效期为永久，且不会被删除，其他进程就永远获取不到锁，
     *      这样会产生死锁
     * 解决方案：
     *      一.
     *          1.使用@PreDestroy注解声名一个方法，该方法中实现删除分布式锁的操作
     *          2.使用shutdown命令关闭Tomcat服务器，因为使用shutdown命令关闭服务器时，关闭服务器之前会调用@PreDestroy注解声明的方法
     *          3.该方法缺点：
     *              3.1 当需要关闭的锁非常非常多时，耗费的时间比较长
     *              3.2 如果直接kill掉Tomcat的进程时，该方法不会执行
     */
    //@Scheduled(cron = "0 */1 * * * ?") //每一分钟（每个一分钟的整数倍）
    public void closeOrderTaskV2(){
        log.info("关闭订单定时任务启动");

        //分布式锁的超时时间5000ms
        long lockTimeout = Long.parseLong(PropertiesUtil.getProperty("lock.timeout","5000"));
        //value 设置为 当前时间 + 分布式锁的超时时间
        Long setnxResult = RedisShardedPoolUtil.setnx(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK, String.valueOf(System.currentTimeMillis()+lockTimeout));
        //*****如果此时发生服务器的关闭，会产生死锁*****
        if (setnxResult != null && setnxResult.intValue() == 1){
            //如果setnx的返回值为1，代表设置成功，获取锁
            closeOrder(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
        }else {
            log.info("没有获得分布式锁：{}",Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
        }

        log.info("关闭订单定时任务结束");
    }

    /**
     * 分布式锁，双重防死锁
     */
    //@Scheduled(cron = "0 */1 * * * ?")
    public void closeOrderTaskV3(){
        log.info("关闭订单定时任务启动");

        //分布式锁的超时时间5000ms
        long lockTimeout = Long.parseLong(PropertiesUtil.getProperty("lock.timeout", "5000"));
        //value 设置为 当前时间 + 分布式锁的超时时间
        Long setnxResult = RedisShardedPoolUtil.setnx(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK, String.valueOf(System.currentTimeMillis()+lockTimeout));

        if (setnxResult != null && setnxResult.intValue() == 1){
            //如果setnx的返回值为1，代表设置成功，获取锁，关闭订单
            closeOrder(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
        }else {
            //未获取到锁，继续判断，看是否可以重置并获取到锁
            String lockValueStr = RedisShardedPoolUtil.get(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
            if (lockValueStr != null && System.currentTimeMillis() > Long.parseLong(lockValueStr)){
                //分布式锁存在，但已经超过设置的锁的有效期，则有权重置锁

                //因为是多台服务器集群，getSet的得到的值有可能被其他进程修改，而与刚才得到的lockValueStr不同
                String getSetResult = RedisShardedPoolUtil.getSet(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK, String.valueOf(System.currentTimeMillis()+lockTimeout));
                //通过getSet，得到给定key的旧值，根据旧值判断是否可以获取锁
                //当key没有旧值时，返回nil（NULL），代表key不存在，则可以获取锁
                //当key有旧值，且lockValueStr与getSetResult相同时（代表key的旧值没有被其他进程修改）
                if (getSetResult == null || (getSetResult != null && StringUtils.equals(lockValueStr, getSetResult))){
                    //获取到锁,关闭订单
                    closeOrder(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
                }else {
                    log.info("没有获得分布式锁：{}",Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
                }
            }else {
                //锁还未失效
                log.info("没有获得分布式锁：{}",Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
            }

        }
        log.info("关闭订单定时任务结束");
    }

    /**
     * Spring Scheduled  +  Redisson 实现定时任务
     */
    @Scheduled(cron = "0 */1 * * * ?")
    public void closeOrderTaskV4(){
        RLock lock = redissonManager.getRedisson().getLock(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
        boolean getLock = false;
        //尝试获取锁
        try {
            /**
             * waitTime : 尝试获取锁等待时间
             * leaseTime ： 锁的释放时间
             * unit ：时间单位
             */
            if (getLock = lock.tryLock(2, 5, TimeUnit.SECONDS)){
                log.info("Redisson获取分布式锁：{}，ThreadName：{}", Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK, Thread.currentThread().getName());
                int hour = Integer.parseInt(PropertiesUtil.getProperty("close.order.task.time.hour", "2"));
                iOrderService.closeOrder(hour);//关闭订单
            }else {
                log.info("Redisson没有获取分布式锁：{}，ThreadName：{}", Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK, Thread.currentThread().getName());
            }
        } catch (InterruptedException e) {
            log.error("Redisson分布式锁获取异常", e);
        }finally {
            if (!getLock){
                //未获取到锁
                return;
            }
            lock.unlock();//释放锁
            log.info("Redisson分布式锁释放锁");
        }
    }

    private void closeOrder(String lockName){
        RedisShardedPoolUtil.expire(lockName, 5);//设置分布式锁5秒的有效期，防止产生死锁
        log.info("获取：{}，ThreadName：{}",Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK,Thread.currentThread().getName());
        int hour = Integer.parseInt(PropertiesUtil.getProperty("close.order.task.time.hour", "2"));
        iOrderService.closeOrder(hour);//关闭订单
        RedisShardedPoolUtil.del(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);//订单关闭后，立即释放锁
        log.info("释放{}，ThreadName：{}",Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK,Thread.currentThread().getName());
        log.info("===============================================");
    }
}
