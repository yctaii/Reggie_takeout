package com.yc.reggie.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yc.reggie.common.R;
import com.yc.reggie.entity.Employee;
import com.yc.reggie.service.EmployeeService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {
    
    @Autowired
    private EmployeeService employeeService;

    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request,@RequestBody Employee employee) {
        //1、将收到的密码进行与数据库密码进行相同的加密
        String pass = employee.getPassword();
        pass = DigestUtils.md5DigestAsHex(pass.getBytes());

        //2、根据用户名查询数据库是否存在用户
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername,employee.getUsername());
        Employee employe = employeeService.getOne(queryWrapper);

        //3.未查到这个用户
        if(employe == null){
            return R.error("登录失败，用户不存在！");
        }

        //4.密码错误
        if(!employe.getPassword().equals(pass)){
            return R.error("密码错误！");
        }

        //5.用户被禁用
        if(employe.getStatus() != 1){
            return R.error("账号已禁用");
        } 

        //6.登录成功,将员工id存入session方便后续调用
        request.getSession().setAttribute("employee", employe.getId());
        log.info("找到了....");
        return R.success(employe);
    }

    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request) {
        //1、清理Session中保存的当前登陆员工的id
        request.removeAttribute("employee");
        return R.success("退出成功");
    }
}
