package com.yc.reggie.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yc.reggie.entity.Category;
import com.yc.reggie.mapper.CategoryMapper;
import com.yc.reggie.service.CategoryService;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService{
    
}
