package com.bo.spring.beans;

import java.util.Map;

/**
 * bean工厂
 * @author gpb
 * @date 2022-09-21
 */
public interface BeanFactory {

    /**
     * 对外提供获取bean实例的方法
     * @param name
     * @return
     * @throws Exception
     */
    Object getBean(String name) throws Exception;

    /**
     * class类型获取
     * @param type
     * @param <T>
     * @return
     * @throws Exception
     */
    <T> T getBean(Class<T> type)throws Exception;

    /**
     * class类型获取
     * @param type
     * @param <T>
     * @return
     * @throws Exception
     */
    <T> Map<String,T> getBeanOfType(Class<T> type)throws Exception;

    Class<?> getType(String name) throws Exception;
}
