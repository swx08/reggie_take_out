package com.itheima.common;

/**
 * ClassName:CustomException
 * Package:com.itheima.common
 * Description
 *
 * @Author:@wenxueshi
 * @Create:2023/2/10 - 14:20
 * @Version:v1.0
 */

/**
 * 套餐业务异常
 */
public class CustomException extends RuntimeException{
    public CustomException(String message){
        super(message);
    }
}
