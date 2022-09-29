package com.bo.v1;

import com.bo.spring.beans.DefaultBeanFactory;
import com.bo.spring.beans.config.BeanDefinition;
import com.bo.spring.beans.support.GenericBeanDefinition;
import com.bo.spring.samples.ioc.ABean;
import com.bo.spring.samples.ioc.ABeanFactory;
import org.apache.commons.lang3.StringUtils;
import org.junit.AfterClass;
import org.junit.Test;
import org.omg.SendingContext.RunTime;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * github
 * token: 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
 */
public class DefaultBeanFactoryTest {

    static DefaultBeanFactory bf = new DefaultBeanFactory();


    public static void main(String[] args) throws Exception {
        Object aBean = new ABean();
        Class aClass = aBean.getClass();
        Method init = aClass.getMethod("init");
        init.invoke(aClass.newInstance());


        GenericBeanDefinition bd = new GenericBeanDefinition();
        bd.setBeanClass(ABean.class);
        bd.setScope(BeanDefinition.SCOPE_PROTOTYPE);
        bd.setInitMethodName("init");
        bd.setDestroyMethodName("destroy");

        System.out.println(StringUtils.isBlank(bd.getFactoryBeanName()));
        Object o = bd.getBeanClass().newInstance();
        o.getClass().getMethod("init",null).invoke(o);
    }

    // 通过构造器创建
    @Test
    public void testRegistry() throws Exception{
        GenericBeanDefinition bd = new GenericBeanDefinition();
        bd.setBeanClass(ABean.class);
        bd.setScope(BeanDefinition.SCOPE_SINGLETON);
        bd.setInitMethodName("init");
        bd.setDestroyMethodName("destroy");
        bf.registerBeanDefinition("aBean",bd);

    }

    // 通过注册工厂bean
    @Test
    public void testRegistryFactoryMethod() throws Exception{
        GenericBeanDefinition bd = new GenericBeanDefinition();
        // 注册bean工厂
        String factoryBeanName = "factory";
        bd.setBeanClass(ABeanFactory.class);
        bf.registerBeanDefinition(factoryBeanName,bd);
        // 注册用工厂bean来创建实例的bean
        bd = new GenericBeanDefinition();
        bd.setFactoryBeanName(factoryBeanName);
        bd.setFactoryMethodName("getABean2");
        bd.setScope(BeanDefinition.SCOPE_PROTOTYPE);
        bd.setPrimary(true);

        bf.registerBeanDefinition("factoryABean",bd);
    }

    // 通过注册静态工厂bean
    @Test
    public void testRegistryStaticFactoryMethod() throws Exception{
        GenericBeanDefinition bd = new GenericBeanDefinition();
        bd.setBeanClass(ABeanFactory.class);
        bd.setFactoryMethodName("getABean");
        bf.registerBeanDefinition("staticABean", bd);

        GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
        beanDefinition.setBeanClass(ABean.class);
        beanDefinition.setInitMethodName("init");
        beanDefinition.setDestroyMethodName("destroy");
        bf.registerBeanDefinition("bdABean",beanDefinition);
    }



    @AfterClass
    public static void testGetBean() throws Exception{

        //执行typeMap生成
   //     bf.registerTypeMap();

//        System.out.println("构造方法执行-------");
//        for (int i = 0; i < 3; i++) {
//            ABean aBean = (ABean) bf.getBean("aBean");
//            aBean.doSomething();
//        }

//        System.out.println("工厂方法-------");
//        for (int i = 0; i < 3; i++) {
//            ABean aBean = (ABean) bf.getBean("factoryABean");
//            aBean.doSomething();
//        }

        System.out.println("静态工厂方法方式------------");
        for (int i = 0; i < 3; i++) {
            ABean ab = (ABean) bf.getBean("staticABean");
            ab.doSomething();
        }

        System.out.println("getBeanOfType类型获取bean");
        Map<String, ABean> beanOfType = bf.getBeanOfType(ABean.class);
        beanOfType.forEach((s, aBean) -> System.out.println(s + " == " + aBean));

        System.out.println("type类型获取bean");
        ABean bean = bf.getBean(ABean.class);
        System.out.println(bean);

        // 程序结束的时候 自然结束, 主动关停,程序结束的时候调用关闭方法，调用bean指定的销毁方法
        Runtime.getRuntime().addShutdownHook(new Thread(()->{
            System.out.println("hook shut down");
            try {
                bf.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }));
    }
}
