package com.tiandh.controller.common;

import com.tiandh.common.Const;
import com.tiandh.pojo.User;
import com.tiandh.util.CookieUtil;
import com.tiandh.util.JsonUtil;
import com.tiandh.util.RedisPoolUtil;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * @Auther: lenovo
 * @Date: 2018/11/3 15:20
 * @Description: 请求拦截器：当用户向服务端发送不同请求时，重置Redis中存储的session的有效期
 */
public class SessionExpireFilter implements Filter {
    //注意：导入的是javax.servlet.Filter包
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        //将ServletRequest强转为HttpServletRequest
        HttpServletRequest httpServletRequest = (HttpServletRequest)servletRequest;

        String loginToken = CookieUtil.readLoginCookie(httpServletRequest);

        if (StringUtils.isNotEmpty(loginToken)) {
            //判断loginToken是否为空或""
            //如果不为空，符合条件，继续拿User信息
            String userJsonStr = RedisPoolUtil.get(loginToken);
            User user = JsonUtil.stringToObject(userJsonStr, User.class);
            if (user != null) {
                //use不为空，则重置session的时间
                RedisPoolUtil.expire(loginToken, Const.RedisCacheExTime.REDIS_SESSION_EXTIME);
            }
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {

    }
}
