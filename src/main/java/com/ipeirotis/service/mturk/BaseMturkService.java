package com.ipeirotis.service.mturk;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Holder;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;

import com.ipeirotis.exception.MturkException;
import com.ipeirotis.mturk.requester.AWSMechanicalTurkRequester;
import com.ipeirotis.mturk.requester.AWSMechanicalTurkRequesterPortType;
import com.ipeirotis.mturk.requester.OperationRequest;

public abstract class BaseMturkService <REQUEST, RESULT> {

    private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";
    private static final String SERVICE = "AWSMechanicalTurkRequester";
    private static final String ENDPOINT_SANDBOX = "https://mechanicalturk.sandbox.amazonaws.com";
    private static final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

    protected abstract void run(String awsAccessKeyId, Calendar timestamp, String signature, String validate, 
            String credential, List<REQUEST> request, Holder<OperationRequest> operationRequest, Holder<List<RESULT>> result);

    public Holder<List<RESULT>> request(String operation) throws Exception {
        return this.request(operation, null);
    }

    public Holder<List<RESULT>> request(String operation, REQUEST request) throws Exception {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());

        List<REQUEST> requests = new ArrayList<REQUEST>();
        if(request != null) {
            requests.add(request);
        }
        Holder<OperationRequest> operationRequest = new Holder<OperationRequest>();
        Holder<List<RESULT>> result = new Holder<List<RESULT>>();
        String signature = getSignature(SERVICE, operation, format.format(calendar.getTime()), System.getProperty("AWS_SECRET_KEY"));

        run(System.getProperty("AWS_ACCESS_KEY_ID"), calendar, signature, 
                null, null, requests, operationRequest, result);
        handleErrors(operationRequest);
        
        return result;
    }

    protected AWSMechanicalTurkRequesterPortType getPort() {
        AWSMechanicalTurkRequesterPortType port = 
                new AWSMechanicalTurkRequester().getAWSMechanicalTurkRequesterPort();
        BindingProvider bp = (BindingProvider)port;
        bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, ENDPOINT_SANDBOX);
        return port;
    }

    protected String getSignature(String service, String operation,
            String timestamp, String secretKey) throws Exception {
        Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
        mac.init(new SecretKeySpec(secretKey.getBytes(), HMAC_SHA1_ALGORITHM));
        byte[] byteArray = Base64.encodeBase64(mac.doFinal((service + operation + timestamp)
                .getBytes()));
        return new String(byteArray);
    }

    protected void handleErrors(Holder<OperationRequest> operationRequest) throws MturkException{
        if(operationRequest.value != null && operationRequest.value.getErrors() != null) {
            List<String> errors = new ArrayList<String>();
            for(com.ipeirotis.mturk.requester.Error error : operationRequest.value.getErrors().getError()) {
                errors.add(error.getMessage());
            }
            if (!errors.isEmpty()) {
                throw new MturkException(String.format("Error: %s", StringUtils.join(errors, ", ")));
            }
        }
    }
}
