package com.yc.reggie.controller;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yc.reggie.common.CustomException;
import com.yc.reggie.common.R;
import com.yc.reggie.dto.SetmealDto;
import com.yc.reggie.entity.Category;
import com.yc.reggie.entity.Setmeal;
import com.yc.reggie.service.CategoryService;
import com.yc.reggie.service.SetmealService;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping("/setmeal")
public class SetmealController {

    @Autowired
    private SetmealService setmealService;

    @Autowired
    private CategoryService categoryService;


    @GetMapping("/page")
    public R<Page> getSetMeal(int page, int pageSize, String name) {

        // 构造分页构造器
        Page<Setmeal> pageInfo = new Page<>(page, pageSize);
        LambdaQueryWrapper<Setmeal> sQueryWrapper = new LambdaQueryWrapper<>();

        // 设置过滤条件
        sQueryWrapper.like(StringUtils.isNotEmpty(name), Setmeal::getName, name);

        setmealService.page(pageInfo, sQueryWrapper);

        // 分页信息中还包含了套餐分类，还需要查询Category表，并设置categoryName
        // 所以返回的页面信息应该是SetmealDto
        Page<SetmealDto> pInfo = new Page<>();
        BeanUtils.copyProperties(pageInfo, pInfo, "records");

        // 因为records包含的范型不同,所以拷贝时忽略
        List<Setmeal> records = pageInfo.getRecords();
        List<SetmealDto> list = records.stream().map((item) -> {
            SetmealDto setmealDto = new SetmealDto();
            BeanUtils.copyProperties(item, setmealDto);

            Long catId = item.getCategoryId();

            Category byId = categoryService.getById(catId);
            ;
            if (byId != null) {
                setmealDto.setCategoryName(byId.getName());
            }

            return setmealDto;
        }).collect(Collectors.toList());

        pInfo.setRecords(list);
        return R.success(pInfo);
    }

    /**
     * 新增菜品分类
     * 
     * @param setmealDto
     * @return
     */
    @PostMapping
    public R<String> saveSetMealDish(@RequestBody SetmealDto setmealDto) {

        setmealService.saveWithSetmeal(setmealDto);

        return R.success("新增成功！");
    }

    /**
     * 获取套餐的信息，回写到页面
     * 
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<SetmealDto> getSetmeal(@PathVariable Long id) {
        // log.info("收到要修改的套餐id：{}",id);

        SetmealDto setmealDto = setmealService.getWithDish(id);

        return R.success(setmealDto);
    }

    /**
     * 修改套餐
     * 
     * @param setmealDto
     * @return
     */
    @PutMapping
    public R<String> modify(@RequestBody SetmealDto setmealDto) {

        setmealService.modifyWithDish(setmealDto);

        return R.success("更新成功");
    }

    /**
     * 更改禁售状态
     * 
     * @param s
     * @param ids
     * @return
     */
    @PostMapping("/status/{s}")
    public R<String> changeStatus(@PathVariable Integer s, @RequestParam List<Long> ids){
        LambdaQueryWrapper<Setmeal> setmQueryWrapper = new LambdaQueryWrapper<>();    
        setmQueryWrapper.in(Setmeal::getId,ids);

        List<Setmeal> collect = setmealService.list(setmQueryWrapper).stream().map((item->{
            item.setStatus(s);
            return item;
        })).collect(Collectors.toList());

        setmealService.updateBatchById(collect);

        return R.success("更改售卖状态成功！");
    }



    /**
     * 当请求中有多个同一类型数据，可以用list封装
     * 要用@RequestParam注解
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> deleteSetmeal(@RequestParam List<Long> ids) {
        if(ids.size() == 0){
            throw new CustomException("请选中要批量删除的套餐！");
        }
        setmealService.removeWithDish(ids);
        return R.success("删除成功!");
    }


    @GetMapping("/list")
    public R<List<Setmeal>> getSetMeal(SetmealDto setmealDto){
        // log.info("收到要查询的Setmeal{}",setmealDto.getCategoryId());
        LambdaQueryWrapper<Setmeal> sWrapper = new LambdaQueryWrapper<>();
        sWrapper.eq(Setmeal::getCategoryId,setmealDto.getCategoryId());
        sWrapper.eq(Setmeal::getStatus,setmealDto.getStatus());

        List<Setmeal> list = setmealService.list(sWrapper);;
        return R.success(list);
    }
}
