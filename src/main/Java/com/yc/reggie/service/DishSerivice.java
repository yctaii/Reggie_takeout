package com.yc.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yc.reggie.dto.DishDto;
import com.yc.reggie.entity.Dish;

public interface DishSerivice extends IService<Dish>{
    public void saveWithFalvor(DishDto dishDto);

    public DishDto getWithFlavor(Long id);

    public void updateWF(DishDto dishDto);
}
