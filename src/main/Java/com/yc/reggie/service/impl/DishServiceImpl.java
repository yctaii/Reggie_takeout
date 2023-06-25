package com.yc.reggie.service.impl;

import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yc.reggie.dto.DishDto;
import com.yc.reggie.entity.Dish;
import com.yc.reggie.entity.DishFlavor;
import com.yc.reggie.mapper.DishMapper;
import com.yc.reggie.service.DishFlavorService;
import com.yc.reggie.service.DishSerivice;

@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishSerivice {

    @Autowired
    private DishFlavorService dishFlavorService;

    /***
     * 新增菜品，同时保存对应口味数据
     */
    @Override
    @Transactional
    public void saveWithFalvor(DishDto dishDto) {

        // 保存菜品的基本信息dish
        this.save(dishDto);

        // 菜品口味，保存后有了菜品id，首先获取菜品ID
        Long id = dishDto.getId();

        List<DishFlavor> flavors = dishDto.getFlavors();
        // 遍历flavor集合，为每种口味绑定菜品ID
        flavors = flavors.stream().map((item) -> {
            item.setDishId(id);
            return item;
        }).collect(Collectors.toList());

        dishFlavorService.saveBatch(flavors);
    }

    @Override
    public DishDto getWithFlavor(Long id) {

        // 根据菜品id获取菜品的基本信息
        Dish dish = this.getById(id);
        // 由于回写中还有风味，所以返回对象应该是dishDTO
        // 进行对象拷贝，将菜品基本信息拷贝斤dishDTO
        DishDto dishDto = new DishDto();
        BeanUtils.copyProperties(dish, dishDto);

        // 获取菜品的风味
        // 添加查询条件过滤
        LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(DishFlavor::getDishId, dish.getId());

        List<DishFlavor> list = dishFlavorService.list(lambdaQueryWrapper);

        dishDto.setFlavors(list);

        return dishDto;
    }


    @Transactional
    @Override
    public void updateWF(DishDto dishDto) {
        //更新dish的基本信息
        this.updateById(dishDto);
        
        List<DishFlavor> list = dishDto.getFlavors();

        //删除原有的风味
        LambdaQueryWrapper<DishFlavor> dishFQueryWrapper = new LambdaQueryWrapper<>();
        dishFQueryWrapper.eq(DishFlavor::getDishId,dishDto.getId()); 
        dishFlavorService.remove(dishFQueryWrapper);
        
        //新增风味，是没有dishID的，要给风味绑定dishID
        list = list.stream().map((item)->{
            item.setDishId(dishDto.getId());  
            return item;
        }).collect(Collectors.toList());
        
        dishFlavorService.saveBatch(list);
    }
}
