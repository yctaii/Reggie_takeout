package com.yc.reggie.controller;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.jaxb.SpringDataJaxb.OrderDto;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yc.reggie.common.BaseContext;
import com.yc.reggie.common.R;
import com.yc.reggie.dto.DishDto;
import com.yc.reggie.dto.OrdersDto;
import com.yc.reggie.entity.DishFlavor;
import com.yc.reggie.entity.OrderDetail;
import com.yc.reggie.entity.Orders;
import com.yc.reggie.service.OrderDetailService;
import com.yc.reggie.service.OrdersService;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping("/order")
public class OrdersController {

    @Autowired
    private OrdersService ordersService;

    @Autowired
    private OrderDetailService orderDetailService;

    /**
     * 用户下单，提交给服务器
     * 
     * @param order
     * @return
     */
    @PostMapping("/submit")
    public R<String> submitOrder(@RequestBody Orders order) {
        // 收到提交的订单
        // log.info("{}",order.getAddressBookId(),order.getRemark());
        // 为订单榜上用户id
        ordersService.submitOrder(order);

        return R.success("提交成功！");
    }

    /**
     * 前端用户查询用户自己的订单
     * 
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/userPage")
    public R<Page> getOrder(int page, int pageSize) {
        // log.info("接收到查询请求{}{}",page,pageSize);

        Page<Orders> orderPage = new Page<>(page, pageSize);

        // 设置条件查询过滤
        Long userId = BaseContext.getCurrentId();
        LambdaQueryWrapper<Orders> oWrapper = new LambdaQueryWrapper<>();
        oWrapper.eq(Orders::getUserId, userId);
        oWrapper.orderByDesc(Orders::getCheckoutTime);

        List<Orders> oList = ordersService.list(oWrapper);

        List<OrdersDto> orderDtos = oList.stream().map((item) -> {

            OrdersDto ordersDto = new OrdersDto();
            BeanUtils.copyProperties(item, ordersDto);
            // 因为每次的过滤条件不一样，所以要每次都要new一下
            LambdaQueryWrapper<OrderDetail> odetaiLambdaQueryWrapper = new LambdaQueryWrapper<>();

            odetaiLambdaQueryWrapper.eq(OrderDetail::getOrderId, item.getId());

            List<OrderDetail> oDetaillist = orderDetailService.list(odetaiLambdaQueryWrapper);
            ordersDto.setOrderDetails(oDetaillist);

            return ordersDto;

        }).collect(Collectors.toList());

        ordersService.page(orderPage, oWrapper);
        ;

        Page<OrdersDto> odPage = new Page<>();
        BeanUtils.copyProperties(orderPage, odPage, "records");

        odPage.setRecords(orderDtos);

        return R.success(odPage);
    }

    /**
     * 后端员工获取所有用户的订单
     * 前端会传来时间数据
     * 前端传来的数据名称一定要相同
     * 强转一下
     * 
     * @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
     * @param page
     * @param pageSize
     * @param number
     * @param @DateTimeFormat beginTime
     * @param @DateTimeFormat endTime
     * @return
     */

    @GetMapping("/page")
    public R<Page> getInfo(
            int page,
            int pageSize,
            Long number,
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date beginTime,
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endTime) {
        log.info("收到了，起止时间：{}，结束时间{}", beginTime, endTime);
        // ordersService.getOrderWithDetail(page,pageSize,number);

        LambdaQueryWrapper<Orders> oWrapper = new LambdaQueryWrapper<>();
        oWrapper.eq(number != null, Orders::getId, number);
        // greater than 大于这个参数
        oWrapper.gt(beginTime != null, Orders::getOrderTime, beginTime);
        // less than 小于这个参数
        oWrapper.lt(endTime != null, Orders::getOrderTime, endTime);

        oWrapper.orderByDesc(Orders::getCheckoutTime);

        Page<Orders> oPage = new Page<>(page, pageSize);

        ordersService.page(oPage, oWrapper);

        List<Orders> records = oPage.getRecords();
        records = records.stream().map((item) -> {
            item.setUserName("用户" + item.getUserId());
            return item;
        }).collect(Collectors.toList());

        oPage.setRecords(records);

        return R.success(oPage);
    }

    /**
     * 因为参数在payload中而不在请求连接中
     * 所以需要RequestBody来封装json对象
     * 
     * @param map
     * @return
     */
    @PutMapping
    public R<String> delivary(@RequestBody Map map) {
        // log.info("接收到改状态的id:{}",map.get("id"));
        Orders orders = new Orders();
        Long id = Long.parseLong((String) map.get("id"));
        Integer status = (Integer) map.get("status");
        orders.setId(id);
        orders.setStatus(status);

        ordersService.updateById(orders);
        return R.success("派送成功！");
    }
}
