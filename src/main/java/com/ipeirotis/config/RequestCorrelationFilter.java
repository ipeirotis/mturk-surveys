package com.ipeirotis.config;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;

import java.io.IOException;
import java.util.UUID;

/**
 * Servlet filter that sets MDC context for structured logging.
 * <p>
 * Extracts the Cloud Trace context from App Engine's X-Cloud-Trace-Context header
 * (format: TRACE_ID/SPAN_ID;o=TRACE_TRUE) and generates a unique request ID.
 * Both values are available in log output via MDC keys "traceId" and "requestId".
 */
public class RequestCorrelationFilter implements Filter {

    private static final String TRACE_HEADER = "X-Cloud-Trace-Context";
    private static final String PARENT_REQUEST_HEADER = "X-Parent-Request-Id";
    private static final String MDC_REQUEST_ID = "requestId";
    private static final String MDC_PARENT_REQUEST_ID = "parentRequestId";
    private static final String MDC_TRACE_ID = "traceId";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        try {
            String requestId = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
            MDC.put(MDC_REQUEST_ID, requestId);

            if (request instanceof HttpServletRequest httpRequest) {
                String traceHeader = httpRequest.getHeader(TRACE_HEADER);
                if (traceHeader != null && !traceHeader.isBlank()) {
                    String traceId = traceHeader.contains("/")
                            ? traceHeader.substring(0, traceHeader.indexOf('/'))
                            : traceHeader;
                    MDC.put(MDC_TRACE_ID, traceId);
                }

                // Pick up parent request ID from Cloud Tasks for end-to-end tracing
                String parentRequestId = httpRequest.getHeader(PARENT_REQUEST_HEADER);
                if (parentRequestId != null && !parentRequestId.isBlank()) {
                    MDC.put(MDC_PARENT_REQUEST_ID, parentRequestId);
                }
            }

            chain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }
}
