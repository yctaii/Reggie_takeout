package com.yc.reggie.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yc.reggie.common.R;
import com.yc.reggie.dto.DishDto;
import com.yc.reggie.entity.Category;
import com.yc.reggie.entity.Dish;
import com.yc.reggie.entity.DishFlavor;
import com.yc.reggie.service.CategoryService;
import com.yc.reggie.service.DishFlavorService;
import com.yc.reggie.service.DishSerivice;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/dish")
public class DishController {
    @Autowired
    private DishSerivice dishSerivice;

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private CategoryService categoryService;

    /**
     * 菜品信息分页查询
     * 
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> pageSele(int page, int pageSize, String name) {

        // 构建分页构造器
        Page<Dish> pageInfo = new Page<>(page, pageSize);
        Page<DishDto> dtoInfo = new Page<>();

        // 构建条件查询
        LambdaQueryWrapper<Dish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        // 当页面发出包含菜名的查询，即name不空时，构造查询器
        lambdaQueryWrapper.like(StringUtils.isNotEmpty(name), Dish::getName, name);

        lambdaQueryWrapper.orderByDesc(Dish::getPrice);

        // 执行分页查询
        dishSerivice.page(pageInfo, lambdaQueryWrapper);

        // 对象拷贝，dish中没有分类名称，而dishdto有
        BeanUtils.copyProperties(pageInfo, dtoInfo, "records");

        // 对页面展示的Records单独处理，添加上分类
        List<Dish> records = pageInfo.getRecords();
        List<DishDto> list = records.stream().map((item) -> {
            DishDto dishDto = new DishDto();

            // 首先把records中的dish基本属性拷贝
            BeanUtils.copyProperties(item, dishDto);

            // 获取新增菜品的菜品分类id
            Long catID = item.getCategoryId();

            // 查询出对应菜品分类对象
            Category category = categoryService.getById(catID);

            if (category != null) {
                String catName = category.getName();
                dishDto.setCategoryName(catName);
            }

            return dishDto;
        }).collect(Collectors.toList());

        dtoInfo.setRecords(list);
        return R.success(dtoInfo);
    }

    /***
     * 新增菜品，因为有风味，所以不能单纯用dish
     * 而是用dishdto
     * 
     * @param dishDto
     * @return
     */
    @PostMapping
    public R<String> addDish(@RequestBody DishDto dishDto) {
        dishSerivice.saveWithFalvor(dishDto);

        return R.success("新增菜品成功！");
    }

    /**
     * 获取菜品信息，且获取风味信息
     * 
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<DishDto> getDTO(@PathVariable Long id) {
        DishDto withFlavor = dishSerivice.getWithFlavor(id);
        return R.success(withFlavor);
    }

    /**
     * 更新菜品信息，且更新风味
     * 
     * @param dishDto
     * @return
     */
    @PutMapping
    public R<String> updateWithFlavor(@RequestBody DishDto dishDto) {
        dishSerivice.updateWF(dishDto);
        return R.success("更新成功！");
    }

    @DeleteMapping
    public R<String> deleteDish(Long[] ids) {
        for (int ids2 = 0; ids2 < ids.length; ids2++) {
            dishSerivice.removeById(ids[ids2]);
            LambdaQueryWrapper<DishFlavor> dishFQueryWrapper = new LambdaQueryWrapper<>();
            dishFQueryWrapper.eq(DishFlavor::getDishId,ids[ids2]);

            dishFlavorService.remove(dishFQueryWrapper);
        }

        return R.success("删除成功！");
    }

    @PostMapping("/status/{s}")
    public R<String> changeDish(@PathVariable Integer s,Long[] ids) {
    
        // log.info("接收到状态,{}",s);
        // log.info("接收到要更改状态的菜品id{}",ids[0]);
        
      for (int id = 0; id < ids.length; id++) {
            Dish dish = new Dish();
            dish.setId(ids[id]);
            dish.setStatus(s);
            dishSerivice.updateById(dish);
      }

        return R.success("操作成功");
    }


    /**
     * 根据条件，查询菜品数据
     * @param dish
     * @return
     */
    @GetMapping("/list")
    public R<List<Dish>> getCat(Dish dish){

        LambdaQueryWrapper<Dish> dLambdaQueryWrapper = new LambdaQueryWrapper<>();
        dLambdaQueryWrapper.eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId());

        //添加条件，查询在售菜品
        dLambdaQueryWrapper.eq(Dish::getStatus,1);
        dLambdaQueryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);

        List<Dish> dList = dishSerivice.list(dLambdaQueryWrapper);


        return R.success(dList);
    }

}
