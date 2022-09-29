package com.bo.spring.samples.ioc;

public class ABean {

    public void doSomething() {
        System.out.println(System.currentTimeMillis() + " " + this);
    }

    public void init() {
        System.out.println("ABean.init() 执行了");
    }

    public void destroy() {
        System.out.println("ABean.destroy() 执行了");
    }

}
