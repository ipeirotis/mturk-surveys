
package com.ipeirotis.mturk.requester;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for SetHITTypeNotificationRequest complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="SetHITTypeNotificationRequest">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="HITTypeId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Notification" type="{http://requester.mturk.amazonaws.com/doc/2013-11-15}NotificationSpecification" minOccurs="0"/>
 *         &lt;element name="Active" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SetHITTypeNotificationRequest", propOrder = {
    "hitTypeId",
    "notification",
    "active"
})
public class SetHITTypeNotificationRequest {

    @XmlElement(name = "HITTypeId", required = true)
    protected String hitTypeId;
    @XmlElement(name = "Notification")
    protected NotificationSpecification notification;
    @XmlElement(name = "Active")
    protected Boolean active;

    /**
     * Gets the value of the hitTypeId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHITTypeId() {
        return hitTypeId;
    }

    /**
     * Sets the value of the hitTypeId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHITTypeId(String value) {
        this.hitTypeId = value;
    }

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
     * Gets the value of the active property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isActive() {
        return active;
    }

    /**
     * Sets the value of the active property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setActive(Boolean value) {
        this.active = value;
    }

}
