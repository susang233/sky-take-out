package com.sky.mapper;

import com.sky.entity.Dish;
import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealDishMapper {
    /**
     * 根据菜品id查询关联套餐id
     * @param dishIds
     * @return
     */
    // select setmeal_id from setmeal_dish where dish_id in (1,2,3,4)
    List<Long> getSetmeallIdByDishIds(List<Long> dishIds);


    /**
     * 批量插入套餐内含的菜品
     * @param setmealDishes
     */
    void insertBatch(List<SetmealDish> setmealDishes);


    @Select("select * from setmeal_dish where setmeal_id=#{setmealId} ")
    List<SetmealDish> getBySetmealId(Long id);

    @Delete("delete from setmeal_dish where setmeal_id=#{setmealId}")
    void deleteBySetmealId(Long id);





    /**
     * 根据套餐id查询菜品dish表里对应的菜
     * @param setmealId
     * @return
     */
    /*
    select * from setmeal_dish as sd join dish on sd.dish_id=dish.id
    where setmeal_id=#{id}



    */
    @Select(" select dish.* from setmeal_dish as sd join dish on sd.dish_id=dish.id" +
            "    where setmeal_id=#{setmealId}")
    List<Dish> getDishBySetmealId(Long setmealId);
}
