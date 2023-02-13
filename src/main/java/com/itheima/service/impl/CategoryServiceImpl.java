package com.itheima.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.common.ServiceException;
import com.itheima.entity.Category;
import com.itheima.entity.Dish;
import com.itheima.entity.Employee;
import com.itheima.entity.Setmeal;
import com.itheima.mapper.CategoryMapper;
import com.itheima.mapper.DishMapper;
import com.itheima.mapper.EmployeeMapper;
import com.itheima.service.CategoryService;
import com.itheima.service.DishService;
import com.itheima.service.EmployeeService;
import com.itheima.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.Serializable;

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
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {
    @Autowired
    private DishService dishService;

    @Autowired
    private SetmealService setmealService;


    /**
     * 根据id进行删除，删除之前需要进行判断操作
     * @param ids
     */
    @Override
    public void remove(Long ids) {
        //如果关联了菜品分类和套餐分类就不能进行正常删除
        //1.添加查询条件，进行菜品查询
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        //根据分类id进行查询
        dishLambdaQueryWrapper.eq(Dish::getCategoryId,ids);
        int dishCount = dishService.count(dishLambdaQueryWrapper);

        //查询当前分类是否关联了菜品，如果已经关联则抛出一个业务异常
        if(dishCount > 0){
            throw new ServiceException("当前分类下关联了菜品，不能删除!");
        }

        //2.添加查询条件，进行套餐查询
        LambdaQueryWrapper<Setmeal> setmealQueryWrapper = new LambdaQueryWrapper<>();
        //根据分类id进行查询
        setmealQueryWrapper.eq(Setmeal::getCategoryId,ids);
        int setmealCount = setmealService.count(setmealQueryWrapper);
        if(setmealCount > 0){
            throw new ServiceException("当前分类下关联了套餐，不能删除!");
        }

        //如果都不关联则进行正常删除
        super.removeById(ids);
    }
}
