package com.itheima.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.common.R;
import com.itheima.dto.DishDto;
import com.itheima.entity.Category;
import com.itheima.entity.Dish;
import com.itheima.entity.DishFlavor;
import com.itheima.service.CategoryService;
import com.itheima.service.DishFlavorService;
import com.itheima.service.DishService;
import org.jacoco.agent.rt.RT;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * ClassName:DishController
 * Package:com.itheima.controller
 * Description
 *
 * @Author:@wenxueshi
 * @Create:2023/2/9 - 19:48
 * @Version:v1.0
 */

/**
 * 菜品管理
 */
@RestController
@RequestMapping("/dish")
public class DishController {
    @Autowired
    private DishService dishService;

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private CategoryService categoryService;

    /**
     * 菜品管理分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,String name){
        //构造分页构造器
        Page<Dish> pageInfo = new Page<>(page,pageSize);
        Page<DishDto> disDtoPage = new Page<>();
        //构造查询构造器
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        //根据菜品名进行模糊查询
        queryWrapper.like(!StringUtils.isEmpty(name),Dish::getName,name);
        //添加排序条件，根据修改时间排序
        queryWrapper.orderByDesc(Dish::getUpdateTime);
        dishService.page(pageInfo,queryWrapper);

        //赋值菜品分类名:因为前端页面需要展示菜品分类名(categoryName),而Dish里面没有categoryName这个属性，
        //所以需要DishDto，将Dish查询回来的数据全部复制到DishDto,还需要通过categoryId查询categoryName,
        //最后将categoryName赋值给DishDto,返回DishDto Page对象
        //对象复制
        BeanUtils.copyProperties(pageInfo,disDtoPage,"records");
        List<Dish> records = pageInfo.getRecords();
        List<DishDto> list = records.stream().map((item) ->{
            DishDto dishDto = new DishDto();
            //对象复制
            BeanUtils.copyProperties(item,dishDto);
            Long categoryId = item.getCategoryId();
            //根据categoryId查询分类对象
            Category category = categoryService.getById(categoryId);
            if(category != null){
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }

            return dishDto;
        }).collect(Collectors.toList());
        disDtoPage.setRecords(list);
        return R.success(disDtoPage);
    }

    /**
     * 保存新增菜品同时保存菜品口味
     * @param dishDto
     * @return
     */
    @PostMapping
    public R<String> saveWithFlavor(@RequestBody DishDto dishDto){
        dishService.saveWithFlavor(dishDto);
        return R.success("新增菜品成功!");
    }

    /**
     * 查询菜品分类基本信息和菜品口味(回显菜品数据)
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<DishDto> get(@PathVariable Long id){
        DishDto dishDto = dishService.getByIdFlavor(id);
        return R.success(dishDto);
    }

    /**
     * 修改新增菜品同时修改菜品口味
     * @param dishDto
     * @return
     */
    @PutMapping
    public R<String> updateWithFlavor(@RequestBody DishDto dishDto){
        dishService.updateWithFlavor(dishDto);
        return R.success("新增菜品成功!");
    }
/*
    *//**
     * 根据条件获取菜品
     * @return
     *//*
    @GetMapping("/list")
    public R<List<Dish>> list(Dish dish){
        //构造查询条件
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(dish.getCategoryId() != null,Dish::getCategoryId,dish.getCategoryId());
        queryWrapper.eq(Dish::getStatus,1);
        //添加排序条件
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);

        List<Dish> dishList = dishService.list(queryWrapper);
        return R.success(dishList);
    }*/

    /**
     * 根据条件获取菜品
     * @return
     */
    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish){
        //构造查询条件
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(dish.getCategoryId() != null,Dish::getCategoryId,dish.getCategoryId());
        queryWrapper.eq(Dish::getStatus,1);
        //添加排序条件
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);

        List<Dish> dishList = dishService.list(queryWrapper);

        List<DishDto> dishDtoList = dishList.stream().map((item) ->{
            DishDto dishDto = new DishDto();
            //对象复制
            BeanUtils.copyProperties(item,dishDto);
            Long categoryId = item.getCategoryId();
            //根据categoryId查询分类对象
            Category category = categoryService.getById(categoryId);
            if(category != null){
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }
            //根据dishId查询菜品对应的口味信息
            Long dishId = item.getId();
            LambdaQueryWrapper<DishFlavor> dishDtoLambdaQueryWrapper = new LambdaQueryWrapper<>();
            dishDtoLambdaQueryWrapper.eq(DishFlavor::getDishId,dishId);
            List<DishFlavor> flavorList = dishFlavorService.list(dishDtoLambdaQueryWrapper);
            dishDto.setFlavors(flavorList);
            return dishDto;
        }).collect(Collectors.toList());
        return R.success(dishDtoList);
    }

    /**
     * 删除菜品管理
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids){
        dishService.removeWithFlavor(ids);
        return R.success("删除成功!");
    }

    /**
     * 菜品启售、停售
     * @param status
     * @param ids
     * @return
     */
    @PostMapping("/status/{status}")
    public R<String> updateStatus(@PathVariable int status,@RequestParam List<Long> ids){
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        dishLambdaQueryWrapper.in(Dish::getId,ids);
        //先根据ids条件查询到要修改的套餐数据
        List<Dish> dishList = dishService.list(dishLambdaQueryWrapper);
        //查到后将状态值动态赋值
        dishList = dishList.stream().map((item) ->{
            item.setStatus(status);
            return item;
        }).collect(Collectors.toList());
        //赋值后进行状态修改
        dishService.updateBatchById(dishList);
        return R.success("修改成功!");
    }
}
