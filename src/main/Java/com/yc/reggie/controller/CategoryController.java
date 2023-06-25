package com.yc.reggie.controller;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yc.reggie.common.R;
import com.yc.reggie.entity.Category;
import com.yc.reggie.service.CategoryService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/category")
public class CategoryController {
    @Autowired 
    private CategoryService categoryService;


    /**
     * 新增菜品分类
     * @param category
     * @return
     */    
    @PostMapping
    public R<String> addCategory(@RequestBody Category category){

        log.info("接收到新分类：{}" , category);

        categoryService.save(category);

        return R.success("新增分类成功");
    }


    /**
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> pageCategory(int page, int pageSize,String name) {
        
        log.info("收到菜类分页查询", page, pageSize,name);

        //构造分页构造器
        Page pageInfo = new Page<>(page,pageSize);

        //接收到name时，构造条件构造器
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotEmpty(name), Category::getName, name);
        
        //添加排序条件
        queryWrapper.orderByAsc(Category::getSort);
        //执行分页查询
        categoryService.page(pageInfo, queryWrapper);
        
        return R.success(pageInfo);
    }


    @DeleteMapping
    public R<String> delete(Long ids){
        log.info("接收到删除ID：{}",ids);
        //当分类关联了菜品或套餐时，不能直接删除，而是抛异常，
        //所以不能用Mybatis提供的删除方法
        // categoryService.removeById(ids);
        categoryService.removeCate(ids);
        return R.success("删除成功");
    }

    /**
     * 根据id修改分类信息
     * @param category
     * @return
     */
    @PutMapping
    public R<String> modify(@RequestBody Category category){
        categoryService.updateById(category);
        return R.success("修改成功");
    }


    @GetMapping("/list")
    public R<List<Category>> getCate(Category category){
        
        //条件构造器
        LambdaQueryWrapper<Category> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        //添加条件
        lambdaQueryWrapper.eq(category.getType() != null, Category::getType, category.getType());
        //添加排序条件
        lambdaQueryWrapper.orderByAsc(Category::getSort).orderByDesc(Category::getUpdateTime);

        List<Category> list = categoryService.list(lambdaQueryWrapper);


        return R.success(list);
    }

}
