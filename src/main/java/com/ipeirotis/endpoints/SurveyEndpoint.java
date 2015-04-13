package com.ipeirotis.endpoints;

import static com.google.api.server.spi.Constant.API_EXPLORER_CLIENT_ID;

import java.text.ParseException;
import java.util.logging.Logger;

import javax.inject.Named;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiMethod.HttpMethod;
import com.google.api.server.spi.config.Nullable;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.api.server.spi.response.NotFoundException;
import com.google.appengine.api.users.User;
import com.google.inject.Inject;
import com.ipeirotis.dto.DemographicsSurveyAnswersByPeriod;
import com.ipeirotis.endpoints.response.StringResponse;
import com.ipeirotis.entity.Survey;
import com.ipeirotis.entity.UserAnswer;
import com.ipeirotis.service.SurveyService;
import com.ipeirotis.service.UserAnswerService;
import com.ipeirotis.util.Security;

@Api(name = "survey", description = "The API for surveys", version = "v1",
    clientIds = {API_EXPLORER_CLIENT_ID},
    scopes = {"https://www.googleapis.com/auth/userinfo.email"})
public class SurveyEndpoint {

    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(SurveyEndpoint.class.getName());

    private SurveyService surveyService;
    private UserAnswerService userAnswerService;

    @Inject
    public SurveyEndpoint(SurveyService surveyService, UserAnswerService userAnswerService) {
        this.surveyService = surveyService;
        this.userAnswerService = userAnswerService;
    }

    @ApiMethod(name = "create", path = "survey", httpMethod = HttpMethod.POST)
    public Survey create(Survey survey, @Nullable @Named("production") Boolean production, User user)
            throws Exception {
        Security.verifyAuthenticatedUser(user);
        return surveyService.create(survey, production);
    }

    @ApiMethod(name = "get", path = "survey/{id}", httpMethod = HttpMethod.GET)
    public Survey get(@Named("id") String id, User user) throws Exception {
        Security.verifyAuthenticatedUser(user);
        Survey survey = surveyService.get(id);
        if(survey == null) {
            throw new NotFoundException(String.format("Survey with id=%s doesn't exist", id));
        } else {
            return surveyService.get(id);
        }
    }

    @ApiMethod(name = "delete", path = "survey/{id}", httpMethod = HttpMethod.DELETE)
    public StringResponse delete(@Named("id") String id, User user) throws Exception {
        Security.verifyAuthenticatedUser(user);
        surveyService.delete(id);
        return new StringResponse(String.format("Survey %s deleted successfully", id));
    }

    @ApiMethod(name = "getAggregatedDemographicsAnswers", path = "survey/demographics/aggregatedAnswers", httpMethod = HttpMethod.GET)
    public DemographicsSurveyAnswersByPeriod getDemographicsAnswers(
            @Named("from") String from, @Named("to") String to) throws ParseException {
        return surveyService.getDemographicsAnswers(from, to);
    }

    @ApiMethod(name = "listAnswers", path = "survey/demographics/answers", httpMethod = HttpMethod.GET)
    public CollectionResponse<UserAnswer> getSurveyAnswers(
            @Nullable @Named("cursor") String cursorString, @Named("limit") Integer limit)
            throws Exception {
        return userAnswerService.list(cursorString, limit);
    }
}