package com.yc.reggie.service.impl;

import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yc.reggie.dto.OrdersDto;
import com.yc.reggie.entity.OrderDetail;
import com.yc.reggie.entity.Orders;
import com.yc.reggie.mapper.OrdersMapper;
import com.yc.reggie.service.OrderDetailService;
import com.yc.reggie.service.OrdersService;

@Service
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper,Orders> implements OrdersService{

    @Autowired
    private OrderDetailService orderDetailService;

    @Override
    @Transactional
    public void getOrderWithDetail(int page, int pageSize, String number) {
        //构建分页构造器
        Page<Orders> oPage = new Page<>(page,pageSize);

        //构造条件过滤器
        LambdaQueryWrapper<Orders> oLambdaQueryWrapper = new LambdaQueryWrapper<>();
        oLambdaQueryWrapper.like(number != null, Orders::getNumber, number);
        oLambdaQueryWrapper.orderByDesc(Orders::getCheckoutTime);

        this.page(oPage, oLambdaQueryWrapper);

        //因为还有订单详情信息，所以要查order_detail表
        Page<OrderDetail> odPage = new Page<>();

        //由于两个page的范型不一样，所以拷贝是忽略掉records
        BeanUtils.copyProperties(oPage, odPage, "records");

        List<Orders> orecords = oPage.getRecords();
        orecords.stream().map((item)->{
            OrdersDto ordersDto = new OrdersDto();
            BeanUtils.copyProperties(item, ordersDto);

            

            return item;
        });
    }
    
}
