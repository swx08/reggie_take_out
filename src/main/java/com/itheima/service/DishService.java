package com.itheima.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.dto.DishDto;
import com.itheima.entity.Category;
import com.itheima.entity.Dish;

import java.util.List;

/**
 * ClassName:EmployeeService
 * Package:com.itheima.service.impl
 * Description
 *
 * @Author:@wenxueshi
 * @Create:2023/2/7 - 19:54
 * @Version:v1.0
 */
public interface DishService extends IService<Dish> {
    /**
     * 查询菜品分类基本信息和菜品口味
     * @param id
     */
    DishDto getByIdFlavor(Long id);

    /**
     * 保存新增菜品同时保存菜品口味
     * @param dishDto
     */
    void saveWithFlavor(DishDto dishDto);

    /**
     * 修改新增菜品同时修改菜品口味
     * @param dishDto
     */
    void updateWithFlavor(DishDto dishDto);

    void removeWithFlavor(List<Long> ids);
}
