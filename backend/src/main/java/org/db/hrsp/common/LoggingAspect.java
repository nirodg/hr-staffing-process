package org.db.hrsp.common;

import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.lang.reflect.Proxy;
import java.util.UUID;

@Aspect
@Component
@Slf4j
public class LoggingAspect extends HttpFilter {

    private static final String TRACE_ID = "traceId";
    private static final String USER_ID = "userId";
    private static final String HEADER_TRACE_ID = "X-Trace-Id";

    @Around("@within(logMethodExecution)")
    public Object logAnnotatedClassMethods(ProceedingJoinPoint joinPoint, LogMethodExecution logMethodExecution) throws Throwable {
        return logExecution(joinPoint, "CLASS", logMethodExecution.value());
    }

    @Around("@annotation(logMethodExecution)")
    public Object logAnnotatedMethod(ProceedingJoinPoint joinPoint, LogMethodExecution logMethodExecution) throws Throwable {
        return logExecution(joinPoint, "METHOD", logMethodExecution.value());
    }

    @Around("execution(* org.springframework.data.repository.Repository+.*(..)) && " +
            "!@annotation(org.db.hrsp.common.LogMethodExecution) && " +
            "!@within(org.db.hrsp.common.LogMethodExecution)")
    public Object logRepositoryMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        return logExecution(joinPoint, "REPOSITORY", "");
    }

    // Unified logging logic
    private Object logExecution(ProceedingJoinPoint joinPoint, String methodType, String customMessage) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String className = getTargetClassName(joinPoint.getTarget());
//        String traceId = MDC.get(TRACE_ID);
//        String userId = MDC.get(USER_ID);

        // Log entry (include custom message if provided)
        String logPrefix = String.format("[%s] Class: %s",
                methodType, className);
        log.info("{} => Entering {}{}", logPrefix, methodName,
                customMessage.isEmpty() ? "" : " (" + customMessage + ")");

        try {
            Object result = joinPoint.proceed();
            log.info("{} => Exiting {}", logPrefix, methodName);
            return result;
        } catch (Throwable ex) {
            log.error("{} => FAILED {}: {}", logPrefix, methodName, ex.getMessage());
            throw ex;
        }
    }

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {

        // Get traceId from the request header, or generate a new one if missing
        String traceId = request.getHeader(HEADER_TRACE_ID);
        if (traceId == null || traceId.isEmpty()) {
            traceId = UUID.randomUUID().toString();
        }
        MDC.put(TRACE_ID, traceId);

        // Retrieve userId from the security context
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = (authentication != null && authentication.isAuthenticated()) ? authentication.getName() : "ANONYMOUS";
        MDC.put(USER_ID, userId);

        try {
            chain.doFilter(request, response);  // Continue with the next filter in the chain
        } finally {
            // Remove traceId and userId from MDC after the request is processed
            MDC.remove(TRACE_ID);
            MDC.remove(USER_ID);
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        super.init(filterConfig);
    }

    private String getTargetClassName(Object proxy) {
        try {
            if (Proxy.isProxyClass(proxy.getClass())) {
                Class<?>[] interfaces = proxy.getClass().getInterfaces();
                if (interfaces.length > 0) {
                    return interfaces[0].getSimpleName();  // Returns e.g. UserRepository
                }
            } else if (proxy.getClass().getName().contains("$$EnhancerBySpringCGLIB")) {
                return proxy.getClass().getSuperclass().getSimpleName();  // Handles CGLIB proxies
            }
            return proxy.getClass().getSimpleName();
        } catch (Exception e) {
            log.warn("Failed to resolve target class: {}", e.getMessage());
            return proxy.getClass().getSimpleName();
        }
    }
}