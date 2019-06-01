package com.ipeirotis.servlet;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.ipeirotis.service.MturkService;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Logger;

@Singleton
public class GetAccountBalanceServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(GetAccountBalanceServlet.class.getName());

    private MturkService mturkService;

    @Inject
    public GetAccountBalanceServlet(MturkService mturkService) {
        this.mturkService = mturkService;
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.getWriter().print("=" + mturkService.getAccountBalance(true));
    }
}