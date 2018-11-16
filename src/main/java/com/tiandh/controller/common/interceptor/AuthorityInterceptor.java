package com.tiandh.controller.common.interceptor;

import com.github.pagehelper.util.StringUtil;
import com.google.common.collect.Maps;
import com.tiandh.common.Const;
import com.tiandh.common.ServerResponse;
import com.tiandh.pojo.User;
import com.tiandh.util.CookieUtil;
import com.tiandh.util.JsonUtil;
import com.tiandh.util.RedisShardedPoolUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.AsyncHandlerInterceptor;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.json.Json;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

/**
 * @Auther: lenovo
 * @Date: 2018/11/10 20:32
 * @Description: SpringMVC 请求拦截器
 */
@Slf4j
public class AuthorityInterceptor implements HandlerInterceptor {

    /*
    进入Controller层之前调用；
    返回值为true，调用Controller层的方法；
    返回值为false，不再进入Controller层，重写response，返回到页面。
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        log.info("preHandle");

        //请求中Controller中的方法名
        HandlerMethod handlerMethod = (HandlerMethod) handler;

        //解析HandlerMethod
        String methodName = handlerMethod.getMethod().getName();
        String className = handlerMethod.getBean().getClass().getSimpleName();

        //解析参数
        StringBuffer requestParamBuffer = new StringBuffer();
        Map paramMap = request.getParameterMap();
        Iterator iterator = paramMap.entrySet().iterator();
        while (iterator.hasNext()){
            Map.Entry entry = (Map.Entry)iterator.next();
            String mapKey = (String) entry.getKey();
            String mapValue = StringUtils.EMPTY;
            //request这个参数的map，里面的value返回的是一个String数组,先用Object接收
            Object object = entry.getValue();
            if (object instanceof String[]){
                String[] strings = (String[]) object;
                mapValue = Arrays.toString(strings);
            }
            requestParamBuffer.append(mapKey).append("=").append(mapValue);
        }

        //拦截到UserManagerController类的login方法
        if (StringUtils.equals(className, "UserManagerController") && StringUtils.equals(methodName, "login")){
            log.info("权限拦截器拦截到登录请求，calssName:{},methodName:{}",className,methodName);
            //拦截到登录请求，不打印参数，因为参数里面有密码
            //拦截到登录请求，立即交由Controller层处理
            return true;
        }

        //将拦截到的所有非登录请求打印到控制台
        log.info("权限拦截器拦截到请求，calssName:{},methodName:{},params:{}",className,methodName,requestParamBuffer.toString());

        //根据request在Redis中获取User
        User user = null;
        String loginToken = CookieUtil.readLoginCookie(request);
        if (StringUtils.isNotEmpty(loginToken)){
            String userJsonStr = RedisShardedPoolUtil.get(loginToken);
            user = JsonUtil.stringToObject(userJsonStr, User.class);
        }

        //对User进行校验
        if (user == null || (user.getRole() != Const.Role.ROLE_ADMIN)){
            //user为空或者不是管理员，返回false
            response.reset();//添加reset，否则会报异常
            response.setCharacterEncoding("UTF-8");//设置编码，否则会乱码
            response.setContentType("application/json;charset=UTF-8");//设置返回值的类型

            PrintWriter out = response.getWriter();
            if (user == null){
                //用户为空
                if (StringUtils.equals(className, "ProductManageController") && StringUtils.equals(methodName, "richtextImgUpload")){
                    //拦截到富文本图片上传的请求,用户未登录
                    Map resultMap = Maps.newHashMap();
                    resultMap.put("success",false);
                    resultMap.put("msg","请登录管理员账号");
                    out.print(JsonUtil.objectToString(JsonUtil.objectToString(resultMap)));
                }else {
                    out.print(JsonUtil.objectToString(ServerResponse.createByErrorMessage("拦截器拦截，用户未登录")));
                }
            }else {
                //用户非管理员
                if (StringUtils.equals(className, "ProductManageController") && StringUtils.equals(methodName, "richtext_img_upload")){
                    //拦截到富文本图片上传的请求，无权限操作
                    Map resultMap = Maps.newHashMap();
                    resultMap.put("success",false);
                    resultMap.put("msg","无权限操作");
                    out.print(JsonUtil.objectToString(JsonUtil.objectToString(resultMap)));
                }else {
                    out.print(JsonUtil.objectToString(ServerResponse.createByErrorMessage("拦截器拦截，无权限操作")));
                }
            }
            out.flush();//将out流中的数据清空
            out.close();//将out流关闭

            return false;
        }
        return true;
    }

    //Controller层处理完成之后调用，与preHandle方法相对应
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        log.info("postHandle");
    }

    /*
    所有请求处理完成之后调用。
    例如：前后端不分离的项目，当ModelAndView返回到前端页面并展示出来之后，才调用此方法
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        log.info("afterCompletion");
    }
}
