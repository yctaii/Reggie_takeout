package com.yc.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yc.reggie.entity.Orders;



public interface OrdersService extends IService<Orders>{

    void getOrderWithDetail(int page, int pageSize, String number);

    void submitOrder(Orders order);
    
}
