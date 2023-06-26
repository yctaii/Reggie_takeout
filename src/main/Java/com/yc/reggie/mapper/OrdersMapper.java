package com.yc.reggie.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yc.reggie.entity.Orders;

@Mapper
public interface OrdersMapper extends BaseMapper<Orders>{
    
}
