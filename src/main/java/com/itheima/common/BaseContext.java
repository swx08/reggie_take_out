package com.itheima.common;

/**
 * ClassName:BaseContext
 * Package:com.itheima.common
 * Description
 *
 * @Author:@wenxueshi
 * @Create:2023/2/9 - 13:00
 * @Version:v1.0
 */

import java.net.URLEncoder;

/**
 * 基于ThreadLocal封装工具类，用来保存和获取当前登录的用户Id
 */
public class BaseContext {
    private static ThreadLocal<Long> threadLocal = new ThreadLocal<Long>();

    public static void setCurrentId(Long id){
        threadLocal.set(id);
    }
    public static Long getCurrentId(){
        return threadLocal.get();
    }
}
