package com.bo.spring.samples.ioc;

public class SuperClassTest extends ABean {


    public static void main(String[] args) {
        Class<? super SuperClassTest> superclass = SuperClassTest.class.getSuperclass();
        System.out.println(superclass);
        System.out.println(superclass.getSuperclass());
         registerSuperClassTypeMap(SuperClassTest.class);
    }


    public static void registerSuperClassTypeMap(Class<?> type) {
        Class<?> superclass = type.getSuperclass();
        // bean父类
        System.out.println("superclass=: " + superclass);
        if (superclass != null && !superclass.equals(Object.class)) {
            registerSuperClassTypeMap(superclass);
        }
    }
}
