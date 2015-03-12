package com.ipeirotis.service.mturk;

import java.util.Calendar;
import java.util.List;

import javax.xml.ws.Holder;

import com.ipeirotis.exception.MturkException;
import com.ipeirotis.mturk.requester.HIT;
import com.ipeirotis.mturk.requester.OperationRequest;
import com.ipeirotis.mturk.requester.SearchHITsRequest;
import com.ipeirotis.mturk.requester.SearchHITsResult;

public class SearchHITsService extends BaseMturkService<SearchHITsRequest, SearchHITsResult>{

    @Override
    protected void run(String awsAccessKeyId, Calendar timestamp,
            String signature, String validate, String credential,
            List<SearchHITsRequest> request,
            Holder<OperationRequest> operationRequest,
            Holder<List<SearchHITsResult>> result) {

        getPort().searchHITs(awsAccessKeyId, timestamp, signature, validate, credential, 
                request, operationRequest, result);
    }

    public List<HIT> searchHITs() throws MturkException {
        SearchHITsRequest searchHITsRequest = new SearchHITsRequest();
        Holder<List<SearchHITsResult>> result = request("SearchHITs", searchHITsRequest);
        handleErrors(result.value.get(0).getRequest());
        
        return result.value.get(0).getHIT();
    }
}
