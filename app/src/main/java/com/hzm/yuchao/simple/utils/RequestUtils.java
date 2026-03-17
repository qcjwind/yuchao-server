package com.hzm.yuchao.simple.utils;

import com.alibaba.fastjson.JSONObject;
import com.hzm.yuchao.simple.filter.RequestWrapper;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class RequestUtils {

    public static boolean isApplicationJson(ServletRequest request) {
        String contentType = request.getContentType();

        // 可能会为null
        if (contentType == null) {
            contentType = "";
        }

        return contentType.toLowerCase().contains("application/json");
    }

    public static String getParam(HttpServletRequest request) {

        if (request instanceof RequestWrapper) {
            return ((RequestWrapper) request).getBodyStr();
        }

        if (isApplicationJson(request)) {
//            Object[] args = joinPoint.getArgs();
//
//            if (args == null) {
//                return "";
//            } else if (args.length == 1) {
//                return JsonUtil.toJsonStr(args[0]);
//            } else {
//                return JsonUtil.toJsonStr(args);
//            }

            try {

                StringBuilder sb = new StringBuilder();
                String line;
                BufferedReader reader = request.getReader();
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }

                return sb.toString();
            } catch (Exception e) {
                log.error("参数读取异常", e);
                return "读取异常";
            }
        }

        Map<String, Object> params = new HashMap<>();
        Enumeration<String> paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String key = paramNames.nextElement();
            String value = request.getParameter(key);

            params.put(key, value);
        }

        return JSONObject.toJSONString(params);
    }
}
