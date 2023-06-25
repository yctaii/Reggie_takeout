package com.yc.reggie.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yc.reggie.dto.SetmealDto;
import com.yc.reggie.entity.Setmeal;

public interface SetmealService extends IService<Setmeal>{

    void saveWithSetmeal(SetmealDto setmealDto);

    SetmealDto getWithDish(Long id);

    void modifyWithDish(SetmealDto setmealDto);

    void removeWithDish(List<Long> ids);
    
}
