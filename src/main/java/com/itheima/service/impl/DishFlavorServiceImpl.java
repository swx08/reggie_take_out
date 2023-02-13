package com.itheima.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.entity.Dish;
import com.itheima.entity.DishFlavor;
import com.itheima.mapper.DishFlavorMapper;
import com.itheima.mapper.DishMapper;
import com.itheima.service.DishFlavorService;
import com.itheima.service.DishService;
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
public class DishFlavorServiceImpl extends ServiceImpl<DishFlavorMapper, DishFlavor> implements DishFlavorService {
}
