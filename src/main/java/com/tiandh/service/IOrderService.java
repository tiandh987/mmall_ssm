package com.tiandh.service;

import com.tiandh.common.ServerResponse;

import java.util.Map;

public interface IOrderService {

    //*********************对接支付宝******************************
    ServerResponse pay(Integer userId,Long orderNo,String path);

    ServerResponse aliCallback(Map<String,String> params);

    ServerResponse queryOrderPayStatus(Integer userId,Long orderNo);
}
