
package com.ipeirotis.mturk.requester;

import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceFeature;


/**
 * This class was generated by the JAX-WS RI.
 * JAX-WS RI 2.2.4-b01
 * Generated source version: 2.2
 * 
 */
@WebServiceClient(name = "AWSMechanicalTurkRequester", targetNamespace = "http://requester.mturk.amazonaws.com/doc/2013-11-15", wsdlLocation = "file:/D:/1/AWSMechanicalTurkRequester.wsdl")
public class AWSMechanicalTurkRequester
    extends Service
{

    private final static URL AWSMECHANICALTURKREQUESTER_WSDL_LOCATION;
    private final static WebServiceException AWSMECHANICALTURKREQUESTER_EXCEPTION;
    private final static QName AWSMECHANICALTURKREQUESTER_QNAME = new QName("http://requester.mturk.amazonaws.com/doc/2013-11-15", "AWSMechanicalTurkRequester");

    static {
        URL url = null;
        WebServiceException e = null;
        try {
            url = new URL("http://mechanicalturk.amazonaws.com/AWSMechanicalTurk/2013-11-15/AWSMechanicalTurkRequester.wsdl");
        } catch (MalformedURLException ex) {
            e = new WebServiceException(ex);
        }
        AWSMECHANICALTURKREQUESTER_WSDL_LOCATION = url;
        AWSMECHANICALTURKREQUESTER_EXCEPTION = e;
    }

    public AWSMechanicalTurkRequester() {
        super(__getWsdlLocation(), AWSMECHANICALTURKREQUESTER_QNAME);
    }

    public AWSMechanicalTurkRequester(WebServiceFeature... features) {
        super(__getWsdlLocation(), AWSMECHANICALTURKREQUESTER_QNAME, features);
    }

    public AWSMechanicalTurkRequester(URL wsdlLocation) {
        super(wsdlLocation, AWSMECHANICALTURKREQUESTER_QNAME);
    }

    public AWSMechanicalTurkRequester(URL wsdlLocation, WebServiceFeature... features) {
        super(wsdlLocation, AWSMECHANICALTURKREQUESTER_QNAME, features);
    }

    public AWSMechanicalTurkRequester(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public AWSMechanicalTurkRequester(URL wsdlLocation, QName serviceName, WebServiceFeature... features) {
        super(wsdlLocation, serviceName, features);
    }

    /**
     * 
     * @return
     *     returns AWSMechanicalTurkRequesterPortType
     */
    @WebEndpoint(name = "AWSMechanicalTurkRequesterPort")
    public AWSMechanicalTurkRequesterPortType getAWSMechanicalTurkRequesterPort() {
        return super.getPort(new QName("http://requester.mturk.amazonaws.com/doc/2013-11-15", "AWSMechanicalTurkRequesterPort"), AWSMechanicalTurkRequesterPortType.class);
    }

    /**
     * 
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns AWSMechanicalTurkRequesterPortType
     */
    @WebEndpoint(name = "AWSMechanicalTurkRequesterPort")
    public AWSMechanicalTurkRequesterPortType getAWSMechanicalTurkRequesterPort(WebServiceFeature... features) {
        return super.getPort(new QName("http://requester.mturk.amazonaws.com/doc/2013-11-15", "AWSMechanicalTurkRequesterPort"), AWSMechanicalTurkRequesterPortType.class, features);
    }

    private static URL __getWsdlLocation() {
        if (AWSMECHANICALTURKREQUESTER_EXCEPTION!= null) {
            throw AWSMECHANICALTURKREQUESTER_EXCEPTION;
        }
        return AWSMECHANICALTURKREQUESTER_WSDL_LOCATION;
    }

}
