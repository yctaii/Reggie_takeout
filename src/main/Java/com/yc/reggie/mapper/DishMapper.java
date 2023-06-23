package com.yc.reggie.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yc.reggie.entity.Dish;

@Mapper
public interface DishMapper extends BaseMapper<Dish>{
    
}
