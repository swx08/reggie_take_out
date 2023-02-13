package com.itheima.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.entity.Employee;
import com.itheima.mapper.EmployeeMapper;
import com.itheima.service.EmployeeService;
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
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee> implements EmployeeService {
}
