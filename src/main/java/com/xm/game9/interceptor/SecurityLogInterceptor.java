package com.xm.game9.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * 安全日志拦截器，记录可疑访问
 *
 * @author X1aoM1ngTX
 */
@Component
public class SecurityLogInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(SecurityLogInterceptor.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        String clientIP = getClientIP(request);
        String userAgent = request.getHeader("User-Agent");
        
        // 记录所有访问
        logger.info("访问日志 - 时间: {}, IP: {}, 方法: {}, 路径: {}, User-Agent: {}", 
                LocalDateTime.now().format(formatter), 
                clientIP, 
                method, 
                requestURI, 
                userAgent);
        
        // 检查可疑访问
        if (isSuspiciousRequest(request)) {
            logger.warn("可疑访问检测 - 时间: {}, IP: {}, 方法: {}, 路径: {}, User-Agent: {}", 
                    LocalDateTime.now().format(formatter), 
                    clientIP, 
                    method, 
                    requestURI, 
                    userAgent);
            
            // 记录详细信息
            logSuspiciousRequestDetails(request);
        }
        
        return true;
    }

    private boolean isSuspiciousRequest(HttpServletRequest request) {
        String requestURI = request.getRequestURI().toLowerCase();
        String method = request.getMethod().toLowerCase();
        
        // 检查敏感路径
        if (requestURI.contains(".env") || 
            requestURI.contains("config") || 
            requestURI.contains("application") ||
            requestURI.contains("bootstrap") ||
            requestURI.contains("web.xml") ||
            requestURI.contains("proc") ||
            requestURI.contains("sys") ||
            requestURI.contains("etc/passwd")) {
            return true;
        }
        
        // 检查攻击模式
        if (requestURI.contains("..") || 
            requestURI.contains("<script") ||
            requestURI.contains("javascript:") ||
            requestURI.contains("vbscript:") ||
            requestURI.contains("xss") ||
            requestURI.contains("sql") ||
            requestURI.contains("select") ||
            requestURI.contains("insert") ||
            requestURI.contains("update") ||
            requestURI.contains("delete") ||
            requestURI.contains("drop") ||
            requestURI.contains("union") ||
            requestURI.contains("exec") ||
            requestURI.contains("eval") ||
            requestURI.contains("shell") ||
            requestURI.contains("system")) {
            return true;
        }
        
        // 检查异常的请求头
        String userAgent = request.getHeader("User-Agent");
        if (userAgent == null || userAgent.isEmpty() || 
            userAgent.contains("bot") || 
            userAgent.contains("spider") || 
            userAgent.contains("crawler") ||
            userAgent.contains("scanner")) {
            return true;
        }
        
        return false;
    }

    private void logSuspiciousRequestDetails(HttpServletRequest request) {
        Map<String, String> details = new HashMap<>();
        
        // 基本信息
        details.put("requestURI", request.getRequestURI());
        details.put("method", request.getMethod());
        details.put("clientIP", getClientIP(request));
        details.put("userAgent", request.getHeader("User-Agent"));
        details.put("referer", request.getHeader("Referer"));
        details.put("timestamp", LocalDateTime.now().format(formatter));
        
        // 请求头
        Enumeration<String> headerNames = request.getHeaderNames();
        Map<String, String> headers = new HashMap<>();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            headers.put(headerName, request.getHeader(headerName));
        }
        details.put("headers", headers.toString());
        
        // 请求参数
        Map<String, String[]> params = request.getParameterMap();
        if (!params.isEmpty()) {
            details.put("parameters", params.toString());
        }
        
        try {
            logger.warn("可疑访问详细信息: {}", objectMapper.writeValueAsString(details));
        } catch (Exception e) {
            logger.warn("可疑访问详细信息记录失败: {}", e.getMessage());
        }
    }

    private String getClientIP(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIP = request.getHeader("X-Real-IP");
        if (xRealIP != null && !xRealIP.isEmpty() && !"unknown".equalsIgnoreCase(xRealIP)) {
            return xRealIP;
        }
        
        return request.getRemoteAddr();
    }
}