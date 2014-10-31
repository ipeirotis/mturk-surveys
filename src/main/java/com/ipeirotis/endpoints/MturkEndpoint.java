package com.ipeirotis.endpoints;

import javax.inject.Named;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiMethod.HttpMethod;
import com.google.api.server.spi.response.BadRequestException;
import com.google.inject.Inject;
import com.ipeirotis.endpoints.response.StringResponse;
import com.ipeirotis.exception.MturkException;
import com.ipeirotis.mturk.requester.HIT;
import com.ipeirotis.service.mturk.DisableHITService;
import com.ipeirotis.service.mturk.GetAccountBalanceService;
import com.ipeirotis.service.mturk.GetHITService;

@Api(name = "mturk", description = "The API for mturk", version = "v1")
public class MturkEndpoint {

    private GetHITService getHITService;
    private DisableHITService disableHITService;
    private GetAccountBalanceService getAccountBalanceService;
    
    @Inject
    public MturkEndpoint(GetHITService getHITService, DisableHITService disableHITService,
            GetAccountBalanceService getAccountBalanceService) {
        this.getHITService = getHITService;
        this.disableHITService = disableHITService;
        this.getAccountBalanceService = getAccountBalanceService;
    }
    
    @ApiMethod(name = "getHIT", path = "getHIT/{id}", httpMethod = HttpMethod.GET)
    public HIT getHIT(@Named("id") String id) throws BadRequestException {
        try {
            return getHITService.getHIT(id);
        } catch (MturkException e) {
            throw new BadRequestException(e.getMessage(), e);
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

    @ApiMethod(name = "getBalance", path = "getBalance", httpMethod = HttpMethod.GET)
    public StringResponse getBalance() throws BadRequestException {
        try {
            return new StringResponse(String.format("Your balance: %.2f", getAccountBalanceService.getBalance()));
        } catch (MturkException e) {
            throw new BadRequestException(e.getMessage(), e);
        }
    }
}