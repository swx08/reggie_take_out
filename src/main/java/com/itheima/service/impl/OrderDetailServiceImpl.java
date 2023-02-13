package com.itheima.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.entity.OrderDetail;
import com.itheima.entity.User;
import com.itheima.mapper.OrderDetailMapper;
import com.itheima.mapper.UserMapper;
import com.itheima.service.OrderDetailService;
import com.itheima.service.UserService;
import org.springframework.stereotype.Service;

/**
 * ClassName:EmployeeServiceImpl
 * Package:com.itheima.service.impl
 * Description
 *
 * @Author:@wenxueshi
 * @Create:2023/2/7 - 19:55
 * @Version:v1.0
 */
@Service
public class OrderDetailServiceImpl extends ServiceImpl<OrderDetailMapper, OrderDetail> implements OrderDetailService {
}
