package com.itheima;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * ClassName:ReggieApplication
 * Package:com.itheima
 * Description
 *
 * @Author:@wenxueshi
 * @Create:2023/2/7 - 19:10
 * @Version:v1.0
 */
@Slf4j//lombok提供的日志
@SpringBootApplication//启动类
@ServletComponentScan//开启过滤器
@EnableTransactionManagement//开启事务注解
@EnableCaching //开启缓存功能
public class ReggieApplication {
    public static void main(String[] args) {
        SpringApplication.run(ReggieApplication.class,args);
        log.info("项目启动成功!");
    }
}
