package com.itheima.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.common.CustomException;
import com.itheima.common.ServiceException;
import com.itheima.dto.DishDto;
import com.itheima.dto.SetmealDto;
import com.itheima.entity.Category;
import com.itheima.entity.DishFlavor;
import com.itheima.entity.Setmeal;
import com.itheima.entity.SetmealDish;
import com.itheima.mapper.CategoryMapper;
import com.itheima.mapper.SetmealMapper;
import com.itheima.service.CategoryService;
import com.itheima.service.SetmealDishService;
import com.itheima.service.SetmealService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

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
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {
    @Autowired
    private SetmealDishService setmealDishService;


    /**
     * 新增套餐同时保存菜品信息
     * @param setmealDto
     * @return
     */
    public void saveWithDish(SetmealDto setmealDto) {
        //先保存套餐基本信息
        this.save(setmealDto);
        //获取setmealId并赋值
        List<SetmealDish> setmealDishList = setmealDto.getSetmealDishes();
        setmealDishList.stream().map((item) ->{
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());
        //保存套餐和菜品关联的信息
        setmealDishService.saveBatch(setmealDishList);
    }

    /**
     * 删除套餐及关联的菜品数据
     * @param ids
     */
    @Transactional
    public void removeWithDish(List<Long> ids) {
        //查询套餐状态，如果是启售(1)状态则不能正常删除。
        LambdaQueryWrapper<Setmeal> setmealQueryWrapper = new LambdaQueryWrapper<>();
        setmealQueryWrapper.in(Setmeal::getId,ids);
        setmealQueryWrapper.eq(Setmeal::getStatus,1);
        int count = this.count(setmealQueryWrapper);
        if(count > 0){
            throw new CustomException("套餐正在售卖中，不能删除!");
        }
        //如果可以删除，先删除套餐表中的数据
        this.removeByIds(ids);
        //删除关系表中的数据
        LambdaQueryWrapper<SetmealDish> setmealDishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealDishLambdaQueryWrapper.in(SetmealDish::getSetmealId,ids);
        setmealDishService.remove(setmealDishLambdaQueryWrapper);
    }

    @Override
    public SetmealDto getByIdDish(Long id) {
        Setmeal setmeal = this.getById(id);
        SetmealDto setmealDto = new SetmealDto();
        //对象复制
        BeanUtils.copyProperties(setmeal,setmealDto);
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getDishId,setmeal.getId());
        List<SetmealDish> list = setmealDishService.list(queryWrapper);
        setmealDto.setSetmealDishes(list);
        return setmealDto;
    }

    /**
     * 修改套餐数据
     * @praram setmealDto
     */
    @Override
    public void updateWithDish(SetmealDto setmealDto) {
        //修改套餐(setmeal)基本数据
        this.updateById(setmealDto);
        //先清理当前菜品对应口味数据
        LambdaQueryWrapper<SetmealDish> setmealDishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealDishLambdaQueryWrapper.eq(SetmealDish::getDishId,setmealDto.getId());
        setmealDishService.remove(setmealDishLambdaQueryWrapper);

        //添加当前提交过来的口味数据
        List<SetmealDish> list = setmealDto.getSetmealDishes();
        //菜品口味需要绑定菜品Id
        list.stream().map((item) ->{
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());
        setmealDishService.saveBatch(list);
    }
}
