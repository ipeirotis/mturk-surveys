
package com.ipeirotis.mturk.requester;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for GetAccountBalanceResult complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="GetAccountBalanceResult">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://requester.mturk.amazonaws.com/doc/2013-11-15}Request" minOccurs="0"/>
 *         &lt;element name="AvailableBalance" type="{http://requester.mturk.amazonaws.com/doc/2013-11-15}Price" minOccurs="0"/>
 *         &lt;element name="OnHoldBalance" type="{http://requester.mturk.amazonaws.com/doc/2013-11-15}Price" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GetAccountBalanceResult", propOrder = {
    "request",
    "availableBalance",
    "onHoldBalance"
})
public class GetAccountBalanceResult {

    @XmlElement(name = "Request")
    protected Request request;
    @XmlElement(name = "AvailableBalance")
    protected Price availableBalance;
    @XmlElement(name = "OnHoldBalance")
    protected Price onHoldBalance;

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
     * Gets the value of the availableBalance property.
     * 
     * @return
     *     possible object is
     *     {@link Price }
     *     
     */
    public Price getAvailableBalance() {
        return availableBalance;
    }

    /**
     * Sets the value of the availableBalance property.
     * 
     * @param value
     *     allowed object is
     *     {@link Price }
     *     
     */
    public void setAvailableBalance(Price value) {
        this.availableBalance = value;
    }

    /**
     * Gets the value of the onHoldBalance property.
     * 
     * @return
     *     possible object is
     *     {@link Price }
     *     
     */
    public Price getOnHoldBalance() {
        return onHoldBalance;
    }

    /**
     * Sets the value of the onHoldBalance property.
     * 
     * @param value
     *     allowed object is
     *     {@link Price }
     *     
     */
    public void setOnHoldBalance(Price value) {
        this.onHoldBalance = value;
    }

}
