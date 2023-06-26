package com.yc.reggie.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yc.reggie.entity.OrderDetail;
import com.yc.reggie.mapper.OrderDetailMapper;
import com.yc.reggie.service.OrderDetailService;

@Service
public class OrderDetailServiceImpl extends ServiceImpl<OrderDetailMapper,OrderDetail> implements OrderDetailService{
    
}
