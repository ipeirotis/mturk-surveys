package com.ipeirotis.service.mturk;

import java.util.Calendar;
import java.util.List;

import javax.xml.ws.Holder;

import com.ipeirotis.exception.MturkException;
import com.ipeirotis.mturk.requester.GetAccountBalanceRequest;
import com.ipeirotis.mturk.requester.GetAccountBalanceResult;
import com.ipeirotis.mturk.requester.OperationRequest;

public class GetAccountBalanceService extends BaseMturkService<GetAccountBalanceRequest, GetAccountBalanceResult>{

    @Override
    protected void run(String awsAccessKeyId, Calendar timestamp,
            String signature, String validate, String credential,
            List<GetAccountBalanceRequest> request,
            Holder<OperationRequest> operationRequest,
            Holder<List<GetAccountBalanceResult>> result) {

        getPort().getAccountBalance(awsAccessKeyId, timestamp, signature, validate, 
                credential, request, operationRequest, result);
    }

    public double getBalance() throws MturkException {
        Holder<List<GetAccountBalanceResult>> result = request("GetAccountBalance");
        if(result.value != null && result.value.size() != 0) {
            return result.value.get(0).getAvailableBalance().getAmount().doubleValue();
        } else {
            return 0d;
        }
    }
}
