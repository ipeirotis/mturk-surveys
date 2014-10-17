
package com.ipeirotis.mturk.requester;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for RegisterHITTypeRequest complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="RegisterHITTypeRequest">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="AutoApprovalDelayInSeconds" type="{http://www.w3.org/2001/XMLSchema}long" minOccurs="0"/>
 *         &lt;element name="AssignmentDurationInSeconds" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="Reward" type="{http://requester.mturk.amazonaws.com/doc/2013-11-15}Price"/>
 *         &lt;element name="Title" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Keywords" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Description" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="QualificationRequirement" type="{http://requester.mturk.amazonaws.com/doc/2013-11-15}QualificationRequirement" maxOccurs="unbounded" minOccurs="0"/>
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
@XmlType(name = "RegisterHITTypeRequest", propOrder = {
    "autoApprovalDelayInSeconds",
    "assignmentDurationInSeconds",
    "reward",
    "title",
    "keywords",
    "description",
    "qualificationRequirement",
    "responseGroup"
})
public class RegisterHITTypeRequest {

    @XmlElement(name = "AutoApprovalDelayInSeconds")
    protected Long autoApprovalDelayInSeconds;
    @XmlElement(name = "AssignmentDurationInSeconds")
    protected long assignmentDurationInSeconds;
    @XmlElement(name = "Reward", required = true)
    protected Price reward;
    @XmlElement(name = "Title", required = true)
    protected String title;
    @XmlElement(name = "Keywords")
    protected String keywords;
    @XmlElement(name = "Description", required = true)
    protected String description;
    @XmlElement(name = "QualificationRequirement")
    protected List<QualificationRequirement> qualificationRequirement;
    @XmlElement(name = "ResponseGroup")
    protected List<String> responseGroup;

    /**
     * Gets the value of the autoApprovalDelayInSeconds property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getAutoApprovalDelayInSeconds() {
        return autoApprovalDelayInSeconds;
    }

    /**
     * Sets the value of the autoApprovalDelayInSeconds property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setAutoApprovalDelayInSeconds(Long value) {
        this.autoApprovalDelayInSeconds = value;
    }

    /**
     * Gets the value of the assignmentDurationInSeconds property.
     * 
     */
    public long getAssignmentDurationInSeconds() {
        return assignmentDurationInSeconds;
    }

    /**
     * Sets the value of the assignmentDurationInSeconds property.
     * 
     */
    public void setAssignmentDurationInSeconds(long value) {
        this.assignmentDurationInSeconds = value;
    }

    /**
     * Gets the value of the reward property.
     * 
     * @return
     *     possible object is
     *     {@link Price }
     *     
     */
    public Price getReward() {
        return reward;
    }

    /**
     * Sets the value of the reward property.
     * 
     * @param value
     *     allowed object is
     *     {@link Price }
     *     
     */
    public void setReward(Price value) {
        this.reward = value;
    }

    /**
     * Gets the value of the title property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the value of the title property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTitle(String value) {
        this.title = value;
    }

    /**
     * Gets the value of the keywords property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getKeywords() {
        return keywords;
    }

    /**
     * Sets the value of the keywords property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setKeywords(String value) {
        this.keywords = value;
    }

    /**
     * Gets the value of the description property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the value of the description property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDescription(String value) {
        this.description = value;
    }

    /**
     * Gets the value of the qualificationRequirement property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the qualificationRequirement property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getQualificationRequirement().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link QualificationRequirement }
     * 
     * 
     */
    public List<QualificationRequirement> getQualificationRequirement() {
        if (qualificationRequirement == null) {
            qualificationRequirement = new ArrayList<QualificationRequirement>();
        }
        return this.qualificationRequirement;
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
