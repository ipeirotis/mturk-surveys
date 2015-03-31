package com.ipeirotis.service.mturk;

import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import javax.xml.ws.Holder;

import com.ipeirotis.exception.MturkException;
import com.ipeirotis.mturk.requester.Assignment;
import com.ipeirotis.mturk.requester.GetAssignmentsForHITRequest;
import com.ipeirotis.mturk.requester.GetAssignmentsForHITResult;
import com.ipeirotis.mturk.requester.OperationRequest;

public class GetAssignmentsForHITService extends BaseMturkService<GetAssignmentsForHITRequest, GetAssignmentsForHITResult>{

    @Override
    protected void run(String awsAccessKeyId, Calendar timestamp,
            String signature, String validate, String credential,
            List<GetAssignmentsForHITRequest> request,
            Holder<OperationRequest> operationRequest,
            Holder<List<GetAssignmentsForHITResult>> result) {

        getPort().getAssignmentsForHIT(awsAccessKeyId, timestamp, signature, validate, 
                credential, request, operationRequest, result);
    }

    public List<Assignment> getAssignments(Boolean production, String hitId) throws MturkException {
        GetAssignmentsForHITRequest getAssignmentsForHITRequest = new GetAssignmentsForHITRequest();
        getAssignmentsForHITRequest.setHITId(hitId);
        Holder<List<GetAssignmentsForHITResult>> result = request(production, "GetAssignmentsForHIT",
                getAssignmentsForHITRequest);
        handleErrors(result.value.get(0).getRequest());

        if(result.value != null && result.value.size() != 0) {
            return result.value.get(0).getAssignment();
        } else {
            return Collections.emptyList();
        }
    }
}
