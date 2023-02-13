package com.itheima.dto;

import com.itheima.entity.Setmeal;
import com.itheima.entity.SetmealDish;
import lombok.Data;
import java.util.List;

/**
 * setmealDto实体类是数据库中套餐和菜品的联合表
 */
@Data
public class SetmealDto extends Setmeal {

    private List<SetmealDish> setmealDishes;

    private String categoryName;
}
