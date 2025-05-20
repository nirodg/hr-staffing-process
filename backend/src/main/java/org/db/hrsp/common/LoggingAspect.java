package org.db.hrsp.common;

import io.opentelemetry.api.trace.*;
import io.opentelemetry.context.Scope;
import io.opentelemetry.sdk.trace.IdGenerator;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.lang.reflect.Proxy;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class LoggingAspect extends HttpFilter {

    private static final String TRACE_ID = "traceId";
    private static final String USER_ID = "userId";
    private static final String HEADER_TRACE_ID = "X-Trace-Id";
    private static final String ZERO_TRACE = "00000000000000000000000000000000";

    private final Tracer tracer;

    @Around("@within(logMethodExecution)")
    public Object logAnnotatedClassMethods(ProceedingJoinPoint joinPoint, LogMethodExecution logMethodExecution)
            throws Throwable {
        return logExecution(joinPoint, "CLASS", logMethodExecution.value());
    }

    @Around("@annotation(logMethodExecution)")
    public Object logAnnotatedMethod(ProceedingJoinPoint joinPoint, LogMethodExecution logMethodExecution)
            throws Throwable {
        return logExecution(joinPoint, "METHOD", logMethodExecution.value());
    }

    @Around("execution(* org.springframework.data.repository.Repository+.*(..)) && "
            + "!@annotation(org.db.hrsp.common.LogMethodExecution) && "
            + "!@within(org.db.hrsp.common.LogMethodExecution)")
    public Object logRepositoryMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        return logExecution(joinPoint, "REPOSITORY", "");
    }

    private Object logExecution(ProceedingJoinPoint jp, String kind, String extra) throws Throwable {
        String className = getTargetClassName(jp.getTarget());
        String methodName = jp.getSignature().getName();
        String logPrefix = "[" + kind + "] Class: " + className;

        Span span = tracer.spanBuilder(className + "." + methodName).setAttribute("kind", kind)
                .setAttribute("extra", extra)
                .setAttribute(TRACE_ID, MDC.get(TRACE_ID))
                .setAttribute(USER_ID, MDC.get(USER_ID))
                .startSpan();


        String traceId = MDC.get(TRACE_ID);
        if (traceId == null || traceId.isBlank()) {
            MDC.put(TRACE_ID, span.getSpanContext().getTraceId());
        }

		try (Scope ignored = span.makeCurrent()) {

        log.info("{} => Entering {}{}", logPrefix, methodName, extra.isEmpty() ? "" : " (" + extra + ')');

            Object result = jp.proceed();

        log.info("{} => Exiting {}", logPrefix, methodName);
			span.setAttribute("success", true);
			return result;

    } catch(
    Throwable ex)

    {
        log.error("{} => FAILED {}: {}", logPrefix, methodName, ex.getMessage());
        span.recordException(ex).setAttribute("success", false);
        throw ex;

    } finally

    {
        span.end();
    }
}

@Override
protected void doFilter(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
        throws IOException, ServletException {

		String traceId = req.getHeader(HEADER_TRACE_ID);
		if (traceId == null || traceId.isBlank()) {
            TraceFlags traceFlags = TraceFlags.getDefault();
            String generatedTraceId = IdGenerator.random().generateTraceId();
            String generateSpanId = IdGenerator.random().generateSpanId();
            TraceState traceState = TraceState.getDefault();
            SpanContext spanContext = SpanContext.create(generatedTraceId, generateSpanId, traceFlags, traceState);
			traceId = spanContext.getTraceId();
																	// 32-char hex
			if (traceId.isBlank() || ZERO_TRACE.equals(traceId)) { // no span yet ↠ generate one
				traceId = IdGenerator.random().generateTraceId();
			}
		}
		MDC.put(TRACE_ID, traceId);

    // FIXME
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    String userId = (auth != null && auth.isAuthenticated()) ? auth.getName() : "ANONYMOUS";
    MDC.put(USER_ID, userId);

    try {
        chain.doFilter(req, res);
    } finally {
        MDC.clear();
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
                return interfaces[0].getSimpleName();
            }
        } else if (proxy.getClass().getName().contains("$$EnhancerBySpringCGLIB")) {
            return proxy.getClass().getSuperclass().getSimpleName();
        }
        return proxy.getClass().getSimpleName();
    } catch (Exception e) {
        log.warn("Failed to resolve target class: {}", e.getMessage());
        return proxy.getClass().getSimpleName();
    }
}
}