package com.yc.reggie.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yc.reggie.entity.ShoppingCart;
import com.yc.reggie.mapper.ShopcartMapper;
import com.yc.reggie.service.ShopcartService;

@Service
public class ShopcartServiceImpl extends ServiceImpl<ShopcartMapper,ShoppingCart> implements ShopcartService{

}