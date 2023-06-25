package com.yc.reggie.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yc.reggie.common.CustomException;
import com.yc.reggie.dto.SetmealDto;
import com.yc.reggie.entity.Setmeal;
import com.yc.reggie.entity.SetmealDish;
import com.yc.reggie.mapper.SetmealMapper;
import com.yc.reggie.service.SetmealDishService;
import com.yc.reggie.service.SetmealService;

@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {

    @Autowired
    private SetmealService setmealService;

    @Autowired
    private SetmealDishService setmealDishService;

    @Override
    @Transactional
    public void saveWithSetmeal(SetmealDto setmealDto) {
        setmealService.save(setmealDto);

        // 保存菜品关联信息
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        // 前端发来的套餐关联的菜品并未绑定套餐id
        // 为每个菜品绑定套餐id
        // 当保存了套餐基本信息后，框架会为setmealDto注入id
        setmealDishes = setmealDishes.stream().map((item) -> {
            item.setSetmealId(setmealDto.getId());
            return item;

        }).collect(Collectors.toList());
        setmealDishService.saveBatch(setmealDishes);
    }

    @Override
    @Transactional
    public SetmealDto getWithDish(Long id) {
        Setmeal setMeal = this.getById(id);

        SetmealDto setmealDto = new SetmealDto();

        BeanUtils.copyProperties(setMeal, setmealDto);

        // 添加条件过滤器，查询套餐绑定的菜品
        LambdaQueryWrapper<SetmealDish> sQueryWrapper = new LambdaQueryWrapper<>();
        sQueryWrapper.eq(SetmealDish::getSetmealId, setMeal.getId());

        sQueryWrapper.orderByAsc(SetmealDish::getSort).orderByDesc(SetmealDish::getUpdateTime);
        List<SetmealDish> list = setmealDishService.list(sQueryWrapper);

        setmealDto.setSetmealDishes(list);
        return setmealDto;
    }


    @Transactional
    public void modifyWithDish(SetmealDto setmealDto) {
        // 收到修改的请求
        // 修改套餐的基本信息
        setmealService.updateById(setmealDto);

        // 修改套餐绑定的菜品信息
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();

        // 先删除套餐原来绑定的菜品信息
        LambdaQueryWrapper<SetmealDish> sQueryWrapper = new LambdaQueryWrapper<>();
        sQueryWrapper.eq(SetmealDish::getSetmealId, setmealDto.getId());
        setmealDishService.remove(sQueryWrapper);

        // 再重新绑定新的菜品信息
        // 先要为绑定的菜品赋予套餐分类的id
        setmealDishes = setmealDishes.stream().map((item) -> {
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());

        setmealDishService.saveBatch(setmealDishes);
    }


    @Transactional
    public void removeWithDish(List<Long> ids) {
        //查询套餐状态，是否可以删除
        //select count(*) from setmeal where id in (1,2,3) and status = 1
        LambdaQueryWrapper<Setmeal> sWrapper = new LambdaQueryWrapper<>();
        sWrapper.in(Setmeal::getId,ids);
        sWrapper.eq(Setmeal::getStatus, 1);
        
        int count = this.count(sWrapper);
        //如果count>0说明发出的套餐有的正在出售
        if(count > 0){
            throw new CustomException("有套餐正在出售，无法删除！");
        }

        //如果可以删除，先删除套餐表中的数据
        setmealService.removeByIds(ids);

        //删除关系表中的数据，
        //delete * from setmeal_dish where setmeal_id in (1,2,3)
        LambdaQueryWrapper<SetmealDish> sQueryWrapper = new LambdaQueryWrapper<>();
        sQueryWrapper.in(SetmealDish::getSetmealId,ids);

        setmealDishService.remove(sQueryWrapper);


    }

}
