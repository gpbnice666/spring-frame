package com.bo.spring.beans.config;

import org.apache.commons.lang3.StringUtils;

/**
 * bean对象定义
 * @author gpb
 * @date 2022-09-21
 */
public interface BeanDefinition {

    /**
     * 单例
     */
    String SCOPE_SINGLETON = "singleton";

    /**
     * 原型
     */
    String SCOPE_PROTOTYPE = "prototype";

    /**
     * 类名
     * @return
     */
    Class<?> getBeanClass();

    /**
     * 工厂类名、工厂方法名
     * @return
     */
    String getFactoryMethodName();

    /**
     * 工厂bean名、工厂方法名
     * @return
     */
    String getFactoryBeanName();

    /**
     * Scope获取类型
     */
    String getScope();

    /**
     * 是否单例
     * @return
     */
    boolean isSingleton();

    /**
     * 是否原型
     * @return
     */
    boolean isPrototype();

    /**
     * 初始化方法
     * @return
     */
    String getInitMethodName();

    /**
     * 销毁方法
     * @return
     */
    String getDestroyMethodName();

    boolean isPrimary();

    /**
     * 校验bean定义的合法性
     * @return
     */
    default boolean validate(){
        // 没定义class,工厂bean或者工厂方法没指定，则不合法
        if(this.getBeanClass() == null){
            if(StringUtils.isBlank(getFactoryBeanName()) || StringUtils.isBlank(getFactoryMethodName()))
                return false;
        }
        // 定义了类，又定义工厂bean,不合法
        if(this.getBeanClass() != null && StringUtils.isNoneBlank(getFactoryBeanName()))
            return false;
        return true;
    }
}
