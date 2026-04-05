package com.ipeirotis.config;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Security filter that restricts access to /tasks/ endpoints.
 *
 * Only allows requests from:
 * - App Engine Cron (X-Appengine-Cron: true)
 * - Google Cloud Tasks (X-CloudTasks-TaskName header present)
 * - Local development (GAE_APPLICATION env var not set)
 *
 * On App Engine, these headers are stripped from external requests by the
 * infrastructure, so they can only be present on genuine internal requests.
 */
public class TaskAuthFilter implements Filter {

    private static final Logger logger = Logger.getLogger(TaskAuthFilter.class.getName());

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

        // Allow Cloud Tasks requests
        if (httpRequest.getHeader("X-CloudTasks-TaskName") != null) {
            chain.doFilter(request, response);
            return;
        }

        // Reject all other requests
        logger.warning("Rejected unauthorized request to " + httpRequest.getRequestURI()
                + " from " + httpRequest.getRemoteAddr());
        httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
        httpResponse.setContentType("application/json");
        httpResponse.getWriter().write("{\"error\":\"Forbidden: task endpoints require App Engine Cron or Cloud Tasks headers\"}");
    }
}
