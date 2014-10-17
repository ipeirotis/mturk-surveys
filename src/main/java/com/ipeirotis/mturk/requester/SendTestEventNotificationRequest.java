
package com.ipeirotis.mturk.requester;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for SendTestEventNotificationRequest complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="SendTestEventNotificationRequest">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Notification" type="{http://requester.mturk.amazonaws.com/doc/2013-11-15}NotificationSpecification"/>
 *         &lt;element name="TestEventType" type="{http://requester.mturk.amazonaws.com/doc/2013-11-15}EventType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SendTestEventNotificationRequest", propOrder = {
    "notification",
    "testEventType"
})
public class SendTestEventNotificationRequest {

    @XmlElement(name = "Notification", required = true)
    protected NotificationSpecification notification;
    @XmlElement(name = "TestEventType")
    protected EventType testEventType;

    /**
     * Gets the value of the notification property.
     * 
     * @return
     *     possible object is
     *     {@link NotificationSpecification }
     *     
     */
    public NotificationSpecification getNotification() {
        return notification;
    }

    /**
     * Sets the value of the notification property.
     * 
     * @param value
     *     allowed object is
     *     {@link NotificationSpecification }
     *     
     */
    public void setNotification(NotificationSpecification value) {
        this.notification = value;
    }

    /**
     * Gets the value of the testEventType property.
     * 
     * @return
     *     possible object is
     *     {@link EventType }
     *     
     */
    public EventType getTestEventType() {
        return testEventType;
    }

    /**
     * Sets the value of the testEventType property.
     * 
     * @param value
     *     allowed object is
     *     {@link EventType }
     *     
     */
    public void setTestEventType(EventType value) {
        this.testEventType = value;
    }

}
