package com.tiandh.common;

import com.google.common.collect.Sets;

import java.util.Set;

/**
 * 常量类
 */
public class Const {
    //
    public static final String CURRENT_USER = "currentUser";

    //Redis存储session信息有效时间
    public interface RedisCacheExTime {
        int REDIS_SESSION_EXTIME = 60 * 30; //30分钟
    }

    //
    public static final String EMAIL = "email";
    //
    public static final String USERNAME = "username";

    //
    public interface ProductListOrderBy{
        Set<String> PRICE_ASC_DESC = Sets.newHashSet("price_desc","price_asc");
    }

    //
    public interface Cart{
        int CHECKED = 1;//选中状态
        int UN_CHECKED = 0;//未选中状态

        String LIMIT_NUM_FAIL = "LIMIT_NUM_FAIL";
        String LIMIT_NUM_SUCCESS = "LIMIT_NUM_SUCCESS";
    }

    //
    public interface Role{
        int ROLE_CUSTOMER = 0;//普通用户
        int ROLE_ADMIN = 1;//管理员
    }

    //产品状态
    public enum ProductStatusEnum{
        ON_SALE(1,"在售");

        private int code;
        private String value;

        ProductStatusEnum(int code,String value){
            this.code = code;
            this.value = value;
        }

        public int getCode() {
            return code;
        }

        public String getValue() {
            return value;
        }
    }

    //订单状态
    public enum OrderStatus{
        CANCELED(0,"已取消"),
        NO_PAY(10,"未支付"),
        PAID(20,"已付款"),
        SHIPPED(40,"已发货"),
        ORDER_SUCCESS(50,"订单完成"),
        ORDER_CLOSE(60,"订单关闭")
        ;

        OrderStatus(int code,String value){
            this.code = code;
            this.value = value;
        }
        private int code;
        private String value;

        public int getCode() {
            return code;
        }

        public String getValue() {
            return value;
        }

        public static OrderStatus codeOf(int code){
            for (OrderStatus orderStatus : values()){
                if (orderStatus.getCode() == code){
                    return orderStatus;
                }
            }
            throw new RuntimeException("没有找到对应的枚举");
        }
    }

    //
    public interface AlipayCallback{
        String TRADE_STATUS_WAIT_BUYER_PAY = "WAIT_BUYER_PAY";
        String TRADE_STATUS_TRADE_SUCCESS = "TRADE_SUCCESS";

        String RESPONSE_SUCCESS = "success";
        String RESPONSE_FAILED = "failed";
    }

    //支付平台
    public enum PayPlatform{
        ALIPAY(1,"支付宝")
        ;

        private Integer code;
        private String value;

        PayPlatform(Integer code,String value){
            this.code = code;
            this.value = value;
        }

        public Integer getCode() {
            return code;
        }

        public String getValue() {
            return value;
        }
    }

    //支付类型
    public enum PaymentType{
        ONLINE_PAY(1,"在线支付")
        ;

        private Integer code;
        private String value;

        PaymentType(Integer code,String value){
            this.code = code;
            this.value = value;
        }

        public Integer getCode() {
            return code;
        }

        public String getValue() {
            return value;
        }

        public static PaymentType codeOf(Integer code){
            for (PaymentType paymentType : values()){
                if (paymentType.getCode() == code){
                    return paymentType;
                }
            }
            throw new RuntimeException("没有找到对应的枚举");
        }
    }
}
