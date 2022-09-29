package com.bo.spring.beans;

public class BeanDefinitionRegistryException extends Exception {

    private static final long serialVersionUID = 6056374114834139330L;

    public BeanDefinitionRegistryException(String mess) {
        super(mess);
    }

    public BeanDefinitionRegistryException(String mess, Throwable e) {
        super(mess, e);
    }
}
