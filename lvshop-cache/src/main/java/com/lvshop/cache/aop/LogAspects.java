package com.lvshop.cache.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * 日志切面类
 * @author Galliano
 */
@Aspect
@Component
public class LogAspects {
    private static final Logger LOGGER = LoggerFactory.getLogger(LogAspects.class);

    @Pointcut("execution(* com.lvshop.cache.controller.*Controller.*(..))")
    public void pointCut() {}

    @Before("pointCut()")
    public void logStart(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        LOGGER.info(joinPoint.getSignature().getName() + "方法即将运行，参数列表：" + Arrays.asList(args));
    }

    @After("pointCut()")
    public void logEnd(JoinPoint joinPoint) {
        LOGGER.info(joinPoint.getSignature().getName() + "方法运行结束");
    }

    @AfterReturning(value = "pointCut()", returning = "result")
    public void logReturn(JoinPoint joinPoint, Object result) {
        LOGGER.info(joinPoint.getSignature().getName() + "方法正常返回，返回结果：" + result);
    }

    @AfterThrowing(value = "pointCut()", throwing = "exception")
    public void logException(JoinPoint joinPoint, Exception exception) {
        LOGGER.info(joinPoint.getSignature().getName() + "方法运行出现异常，异常信息：" + exception);
    }
}
