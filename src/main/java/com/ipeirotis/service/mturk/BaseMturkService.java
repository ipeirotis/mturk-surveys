package com.ipeirotis.service.mturk;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
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
import com.ipeirotis.mturk.requester.HIT;
import com.ipeirotis.mturk.requester.OperationRequest;
import com.ipeirotis.mturk.requester.Request;

public abstract class BaseMturkService <REQUEST, RESULT> {

    public static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";
    public static final String SERVICE = "AWSMechanicalTurkRequester";
    public static final String ENDPOINT_SANDBOX = "https://mechanicalturk.sandbox.amazonaws.com";
    public static final String ENDPOINT_PRODUCTION =
            "http://mechanicalturk.amazonaws.com/?Service=AWSMechanicalTurkRequester";
    public static final String PROD_WORKER_WEBSITE_URL = "http://www.mturk.com";
    public static final String SANDBOX_WORKER_WEBSITE_URL = "http://workersandbox.mturk.com";
    public static final String PROD_REQUESTER_WEBSITE_URL = "http://requester.mturk.com";
    public static final String SANDBOX_REQUESTER_WEBSITE_URL = "http://requestersandbox.mturk.com";
    private static final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

    protected abstract void run(String awsAccessKeyId, Calendar timestamp, String signature, String validate, 
            String credential, List<REQUEST> request, Holder<OperationRequest> operationRequest, Holder<List<RESULT>> result);

    public Holder<List<RESULT>> request(String operation) throws MturkException {
        return this.request(operation, null);
    }

    public Holder<List<RESULT>> request(String operation, REQUEST request) throws MturkException {
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
        handleErrors(operationRequest, result);
        
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
            String timestamp, String secretKey) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
            mac.init(new SecretKeySpec(secretKey.getBytes(), HMAC_SHA1_ALGORITHM));
            byte[] byteArray = Base64.encodeBase64(mac.doFinal((service + operation + timestamp)
                    .getBytes()));
            return new String(byteArray);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    protected void handleErrors(Holder<OperationRequest> operationRequest, Holder<List<RESULT>> result) throws MturkException{
        List<String> errors = new ArrayList<String>();
        RESULT res = result.value == null ? null : result.value.get(0);
        if (res != null && res instanceof HIT) {
            HIT hit = (HIT)res;
            if(hit.getRequest().getErrors() != null) {
                for(com.ipeirotis.mturk.requester.Error error : hit.getRequest().getErrors().getError()) {
                    errors.add(error.getMessage());
                }
            }
        } else if(operationRequest.value != null && operationRequest.value.getErrors() != null) {
            for(com.ipeirotis.mturk.requester.Error error : operationRequest.value.getErrors().getError()) {
                errors.add(error.getMessage());
            }
        }

        if (!errors.isEmpty()) {
            throw new MturkException("Error: %s", StringUtils.join(errors, ", "));
        }
    }

    protected void handleErrors(Request request) throws MturkException{
        List<String> errors = new ArrayList<String>();
        if(request.getErrors() != null) {
            for(com.ipeirotis.mturk.requester.Error error : request.getErrors().getError()) {
                errors.add(error.getMessage());
            }
        }

        if (!errors.isEmpty()) {
            throw new MturkException("Error: %s", StringUtils.join(errors, ", "));
        }
    }
}
