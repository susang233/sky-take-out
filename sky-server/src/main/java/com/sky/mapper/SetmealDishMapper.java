package com.sky.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SetmealDishMapper {
    /**
     * 根据菜品id查询关联套餐id
     * @param dishIds
     * @return
     */
    List<Long> getSetmeallIdByDishIds(List<Long> dishIds);
    // select setmeal_id from setmeal_dish where dish_id in (1,2,3,4)
}
