package com.yc.reggie.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.util.AntPathMatcher;

import com.alibaba.fastjson2.JSON;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.yc.reggie.common.BaseContext;
import com.yc.reggie.common.R;

import lombok.extern.slf4j.Slf4j;

/**
 * 检查用户是否已经完成登陆
 */
@WebFilter(filterName = "loginCheckFilter", urlPatterns = "/*")
@Slf4j
public class LoginCheckFilter implements Filter {

    public static final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        // TODO Auto-generated method stub
        HttpServletRequest hRequest = (HttpServletRequest) request;
        HttpServletResponse hResponse = (HttpServletResponse) response;

        // 1、获取本次请求的URI
        String requestURI = hRequest.getRequestURI();
        log.info("拦截到本次请求：{}", requestURI);

        String[] uris = new String[] {
                "/employee/login", // 本身就是发送登陆请求，不需要拦截
                "/employee/logout",
                "/backend/**", // 静态资源，不用拦截，不会造成数据库泄漏
                "/front/**",
                "/common/**",
                "/user/login",
                "/user/sendMsg"
        };

        // 2、判断本次请求是否需要处理
        Boolean checkres = check(uris, requestURI);

        // 3、如果不需要处理，直接放行
        if (checkres) {
            log.info("本次请求{}不需要处理，直接放行", requestURI);
            chain.doFilter(hRequest, hResponse);
            return;
        }
        // 4-1、判断登录状态，如果已经登陆，则直接放行
        if (hRequest.getSession().getAttribute("employee") != null) {
            log.info("当前线程id:{}", Thread.currentThread().getId());
            
            Long empId = (Long) hRequest.getSession().getAttribute("employee");
            BaseContext.setCurrentId(empId);
            
            log.info("用户{}已经处于登录状态，直接放行", hRequest.getSession().getAttribute("employee"));
            chain.doFilter(hRequest, hResponse);
            return;
        }


        //4-2 判断用户是否登陆，登陆则放行
        if(hRequest.getSession().getAttribute("user") != null){
            Long userId = (Long) hRequest.getSession().getAttribute("user");;
            BaseContext.setCurrentId(userId);

            chain.doFilter(hRequest, hResponse);
            return;
        }
        // 5、如果未登录,返回未登录结果，通过输出流方式向客户端页面响应数据
        log.info("用户未登录！");
        hResponse.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
        return;
    }

    /**
     * 路径匹配，检测此次请求是否放行
     */
    private Boolean check(String[] uris, String requestURI) {
        for (String uri : uris) {
            // 包含通配符的比较，要用 AntPathMatcher
            if (pathMatcher.match(uri, requestURI)) {
                return true;
            }
        }
        return false;
    }

}
