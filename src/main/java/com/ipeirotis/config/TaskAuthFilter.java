package com.ipeirotis.config;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Security filter that restricts access to /tasks/ endpoints.
 *
 * Only allows requests from:
 * - App Engine Cron (X-Appengine-Cron: true)
 * - Google Cloud Tasks (X-AppEngine-TaskName header)
 * - Admin API key (X-Task-Admin-Key header matching task-admin-key secret)
 * - Local development (GAE_APPLICATION env var not set)
 *
 * On App Engine, cron and task headers are stripped from external requests by
 * the infrastructure, so they can only be present on genuine internal requests.
 */
public class TaskAuthFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(TaskAuthFilter.class);

    private final String adminKey;

    public TaskAuthFilter(String adminKey) {
        this.adminKey = adminKey;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Allow all requests in local development (not running on App Engine)
        if (System.getenv("GAE_APPLICATION") == null) {
            chain.doFilter(request, response);
            return;
        }

        // Allow App Engine Cron requests
        if ("true".equals(httpRequest.getHeader("X-Appengine-Cron"))) {
            chain.doFilter(request, response);
            return;
        }

        // Allow Cloud Tasks requests via App Engine task header.
        // We use AppEngineHttpRequest targets exclusively, so only check
        // X-AppEngine-TaskName (stripped from external requests by GAE infra).
        if (httpRequest.getHeader("X-AppEngine-TaskName") != null) {
            chain.doFilter(request, response);
            return;
        }

        // Allow requests with a valid admin API key
        if (adminKey != null && !adminKey.isBlank()) {
            String providedKey = httpRequest.getHeader("X-Task-Admin-Key");
            if (adminKey.equals(providedKey)) {
                chain.doFilter(request, response);
                return;
            }
        }

        // Reject all other requests
        logger.warn("Rejected unauthorized request to " + httpRequest.getRequestURI()
                + " from " + httpRequest.getRemoteAddr());
        httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
        httpResponse.setContentType("application/json");
        httpResponse.getWriter().write("{\"error\":\"Forbidden: task endpoints require App Engine Cron, Cloud Tasks, or admin key\"}");
    }
}
