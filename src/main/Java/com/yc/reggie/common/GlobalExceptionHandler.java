package com.yc.reggie.common;

import java.sql.SQLIntegrityConstraintViolationException;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

/**
 * 全局异常处理
 */

 @ControllerAdvice(annotations = {RestController.class, Controller.class})
 @ResponseBody
 @Slf4j
public class GlobalExceptionHandler {
    

    /**
     * 异常处理方法
     * @param ex
     * @return
     */
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public R<String> exceptionHandler(SQLIntegrityConstraintViolationException ex) {
        log.error(ex.getMessage());

        if(ex.getMessage().contains("Duplicate entry")){
            String[] res = ex.getMessage().split(" ");
            String msg = res[2] + "已存在！";
            return R.error(msg);
        }
        return R.error("出现异常");
    }

    @ExceptionHandler(CustomException.class)
    public R<String> exceptionHandler(CustomException ex) {

        return R.error(ex.getMessage());
    }



}
