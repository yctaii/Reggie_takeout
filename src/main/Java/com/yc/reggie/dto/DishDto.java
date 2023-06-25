package com.yc.reggie.dto;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

import com.yc.reggie.entity.Dish;
import com.yc.reggie.entity.DishFlavor;

@Data
public class DishDto extends Dish {

    private List<DishFlavor> flavors = new ArrayList<>();

    private String categoryName;

    private Integer copies;
}
