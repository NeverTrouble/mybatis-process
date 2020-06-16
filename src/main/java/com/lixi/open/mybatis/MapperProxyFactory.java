package com.lixi.open.mybatis;

import lombok.AllArgsConstructor;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

@AllArgsConstructor
public class MapperProxyFactory<T> {

    private Class<T> mapperInterface;
    private Map<Method, MapperMethod> methodToMapperMethod;

    public T newInstance() {
        MapperProxy<T> proxy = new MapperProxy<T>(mapperInterface, methodToMapperMethod);
        return newInstance(proxy);
    }

    @SuppressWarnings("unchecked")
    private T newInstance(MapperProxy<T> mapperProxy) {
        return (T) Proxy.newProxyInstance(mapperInterface.getClassLoader(), new Class[]{mapperInterface}, mapperProxy);
    }

}
