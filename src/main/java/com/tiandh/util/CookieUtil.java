package com.tiandh.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @Auther: lenovo
 * @Date: 2018/11/2 19:24
 * @Description:
 */
@Slf4j
public class CookieUtil {

    //private final static String COOKIE_DOMAIN = ".happymmall.com";
    private final static String COOKIE_DOMAIN = "192.168.1.61";
    private final static String COOKIE_NAME = "mmall_login_token";

    /**
     * 向客户端写入Cookie
     * @param response
     * @param token
     */
    public static void writeLoginToken(HttpServletResponse response, String token) {
        //创建Cookie，并设置Cookie的name与value
        Cookie ck = new Cookie(COOKIE_NAME, token);
        //设置domain，（本地开发默认为localhost）
        ck.setDomain(COOKIE_DOMAIN);
        //设置路径为根目录
        ck.setPath("/");
        //不允许通过脚本访问cookie信息
        ck.setHttpOnly(true);
        //设置Cookie的有效期（单位：秒）
        //如果不设置MaxAge,Cookie就不会写入硬盘，而是写在内存，只在当前页面有效
        // 如果是-1，代表永久有效
        ck.setMaxAge(60*60*24*365);//设置为1年的有效期
        log.info("write cookieName : {}, cookieValue : {}",ck.getName(),ck.getValue());
        //在返回中写入cookie
        response.addCookie(ck);
    }

    /**
     * 从客户端读取Cookie
     * @param request
     * @return
     */
    public static String readLoginCookie(HttpServletRequest request) {
        //从请求中获取cookie
        Cookie[] cks = request.getCookies();
        if (cks != null) {
            //遍历得到的cookie数组
            for (Cookie ck : cks) {
                log.info("cookieName : {}, cookieValue : {}",ck.getName(),ck.getValue());
                //判断
                if (StringUtils.equals(COOKIE_NAME, ck.getName())) {
                    log.info("return cookieName : {}, cookieValue : {}",ck.getName(), ck.getValue());
                    return ck.getValue();
                }
            }
        }
        return null;
    }

    /**
     * 删除客户端的Cookie
     * @param request
     * @param response
     */
    public static void delLoginToken(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cks = request.getCookies();
        if (cks != null) {
            for (Cookie ck : cks) {
                if (StringUtils.equals(COOKIE_NAME, ck.getName())){
                    ck.setDomain(COOKIE_DOMAIN);
                    ck.setPath("/");
                    //MaxAge设置为0(即：有效期为0)，代表删除Cookie
                    ck.setMaxAge(0);
                    log.info("del cookieName : {}, cookieValue : {}",ck.getName(),ck.getValue());
                    response.addCookie(ck);
                    return;
                }
            }
        }
    }
}
