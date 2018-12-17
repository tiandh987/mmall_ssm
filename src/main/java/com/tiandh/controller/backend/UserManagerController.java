package com.tiandh.controller.backend;

import com.tiandh.common.Const;
import com.tiandh.common.ServerResponse;
import com.tiandh.pojo.User;
import com.tiandh.service.IUserService;
import com.tiandh.util.CookieUtil;
import com.tiandh.util.JsonUtil;
import com.tiandh.util.RedisShardedPoolUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@Controller
@RequestMapping(value = "/manage/user/")
public class UserManagerController {

    @Autowired
    private IUserService userService;

    // 管理员登录
    @RequestMapping(value = "login.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> login(String username, String password, HttpSession session, HttpServletResponse httpServletResponse){
        ServerResponse<User> response = userService.login(username,password);
        if (response.isSuccess()){
            User user = response.getData();
            if (user.getRole() == Const.Role.ROLE_ADMIN){
                //用户身份：管理员
                //session.setAttribute(Const.CURRENT_USER,user);
                //新增redis共享cookie，session的方式
                RedisShardedPoolUtil.setex(session.getId(), Const.RedisCacheExTime.REDIS_SESSION_EXTIME, JsonUtil.objectToString(user));
                CookieUtil.writeLoginToken(httpServletResponse, session.getId());
                return response;
            }else {
                return ServerResponse.createByErrorMessage("不是管理员，无法登陆");
            }
        }
        return response;
    }

    // 管理员获取非管理员用户列表
    @RequestMapping(value = "getUserList.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse getUserList(@RequestParam(value = "pageNum",defaultValue = "1") Integer pageNum,
                                      @RequestParam(value = "pageSize",defaultValue = "10") Integer pageSize){
        //全部通过拦截器验证，已登录管理员账号
        return userService.getUserList(pageNum, pageSize);
    }
}
