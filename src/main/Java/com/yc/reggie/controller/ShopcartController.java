package com.yc.reggie.controller;

import java.time.LocalDateTime;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yc.reggie.common.BaseContext;
import com.yc.reggie.common.R;
import com.yc.reggie.dto.DishDto;
import com.yc.reggie.entity.ShoppingCart;
import com.yc.reggie.service.ShopcartService;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping("/shoppingCart")
public class ShopcartController {

    @Autowired
    private ShopcartService shopcartService;

    @GetMapping("/list")
    public R<List<ShoppingCart>> getList() {
        // 查找某位用户的购物车，可以用当前的用户id查
        Long userID = BaseContext.getCurrentId();

        // 构造条件查询器
        LambdaQueryWrapper<ShoppingCart> shopCart = new LambdaQueryWrapper<>();
        shopCart.eq(ShoppingCart::getUserId, userID);
        shopCart.orderByDesc(ShoppingCart::getCreateTime);

        List<ShoppingCart> list = shopcartService.list(shopCart);
        return R.success(list);

    }

    /**
     * 添加购物车
     * 
     * @param sCart
     * @return
     */
    @PostMapping("/add")
    public R<ShoppingCart> addToCart(@RequestBody ShoppingCart sCart) {
        sCart.setUserId(BaseContext.getCurrentId());
        // log.info("接收到要添加的菜品:{}",sCart.getName());
        // log.info("接收到要添加的菜品风味:{}",sCart.getDishFlavor());

        // 需要判断，当添加同一个菜品时，风味相同则数量加一
        Long dishId = sCart.getDishId();

        LambdaQueryWrapper<ShoppingCart> sclWrapper = new LambdaQueryWrapper<>();
        sclWrapper.eq(ShoppingCart::getUserId, BaseContext.getCurrentId());

        if (dishId != null) {
            // 添加的菜品
            sclWrapper.eq(ShoppingCart::getDishId, dishId);
        } else {
            // dishId没有，那这次就是添加套餐
            sclWrapper.eq(ShoppingCart::getSetmealId, sCart.getSetmealId());
        }
        // 查询当前添加的东西是否已在购物车
        ShoppingCart sCart2 = shopcartService.getOne(sclWrapper);

        if (sCart2 != null) {
            Integer number = sCart2.getNumber();
            sCart2.setNumber(number + 1);
            shopcartService.updateById(sCart2);

        } else {
            // 不存在，赠新增，默认数量为1
            sCart.setNumber(1);
            sCart.setCreateTime(LocalDateTime.now());
            shopcartService.save(sCart);
            sCart2 = sCart;
        }
        return R.success(sCart2);
    }

    @PostMapping("/sub")
    public R<String> delCart(@RequestBody ShoppingCart shoppingCart) {
        LambdaQueryWrapper<ShoppingCart> shopCWrapper = new LambdaQueryWrapper<>();
        if (shoppingCart.getDishId() != null) {
            // 如果是删除dish的话
            // 先查出数量

            shopCWrapper.eq(ShoppingCart::getDishId, shoppingCart.getDishId());
            ShoppingCart one = shopcartService.getOne(shopCWrapper);

            if (one.getNumber() == 1) {
                shopcartService.remove(shopCWrapper);
            } else {
                Integer number = one.getNumber();
                one.setNumber(number - 1);
                shopcartService.updateById(one);
            }
        } else {
            shopCWrapper.eq(ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
            ShoppingCart one = shopcartService.getOne(shopCWrapper);

            if (one.getNumber() == 1) {
                shopcartService.remove(shopCWrapper);
            } else {
                Integer number = one.getNumber();
                one.setNumber(number - 1);
                shopcartService.updateById(one);
            }
        }

        return R.success("删除成功！");
    }
}
