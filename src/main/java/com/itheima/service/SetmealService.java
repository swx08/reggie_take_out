package com.itheima.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.dto.SetmealDto;
import com.itheima.entity.Category;
import com.itheima.entity.Setmeal;

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
public interface SetmealService extends IService<Setmeal> {
    /**
     * 新增套餐同时保存菜品信息
     * @param setmealDto
     * @return
     */
    void saveWithDish(SetmealDto setmealDto);

    /**
     * 删除套餐及关联的菜品数据
     * @param ids
     */
    void removeWithDish(List<Long> ids);

    SetmealDto getByIdDish(Long id);

    /**
     * 修改套餐数据
     * @param setmealDto
     */
    void updateWithDish(SetmealDto setmealDto);
}
