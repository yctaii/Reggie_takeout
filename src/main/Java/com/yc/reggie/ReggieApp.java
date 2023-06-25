package com.yc.reggie;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import lombok.extern.slf4j.Slf4j;


@Slf4j
@SpringBootApplication
@ServletComponentScan    //这样才会扫描Webfilter注解，然后使过滤器生效
@EnableTransactionManagement
public class ReggieApp {
    public static void main(String[] args) {
        SpringApplication.run(ReggieApp.class, args);
        log.info("项目启动成功");
    }
}
