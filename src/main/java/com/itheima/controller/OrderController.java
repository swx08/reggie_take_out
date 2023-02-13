package com.itheima.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.common.R;
import com.itheima.entity.Orders;
import com.itheima.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * ClassName:OrderController
 * Package:com.itheima.controller
 * Description
 *
 * @Author:@wenxueshi
 * @Create:2023/2/11 - 12:27
 * @Version:v1.0
 */
@Slf4j
@RestController
@RequestMapping("/order")
public class OrderController {
    @Autowired
    private OrderService orderService;

    /**
     * 用户下单
     * @param orders
     * @return
     */
    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders){
        orderService.submit(orders);
        return R.success("下单成功！");
    }

    /**
     * 订单明细分页查询
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,Long number){
        //分页构造器
        Page<Orders> pageInfo = new Page<>(page, pageSize);
        //查询构造器
        LambdaQueryWrapper<Orders> ordersLambdaQueryWrapper = new LambdaQueryWrapper<>();
        //添加查询条件
        ordersLambdaQueryWrapper.eq(number != null,Orders::getNumber,number);
        //添加排序条件
        ordersLambdaQueryWrapper.orderByDesc(Orders::getOrderTime);
        //分页查询
        orderService.page(pageInfo,ordersLambdaQueryWrapper);
        return R.success(pageInfo);
    }
}
