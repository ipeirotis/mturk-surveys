
package com.ipeirotis.mturk.requester;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for GrantBonusRequest complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="GrantBonusRequest">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="WorkerId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="BonusAmount" type="{http://requester.mturk.amazonaws.com/doc/2013-11-15}Price"/>
 *         &lt;element name="AssignmentId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Reason" type="{http://www.w3.org/2001/XMLSchema}string"/>
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
@XmlType(name = "GrantBonusRequest", propOrder = {
    "workerId",
    "bonusAmount",
    "assignmentId",
    "reason",
    "uniqueRequestToken",
    "responseGroup"
})
public class GrantBonusRequest {

    @XmlElement(name = "WorkerId", required = true)
    protected String workerId;
    @XmlElement(name = "BonusAmount", required = true)
    protected Price bonusAmount;
    @XmlElement(name = "AssignmentId", required = true)
    protected String assignmentId;
    @XmlElement(name = "Reason", required = true)
    protected String reason;
    @XmlElement(name = "UniqueRequestToken")
    protected String uniqueRequestToken;
    @XmlElement(name = "ResponseGroup")
    protected List<String> responseGroup;

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

    /**
     * Gets the value of the bonusAmount property.
     * 
     * @return
     *     possible object is
     *     {@link Price }
     *     
     */
    public Price getBonusAmount() {
        return bonusAmount;
    }

    /**
     * Sets the value of the bonusAmount property.
     * 
     * @param value
     *     allowed object is
     *     {@link Price }
     *     
     */
    public void setBonusAmount(Price value) {
        this.bonusAmount = value;
    }

    /**
     * Gets the value of the assignmentId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAssignmentId() {
        return assignmentId;
    }

    /**
     * Sets the value of the assignmentId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAssignmentId(String value) {
        this.assignmentId = value;
    }

    /**
     * Gets the value of the reason property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getReason() {
        return reason;
    }

    /**
     * Sets the value of the reason property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setReason(String value) {
        this.reason = value;
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
