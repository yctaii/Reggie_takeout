package com.yc.reggie.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yc.reggie.entity.Category;

@Mapper
public interface CategoryMapper extends BaseMapper<Category>{
    
}
