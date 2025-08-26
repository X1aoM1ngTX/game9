package com.xm.game9.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * 安全过滤器，阻止对敏感文件的访问
 *
 * @author X1aoM1ngTX
 */
public class SecurityFilter implements Filter {

    private static final List<String> SENSITIVE_PATHS = Arrays.asList(
            "/.env",
            "/.env.local",
            "/.env.development",
            "/.env.production",
            "/.env.test",
            "/config",
            "/application.properties",
            "/application.yml",
            "/application.yaml",
            "/bootstrap.properties",
            "/bootstrap.yml",
            "/web.xml",
            "/proc",
            "/sys",
            "/etc/passwd"
    );

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        String requestURI = httpRequest.getRequestURI();
        
        // 检查是否包含敏感路径
        for (String sensitivePath : SENSITIVE_PATHS) {
            if (requestURI.contains(sensitivePath)) {
                httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied");
                return;
            }
        }
        
        // 检查是否尝试访问环境变量文件
        if (requestURI.toLowerCase().contains("env") && 
            (requestURI.endsWith(".env") || requestURI.contains(".env."))) {
            httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied");
            return;
        }
        
        // 检查是否包含典型的攻击模式
        if (containsAttackPattern(requestURI)) {
            httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied");
            return;
        }
        
        chain.doFilter(request, response);
    }

    private boolean containsAttackPattern(String uri) {
        String lowerUri = uri.toLowerCase();
        
        // 检查常见的攻击模式，但排除合法的API路径
        // 排除 /api/user/adminUpdate 等合法路径
        if (lowerUri.contains("/api/user/adminupdate") || 
            lowerUri.contains("/api/user/delete") ||
            lowerUri.contains("/api/game/update") ||
            lowerUri.contains("/api/game/delete")) {
            return false;
        }
        
        // 检查典型的攻击模式
        return lowerUri.contains("..") || 
               lowerUri.contains("<script") ||
               lowerUri.contains("javascript:") ||
               lowerUri.contains("vbscript:") ||
               lowerUri.contains("onload=") ||
               lowerUri.contains("onerror=") ||
               lowerUri.contains("onclick=") ||
               lowerUri.contains("xss") ||
               // 更精确的SQL注入检测
               (lowerUri.contains("sql") && !lowerUri.contains("/api/")) ||
               (lowerUri.contains("select") && !lowerUri.contains("/api/")) ||
               (lowerUri.contains("insert") && !lowerUri.contains("/api/")) ||
               (lowerUri.contains("update") && !lowerUri.contains("/api/")) ||
               (lowerUri.contains("delete") && !lowerUri.contains("/api/")) ||
               (lowerUri.contains("drop") && !lowerUri.contains("/api/")) ||
               (lowerUri.contains("union") && !lowerUri.contains("/api/")) ||
               lowerUri.contains("exec(") ||
               lowerUri.contains("system(") ||
               lowerUri.contains("eval(") ||
               lowerUri.contains("shell_exec(") ||
               lowerUri.contains("passthru(") ||
               lowerUri.contains("proc_open(");
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // 初始化逻辑
    }

    @Override
    public void destroy() {
        // 清理逻辑
    }
}