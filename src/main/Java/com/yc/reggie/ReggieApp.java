package com.yc.reggie;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import lombok.extern.slf4j.Slf4j;


@Slf4j
@SpringBootApplication
public class ReggieApp {
    public static void main(String[] args) {
        SpringApplication.run(ReggieApp.class, args);
        log.info("项目启动成功");
    }
}
