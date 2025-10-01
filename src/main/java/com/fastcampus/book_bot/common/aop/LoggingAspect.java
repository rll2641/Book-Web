package com.fastcampus.book_bot.common.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class LoggingAspect {

    @Around("execution(* com.fastcampus.book_bot.controller..*(..))")
    public Object logControllerExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();

        try {
            Object result = joinPoint.proceed();

            long executionTime = System.currentTimeMillis() - startTime;
            log.info("[CONTROLLER] {}.{}() 실행 시간: {}ms",
                    className, methodName, executionTime);

            return result;

        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            log.error("[CONTROLLER-FAIL] {}.{}() 실행 시간: {}ms - 에러: {}",
                    className, methodName, executionTime, e.getMessage());
            throw e;
        }
    }

    @Around("execution(* com.fastcampus.book_bot.service..*(..))")
    public Object logServiceExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();

        try {
            Object result = joinPoint.proceed();

            long executionTime = System.currentTimeMillis() - startTime;
            log.info("[SERVICE] {}.{}() 실행 시간: {}ms",
                    className, methodName, executionTime);

            return result;

        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            log.error("[SERVICE-FAIL] {}.{}() 실행 시간: {}ms - 에러: {}",
                    className, methodName, executionTime, e.getMessage());
            throw e;
        }
    }
}