package com.ipeirotis.endpoints;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Named;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiMethod.HttpMethod;
import com.google.api.server.spi.response.BadRequestException;
import com.google.api.server.spi.response.InternalServerErrorException;
import com.google.api.server.spi.response.NotFoundException;
import com.google.inject.Inject;
import com.ipeirotis.endpoints.response.StringResponse;
import com.ipeirotis.entity.Survey;
import com.ipeirotis.exception.MturkException;
import com.ipeirotis.mturk.requester.HIT;
import com.ipeirotis.service.SurveyService;
import com.ipeirotis.service.mturk.CreateHITService;
import com.ipeirotis.service.mturk.DisableHITService;
import com.ipeirotis.service.mturk.DisposeHITService;
import com.ipeirotis.service.mturk.GetAccountBalanceService;
import com.ipeirotis.service.mturk.GetHITService;
import com.ipeirotis.service.mturk.SearchHITsService;

@Api(name = "mturk", description = "The API for mturk", version = "v1")
public class MturkEndpoint {

    private static final Logger logger = Logger.getLogger(MturkEndpoint.class.getName());

    private GetHITService getHITService;
    private SearchHITsService searchHITsService;
    private CreateHITService createHITService;
    private DisableHITService disableHITService;
    private DisposeHITService disposeHITService;
    private GetAccountBalanceService getAccountBalanceService;
    private SurveyService surveyService;
    
    @Inject
    public MturkEndpoint(GetHITService getHITService, SearchHITsService searchHITsService,
            CreateHITService createHITService, DisableHITService disableHITService,
            DisposeHITService disposeHITService, GetAccountBalanceService getAccountBalanceService,
            SurveyService surveyService) {
        this.getHITService = getHITService;
        this.searchHITsService = searchHITsService;
        this.createHITService = createHITService;
        this.disableHITService = disableHITService;
        this.disposeHITService = disposeHITService;
        this.getAccountBalanceService = getAccountBalanceService;
        this.surveyService = surveyService;
    }
    
    @ApiMethod(name = "getHIT", path = "getHIT/{id}", httpMethod = HttpMethod.GET)
    public HIT getHIT(@Named("id") String id) throws BadRequestException {
        try {
            return getHITService.getHIT(id);
        } catch (MturkException e) {
            throw new BadRequestException(e.getMessage(), e);
        }
    }

    @ApiMethod(name = "searhHITs", path = "searhHITs", httpMethod = HttpMethod.GET)
    public List<HIT> searhHITs() throws BadRequestException {
        try {
            return searchHITsService.searchHITs();
        } catch (MturkException e) {
            throw new BadRequestException(e.getMessage(), e);
        }
    }

    @ApiMethod(name = "createHIT", path = "createHIT/{surveyId}", httpMethod = HttpMethod.POST)
    public StringResponse createHIT(@Named("surveyId") String surveyId)
            throws InternalServerErrorException, NotFoundException {
        try {
            Survey survey = surveyService.get(surveyId);
            if(survey == null) {
                throw new NotFoundException(String.format("Survey %s doesn't exist", surveyId));
            }
            HIT hit = createHITService.createHIT(survey);
            return new StringResponse(String.format("created HIT with id: %s", hit.getHITId()));
        } catch (MturkException e) {
            logger.log(Level.SEVERE, "Error creating HIT", e);
            throw new InternalServerErrorException(e.getMessage(), e);
        }
    }

    @ApiMethod(name = "disableHIT", path = "disableHIT/{id}", httpMethod = HttpMethod.GET)
    public StringResponse disableHIT(@Named("id") String id) throws BadRequestException {
        try {
            disableHITService.disableHIT(id);
            return new StringResponse(String.format("HIT %s disabled successfully", id));
        } catch (MturkException e) {
            throw new BadRequestException(e.getMessage(), e);
        }
    }

    @ApiMethod(name = "disposeHIT", path = "disposeHIT/{id}", httpMethod = HttpMethod.GET)
    public StringResponse disposeHIT(@Named("id") String id) throws BadRequestException {
        try {
            disposeHITService.disposeHIT(id);
            return new StringResponse(String.format("HIT %s disposed successfully", id));
        } catch (MturkException e) {
            throw new BadRequestException(e.getMessage(), e);
        }
    }

    @ApiMethod(name = "getBalance", path = "getBalance", httpMethod = HttpMethod.GET)
    public StringResponse getBalance() throws BadRequestException {
        try {
            return new StringResponse(String.format("Your balance: %.2f", getAccountBalanceService.getBalance()));
        } catch (MturkException e) {
            throw new BadRequestException(e.getMessage(), e);
        }
    }
}