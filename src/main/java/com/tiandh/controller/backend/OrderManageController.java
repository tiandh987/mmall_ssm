package com.tiandh.controller.backend;

import com.github.pagehelper.PageInfo;
import com.tiandh.common.Const;
import com.tiandh.common.ResponseCode;
import com.tiandh.common.ServerResponse;
import com.tiandh.pojo.User;
import com.tiandh.service.IOrderService;
import com.tiandh.service.IUserService;
import com.tiandh.util.CookieUtil;
import com.tiandh.util.JsonUtil;
import com.tiandh.util.RedisPoolUtil;
import com.tiandh.vo.OrderVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/manage/order/")
public class OrderManageController {

    @Autowired
    private IUserService iUserService;
    @Autowired
    private IOrderService iOrderService;


    //查看订单列表
    @RequestMapping("list.do")
    @ResponseBody
    public ServerResponse<PageInfo> orderList(HttpServletRequest httpServletRequest,
                                              @RequestParam(value = "pageNum",defaultValue = "1") int pageNum,
                                              @RequestParam(value = "pageSize",defaultValue = "10") int pageSize){
        //User user = (User)session.getAttribute(Const.CURRENT_USER);
        //从客户端中读取Cookie
        String loginToken = CookieUtil.readLoginCookie(httpServletRequest);
        if (StringUtils.isEmpty(loginToken)) {
            return ServerResponse.createByErrorMessage("用户未登录");
        }
        //从redis中获取User的json字符串
        String userJsonStr = RedisPoolUtil.get(loginToken);
        User user = JsonUtil.stringToObject(userJsonStr, User.class);
        if (user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"未登录，请登录管理员");
        }
        //校验管理员身份
        if(iUserService.checkAdminRole(user).isSuccess()){
            //
            return iOrderService.manageList(pageNum,pageSize);
        }else {
            return ServerResponse.createByErrorMessage("无操作权限");
        }
    }

    //查看订单详情
    @RequestMapping("detail.do")
    @ResponseBody
    public ServerResponse<OrderVo> detail(HttpServletRequest httpServletRequest, Long orderNo){
        //User user = (User)session.getAttribute(Const.CURRENT_USER);
        //从客户端中读取Cookie
        String loginToken = CookieUtil.readLoginCookie(httpServletRequest);
        if (StringUtils.isEmpty(loginToken)) {
            return ServerResponse.createByErrorMessage("用户未登录");
        }
        //从redis中获取User的json字符串
        String userJsonStr = RedisPoolUtil.get(loginToken);
        User user = JsonUtil.stringToObject(userJsonStr, User.class);
        if (user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"未登录，请登录管理员");
        }
        //校验管理员身份
        if(iUserService.checkAdminRole(user).isSuccess()){
            //
            return iOrderService.manageDetail(orderNo);
        }else {
            return ServerResponse.createByErrorMessage("无操作权限");
        }
    }

    //查找订单
    @RequestMapping("search.do")
    @ResponseBody
    public ServerResponse<PageInfo> orderSearch(HttpServletRequest httpServletRequest, Long orderNo,
                                               @RequestParam(value = "pageNum",defaultValue = "1") int pageNum,
                                               @RequestParam(value = "pageSize",defaultValue = "10") int pageSize){
        //User user = (User)session.getAttribute(Const.CURRENT_USER);
        //从客户端中读取Cookie
        String loginToken = CookieUtil.readLoginCookie(httpServletRequest);
        if (StringUtils.isEmpty(loginToken)) {
            return ServerResponse.createByErrorMessage("用户未登录");
        }
        //从redis中获取User的json字符串
        String userJsonStr = RedisPoolUtil.get(loginToken);
        User user = JsonUtil.stringToObject(userJsonStr, User.class);
        if (user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"未登录，请登录管理员");
        }
        //校验管理员身份
        if(iUserService.checkAdminRole(user).isSuccess()){
            //
            return iOrderService.manageSearch(orderNo,pageNum,pageSize);
        }else {
            return ServerResponse.createByErrorMessage("无操作权限");
        }
    }

    //发货
    @RequestMapping("send_goods.do")
    @ResponseBody
    public ServerResponse<String> orderSendGoods(HttpServletRequest httpServletRequest, Long orderNo){
        //User user = (User)session.getAttribute(Const.CURRENT_USER);
        //从客户端中读取Cookie
        String loginToken = CookieUtil.readLoginCookie(httpServletRequest);
        if (StringUtils.isEmpty(loginToken)) {
            return ServerResponse.createByErrorMessage("用户未登录");
        }
        //从redis中获取User的json字符串
        String userJsonStr = RedisPoolUtil.get(loginToken);
        User user = JsonUtil.stringToObject(userJsonStr, User.class);
        if (user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"未登录，请登录管理员");
        }
        //校验管理员身份
        if(iUserService.checkAdminRole(user).isSuccess()){
            //
            return iOrderService.manageSendGoods(orderNo);
        }else {
            return ServerResponse.createByErrorMessage("无操作权限");
        }
    }

}
