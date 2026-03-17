package com.hzm.yuchao.biz.outter;

import com.hzm.yuchao.simple.BizException;
import com.hzm.yuchao.simple.utils.JsonUtil;
import com.hzm.yuchao.simple.utils.MonitorUtils;
import com.hzm.yuchao.simple.utils.RequestUtils;
import com.hzm.yuchao.simple.utils.TraceUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * 数字越小，优先级越高
 */
@Slf4j
@Aspect
@Component
@Order(Integer.MIN_VALUE)
public class OutterAspect {

    @Pointcut("execution(* com.hzm.yuchao.biz.outter..*Controller.*(..))")
    public void pointcut() {

    }

    @Around("pointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                .getRequest();

        TraceUtils.initTrace();

        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        boolean isVoid = methodSignature.getReturnType().equals(void.class);

        String requestStr = RequestUtils.getParam(request);
        boolean success = true;
        long startTime = System.currentTimeMillis();
        Object response = null;

        String digest = "";

        try {
            if (isVoid) {
                joinPoint.proceed();
            } else {
                response = joinPoint.proceed();
            }

        } catch (BizException e) {
            success = false;
            digest = e.getMessage();
            response = "业务失败: " + e.getMessage();
        } catch (Throwable e) {
            log.error("接口异常", e);
            success = false;
            digest = e.getMessage();
            response = "系统失败: " + e.getMessage();
        } finally {

            long timeConsuming = System.currentTimeMillis() - startTime;

            log.info("外部通知 {}, success: {}, 耗时: {}, request: {}, response: {}", request.getServletPath(),
                    success, timeConsuming, requestStr,
                    response == null ? "" : (response instanceof String ? response : JsonUtil.toJsonStr(response)));
            MonitorUtils.log("OUTTER", request.getServletPath(), success, success, timeConsuming, digest);

            MDC.remove("traceId");
        }

        if (isVoid) {
            return null;
        }

        return response;
    }

}
