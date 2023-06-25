package com.yc.reggie.dto;

import lombok.Data;
import java.util.List;

import com.yc.reggie.entity.Setmeal;
import com.yc.reggie.entity.SetmealDish;

@Data
public class SetmealDto extends Setmeal {

    private List<SetmealDish> setmealDishes;

    private String categoryName;
}
