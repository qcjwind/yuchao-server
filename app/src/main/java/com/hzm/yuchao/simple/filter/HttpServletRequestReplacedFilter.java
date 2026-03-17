package com.hzm.yuchao.simple.filter;

import com.hzm.yuchao.simple.utils.RequestUtils;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * @description 继承过滤器
 **/
@Slf4j
@WebFilter(urlPatterns = "/wx/*")
public class HttpServletRequestReplacedFilter implements Filter {

    @Override
    public void init(FilterConfig arg0) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        try {
            if (RequestUtils.isApplicationJson(request)) {

                // 获取请求中的流如何，将取出来的字符串，再次转换成流，然后把它放入到新request对象中。
                ServletRequest requestWrapper = new RequestWrapper((HttpServletRequest) request);
                // 在chain.doFiler方法中传递新的request对象
                chain.doFilter(requestWrapper, response);
            } else {
                chain.doFilter(request, response);
            }
        } catch (Throwable t) {
            log.error("filter 出现严重异常", t);
        }
    }

    @Override
    public void destroy() {

    }
}