package com.tiandh.controller.backend;

import com.tiandh.common.Const;
import com.tiandh.common.ServerResponse;
import com.tiandh.pojo.User;
import com.tiandh.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping(value = "/manage/user/")
public class UserManagerController {

    @Autowired
    private IUserService userService;

    @RequestMapping(value = "login.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> login(String username,String password,HttpSession session){
        ServerResponse<User> response = userService.login(username,password);
        if (response.isSuccess()){
            User user = response.getData();
            if (user.getRole() == Const.Role.ROLE_ADMIN){
                //用户身份：管理员
                session.setAttribute(Const.CURRENT_USER,user);
                return response;
            }else {
                return ServerResponse.createByErrorMessage("不是管理员，无法登陆");
            }
        }
        return response;
    }
}
