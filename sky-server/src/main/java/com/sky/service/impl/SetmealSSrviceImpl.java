package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.exception.SetmealEnableFailedException;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class SetmealSSrviceImpl implements SetmealService {
    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;

    /**
     * 新增菜品
     * @param setmealDTO
     */
    @Override
    @Transactional//其中一项失败就回滚
    public void save(SetmealDTO setmealDTO) {
        //涉及到setmeal和setmeal_dish的两表操作
        //setmeal
        Setmeal setmeal=new Setmeal();
        BeanUtils.copyProperties(setmealDTO,setmeal);
        setmealMapper.insert(setmeal);//新增套餐成功，还需要插入具体有什么菜品（dish）
        //setmealDish 需要套餐id（从setmeal获取），dish相关信息（从dto获取）
        Long setmealId = setmeal.getId();
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();//获取dish相关信息除了套餐id
        if(setmealDishes!=null && setmealDishes.size()>0) {
            setmealDishes.forEach(setmealDish -> {
                setmealDish.setSetmealId(setmealId);//把所有的dish的套餐id都赋值

            });
            //向套餐菜品表插入n条数据(批量插入)
            setmealDishMapper.insertBatch(setmealDishes);


    }}

    /**
     * 根据id查询套餐信息
     * @param id
     * @return
     */
    @Override
    public SetmealVO getById(Long id) {
        //还要把套餐dish也查询出来
        SetmealVO setmealVO=new SetmealVO();
        Setmeal setmeal=setmealMapper.getById(id);
        BeanUtils.copyProperties(setmeal,setmealVO);
        setmealVO.setSetmealDishes(setmealDishMapper.getBySetmealId(id));

        return setmealVO;
    }

    @Override
    public PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {
        PageHelper.startPage(setmealPageQueryDTO.getPage(),setmealPageQueryDTO.getPageSize());
        Page<Setmeal> page=setmealMapper.pageQuery(setmealPageQueryDTO);

        return new PageResult(page.getTotal(),page.getResult());
    }


    @Override
    public void startOrStop(Integer status, Long id) {
        //当想要启售套餐即使套餐的status=1时，如果套餐内包含停售的菜品，则不能起售
        if(status == StatusConstant.ENABLE){
            //根据套餐的id在套餐菜品表找该套餐包含的菜品
           /* List<SetmealDish> setmealDishes = setmealDishMapper.getBySetmealId(id);
            //获取这些菜品的dishId后再
            setmealDishes.forEach(setmealDish->{
                setmealDish.getDishId()
            });*/

            //多表连接查询菜品dish表里对应的菜
            List<Dish> dishes=setmealDishMapper.getDishBySetmealId(id);
            //查询是否停售（status=0），如果是则抛出异常
            if(dishes.size()>0&&dishes!=null){
                dishes.forEach(dish -> {

                    if(dish.getStatus()==0){
                        throw new SetmealEnableFailedException(MessageConstant.SETMEAL_ENABLE_FAILED);
                    }
                });


        }}

        //判断完套餐内菜品无停售菜品，可以启售套餐
        Setmeal setmeal=Setmeal.builder()
                .status(status)
                .id(id)
                .build();
        setmealMapper.update(setmeal);
    }

    @Override
    @Transactional
    public void update(SetmealDTO setmealDTO) {
        //要把套餐菜品也修改

        Setmeal setmeal=new Setmeal();
        BeanUtils.copyProperties(setmealDTO,setmeal);
        setmealMapper.update(setmeal);//先更改套餐基础信息，还需要改套餐菜品
        //先把这个套餐所有的菜品删除（根据setmealId删除）
        setmealDishMapper.deleteBySetmealId(setmealDTO.getId());

        //再重新插入
        //先获取前端传递过来的参数
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();//但这里面没有setmealId的值，需要再获取一遍
        Long setmealId = setmealDTO.getId();
        //插入
        if(setmealDishes!=null && setmealDishes.size()>0) {
            setmealDishes.forEach(setmealDish -> {
                setmealDish.setSetmealId(setmealId);//把所有的dish的setmealId都赋值

            });
            //向套餐菜品表插入n条数据(批量插入)
            setmealDishMapper.insertBatch(setmealDishes);



    }}

    //TODO 删除的套餐不能包含正在售卖的套餐
    @Override
    @Transactional
    public void deleteBatch(List<Long> ids) {
        //先判断要删除的套餐有没有正在售卖的（status==1）
        ids.forEach(id ->{
            Setmeal setmeal=setmealMapper.getById(id);
            if(setmeal.getStatus()==1){
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
            }
        });

        //判断完毕，可以删除，要把setmealdish里的也删了
        ids.forEach(setmealId->{
            setmealMapper.deleteById(setmealId);
            setmealDishMapper.deleteBySetmealId(setmealId);
        });


    }

    /**
     * 条件查询
     * @param setmeal
     * @return
     */
    @Override
    public List<Setmeal> list(Setmeal setmeal) {
        List<Setmeal> list = setmealMapper.list(setmeal);
        return list;
    }
    /**
     * 根据id查询菜品选项
     * @param id
     * @return
     */
    public List<DishItemVO> getDishItemById(Long id) {
        return setmealMapper.getDishItemBySetmealId(id);
    }


}
