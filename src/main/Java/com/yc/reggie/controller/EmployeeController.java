package com.yc.reggie.controller;

import java.time.LocalDateTime;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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

    /**
     * 
     * @param request
     * @param employee
     * @return
     */
    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee) {
        // 1、将收到的密码进行与数据库密码进行相同的加密
        String pass = employee.getPassword();
        pass = DigestUtils.md5DigestAsHex(pass.getBytes());

        // 2、根据用户名查询数据库是否存在用户
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername, employee.getUsername());
        Employee employe = employeeService.getOne(queryWrapper);

        // 3.未查到这个用户
        if (employe == null) {
            return R.error("登录失败，用户不存在！");
        }

        // 4.密码错误
        if (!employe.getPassword().equals(pass)) {
            return R.error("密码错误！");
        }

        // 5.用户被禁用
        if (employe.getStatus() != 1) {
            return R.error("账号已禁用");
        }

        // 6.登录成功,将员工id存入session方便后续调用
        request.getSession().setAttribute("employee", employe.getId());
        log.info("找到了....");
        return R.success(employe);
    }

    /**
     * 
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request) {
        // 1、清理Session中保存的当前登陆员工的id
        request.removeAttribute("employee");
        return R.success("退出成功");
    }

    /**
     * 新增员工
     * 
     * @param employee
     * @return
     */
    @PostMapping
    public R<String> addUser(HttpServletRequest request, @RequestBody Employee employee) {
        log.info("收到员工信息：{}", employee.toString());

        log.info("当前线程id:{}", Thread.currentThread().getId());
        // 在网页添加用户时，没有密码选项，给个默认赋值123456
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));
        // employee.setCreateTime(LocalDateTime.now());
        // employee.setUpdateTime(LocalDateTime.now());

        // 添加用户时，还有个操作人信息,登录时存储的employee是存的他的id
        Long empId = (Long) request.getSession().getAttribute("employee");

        // employee.setCreateUser(empId);
        // employee.setUpdateUser(empId) ;

        // 继承Mybatisplus中已有的保存方法，插入数据库
        employeeService.save(employee);
        return null;
    }

    /***
     * 员工分页查询
     * 
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> pageEmp(int page, int pageSize, String name) {
        log.info("page={}, pageSize = {}, name = {}", page, pageSize, name);

        // 构造分页构造器
        Page pageInfo = new Page<>(page, pageSize);

        // 接受到name时，构造条件构造器
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotEmpty(name), Employee::getName, name);
        // 添加排序条件
        queryWrapper.orderByDesc(Employee::getCreateTime);
        // 执行查询
        employeeService.page(pageInfo, queryWrapper);

        return R.success(pageInfo);
    }

    @PutMapping
    public R<String> update(HttpServletRequest request, @RequestBody Employee employee) {
        log.info(employee.toString());
        log.info("当前线程id:{}", Thread.currentThread().getId());
        Long empId = (Long) request.getSession().getAttribute("employee");
        employee.setUpdateTime(LocalDateTime.now());
        employee.setUpdateUser(empId);
        employeeService.updateById(employee);

        return null;
    }

    /**
     * 根据id查询员工
     * 
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<Employee> getById(@PathVariable Long id) {
        log.info("根据id={},查询员工", id);

        Employee emp = employeeService.getById(id);
        return R.success(emp);

    }

}
