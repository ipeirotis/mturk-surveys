package com.ipeirotis.service.mturk;

import java.util.Calendar;
import java.util.List;

import javax.xml.ws.Holder;

import com.ipeirotis.exception.MturkException;
import com.ipeirotis.mturk.requester.DisposeHITRequest;
import com.ipeirotis.mturk.requester.DisposeHITResult;
import com.ipeirotis.mturk.requester.OperationRequest;

public class DisposeHITService extends BaseMturkService<DisposeHITRequest, DisposeHITResult>{

    @Override
    protected void run(String awsAccessKeyId, Calendar timestamp,
            String signature, String validate, String credential,
            List<DisposeHITRequest> request,
            Holder<OperationRequest> operationRequest,
            Holder<List<DisposeHITResult>> result) {

        getPort().disposeHIT(awsAccessKeyId, timestamp, signature, validate, 
                credential, request, operationRequest, result);
    }

    public void disposeHIT(String hitId) throws MturkException {
        DisposeHITRequest disposeHITRequest = new DisposeHITRequest();
        disposeHITRequest.setHITId(hitId);

        Holder<List<DisposeHITResult>> result = request("DisposeHIT", disposeHITRequest);
        handleErrors(result.value.get(0).getRequest());
    }
}
