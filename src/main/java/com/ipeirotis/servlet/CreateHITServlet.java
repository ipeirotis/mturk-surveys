package com.ipeirotis.servlet;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.ipeirotis.exception.MturkException;
import com.ipeirotis.mturk.requester.HIT;
import com.ipeirotis.service.mturk.CreateHITService;

@Singleton
public class CreateHITServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger(CreateHITServlet.class.getName());

    private CreateHITService createHITService;

    @Inject
    public CreateHITServlet(CreateHITService createHITService){
        this.createHITService = createHITService;
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String surveyId = request.getParameter("id");

        try {
            HIT hit = createHITService.createHIT(surveyId);
            response.setContentType("text/plain");
            response.getWriter().println("created HIT with id: " + hit.getHITId());
        } catch (MturkException e) {
            logger.log(Level.SEVERE, "Error creating HIT", e);
            response.sendError(500, e.getMessage());
        }
    }

    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {

    }
}

