
package com.ipeirotis.mturk.requester;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for NotifyWorkersResult complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="NotifyWorkersResult">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://requester.mturk.amazonaws.com/doc/2013-11-15}Request" minOccurs="0"/>
 *         &lt;element name="NotifyWorkersFailureStatus" type="{http://requester.mturk.amazonaws.com/doc/2013-11-15}NotifyWorkersFailureStatus" maxOccurs="100" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "NotifyWorkersResult", propOrder = {
    "request",
    "notifyWorkersFailureStatus"
})
public class NotifyWorkersResult {

    @XmlElement(name = "Request")
    protected Request request;
    @XmlElement(name = "NotifyWorkersFailureStatus")
    protected List<NotifyWorkersFailureStatus> notifyWorkersFailureStatus;

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
     * Gets the value of the notifyWorkersFailureStatus property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the notifyWorkersFailureStatus property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getNotifyWorkersFailureStatus().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link NotifyWorkersFailureStatus }
     * 
     * 
     */
    public List<NotifyWorkersFailureStatus> getNotifyWorkersFailureStatus() {
        if (notifyWorkersFailureStatus == null) {
            notifyWorkersFailureStatus = new ArrayList<NotifyWorkersFailureStatus>();
        }
        return this.notifyWorkersFailureStatus;
    }

}
