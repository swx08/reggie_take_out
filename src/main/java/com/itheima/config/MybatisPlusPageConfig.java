package com.itheima.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ClassName:MybatisPlusPageConfig
 * Package:com.itheima.config
 * Description
 *
 * @Author:@wenxueshi
 * @Create:2023/2/8 - 19:41
 * @Version:v1.0
 */

/**
 * 配置MP的分页插件
 */
@Configuration
public class MybatisPlusPageConfig {
    @Bean//交给spring管理
    public MybatisPlusInterceptor mybatisPlusInterceptor(){
        MybatisPlusInterceptor mybatisPlusInterceptor = new MybatisPlusInterceptor();
        mybatisPlusInterceptor.addInnerInterceptor(new PaginationInnerInterceptor());
        return mybatisPlusInterceptor;
    }
}
