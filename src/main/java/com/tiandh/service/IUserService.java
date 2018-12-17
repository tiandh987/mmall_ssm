package com.tiandh.service;

import com.tiandh.common.ServerResponse;
import com.tiandh.pojo.User;

public interface IUserService {

    ServerResponse<User> login(String username, String password);

    ServerResponse<String> register(User user);

    ServerResponse<String> checkValid(String str, String type);

    ServerResponse<String> selectQuestion(String username);

    ServerResponse<String> checkAnswer(String username, String question, String answer);

    ServerResponse<String> forgetResetPassword(String username, String passwordNew, String forgetToken);

    ServerResponse<String> resetPassword(String passwordOld, String passwordNew, User user);

    ServerResponse<User> updateInformation(User user);

    ServerResponse<User> getInformation(int userId);

    //**********************backend***********************
    ServerResponse checkAdminRole(User user);

    ServerResponse getUserList(int pageNum, int pageSize);
}
