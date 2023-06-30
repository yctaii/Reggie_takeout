package com.yc.reggie.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yc.reggie.common.BaseContext;
import com.yc.reggie.common.CustomException;
import com.yc.reggie.common.R;
import com.yc.reggie.dto.OrdersDto;
import com.yc.reggie.entity.AddressBook;
import com.yc.reggie.entity.OrderDetail;
import com.yc.reggie.entity.Orders;
import com.yc.reggie.entity.ShoppingCart;
import com.yc.reggie.entity.User;
import com.yc.reggie.mapper.OrdersMapper;
import com.yc.reggie.service.AddressBookService;
import com.yc.reggie.service.OrderDetailService;
import com.yc.reggie.service.OrdersService;
import com.yc.reggie.service.ShopcartService;
import com.yc.reggie.service.UserService;

@Service
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements OrdersService {

    @Autowired
    private OrderDetailService orderDetailService;

    @Autowired
    private ShopcartService shopcartService;

    @Autowired
    private UserService userService;

    @Autowired
    private AddressBookService addressBookService;

    @Override
    @Transactional
    public void getOrderWithDetail(int page, int pageSize, String number) {
        // 构建分页构造器
        Page<Orders> oPage = new Page<>(page, pageSize);

        // 构造条件过滤器
        LambdaQueryWrapper<Orders> oLambdaQueryWrapper = new LambdaQueryWrapper<>();
        oLambdaQueryWrapper.like(number != null, Orders::getNumber, number);
        oLambdaQueryWrapper.orderByDesc(Orders::getCheckoutTime);

        this.page(oPage, oLambdaQueryWrapper);

        // 因为还有订单详情信息，所以要查order_detail表
        Page<OrderDetail> odPage = new Page<>();

        // 由于两个page的范型不一样，所以拷贝是忽略掉records
        BeanUtils.copyProperties(oPage, odPage, "records");

        List<Orders> orecords = oPage.getRecords();
        orecords.stream().map((item) -> {
            OrdersDto ordersDto = new OrdersDto();
            BeanUtils.copyProperties(item, ordersDto);

            return item;
        });
    }

    @Override
    @Transactional
    public void submitOrder(Orders orders) {
        // 获取用户id
        Long userId = BaseContext.getCurrentId();

        // 查询当前用户的购物车数据
        LambdaQueryWrapper<ShoppingCart> scQWrapper = new LambdaQueryWrapper<>();
        scQWrapper.eq(ShoppingCart::getUserId, userId);
        List<ShoppingCart> shopCarts = shopcartService.list(scQWrapper);

        if (shopCarts == null || shopCarts.size() == 0) {
            throw new CustomException("购物车为空，不能下单！");
        }

        // 查询用户数据
        User user = userService.getById(userId);

        // 查询用户的地址数据
        Long addressBookId = orders.getAddressBookId();
        AddressBook addressBook = addressBookService.getById(addressBookId);

        if (addressBook == null) {
            throw new CustomException("用户地址信息有误，不能下单");
        }

        // 为订单绑上订单id
        Long orderId = IdWorker.getId();
        // 原子操作，防止多线程工作时数据算错
        AtomicInteger amount = new AtomicInteger(0);

        List<OrderDetail> orderDetails = shopCarts.stream().map((item) -> {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrderId(orderId);
            orderDetail.setNumber(item.getNumber());
            orderDetail.setDishFlavor(item.getDishFlavor());
            orderDetail.setDishId(item.getDishId());
            orderDetail.setSetmealId(item.getSetmealId());
            orderDetail.setName(item.getName());
            orderDetail.setImage(item.getImage());
            orderDetail.setAmount(item.getAmount());
            amount.addAndGet(item.getAmount().multiply(new BigDecimal(item.getNumber())).intValue());
            return orderDetail;
        }).collect(Collectors.toList());

        orders.setId(orderId);
        orders.setOrderTime(LocalDateTime.now());
        orders.setCheckoutTime(LocalDateTime.now());
        orders.setStatus(2);
        orders.setAmount(new BigDecimal(amount.get()));// 总金额
        orders.setUserId(userId);
        orders.setNumber(String.valueOf(orderId));
        orders.setUserName(user.getName());
        orders.setConsignee(addressBook.getConsignee());
        orders.setPhone(addressBook.getPhone());
        orders.setAddress((addressBook.getProvinceName() == null ? "" : addressBook.getProvinceName())
                + (addressBook.getCityName() == null ? "" : addressBook.getCityName())
                + (addressBook.getDistrictName() == null ? "" : addressBook.getDistrictName())
                + (addressBook.getDetail() == null ? "" : addressBook.getDetail()));
        // 向订单表插入数据，一条数据
        this.save(orders);

        // 向订单明细表插入数据，多条数据
        orderDetailService.saveBatch(orderDetails);

        // 清空购物车数据
        shopcartService.remove(scQWrapper);
    }

    @Override
    @Transactional
    public Page<OrdersDto> getUserOrder(int page, int pageSize) {
        Page<Orders> orderPage = new Page<>(page, pageSize);

        // 设置条件查询过滤
        Long userId = BaseContext.getCurrentId();
        LambdaQueryWrapper<Orders> oWrapper = new LambdaQueryWrapper<>();
        oWrapper.eq(Orders::getUserId, userId);
        oWrapper.orderByDesc(Orders::getCheckoutTime);

        List<Orders> oList = this.list(oWrapper);

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

        this.page(orderPage, oWrapper);
        

        Page<OrdersDto> odPage = new Page<>();
        BeanUtils.copyProperties(orderPage, odPage, "records");

        odPage.setRecords(orderDtos);

        return odPage;
    }

    @Override
    @Transactional
    public void againOrder(Long id) {
        // 查询订单数据
        Orders orders = this.getById(id);

        // 查询订单明细数据
        LambdaQueryWrapper<OrderDetail> odWrapper = new LambdaQueryWrapper<>();
        odWrapper.eq(OrderDetail::getOrderId, id);
        List<OrderDetail> orderDetails = orderDetailService.list(odWrapper);

        List<ShoppingCart> sCarts = orderDetails.stream().map((item)->{
            ShoppingCart sCart = new ShoppingCart();
            sCart.setDishFlavor(item.getDishFlavor());
            sCart.setAmount(item.getAmount());
            sCart.setCreateTime(LocalDateTime.now());
            sCart.setDishId(item.getDishId());
            sCart.setImage(item.getImage());
            sCart.setName(item.getName());
            sCart.setNumber(item.getNumber());
            sCart.setSetmealId(item.getSetmealId());
            sCart.setUserId(BaseContext.getCurrentId());
            return sCart;
        }).collect(Collectors.toList());
        // 构建订单dto
        shopcartService.saveBatch(sCarts);
    }

}
