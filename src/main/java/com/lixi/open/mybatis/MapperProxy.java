package com.lixi.open.mybatis;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;

@Slf4j
@AllArgsConstructor
public class MapperProxy<T> implements InvocationHandler {

    private Class<T> interfaceCls;
    private Map<Method, MapperMethod> interfaceMethodToActualMethodToCall;

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
//        log.info("执行 pre");
        // 因为这里本身就是接口的 Method 没有实现不能直接调用，只能通过这个方法去找动态生成的那个方法
        Object result = interfaceMethodToActualMethodToCall.get(method).execute(args);
//        log.info("执行 after");
        return result;
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
