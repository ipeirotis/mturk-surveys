package com.ipeirotis.service.mturk;

import java.util.Calendar;
import java.util.List;

import javax.xml.ws.Holder;

import com.ipeirotis.exception.MturkException;
import com.ipeirotis.mturk.requester.ApproveAssignmentRequest;
import com.ipeirotis.mturk.requester.ApproveAssignmentResult;
import com.ipeirotis.mturk.requester.OperationRequest;

public class ApproveAssignmentService extends BaseMturkService<ApproveAssignmentRequest, ApproveAssignmentResult>{

    @Override
    protected void run(String awsAccessKeyId, Calendar timestamp,
            String signature, String validate, String credential,
            List<ApproveAssignmentRequest> request,
            Holder<OperationRequest> operationRequest,
            Holder<List<ApproveAssignmentResult>> result) {

        getPort().approveAssignment(awsAccessKeyId, timestamp, signature, validate, 
                credential, request, operationRequest, result);
    }

    public void approveAssignment(Boolean production, String assignmentId) throws MturkException {
        ApproveAssignmentRequest approveAssignmentRequest = new ApproveAssignmentRequest();
        approveAssignmentRequest.setAssignmentId(assignmentId);

        Holder<List<ApproveAssignmentResult>> result = request(production, "ApproveAssignment", approveAssignmentRequest);
        handleErrors(result.value.get(0).getRequest());
    }
}
