package com.hzm.yuchao.simple;

import cn.hutool.crypto.SecureUtil;
import com.alibaba.fastjson.JSONObject;
import com.hzm.yuchao.simple.base.BaseResponse;
import com.hzm.yuchao.simple.base.SimpleResponse;
import com.hzm.yuchao.simple.constant.Constants;
import com.hzm.yuchao.simple.constant.SessionUtils;
import com.hzm.yuchao.simple.utils.MonitorUtils;
import com.hzm.yuchao.simple.utils.RequestUtils;
import com.hzm.yuchao.simple.utils.TraceUtils;
import com.hzm.yuchao.simple.utils.UuidUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.HashSet;
import java.util.Set;

/**
 * 数字越小，优先级越高
 */
@Slf4j
@Aspect
@Component
@Order(Integer.MIN_VALUE)
public class ControllerAspect {

    private static final Set<String> BLANK_SET = new HashSet<>();

    static {
        BLANK_SET.add("/app/match/list");
        BLANK_SET.add("/app/match/info");
        BLANK_SET.add("/app/match/info");
        BLANK_SET.add("/app/banner/list");
        BLANK_SET.add("/mng/match/info");
    }


    @Pointcut("execution(* com.hzm.yuchao.biz.controller..*.*(..))")
    public void pointcut() {

    }

    @Around("pointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                .getRequest();

        String traceId = UuidUtils.uuid16();
        TraceUtils.initTrace(traceId);

        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        boolean isVoid = methodSignature.getReturnType().equals(void.class);

        String requestStr = RequestUtils.getParam(request);
        boolean success = true;
        long startTime = System.currentTimeMillis();
        BaseResponse response = null;

        String digest = "";

        try {
            // token
            tokenCheck(request);

            if (isVoid) {
                joinPoint.proceed();
                // 构造虚拟 response
                response = BaseResponse.success();
            } else {
                response = (BaseResponse) joinPoint.proceed();
            }

        } catch (BizException e) {
            digest = e.getMessage();
            response = buildFailResponse(joinPoint, e.code, e.msg);
        } catch (Throwable e) {
            log.error("接口异常", e);
            success = false;
            digest = e.getMessage();
            response = buildFailResponse(joinPoint, 501, e.getMessage());
        } finally {
            response.setTraceId(traceId);

            long timeConsuming = System.currentTimeMillis() - startTime;
            boolean bizSuccess = response.getCode() == 200;

            log.info("http请求 {}, success: {}, 耗时: {}, request: {}, response: {}", request.getServletPath(),
                    success + "/" + bizSuccess, timeConsuming, requestStr, JSONObject.toJSONString(response));
            MonitorUtils.log("CONTROLLER", request.getServletPath(), success, bizSuccess, timeConsuming, digest);

            SessionUtils.removeUser();
            TraceUtils.removeTraceId();
        }

        if (isVoid) {
            return null;
        }

        return response;
    }

    private BaseResponse buildFailResponse(ProceedingJoinPoint joinPoint, int code, String msg) {

        BaseResponse instance;
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Class returnType = signature.getReturnType();

            if (returnType.equals(void.class)) {
                // 构造虚拟 response
                instance = BaseResponse.success();
            } else {
                instance = (BaseResponse) returnType.newInstance();
            }

        } catch (Throwable e) {
            log.error("内部处理异常", e);
            instance = new SimpleResponse<>();
        }

        instance.setCode(code);
        instance.setMsg(msg);
        return instance;
    }

    private void tokenCheck(HttpServletRequest request) {

        String servletPath = request.getServletPath();

        // 本地模式，不验证token.
        if (request.getServerName().contains("127.0.0.1")) {
            boolean isMng = servletPath.startsWith("/mng");
            SessionUtils.putUser(isMng ? "admin" : "1");

//            if (servletPath.contains("buySaleTicket")) {
//                SessionUtils.putUser((long) (Math.random() * 9999999) + "");
//            }

            return;
        }


        if ((servletPath.startsWith("/mng") && !servletPath.contains("login"))
                || (servletPath.startsWith("/app") && !servletPath.contains("auth"))) {

            String token = request.getHeader("token");
            if (token == null) {
                // 下载文件需要从参数里拿Token
                token = request.getParameter("token");
            }
            if (token == null) {
                // 不验证Token
                if (BLANK_SET.contains(servletPath)) {
                    return;
                } else {
                    throw new BizException(1000, "token不存在");
                }
            }

            TokenDTO tokenDTO;
            try {
                String info = SecureUtil.aes(Constants.AES_PASSWORD).decryptStr(token);

                tokenDTO = JSONObject.parseObject(info, TokenDTO.class);
            } catch (Exception e) {
                throw new BizException(1000, "token无效");
            }

            if (tokenDTO.getPlatform() == TokenDTO.PlatformEnum.APP) {
                if (!servletPath.startsWith("/app")) {
                    throw new BizException(1000, "token越权");
                }
                SessionUtils.putUser(tokenDTO.getUserId() + "");

            } else if (tokenDTO.getPlatform() == TokenDTO.PlatformEnum.MNG) {
                if (!servletPath.startsWith("/mng")) {
                    throw new BizException(1000, "token越权");
                }
                SessionUtils.putUser(tokenDTO.getUsername());

            } else {
                throw new BizException(1000, "token非法");
            }

            // 24 小时
            if (System.currentTimeMillis() - tokenDTO.getCurrentTime() > 24 * 60 * 60 * 1000L) {
                throw new BizException(1000, "token已过期，请重新登录");
            }
        }
    }

}
