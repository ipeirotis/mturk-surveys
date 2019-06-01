package com.ipeirotis.endpoints;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiMethod.HttpMethod;
import com.google.api.server.spi.config.Nullable;
import com.google.api.server.spi.response.BadRequestException;
import com.google.api.server.spi.response.InternalServerErrorException;
import com.google.api.server.spi.response.NotFoundException;
import com.google.api.server.spi.response.UnauthorizedException;
import com.google.appengine.api.users.User;
import com.google.inject.Inject;
import com.ipeirotis.endpoints.response.StringResponse;
import com.ipeirotis.entity.Survey;
import com.ipeirotis.service.MturkService;
import com.ipeirotis.service.SurveyService;
import com.ipeirotis.util.Security;
import software.amazon.awssdk.services.mturk.model.Assignment;
import software.amazon.awssdk.services.mturk.model.HIT;

import javax.inject.Named;
import java.util.List;
import java.util.logging.Logger;

@Api(name = "mturk", description = "The API for mturk", version = "v1")
public class MturkEndpoint {

    private static final Logger logger = Logger.getLogger(MturkEndpoint.class.getName());

    private SurveyService surveyService;
    private MturkService mturkService;
    
    @Inject
    public MturkEndpoint(SurveyService surveyService, MturkService mturkService) {
        this.surveyService = surveyService;
        this.mturkService = mturkService;
    }

    @ApiMethod(name = "getHIT", path = "getHIT/{id}", httpMethod = HttpMethod.GET)
    public HIT getHIT(@Named("id") String id, @Nullable @Named("production") Boolean production,
                      User user) throws BadRequestException, UnauthorizedException {
        Security.verifyAuthenticatedUser(user);
        return mturkService.getHIT(production, id);
    }

    @ApiMethod(name = "listHITs", path = "listHITs", httpMethod = HttpMethod.GET)
    public List<HIT> listHITs(@Nullable @Named("production") Boolean production,
            User user) throws BadRequestException, UnauthorizedException {
        Security.verifyAuthenticatedUser(user);
        return mturkService.listHits(production);
    }

    @ApiMethod(name = "createHIT", path = "createHIT/{surveyId}", httpMethod = HttpMethod.POST)
    public StringResponse createHIT(@Named("surveyId") String surveyId,
            @Nullable @Named("production") Boolean production, User user)
            throws InternalServerErrorException, NotFoundException, UnauthorizedException {
        Security.verifyAuthenticatedUser(user);
        Survey survey = surveyService.get(surveyId);
        if(survey == null) {
            throw new NotFoundException(String.format("Survey %s doesn't exist", surveyId));
        }
        HIT hit = mturkService.createHIT(production, survey);
        return new StringResponse(String.format("created HIT with id: %s, groupId: ", hit.hitId(), hit.hitGroupId()));
    }

    @ApiMethod(name = "deleteHIT", path = "deleteHIT/{id}", httpMethod = HttpMethod.GET)
    public StringResponse deleteHIT(@Named("id") String id,
            @Nullable @Named("production") Boolean production, User user)
            throws BadRequestException, UnauthorizedException {
        Security.verifyAuthenticatedUser(user);
        mturkService.deleteHIT(production, id);
        return new StringResponse(String.format("HIT %s deleted successfully", id));
    }

    @ApiMethod(name = "getBalance", path = "getBalance", httpMethod = HttpMethod.GET)
    public StringResponse getBalance(@Nullable @Named("production") Boolean production,
            User user) throws BadRequestException, UnauthorizedException {
        Security.verifyAuthenticatedUser(user);
        return new StringResponse(String.format("Your balance: %.2f", mturkService.getAccountBalance(production)));
    }

    @ApiMethod(name = "getAssignmentsForHIT", path = "getAssignmentsForHIT/{hitId}", httpMethod = HttpMethod.GET)
    public List<Assignment> getAssignmentsForHIT(@Named("hitId") String hitId,
                                                 @Nullable @Named("production") Boolean production, User user)
            throws BadRequestException, UnauthorizedException {
        Security.verifyAuthenticatedUser(user);
        return mturkService.listAssignmentsForHit(production, hitId);
    }

}