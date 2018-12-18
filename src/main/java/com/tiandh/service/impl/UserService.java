package com.tiandh.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.tiandh.common.Const;
import com.tiandh.common.ServerResponse;
import com.tiandh.dao.UserMapper;
import com.tiandh.pojo.User;
import com.tiandh.service.IUserService;
import com.tiandh.util.DateTimeUtil;
import com.tiandh.util.MD5Util;
import com.tiandh.util.RedisShardedPoolUtil;
import com.tiandh.vo.UserVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class UserService implements IUserService {

    @Autowired
    private UserMapper userMapper;

    //登录
    @Override
    public ServerResponse<User> login(String username, String password) {
        //检查用户名是否存在
        int resultCount = userMapper.checkUsername(username);
        if(resultCount == 0){
            return ServerResponse.createByErrorMessage("用户名不存在");
        }

        //密码登录MD5
        String md5Password = MD5Util.MD5EncodeUtf8(password);

        User user = userMapper.selectLogin(username,md5Password);
        if(user == null){
            return ServerResponse.createByErrorMessage("密码错误");
        }

        //将密码置空
        user.setPassword(StringUtils.EMPTY);
        return ServerResponse.createBySuccess("登录成功",user);
    }

    //注册
    @Override
    public ServerResponse<String> register(User user){

        //检查用户名
        ServerResponse validResponse = this.checkValid(user.getUsername(),Const.USERNAME);
        if (!validResponse.isSuccess() ){
            return validResponse;
        }

        //检查email
        validResponse = this.checkValid(user.getEmail(),Const.EMAIL);
        if (!validResponse.isSuccess()){
            return validResponse;
        }

        //设置用户级别为普通用户
        user.setRole(Const.Role.ROLE_CUSTOMER);

        //MD5加密
        user.setPassword(MD5Util.MD5EncodeUtf8(user.getPassword()));

        //插入数据库表中
        int resultCount = userMapper.insert(user);
        if (resultCount == 0){
            return ServerResponse.createByErrorMessage("注册失败");
        }

        return ServerResponse.createBySuccessMessage("注册成功");
    }

    //检验用户名、邮箱
    @Override
    public ServerResponse<String> checkValid(String str,String type){
        if (StringUtils.isNotBlank(type)){
            //开始校验
            if(Const.USERNAME.equals(type)){
                int resultCount = userMapper.checkUsername(str);
                System.out.println("resultCount:"+resultCount);
                if (resultCount > 0){
                    return ServerResponse.createByErrorMessage("用户名已存在");
                }
            }
            if (Const.EMAIL.equals(type)) {
                int resultCount = userMapper.checkEmail(str);
                if(resultCount > 0){
                    return ServerResponse.createByErrorMessage("email已被使用");
                }
            }
        }else {
            return ServerResponse.createByErrorMessage("参数错误");
        }
        return ServerResponse.createBySuccessMessage("校验成功");
    }

    //忘记密码问题
    @Override
    public ServerResponse<String> selectQuestion(String username) {
        ServerResponse validResponse = this.checkValid(username,Const.USERNAME);
        if (validResponse.isSuccess()){
            //用户不存在
            return ServerResponse.createByErrorMessage("用户不存在");
        }
        String question = userMapper.selectQuestionByUsername(username);
        if (StringUtils.isNotBlank(question)){
            return ServerResponse.createBySuccess(question);
        }
        return ServerResponse.createByErrorMessage("找回密码的问题为空");
    }

    //检验密码问题答案
    @Override
    public ServerResponse<String> checkAnswer(String username, String question, String answer) {
        int resultCount = userMapper.checkAnswer(username,question,answer);
        if (resultCount > 0){
            //问题及问题答案是该用户的，并且是正确的
            String forgetToken = UUID.randomUUID().toString();
            RedisShardedPoolUtil.setex(Const.TOKEN_PREFIX + username, 60*60*12, forgetToken);
            return ServerResponse.createBySuccess(forgetToken);
        }
        return ServerResponse.createByErrorMessage("问题的答案错误");
    }

    //忘记密码->重置密码
    @Override
    public ServerResponse<String> forgetResetPassword(String username, String passwordNew, String forgetToken) {

        if(StringUtils.isBlank(forgetToken)){
            return ServerResponse.createByErrorMessage("参数错误，token需要传递");
        }

        ServerResponse validResponse = this.checkValid(username,Const.USERNAME);
        if(validResponse.isSuccess()){
            //用户不存在
            return ServerResponse.createByErrorMessage("用户不存在");
        }

        String token = RedisShardedPoolUtil.get(Const.TOKEN_PREFIX + username);
        if (StringUtils.isBlank(token)){
            return ServerResponse.createByErrorMessage("Token无效或过期");
        }

        if (StringUtils.equals(forgetToken,token)){
            String md5Password = MD5Util.MD5EncodeUtf8(passwordNew);
            int resultCount = userMapper.updatePasswordByUsername(username,md5Password);
            if (resultCount > 0){
                return ServerResponse.createBySuccessMessage("修改密码成功");
            }
        }else {
            return ServerResponse.createByErrorMessage("token错误，请重新获取重置密码的token");
        }

        return ServerResponse.createByErrorMessage("密码修改失败");
    }

    //登录状态->重置密码
    @Override
    public ServerResponse<String> resetPassword(String passwordOld, String passwordNew, User user) {
        //防止横向越权，要检验一下这个用户的旧密码，一定要指定是这个用户
        int resultCount = userMapper.checkPassword(MD5Util.MD5EncodeUtf8(passwordOld),user.getId());
        if (resultCount == 0){
            return ServerResponse.createByErrorMessage("旧密码错误");
        }
        user.setPassword(MD5Util.MD5EncodeUtf8(passwordNew));
        int updateCount = userMapper.updateByPrimaryKeySelective(user);
        if (updateCount > 0){
            return ServerResponse.createBySuccessMessage("密码更新成功");
        }
        return ServerResponse.createByErrorMessage("密码更新失败");
    }

    //登录状态->修改用户信息
    @Override
    public ServerResponse<User> updateInformation(User user) {
        //username 不能更新
        //检查新的email是否被其他用户占用
        int resultCount = userMapper.checkEmailByUserId(user.getEmail(),user.getId());
        if (resultCount > 0){
            return ServerResponse.createByErrorMessage("email已经被其他用户使用");
        }
        User updateUser = new User();
        updateUser.setId(user.getId());
        updateUser.setEmail(user.getEmail());
        updateUser.setPhone(user.getPhone());
        updateUser.setQuestion(user.getQuestion());
        updateUser.setAnswer(user.getAnswer());

        int updateCount = userMapper.updateByPrimaryKeySelective(updateUser);
        if (updateCount > 0){
            return ServerResponse.createBySuccess("更新个人信息成功",updateUser);
        }
        return ServerResponse.createByErrorMessage("更新个人信息失败");
    }

    //获取用户信息
    @Override
    public ServerResponse<User> getInformation(int userId) {
            User user = userMapper.selectByPrimaryKey(userId);
            if(user == null){
                return ServerResponse.createByErrorMessage("找不到当前用户");
            }
            //密码置空
            user.setPassword(org.apache.commons.lang3.StringUtils.EMPTY);
            return ServerResponse.createBySuccess(user);
    }

    //*********************backend****************************
    //校验用户身份是否为管理员
    @Override
    public ServerResponse checkAdminRole(User user) {
        if(user != null && user.getRole().intValue() == Const.Role.ROLE_ADMIN){
            return ServerResponse.createBySuccess();
        }
        return ServerResponse.createByError();
    }

    // 管理员获取非管理员用户列表
    @Override
    public ServerResponse getUserList(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize );

        List<User> userList = userMapper.selectList();
        List<UserVo> userVoList = Lists.newArrayList();

        for (User user : userList) {
            UserVo userVo = new UserVo();
            userVo.setId(user.getId());
            userVo.setUsername(user.getUsername());
            userVo.setPhone(user.getPhone());
            userVo.setEmail(user.getEmail());
            userVo.setCreateTime(DateTimeUtil.dateToStr(user.getCreateTime()));
            userVoList.add(userVo);
        }

        PageInfo pageResult = new PageInfo(userList);
        pageResult.setList(userVoList);

        return  ServerResponse.createBySuccess(pageResult);
    }
}
