package com.itheima.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * ClassName:GlobalExceptionHandler
 * Package:com.itheima.common
 * Description
 *
 * @Author:@wenxueshi
 * @Create:2023/2/8 - 18:58
 * @Version:v1.0
 */
//处理全局异常
@ControllerAdvice(annotations = {RestController.class, Controller.class})
//json格式返回
@ResponseBody
@Slf4j
public class GlobalExceptionHandler {
    /**
     * 捕获异常SQLIntegrityConstraintViolationException(SQL异常)
     * @param e
     * @return
     */
    @ExceptionHandler({SQLIntegrityConstraintViolationException.class})
    public R<String> exceptionHandler(SQLIntegrityConstraintViolationException e){
        log.error(e.getMessage());
        //由于数据库中账号(username)为unique,所以这里捕获的是SQL异常
        //查看SQL异常信息里面是否包含"Duplicate entry"字符
        if(e.getMessage().contains("Duplicate entry")){
            //将捕获到的SQL异常信息放到split里
            String[] split = e.getMessage().split(" ");
            //将split数组里的字符串进行分割,split[2]里存得是账号信息
            String msg = split[2] + "已存在";
            return R.error(msg);
        }
        return R.error("未知错误!");
    }

    /**
     * 捕获业务异常
     * @param e
     * @return
     */
    @ExceptionHandler({ServiceException.class,CustomException.class})
    public R<String> exceptionHandler(Throwable e){
        return R.error(e.getMessage());
    }
}
