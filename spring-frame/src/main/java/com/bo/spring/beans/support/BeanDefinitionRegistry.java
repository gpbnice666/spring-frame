package com.bo.spring.beans.support;

import com.bo.spring.beans.BeanDefinitionRegistryException;
import com.bo.spring.beans.config.BeanDefinition;

/**
 * 用于把 beanDefinition注册到bean工厂
 * @author gpb
 * @date 2022-09-21
 */
public interface BeanDefinitionRegistry {

    /**
     * 用于注册bean
     */
    void registerBeanDefinition(String beanName, BeanDefinition beanDefinition) throws BeanDefinitionRegistryException;

    /**
     * 获取bean信息
     * @param beaName
     * @return
     */
    BeanDefinition getBeanDefinition(String beaName);

    /**
     * 是否包含bean
     * @param beanName
     * @return
     */
    boolean containsBeanDefinition(String beanName);
}
