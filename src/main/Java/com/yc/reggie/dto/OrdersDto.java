package com.yc.reggie.dto;

import lombok.Data;
import java.util.List;

import com.yc.reggie.entity.OrderDetail;
import com.yc.reggie.entity.Orders;

@Data
public class OrdersDto extends Orders {

    private String userName;

    private String phone;

    private String address;

    private String consignee;

    private List<OrderDetail> orderDetails;
	
}
