package com.ipeirotis.service.mturk;

import java.util.Calendar;
import java.util.List;

import javax.xml.ws.Holder;

import com.ipeirotis.exception.MturkException;
import com.ipeirotis.mturk.requester.GetHITRequest;
import com.ipeirotis.mturk.requester.HIT;
import com.ipeirotis.mturk.requester.OperationRequest;

public class GetHITService extends BaseMturkService<GetHITRequest, HIT>{

    @Override
    protected void run(String awsAccessKeyId, Calendar timestamp,
            String signature, String validate, String credential,
            List<GetHITRequest> request,
            Holder<OperationRequest> operationRequest,
            Holder<List<HIT>> result) {

        getPort().getHIT(awsAccessKeyId, timestamp, signature, validate, 
                credential, request, operationRequest, result);
    }

    public HIT getHIT(Boolean production, String hitId) throws MturkException {
        GetHITRequest getHITRequest = new GetHITRequest();
        getHITRequest.setHITId(hitId);

        Holder<List<HIT>> result = request(production, "GetHIT", getHITRequest);
        handleErrors(result.value.get(0).getRequest());

        return result.value.get(0);
    }
}
