package com.tiandh.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @Auther: lenovo
 * @Date: 2018/11/10 11:55
 * @Description: SpringMVC 全局异常处理
 */
@Slf4j
@Component
public class ExceptionResolver implements HandlerExceptionResolver {

    @Override
    public ModelAndView resolveException(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) {
        log.error("{} Exception",httpServletRequest.getRequestURI(),e);

        ModelAndView modelAndView = new ModelAndView(new MappingJackson2JsonView());
        modelAndView.addObject("status", ResponseCode.ERROR.getCode());
        modelAndView.addObject("msg", "接口异常，详情请查看服务端日志的详情");
        modelAndView.addObject("data", e.toString());
        return modelAndView;
    }
}
