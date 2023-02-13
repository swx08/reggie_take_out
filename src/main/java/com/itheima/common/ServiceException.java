package com.itheima.common;

/**
 * ClassName:ServiceException
 * Package:com.itheima.common
 * Description
 *
 * @Author:@wenxueshi
 * @Create:2023/2/9 - 15:16
 * @Version:v1.0
 */

/**
 * 自定义业务异常
 */
public class ServiceException extends RuntimeException{
    public ServiceException(String message){
        super(message);
    }
}
