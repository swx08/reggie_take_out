package com.itheima.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.common.R;
import com.itheima.dto.DishDto;
import com.itheima.dto.SetmealDto;
import com.itheima.entity.Category;
import com.itheima.entity.Dish;
import com.itheima.entity.Setmeal;
import com.itheima.service.CategoryService;
import com.itheima.service.SetmealDishService;
import com.itheima.service.SetmealService;
import org.apache.ibatis.annotations.Update;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * ClassName:SetMealDishController
 * Package:com.itheima.controller
 * Description
 *
 * @Author:@wenxueshi
 * @Create:2023/2/10 - 12:09
 * @Version:v1.0
 */
@RestController
@RequestMapping("/setmeal")
public class SetMealDishController {
    @Autowired
    private SetmealService setmealService;

    @Autowired
    private CategoryService categoryService;

    /**
     * 套餐管理分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name){
        //分页构造器
        Page<Setmeal> pageInfo = new Page<>(page, pageSize);
        Page<SetmealDto> setmealDtoPage = new Page<>();

        //查询构造器
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        //根据name进行模糊查询
        queryWrapper.like(!StringUtils.isEmpty(name),Setmeal::getName,name);
        //添加排序条件
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);
        //进行分页查询
        setmealService.page(pageInfo,queryWrapper);

        //赋值套餐分类名:因为前端页面需要展示套餐分类名(categoryName),而Setmeal里面没有categoryName这个属性，
        //所以需要SetmealDto，将Setmeal查询回来的数据全部复制到SetmealDto,还需要通过categoryId查询categoryName,
        //最后将categoryName赋值给SetmealDto,返回SetmealDto Page对象
        //对象复制
        BeanUtils.copyProperties(pageInfo,setmealDtoPage,"records");
        List<Setmeal> records = pageInfo.getRecords();
        List<SetmealDto> list = records.stream().map((item) ->{
            SetmealDto setmealDto = new SetmealDto();
            //对象复制
            BeanUtils.copyProperties(item,setmealDto);
            Long categoryId = item.getCategoryId();
            //根据categoryId查询分类对象
            Category category = categoryService.getById(categoryId);
            String categoryName = category.getName();
            setmealDto.setCategoryName(categoryName);

            return setmealDto;
        }).collect(Collectors.toList());
        setmealDtoPage.setRecords(list);
        return R.success(setmealDtoPage);
    }

    /**
     * 新增套餐
     * @return
     */
    @PostMapping
    //将一条或多条数据从缓存中删除,新增或删除或修改时，需要将套餐下的所有缓存数据删除
    @CacheEvict(value = "setmealCache",allEntries = true)
    public R<String> saveWithDish(@RequestBody SetmealDto setmealDto){
        setmealService.saveWithDish(setmealDto);
        return R.success("新增套餐成功!");
    }

    /**
     * 删除套餐
     * @return
     */
    @DeleteMapping
    //将一条或多条数据从缓存中删除,新增或删除或修改时，需要将套餐下的所有缓存数据删除
    @CacheEvict(value = "setmealCache",allEntries = true)
    public R<String> delete(@RequestParam List<Long> ids){
        setmealService.removeWithDish(ids);
        return R.success("套餐删除成功!");
    }

    /**
     * 移动端展示套餐数据
     * @return
     */
    @GetMapping("/list")
    //在方法执行前spring先查看缓存中是否有数据，如果有数据，则直接返回缓存数据；
    @Cacheable(value = "setmealCache",key = "#setmeal.categoryId + '_' + #setmeal.id")
    public R<List<Setmeal>> list(Setmeal setmeal){
        //根据CategoryId和status查询
        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealLambdaQueryWrapper.eq(setmeal.getCategoryId() != null,Setmeal::getCategoryId,setmeal.getCategoryId());
        setmealLambdaQueryWrapper.eq(setmeal.getStatus() !=null,Setmeal::getStatus,setmeal.getStatus());
        setmealLambdaQueryWrapper.orderByDesc(Setmeal::getUpdateTime);
        List<Setmeal> setmealList = setmealService.list(setmealLambdaQueryWrapper);
        return R.success(setmealList);
    }


    /**
     * 套餐数据回显
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<SetmealDto> get(@PathVariable Long id){
        SetmealDto setmealDto = setmealService.getByIdDish(id);
        return R.success(setmealDto);
    }

    /**
     * 修改套餐数据同时修改菜品数据
     * @param setmealDto
     * @return
     */
    @PutMapping
    //将一条或多条数据从缓存中删除,新增或删除或修改时，需要将套餐下的所有缓存数据删除
    @CacheEvict(value = "setmealCache",allEntries = true)
    public R<String> update(@RequestBody SetmealDto setmealDto){
        setmealService.updateWithDish(setmealDto);
        return R.success("修改成功!");
    }

    /**
     * 修改启售，停售
     * @param ids
     * @return
     */
    @PostMapping("/status/{status}")
    public R<String> updateStatus(@PathVariable int status,@RequestParam List<Long> ids){
        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealLambdaQueryWrapper.in(Setmeal::getId,ids);
        //先根据ids条件查询到要修改的套餐数据
        List<Setmeal> setmealList = setmealService.list(setmealLambdaQueryWrapper);
        //查到后将状态值动态赋值
        setmealList = setmealList.stream().map((item) ->{
            item.setStatus(status);
            return item;
        }).collect(Collectors.toList());
        //赋值后进行状态修改
        setmealService.updateBatchById(setmealList);
        return R.success("修改成功!");
    }
}
