
package com.ipeirotis.mturk.requester;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for GetFileUploadURLResult complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="GetFileUploadURLResult">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://requester.mturk.amazonaws.com/doc/2013-11-15}Request" minOccurs="0"/>
 *         &lt;element name="FileUploadURL" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GetFileUploadURLResult", propOrder = {
    "request",
    "fileUploadURL"
})
public class GetFileUploadURLResult {

    @XmlElement(name = "Request")
    protected Request request;
    @XmlElement(name = "FileUploadURL", required = true)
    protected String fileUploadURL;

    /**
     * Gets the value of the request property.
     * 
     * @return
     *     possible object is
     *     {@link Request }
     *     
     */
    public Request getRequest() {
        return request;
    }

    /**
     * Sets the value of the request property.
     * 
     * @param value
     *     allowed object is
     *     {@link Request }
     *     
     */
    public void setRequest(Request value) {
        this.request = value;
    }

    /**
     * Gets the value of the fileUploadURL property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFileUploadURL() {
        return fileUploadURL;
    }

    /**
     * Sets the value of the fileUploadURL property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFileUploadURL(String value) {
        this.fileUploadURL = value;
    }

}
