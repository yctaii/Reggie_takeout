package com.yc.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yc.reggie.entity.Category;

public interface CategoryService extends IService<Category>{
    
    
    //接口
    public void removeCate(Long id);

    public void getByType(int type);
}
