package com.yc.reggie.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yc.reggie.dto.OrdersDto;
import com.yc.reggie.entity.Orders;



public interface OrdersService extends IService<Orders>{

    void getOrderWithDetail(int page, int pageSize, String number);

    void submitOrder(Orders order);

    Page<OrdersDto> getUserOrder(int page, int pageSize);

    void againOrder(Long id);
    
}
