package com.yc.reggie.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yc.reggie.common.CustomException;
import com.yc.reggie.entity.Category;
import com.yc.reggie.entity.Dish;
import com.yc.reggie.entity.Setmeal;
import com.yc.reggie.mapper.CategoryMapper;
import com.yc.reggie.service.CategoryService;
import com.yc.reggie.service.DishSerivice;
import com.yc.reggie.service.SetmealService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Autowired
    private DishSerivice dishSerivice;

    @Autowired
    private SetmealService setmealService;

    @Override
    public void removeCate(Long id) {

        // 查询是否有关联菜品
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        // 添加条件查询，是否有关联分类id的菜品
        dishLambdaQueryWrapper.eq(Dish::getCategoryId, id);
        // 执行查询
        int count1 = dishSerivice.count(dishLambdaQueryWrapper);

        // 查询是否有关联套餐
        LambdaQueryWrapper<Setmeal> setMeallambdaQueryWrapper = new LambdaQueryWrapper<>();
        // 添加条件查询，是否有关联分类id的套餐
        setMeallambdaQueryWrapper.eq(Setmeal::getCategoryId, id);
        // 执行查询
        int count2 = setmealService.count(setMeallambdaQueryWrapper);

        if (count1 > 0) {
            // 有关联的菜品，不能删除该分类
            throw new CustomException("有关联的菜品，不能删除该分类");
        }

        if (count2 > 0) {
            // 有关联的套餐，不能删除该分类
            throw new CustomException("有关联的套餐，不能删除该分类");
        }

        // 跳过了两个if，说明没有关联的菜品和套餐，直接删除
        super.removeById(id);

    }

}
