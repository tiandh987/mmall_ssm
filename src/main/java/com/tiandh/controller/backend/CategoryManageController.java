package com.tiandh.controller.backend;

import com.tiandh.common.Const;
import com.tiandh.common.ResponseCode;
import com.tiandh.common.ServerResponse;
import com.tiandh.pojo.User;
import com.tiandh.service.ICategoryService;
import com.tiandh.service.IUserService;
import com.tiandh.util.CookieUtil;
import com.tiandh.util.JsonUtil;
import com.tiandh.util.RedisPoolUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/manage/category/")
public class CategoryManageController {

    @Autowired
    private IUserService iUserService;

    @Autowired
    private ICategoryService iCategoryService;

    //添加品类
    @RequestMapping(value = "add_category.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse addCategory(HttpServletRequest httpServletRequest, String categoryName, @RequestParam(value = "parentId",defaultValue = "0")Integer parentId){
        //User user = (User) session.getAttribute(Const.CURRENT_USER);
        //从客户端中读取Cookie
        String loginToken = CookieUtil.readLoginCookie(httpServletRequest);
        if (StringUtils.isEmpty(loginToken)) {
            return ServerResponse.createByErrorMessage("用户未登录");
        }
        //从redis中获取User的json字符串
        String userJsonStr = RedisPoolUtil.get(loginToken);
        User user = JsonUtil.stringToObject(userJsonStr, User.class);
        if (user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录的，请登录");
        }
        //校验用户是否为管理员
        if (iUserService.checkAdminRole(user).isSuccess()){
            //处理分类的逻辑
            return iCategoryService.addCategroy(categoryName,parentId);
        }else {
            return ServerResponse.createByErrorMessage("需管理员权限操作");
        }
    }

    //更新品类名
    @RequestMapping(value = "set_category_name.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse setCategoryName(HttpServletRequest httpServletRequest,Integer categoryId,String categoryName){
        //User user = (User) session.getAttribute(Const.CURRENT_USER);
        //从客户端中读取Cookie
        String loginToken = CookieUtil.readLoginCookie(httpServletRequest);
        if (StringUtils.isEmpty(loginToken)) {
            return ServerResponse.createByErrorMessage("用户未登录");
        }
        //从redis中获取User的json字符串
        String userJsonStr = RedisPoolUtil.get(loginToken);
        User user = JsonUtil.stringToObject(userJsonStr, User.class);
        if (user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录的，请登录");
        }
        //校验用户是否为管理员
        if (iUserService.checkAdminRole(user).isSuccess()){
            //更新categoryName
            return iCategoryService.updateCategoryName(categoryId,categoryName);
        }else {
            return ServerResponse.createByErrorMessage("需管理员权限操作");
        }
    }

    //查询子节点的平级节点
    @RequestMapping(value = "get_category.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse getChildrenParallelCategory(HttpServletRequest httpServletRequest,@RequestParam(value = "categoryId",defaultValue = "0")Integer categoryId){
        //User user = (User) session.getAttribute(Const.CURRENT_USER);
        //从客户端中读取Cookie
        String loginToken = CookieUtil.readLoginCookie(httpServletRequest);
        if (StringUtils.isEmpty(loginToken)) {
            return ServerResponse.createByErrorMessage("用户未登录");
        }
        //从redis中获取User的json字符串
        String userJsonStr = RedisPoolUtil.get(loginToken);
        User user = JsonUtil.stringToObject(userJsonStr, User.class);
        if (user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录的，请登录");
        }
        //校验用户是否为管理员
        if (iUserService.checkAdminRole(user).isSuccess()){
            //查询子节点的平级节点
            return iCategoryService.getChildrenParallelCategory(categoryId);
        }else {
            return ServerResponse.createByErrorMessage("需管理员权限操作");
        }
    }

    //递归查询本节点id及孩子节点的id
    @RequestMapping(value = "get_deep_category.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse getCategoryAndDeepChildrenCategory(HttpServletRequest httpServletRequest,@RequestParam(value = "categoryId",defaultValue = "0") Integer categoryId){
        //User user = (User) session.getAttribute(Const.CURRENT_USER);
        //从客户端中读取Cookie
        String loginToken = CookieUtil.readLoginCookie(httpServletRequest);
        if (StringUtils.isEmpty(loginToken)) {
            return ServerResponse.createByErrorMessage("用户未登录");
        }
        //从redis中获取User的json字符串
        String userJsonStr = RedisPoolUtil.get(loginToken);
        User user = JsonUtil.stringToObject(userJsonStr, User.class);
        if (user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录的，请登录");
        }
        //校验用户是否为管理员
        if (iUserService.checkAdminRole(user).isSuccess()){
            //查询当前节点的id和递归子节点的id
            return iCategoryService.selectCategoryAndChildrenById(categoryId);

        }else {
            return ServerResponse.createByErrorMessage("需管理员权限操作");
        }
    }
}
