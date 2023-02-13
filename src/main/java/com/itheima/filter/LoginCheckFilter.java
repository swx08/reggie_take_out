package com.itheima.filter;

import com.alibaba.fastjson.JSON;
import com.itheima.common.BaseContext;
import com.itheima.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * ClassName:LoginCheckFilter
 * Package:com.itheima.filter
 * Description
 *
 * @Author:@wenxueshi
 * @Create:2023/2/7 - 21:22
 * @Version:v1.0
 * 配置请求过滤器
 */

/**
 * 过滤器注解urlPatterns = "/*"表示过滤掉所有请求
 */
@WebFilter(filterName = "LoginCheckFilter",urlPatterns = "/*")
@Slf4j
public class LoginCheckFilter implements Filter {
    //路径匹配器，支持通配符
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        //1.获取当前的请求url
        String requestURI = request.getRequestURI();
        //以下是不需要处理的请求url,直接放行
        String urls[] = new String[]{
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**",
                "/common/**",
                "/user/sendMsg",
                "/user/login"
        };
        //2.判断本次请求是否需要处理
        boolean check = check(urls, requestURI);
        //3.如果不需要处理则直接放行
        if(check){
            log.info("本次请求不需要处理:{}",request.getRequestURI());
            filterChain.doFilter(request,response);
            return;
        }
        //4-1.查看登录状态，如果已登录则放行
        if(request.getSession().getAttribute("employee") != null){
            log.info("用户已登录,id为:"+request.getSession().getAttribute("employee"));
            Long empId = (Long) request.getSession().getAttribute("employee");
            //同一个线程保存用户Id
            BaseContext.setCurrentId(empId);
            //放行
            filterChain.doFilter(request,response);
            return;
        }

        //4-2.移动端查看登录状态，如果已登录则放行
        if(request.getSession().getAttribute("user") != null){
            log.info("用户已登录,id为:"+request.getSession().getAttribute("user"));
            Long userId = (Long) request.getSession().getAttribute("user");
            //同一个线程保存用户Id
            BaseContext.setCurrentId(userId);
            //放行
            filterChain.doFilter(request,response);
            return;
        }

        log.info("用户未登录!");
        //5.如果未登录则返回未登录结果，通过输出流方式响应给前端
        //如果未登录,只要返回NOTLOGIN字符串，页面跳转由前端js来控制
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
        return;
    }

    /**
     * 判断本次请求是否需要处理封装方法
     * @param urls
     * @param requestURI
     * @return
     */
    public boolean check(String[] urls,String requestURI){
        for (String url : urls) {
            //如果match为true,说明当前请求路径为不需要处理的请求,则说明路径匹配上
            boolean match = PATH_MATCHER.match(url, requestURI);
            if(match){
                return true;
            }
        }
        return false;
    }
}
