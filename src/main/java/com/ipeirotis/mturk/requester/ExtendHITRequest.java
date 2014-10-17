
package com.ipeirotis.mturk.requester;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ExtendHITRequest complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ExtendHITRequest">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="HITId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="MaxAssignmentsIncrement" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="ExpirationIncrementInSeconds" type="{http://www.w3.org/2001/XMLSchema}long" minOccurs="0"/>
 *         &lt;element name="UniqueRequestToken" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ResponseGroup" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ExtendHITRequest", propOrder = {
    "hitId",
    "maxAssignmentsIncrement",
    "expirationIncrementInSeconds",
    "uniqueRequestToken",
    "responseGroup"
})
public class ExtendHITRequest {

    @XmlElement(name = "HITId", required = true)
    protected String hitId;
    @XmlElement(name = "MaxAssignmentsIncrement")
    protected Integer maxAssignmentsIncrement;
    @XmlElement(name = "ExpirationIncrementInSeconds")
    protected Long expirationIncrementInSeconds;
    @XmlElement(name = "UniqueRequestToken")
    protected String uniqueRequestToken;
    @XmlElement(name = "ResponseGroup")
    protected List<String> responseGroup;

    /**
     * Gets the value of the hitId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHITId() {
        return hitId;
    }

    /**
     * Sets the value of the hitId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHITId(String value) {
        this.hitId = value;
    }

    /**
     * Gets the value of the maxAssignmentsIncrement property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getMaxAssignmentsIncrement() {
        return maxAssignmentsIncrement;
    }

    /**
     * Sets the value of the maxAssignmentsIncrement property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setMaxAssignmentsIncrement(Integer value) {
        this.maxAssignmentsIncrement = value;
    }

    /**
     * Gets the value of the expirationIncrementInSeconds property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getExpirationIncrementInSeconds() {
        return expirationIncrementInSeconds;
    }

    /**
     * Sets the value of the expirationIncrementInSeconds property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setExpirationIncrementInSeconds(Long value) {
        this.expirationIncrementInSeconds = value;
    }

    /**
     * Gets the value of the uniqueRequestToken property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUniqueRequestToken() {
        return uniqueRequestToken;
    }

    /**
     * Sets the value of the uniqueRequestToken property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUniqueRequestToken(String value) {
        this.uniqueRequestToken = value;
    }

    /**
     * Gets the value of the responseGroup property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the responseGroup property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getResponseGroup().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getResponseGroup() {
        if (responseGroup == null) {
            responseGroup = new ArrayList<String>();
        }
        return this.responseGroup;
    }

}
