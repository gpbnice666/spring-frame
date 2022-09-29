package com.bo.spring.beans;

import com.bo.spring.beans.config.BeanDefinition;
import com.bo.spring.beans.support.BeanDefinitionRegistry;
import org.apache.commons.lang3.StringUtils;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * beanFactory默认实现
 * 主要实现5个功能
 * 实现Bean定义信息的注册
 * 实现Bean工厂定义的getBean方法
 * 实现初始化方法的执行
 * 实现单例的要求
 * 实现容器关闭是执行单例销毁操作
 */
public class DefaultBeanFactory implements BeanFactory, BeanDefinitionRegistry, Closeable {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DefaultBeanFactory.class);

    protected Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>(256);

    private Map<String, Object> singletonBeanMap = new ConcurrentHashMap<>(256);

    private Map<Class<?>, Set<String>> typeMap = new ConcurrentHashMap<>(256);

    @Override
    public Object getBean(String name) throws Exception {
        return this.doGetBean(name);
    }

    @Override
    public <T> T getBean(Class<T> type) throws Exception {
        /**
         * 逻辑：
         *   1.获得其对应的所有的BeanDefinition
         *   2.如果只有一个,直接获取bean实例返回,否则
         *   3.遍历找出Primary的
         *   4.如果primary没有,或大于1个,抛出异常
         *   5.返回Primary实例
         */
        Set<String> names = this.typeMap.get(type);
        if (names != null) {
            if (names.size() == 1) {
                return (T) this.getBean(names.iterator().next());
            } else {
                // 找 Primary
                BeanDefinition bd = null;
                String primaryName = null;
                StringBuffer nameStrings = new StringBuffer();
                for (String name : names) {
                    bd = this.getBeanDefinition(name);
                    if (bd != null && bd.isPrimary()) {
                        if (primaryName != null) {
                            String mess = type + " 类型的Bean存储多个Primary[" + primaryName + "," + name + "]";
                            log.error(mess);
                            throw new Exception(mess);
                        } else {
                            primaryName = name;
                        }
                    }
                    nameStrings.append(" " + name);
                }

                if (primaryName != null) {
                    return (T) this.getBean(primaryName);
                } else {
                    String mess = type + " 类型的Bean存在多个[" + nameStrings + "] 但无法确定Primary";
                    log.error(mess);
                    throw new Exception(mess);
                }

            }
        }
        return null;
    }

    @Override
    public <T> Map<String, T> getBeanOfType(Class<T> type) throws Exception {
        return null;
    }

    @Override
    public Class<?> getType(String name) throws Exception {
        BeanDefinition bd = this.getBeanDefinition(name);
        Class<?> type = bd.getBeanClass();
        if (type != null) {
            if (StringUtils.isBlank(bd.getFactoryMethodName())) {
                // 构造方法来构造对象,Type就是Class,不需要作什么
            } else {
                // 静态工厂方式的，反射获得Method,在获取Method的返回值类型
                // 是获取类中自己声明的方法，即自己声明的任何权限的方法，包括私有方法 getDeclaredMethod
                type = type.getDeclaredMethod(bd.getFactoryMethodName()).getReturnType();
            }
        } else {
            // 工厂bean方式来构造对象的
            // 获得工厂bean的Class
            type = this.getType(bd.getFactoryBeanName());
            // 获得工厂方法的返回值类型
            type = type.getDeclaredMethod(bd.getFactoryBeanName()).getReturnType();
        }
        return type;
    }

    @Override
    public void registerBeanDefinition(String beanName, BeanDefinition beanDefinition) throws BeanDefinitionRegistryException {
        Objects.requireNonNull(beanName, "注册bean需要给入beanName");
        Objects.requireNonNull(beanDefinition, "注册bean需要给入beanDefinition");

        // 校验beanDefinition是否合法
        if (!beanDefinition.validate())
            throw new BeanDefinitionRegistryException("名字为[" + beanName + "] 的bean定义不合法：" + beanDefinition);
        // Spring中默认是不可以覆盖bean的
        // 可以通过 spring.main.allow-bean-definition-overriding: true 来允许覆盖
        if (this.containsBeanDefinition(beanName))
            throw new BeanDefinitionRegistryException("名字为[" + beanName + "] 的bean已经存在：" + this.getBeanDefinition(beanName));
        this.beanDefinitionMap.put(beanName, beanDefinition);
    }

    /**
     * beanDefinitionMap中的bean对象 映射到 typeMap 中去 key:Class,value:Set<String>(beanName)
     *
     * @throws Exception
     */
    public void registerTypeMap() throws Exception {
        for (String name : this.beanDefinitionMap.keySet()) {
            Class<?> type = this.getType(name);
            // 映射本类
            this.registerTypeMap(name, type);
            // 映射父类
            this.registerSuperClassTypeMap(name, type);
        }
    }

    public void registerTypeMap(String name, Class<?> type) throws Exception {
        Set<String> namesType = this.typeMap.get(type);
        if (namesType == null) {
            namesType = new HashSet<>();
            this.typeMap.put(type, namesType);
        }
        namesType.add(name);
    }

    public void registerSuperClassTypeMap(String name, Class<?> type) throws Exception {
        Class<?> superclass = type.getSuperclass();
        if(superclass != null && !superclass.equals(Object.class)){
            // bean父类
            this.registerTypeMap(name,superclass);
            // 递归找出所有父类
            this.registerSuperClassTypeMap(name,superclass);
            // 找出父类实现的接口注册
            this.registerInterfaceTypeMap(name,superclass);
        }
    }

    private void registerInterfaceTypeMap(String name, Class<?> type) throws Exception {
        Class<?>[] interfaces = type.getInterfaces();
        if (interfaces.length > 0) {
            for (Class<?> anInterface : interfaces) {
                this.registerTypeMap(name,anInterface);
                // 递归找出父类
                this.registerInterfaceTypeMap(name,anInterface);
            }
        }
    }

    @Override
    public BeanDefinition getBeanDefinition(String beaName) {
        return this.beanDefinitionMap.get(beaName);
    }

    @Override
    public boolean containsBeanDefinition(String beanName) {
        return this.beanDefinitionMap.containsKey(beanName);
    }

    protected Object doGetBean(String beanName) throws Exception {
        Objects.requireNonNull(beanName, "beanName不能为空");
        // 如何实现单例
        // 如何保证单例
        Object instance = singletonBeanMap.get(beanName);
        if (instance != null)
            return instance;
        BeanDefinition bd = this.getBeanDefinition(beanName);
        Objects.requireNonNull(bd, "beanDefinition不能为空");
        // 如果是单例
        if (bd.isSingleton()) {
            // 加锁
            synchronized (this.singletonBeanMap) {
                instance = this.singletonBeanMap.get(beanName);
                // DCL 双检锁
                if (instance == null) {
                    instance = doCreateInstance(bd);
                    this.singletonBeanMap.put(beanName, instance);
                }
            }
        } else
            instance = doCreateInstance(bd);
        return instance;
    }

    private Object doCreateInstance(BeanDefinition bd) throws Exception {
        Class<?> type = bd.getBeanClass();
        Object instance = null;
        if (type != null)
            if (StringUtils.isBlank(bd.getFactoryMethodName()))
                // 构造方法来构造对象
                instance = this.createInstanceByConstructor(bd);
            else
                // 静态工厂创建对象
                instance = this.createInstanceByStaticFactoryMethod(bd);
        else
            // 工厂bean方式来构造对象
            instance = this.createInstanceByFactoryBean(bd);
        // 执行初始化方法
        this.doInit(bd, instance);
        return instance;
    }

    /**
     * 执行初始化方法
     *
     * @param bd
     * @param instance
     */
    private void doInit(BeanDefinition bd, Object instance) throws Exception {
        if (StringUtils.isNoneBlank(bd.getInitMethodName())) {
            // 是获取类中所有公共方法，包括继承自父类的 getMethod
            Method method = instance.getClass().getMethod(bd.getInitMethodName(), null);
            method.invoke(instance);
        }
    }

    /**
     * 工厂bean方式来构造对象
     *
     * @param bd
     * @return
     * @throws Exception
     */
    private Object createInstanceByFactoryBean(BeanDefinition bd) throws Exception {
        Object factoryBean = this.doGetBean(bd.getFactoryBeanName());
        Method method = factoryBean.getClass().getMethod(bd.getFactoryMethodName(), null);
        return method.invoke(factoryBean, null);
    }

    /**
     * 静态工厂创建对象
     *
     * @param bd
     * @return
     * @throws Exception
     */
    private Object createInstanceByStaticFactoryMethod(BeanDefinition bd) throws Exception {
        Class<?> type = bd.getBeanClass();
        Method method = type.getMethod(bd.getFactoryMethodName(), null);
        return method.invoke(type, null);
    }

    /**
     * 构造方法来构造对象
     *
     * @param bd
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    private Object createInstanceByConstructor(BeanDefinition bd) throws InstantiationException, IllegalAccessException {
        try {
            return bd.getBeanClass().newInstance();
        } catch (SecurityException e) {
            log.error("创建bean的实例异常,beanDefinition：" + bd, e);
            throw e;
        }
    }


    @Override
    public void close() throws IOException {
        // 执行销毁方法
        for (Map.Entry<String, BeanDefinition> e : this.beanDefinitionMap.entrySet()) {
            String beanName = e.getKey();
            BeanDefinition bd = e.getValue();
            if(bd.isSingleton() && StringUtils.isNoneBlank(bd.getDestroyMethodName())){
                Object instance = this.singletonBeanMap.get(beanName);
                try {
                    Method method = instance.getClass().getMethod(bd.getDestroyMethodName());
                    method.invoke(instance);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    log.error("执行bean[" + beanName + "] " + bd + " 的 销毁方法异常！", ex);
                }
            }
        }

        // 原型bean如果指定了销毁方法 咋整？
    }
}
