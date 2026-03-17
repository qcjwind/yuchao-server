package com.hzm.yuchao.simple;

import com.hzm.yuchao.simple.base.BaseResponse;
import com.hzm.yuchao.simple.utils.RequestUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 权限校验异常
     */
//    @ExceptionHandler(AccessDeniedException.class)
//    public BaseResponse handleAccessDeniedException(AccessDeniedException e, HttpServletRequest request) {
//        String requestURI = request.getRequestURI();
//        log.error("请求地址'{}',权限校验失败'{}'", requestURI, e.getMessage());
//        return BaseResponse.failure(600, "没有权限，请联系管理员授权");
//    }

    /**
     * 请求方式不支持
     */
//    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
//    public BaseResponse handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException e,
//                                                            HttpServletRequest request) {
//        String requestURI = request.getRequestURI();
//        log.error("请求地址'{}',不支持'{}'请求", requestURI, e.getMethod());
//        return BaseResponse.failure(600, e.getMessage());
//    }

    /**
     * 系统异常
     */
    @ExceptionHandler(Exception.class)
    public BaseResponse handleException(Exception e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        String requestStr = RequestUtils.getParam(request);

        log.error("请求地址'{}',发生系统异常. 请求参数: {}", requestURI, requestStr, e);
        return BaseResponse.failure(600, e.getMessage());
    }

    /**
     * 系统异常
     */
    @ExceptionHandler(BizException.class)
    public BaseResponse handleException(BizException e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        String requestStr = RequestUtils.getParam(request);

        log.error("请求地址'{}',发生业务异常. 请求参数: {}, message: {}", requestURI, requestStr, e.getMessage());
        return BaseResponse.failure(e.getCode(), e.getMsg());
    }

    /**
     * 自定义验证异常
     */
    @ExceptionHandler(BindException.class)
    public BaseResponse handleBindException(BindException e) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();

        String requestURI = request.getRequestURI();
        String requestStr = RequestUtils.getParam(request);

        log.warn("请求地址'{}',发生参数绑定错误. 请求参数: {}。message: {}", requestURI, requestStr, e.getMessage());

        String message = e.getAllErrors().get(0).getDefaultMessage();

        String detail = e.getAllErrors().stream().map(t -> t.getCodes()[0]).collect(Collectors.toList()).toString();

        return BaseResponse.failure(600, "参数校验失败: " + message + "。详细信息：" + detail);
    }
}