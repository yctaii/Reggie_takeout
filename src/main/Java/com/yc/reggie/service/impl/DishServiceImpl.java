package com.yc.reggie.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yc.reggie.entity.Dish;
import com.yc.reggie.mapper.DishMapper;
import com.yc.reggie.service.DishSerivice;

@Service
public class DishServiceImpl extends ServiceImpl<DishMapper,Dish> implements DishSerivice{
    
}
