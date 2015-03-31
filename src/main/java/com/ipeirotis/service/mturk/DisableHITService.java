package com.ipeirotis.service.mturk;

import java.util.Calendar;
import java.util.List;

import javax.xml.ws.Holder;

import com.ipeirotis.exception.MturkException;
import com.ipeirotis.mturk.requester.DisableHITRequest;
import com.ipeirotis.mturk.requester.DisableHITResult;
import com.ipeirotis.mturk.requester.OperationRequest;

public class DisableHITService extends BaseMturkService<DisableHITRequest, DisableHITResult>{

    @Override
    protected void run(String awsAccessKeyId, Calendar timestamp,
            String signature, String validate, String credential,
            List<DisableHITRequest> request,
            Holder<OperationRequest> operationRequest,
            Holder<List<DisableHITResult>> result) {

        getPort().disableHIT(awsAccessKeyId, timestamp, signature, validate, 
                credential, request, operationRequest, result);
    }

    public void disableHIT(Boolean production, String hitId) throws MturkException {
        DisableHITRequest disableHITRequest = new DisableHITRequest();
        disableHITRequest.setHITId(hitId);

        Holder<List<DisableHITResult>> result = request(production, "DisableHIT", disableHITRequest);
        handleErrors(result.value.get(0).getRequest());
    }
}
