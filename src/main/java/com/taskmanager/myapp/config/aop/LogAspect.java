package com.taskmanager.myapp.config.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class LogAspect {

    @Around("execution(* com.taskmanager.myapp.service.*Service.*(..)) || execution(* com.taskmanager.myapp.controller.*Controller.*(..))")
    public Object addLog(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        log.info(">>> start : {} <<<", proceedingJoinPoint.getSignature());

        Object result = proceedingJoinPoint.proceed();

        log.info(">>> result : {} <<<", result);
        log.info(">>> end : {} <<<", proceedingJoinPoint.getSignature());

        return result;
    }
}