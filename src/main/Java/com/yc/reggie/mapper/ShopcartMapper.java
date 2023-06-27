package com.yc.reggie.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yc.reggie.entity.ShoppingCart;


@Mapper
public interface ShopcartMapper extends BaseMapper<ShoppingCart>{
    
}
