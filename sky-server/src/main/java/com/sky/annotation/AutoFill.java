package com.sky.annotation;

import com.sky.enumeration.OperationType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义注解，自动填充
 */
@Target(ElementType.METHOD)//表示该注解作用在方法上
@Retention(RetentionPolicy.RUNTIME)//在运行时动态地获取注解信息并执行相应的操作
public @interface AutoFill {
    /**
     * 数据库操作类型 update，insert
     * @return
     */
    OperationType value();
}
