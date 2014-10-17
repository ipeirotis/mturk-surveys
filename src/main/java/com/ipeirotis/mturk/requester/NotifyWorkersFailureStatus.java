
package com.ipeirotis.mturk.requester;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for NotifyWorkersFailureStatus complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="NotifyWorkersFailureStatus">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="NotifyWorkersFailureCode" type="{http://requester.mturk.amazonaws.com/doc/2013-11-15}NotifyWorkersFailureCode"/>
 *         &lt;element name="NotifyWorkersFailureMessage" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="WorkerId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "NotifyWorkersFailureStatus", propOrder = {
    "notifyWorkersFailureCode",
    "notifyWorkersFailureMessage",
    "workerId"
})
public class NotifyWorkersFailureStatus {

    @XmlElement(name = "NotifyWorkersFailureCode", required = true)
    protected NotifyWorkersFailureCode notifyWorkersFailureCode;
    @XmlElement(name = "NotifyWorkersFailureMessage", required = true)
    protected String notifyWorkersFailureMessage;
    @XmlElement(name = "WorkerId", required = true)
    protected String workerId;

    /**
     * Gets the value of the notifyWorkersFailureCode property.
     * 
     * @return
     *     possible object is
     *     {@link NotifyWorkersFailureCode }
     *     
     */
    public NotifyWorkersFailureCode getNotifyWorkersFailureCode() {
        return notifyWorkersFailureCode;
    }

    /**
     * Sets the value of the notifyWorkersFailureCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link NotifyWorkersFailureCode }
     *     
     */
    public void setNotifyWorkersFailureCode(NotifyWorkersFailureCode value) {
        this.notifyWorkersFailureCode = value;
    }

    /**
     * Gets the value of the notifyWorkersFailureMessage property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNotifyWorkersFailureMessage() {
        return notifyWorkersFailureMessage;
    }

    /**
     * Sets the value of the notifyWorkersFailureMessage property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNotifyWorkersFailureMessage(String value) {
        this.notifyWorkersFailureMessage = value;
    }

    /**
     * Gets the value of the workerId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getWorkerId() {
        return workerId;
    }

    /**
     * Sets the value of the workerId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setWorkerId(String value) {
        this.workerId = value;
    }

}
