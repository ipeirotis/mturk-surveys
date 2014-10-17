package com.ipeirotis.endpoints;

import javax.inject.Named;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiMethod.HttpMethod;
import com.google.api.server.spi.response.BadRequestException;
import com.google.api.server.spi.response.NotFoundException;
import com.google.inject.Inject;
import com.ipeirotis.entity.Survey;
import com.ipeirotis.service.SurveyService;

@Api(name = "survey", description = "The API for surveys", version = "v1")
public class SurveyEndpoint {

    private SurveyService surveyService;

    @Inject
    public SurveyEndpoint(SurveyService surveyService) {
        this.surveyService = surveyService;
    }

    @ApiMethod(name = "create", path = "survey", httpMethod = HttpMethod.POST)
    public void create(Survey survey) throws BadRequestException {
        surveyService.create(survey);
    }

    @ApiMethod(name = "get", path = "survey/{id}", httpMethod = HttpMethod.GET)
    public Survey get(@Named("id") String id) throws NotFoundException {
        Survey survey = surveyService.get(id);
        if(survey == null) {
            throw new NotFoundException(String.format("Survey with id=%s doesn't exist", id));
        } else {
            return surveyService.get(id);
        }
    }

    @ApiMethod(name = "delete", path = "survey/{id}", httpMethod = HttpMethod.DELETE)
    public void delete(@Named("id") String id) throws NotFoundException {
        surveyService.delete(id);
    }

}