package com.yc.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yc.reggie.entity.AddressBook;
import com.yc.reggie.mapper.AddressBookMapper;
import com.yc.reggie.service.AddressBookService;

import org.springframework.stereotype.Service;

@Service
public class AddressBookServiceImpl extends ServiceImpl<AddressBookMapper, AddressBook> implements AddressBookService {

}
