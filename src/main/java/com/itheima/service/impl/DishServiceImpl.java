package com.itheima.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.common.CustomException;
import com.itheima.dto.DishDto;
import com.itheima.entity.Category;
import com.itheima.entity.Dish;
import com.itheima.entity.DishFlavor;
import com.itheima.mapper.CategoryMapper;
import com.itheima.mapper.DishMapper;
import com.itheima.service.CategoryService;
import com.itheima.service.DishFlavorService;
import com.itheima.service.DishService;
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
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {
    @Autowired
    private DishFlavorService dishFlavorService;

    /**
     * 查询菜品分类基本信息和菜品口味
     * @param id
     */
    public DishDto getByIdFlavor(Long id) {
        Dish dish = this.getById(id);
        DishDto dishDto = new DishDto();
        //对象复制
        BeanUtils.copyProperties(dish,dishDto);
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId,dish.getId());
        List<DishFlavor> list = dishFlavorService.list(queryWrapper);
        dishDto.setFlavors(list);
        return dishDto;
    }

    /**
     * 保存新增菜品同时保存菜品口味
     * @param dishDto
     */
    @Transactional
    public void saveWithFlavor(DishDto dishDto) {
        //先保存新增菜品的基本信息到dish表
        this.save(dishDto);
        //获取菜品Id
        Long disId = dishDto.getId();
        //菜品口味需要绑定菜品Id
        List<DishFlavor> flavors = dishDto.getFlavors();
        flavors = flavors.stream().map((item) ->{
           item.setDishId(disId);
           return item;
        }).collect(Collectors.toList());
        //保存菜品口味数据到菜品口味表dish_flavor
        dishFlavorService.saveBatch(flavors);
    }

    /**
     * 修改新增菜品同时修改菜品口味
     * @param dishDto
     */
    @Override
    public void updateWithFlavor(DishDto dishDto) {
        //更新dish基本信息
        this.updateById(dishDto);

        //先清理当前菜品对应口味数据
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId,dishDto.getId());
        dishFlavorService.remove(queryWrapper);

        //添加当前提交过来的口味数据
        List<DishFlavor> list = dishDto.getFlavors();
        //菜品口味需要绑定菜品Id
        list.stream().map((item) ->{
            item.setDishId(dishDto.getId());
            return item;
        }).collect(Collectors.toList());
        dishFlavorService.saveBatch(list);
    }

    /**
     * 删除菜品管理数据
     * @param ids
     */
    @Override
    public void removeWithFlavor(List<Long> ids) {
        //查询菜品状态,如果是启售(1)状态则不能正常删除。
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        dishLambdaQueryWrapper.in(Dish::getId,ids);
        dishLambdaQueryWrapper.eq(Dish::getStatus,1);
        int count = this.count(dishLambdaQueryWrapper);
        if(count > 0){
            throw new CustomException("菜品正在售卖中，不能删除!");
        }
        //如果可以删除，先删除菜品表中的数据
        this.removeByIds(ids);
        //删除口味表中的数据
        LambdaQueryWrapper<DishFlavor> dishFlavorLambdaQueryWrapper = new LambdaQueryWrapper<>();
        dishFlavorLambdaQueryWrapper.in(DishFlavor::getDishId,ids);
        dishFlavorService.remove(dishFlavorLambdaQueryWrapper);
    }
}
