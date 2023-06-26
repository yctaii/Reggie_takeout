package com.yc.reggie.controller;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yc.reggie.common.R;
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

    @GetMapping("/page")
    public R<Page> getInfo(int page, int pageSize, String number,@RequestBody Date bDate,@RequestBody Date eDate){
        log.info("收到了，起止时间：{}，结束时间{}",bDate,eDate);        
        // ordersService.getOrderWithDetail(page,pageSize,number);
        return null;
    } 
}
