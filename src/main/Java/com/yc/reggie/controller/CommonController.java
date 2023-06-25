package com.yc.reggie.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.UUID;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.yc.reggie.common.R;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping("/common")
public class CommonController {

    @Value("${reggie.path}")
    private String bathPath;

    /**
     * 文件上传
     * 
     * @param file
     * @return
     */
    @PostMapping("/upload")
    public R<String> upload(MultipartFile file) {
        // 上传时，是临时文件，要转存至指定位置，否则在本次请求后回自动删除；

        // 获取原始文件名
        String originName = file.getOriginalFilename();
        // 获取原始文件后缀
        String suffix = originName.substring(originName.lastIndexOf("."));

        // 为防止重名导致文件被覆盖，每次使用uuid重新命名
        String fileName = UUID.randomUUID().toString() + suffix;

        File dir = new File(bathPath);
        // 若指定的basePath不存在该文件夹，创建它
        if (!dir.exists()) {
            dir.mkdirs();
        }
        try {
            file.transferTo(new File(bathPath + fileName));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return R.success(fileName);
    }

    @GetMapping("/download")
    public R<String> download(HttpServletResponse response, String name) {

        try {
            // 输入流，通过存储路径和文件名读取服务器中存的内容
            FileInputStream fileInputStream = new FileInputStream(new File(bathPath + name));
            
            //输出流，将文件会写给发送请求的浏览器，展示内容
            ServletOutputStream servletOutputStream = response.getOutputStream();

            response.setContentType("image/jpeg");


            int len = 0;
            //字节流读取文件内容
            byte[] bytes = new byte[1024];

            while((len = fileInputStream.read(bytes))!= -1){
                servletOutputStream.write(bytes, 0, len);
                servletOutputStream.flush();
            }

            //关闭输入输出流
            servletOutputStream.close();
            fileInputStream.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
