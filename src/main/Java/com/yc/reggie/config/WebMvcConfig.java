package com.yc.reggie.config;

import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import com.yc.reggie.common.JacksonObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class WebMvcConfig extends WebMvcConfigurationSupport{

    /*
     * 设置静态资源映射，使tomcat能访问到网页
    */
 
    @Override
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
        // TODO Auto-generated method stub
        log.info("开始映射静态资源...");
        registry.addResourceHandler("/backend/**").addResourceLocations("classpath:/backend/");
        registry.addResourceHandler("/front/**").addResourceLocations("classpath:/front/");
    }
    
    /**
     * 扩展MVC框架的消息转换器
     * @param converters
     */
    @Override
    protected void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        //创建消息转换器对象 
        MappingJackson2HttpMessageConverter messageConverter = new MappingJackson2HttpMessageConverter();
        
        //设置对象转换器，底层使用Jackson将java对象转为JSON
        messageConverter.setObjectMapper(new JacksonObjectMapper());

        //将上面的消息转换器添加到MVC框架的转换器集合最前面，最优先使用
        //js处理long型数据会存在最后两位精度丢失，要转换为String
        converters.add(0, messageConverter);

    }
}
