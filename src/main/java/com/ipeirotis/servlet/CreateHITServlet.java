package com.ipeirotis.servlet;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.ipeirotis.entity.Survey;
import com.ipeirotis.exception.MturkException;
import com.ipeirotis.mturk.requester.HIT;
import com.ipeirotis.service.SurveyService;
import com.ipeirotis.service.mturk.CreateHITService;
import com.ipeirotis.service.mturk.GetAccountBalanceService;
import com.ipeirotis.util.MailUtil;

@Singleton
public class CreateHITServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger(CreateHITServlet.class.getName());

    private CreateHITService createHITService;
    private GetAccountBalanceService getAccountBalanceService;
    private SurveyService surveyService;

    @Inject
    public CreateHITServlet(CreateHITService createHITService, GetAccountBalanceService getAccountBalanceService,
            SurveyService surveyService){
        this.createHITService = createHITService;
        this.getAccountBalanceService = getAccountBalanceService;
        this.surveyService = surveyService;
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String surveyId = request.getParameter("surveyId");
        boolean production = "true".equals(request.getParameter("production")) ? true : false;

        try {
            Survey survey = surveyService.get(surveyId);
            if(survey == null) {
                String error = String.format("Error creating HIT: survey %s doesn't exist", surveyId);
                logger.log(Level.SEVERE, error);
                response.sendError(404, error);
            } else {
                double balance = getAccountBalanceService.getBalance(production);
                if(balance < 10.0) {
                    MailUtil.send(String.format("Your balance is too low (%.2f)", balance),
                            "mturk-surveys", "ipeirotis@gmail.com",
                            "mturk-surveys", "ipeirotis@gmail.com");
                    logger.warning(String.format("Balance is too low (%.2f)", balance));
                } else {
                    HIT hit = createHITService.createHIT(production, survey);
                    response.setContentType("text/plain");
                    String responseText = "created HIT with id: " + hit.getHITId();
                    logger.info(responseText);
                    response.getWriter().println(responseText);
                }
            }
        } catch (MturkException e) {
            logger.log(Level.SEVERE, "Error creating HIT", e);
            response.sendError(500, e.getMessage());
        }
    }

    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {

    }

}

