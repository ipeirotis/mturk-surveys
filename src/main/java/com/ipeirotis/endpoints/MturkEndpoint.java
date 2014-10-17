package com.ipeirotis.endpoints;

import javax.inject.Named;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiMethod.HttpMethod;
import com.google.api.server.spi.response.InternalServerErrorException;
import com.google.inject.Inject;
import com.ipeirotis.endpoints.response.StringResponse;
import com.ipeirotis.mturk.requester.HIT;
import com.ipeirotis.service.mturk.GetAccountBalanceService;
import com.ipeirotis.service.mturk.GetHITService;

@Api(name = "mturk", description = "The API for mturk", version = "v1")
public class MturkEndpoint {

    private GetHITService getHITService;
    private GetAccountBalanceService getAccountBalanceService;
    
    @Inject
    public MturkEndpoint(GetHITService getHITService, GetAccountBalanceService getAccountBalanceService) {
        this.getHITService = getHITService;
        this.getAccountBalanceService = getAccountBalanceService;
    }
    
    @ApiMethod(name = "getHIT", path = "getHIT/{id}", httpMethod = HttpMethod.GET)
    public HIT getHIT(@Named("id") String id) throws InternalServerErrorException {
        try {
            return getHITService.getHIT(id);
        } catch (Exception e) {
            throw new InternalServerErrorException("Mturk error", e);
        }
    }

    @ApiMethod(name = "getBalance", path = "getBalance", httpMethod = HttpMethod.GET)
    public StringResponse getBalance() throws InternalServerErrorException {
        try {
            return new StringResponse(String.format("Your balance: %.2f", getAccountBalanceService.getBalance()));
        } catch (Exception e) {
            throw new InternalServerErrorException("Mturk error", e);
        }
    }
}