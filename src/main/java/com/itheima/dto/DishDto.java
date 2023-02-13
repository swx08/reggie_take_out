package com.itheima.dto;

import com.itheima.entity.Dish;
import com.itheima.entity.DishFlavor;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

/**
 * dishDto实体类是数据库中菜品和口味的联合表
 */
@Data
public class DishDto extends Dish {

    private List<DishFlavor> flavors = new ArrayList<>();

    private String categoryName;

    private Integer copies;
}
