package com.itheima.controller;

import com.itheima.service.OrderDetailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ClassName:OrderDetailControler
 * Package:com.itheima.controller
 * Description
 *
 * @Author:@wenxueshi
 * @Create:2023/2/11 - 12:27
 * @Version:v1.0
 */
@Slf4j
@RestController
@RequestMapping("/orderDetail")
public class OrderDetailController {
    @Autowired
    private OrderDetailService orderDetailService;
}
