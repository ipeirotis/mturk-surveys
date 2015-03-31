package com.ipeirotis.endpoints;

import java.text.ParseException;
import java.util.List;
import java.util.logging.Logger;

import javax.inject.Named;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiMethod.HttpMethod;
import com.google.api.server.spi.config.Nullable;
import com.google.api.server.spi.response.BadRequestException;
import com.google.api.server.spi.response.NotFoundException;
import com.google.inject.Inject;
import com.ipeirotis.dto.BirthSurveyAnswers;
import com.ipeirotis.dto.ByCountryAnswers;
import com.ipeirotis.dto.DemographicsSurveyAnswers;
import com.ipeirotis.dto.GenderSurveyAnswers;
import com.ipeirotis.endpoints.response.StringResponse;
import com.ipeirotis.entity.Survey;
import com.ipeirotis.service.SurveyService;
import com.ipeirotis.service.UserAnswerService;

@Api(name = "survey", description = "The API for surveys", version = "v1")
public class SurveyEndpoint {

    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(SurveyEndpoint.class.getName());

    private SurveyService surveyService;

    @Inject
    public SurveyEndpoint(SurveyService surveyService, UserAnswerService userAnswerService) {
        this.surveyService = surveyService;
    }

    @ApiMethod(name = "create", path = "survey", httpMethod = HttpMethod.POST)
    public Survey create(Survey survey, @Nullable @Named("production") Boolean production)
            throws BadRequestException {
        return surveyService.create(survey, production);
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
    public StringResponse delete(@Named("id") String id) throws NotFoundException {
        surveyService.delete(id);
        return new StringResponse(String.format("Survey %s deleted successfully", id));
    }

    @ApiMethod(name = "getDemographicsAnswers", path = "survey/demographics/answers", httpMethod = HttpMethod.GET)
    public DemographicsSurveyAnswers getDemographicsAnswers(
            @Named("from") String from, @Named("to") String to) throws ParseException {
        return surveyService.getDemographicsAnswers(from, to);
    }

    @ApiMethod(name = "getGenderAnswers", path = "survey/gender/answers", httpMethod = HttpMethod.GET)
    public List<GenderSurveyAnswers> getGenderAnswers(
            @Named("from") String from, @Named("to") String to) throws ParseException {
        return surveyService.getGenderAnswers(from, to);
    }

    @ApiMethod(name = "getBirthAnswers", path = "survey/birth/answers", httpMethod = HttpMethod.GET)
    public BirthSurveyAnswers getBirthAnswers(
            @Named("from") String from, @Named("to") String to) throws ParseException {
        return surveyService.getBirthAnswers(from, to);
    }

    @ApiMethod(name = "getByCountryAnswers", path = "survey/byCountry", httpMethod = HttpMethod.GET)
    public ByCountryAnswers getByCountryAnswers(
            @Named("from") String from, @Named("to") String to) throws ParseException {
        return surveyService.getByCountryAnswers(from, to);
    }
}