package com.itheima.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.entity.Dish;
import com.itheima.entity.DishFlavor;
import org.apache.ibatis.annotations.Mapper;

/**
 * ClassName:EmployeeMapper
 * Package:com.itheima.mapper
 * Description
 *
 * @Author:@wenxueshi
 * @Create:2023/2/7 - 19:50
 * @Version:v1.0
 */
//因为使用的Mybatis-Plus所以直接继承baseMapper即可
@Mapper
public interface DishFlavorMapper extends BaseMapper<DishFlavor> {
}
