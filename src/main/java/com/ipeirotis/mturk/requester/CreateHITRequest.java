
package com.ipeirotis.mturk.requester;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CreateHITRequest complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CreateHITRequest">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="HITTypeId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="MaxAssignments" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="AutoApprovalDelayInSeconds" type="{http://www.w3.org/2001/XMLSchema}long" minOccurs="0"/>
 *         &lt;element name="LifetimeInSeconds" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="AssignmentDurationInSeconds" type="{http://www.w3.org/2001/XMLSchema}long" minOccurs="0"/>
 *         &lt;element name="Reward" type="{http://requester.mturk.amazonaws.com/doc/2013-11-15}Price" minOccurs="0"/>
 *         &lt;element name="Title" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Keywords" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Description" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Question" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="RequesterAnnotation" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="QualificationRequirement" type="{http://requester.mturk.amazonaws.com/doc/2013-11-15}QualificationRequirement" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="UniqueRequestToken" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="AssignmentReviewPolicy" type="{http://requester.mturk.amazonaws.com/doc/2013-11-15}ReviewPolicy" minOccurs="0"/>
 *         &lt;element name="HITReviewPolicy" type="{http://requester.mturk.amazonaws.com/doc/2013-11-15}ReviewPolicy" minOccurs="0"/>
 *         &lt;element name="HITLayoutId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="HITLayoutParameter" type="{http://requester.mturk.amazonaws.com/doc/2013-11-15}HITLayoutParameter" maxOccurs="unbounded" minOccurs="0"/>
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
@XmlType(name = "CreateHITRequest", propOrder = {
    "hitTypeId",
    "maxAssignments",
    "autoApprovalDelayInSeconds",
    "lifetimeInSeconds",
    "assignmentDurationInSeconds",
    "reward",
    "title",
    "keywords",
    "description",
    "question",
    "requesterAnnotation",
    "qualificationRequirement",
    "uniqueRequestToken",
    "assignmentReviewPolicy",
    "hitReviewPolicy",
    "hitLayoutId",
    "hitLayoutParameter",
    "responseGroup"
})
public class CreateHITRequest {

    @XmlElement(name = "HITTypeId")
    protected String hitTypeId;
    @XmlElement(name = "MaxAssignments")
    protected Integer maxAssignments;
    @XmlElement(name = "AutoApprovalDelayInSeconds")
    protected Long autoApprovalDelayInSeconds;
    @XmlElement(name = "LifetimeInSeconds")
    protected long lifetimeInSeconds;
    @XmlElement(name = "AssignmentDurationInSeconds")
    protected Long assignmentDurationInSeconds;
    @XmlElement(name = "Reward")
    protected Price reward;
    @XmlElement(name = "Title")
    protected String title;
    @XmlElement(name = "Keywords")
    protected String keywords;
    @XmlElement(name = "Description")
    protected String description;
    @XmlElement(name = "Question")
    protected String question;
    @XmlElement(name = "RequesterAnnotation")
    protected String requesterAnnotation;
    @XmlElement(name = "QualificationRequirement")
    protected List<QualificationRequirement> qualificationRequirement;
    @XmlElement(name = "UniqueRequestToken")
    protected String uniqueRequestToken;
    @XmlElement(name = "AssignmentReviewPolicy")
    protected ReviewPolicy assignmentReviewPolicy;
    @XmlElement(name = "HITReviewPolicy")
    protected ReviewPolicy hitReviewPolicy;
    @XmlElement(name = "HITLayoutId")
    protected String hitLayoutId;
    @XmlElement(name = "HITLayoutParameter")
    protected List<HITLayoutParameter> hitLayoutParameter;
    @XmlElement(name = "ResponseGroup")
    protected List<String> responseGroup;

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
     * Gets the value of the maxAssignments property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getMaxAssignments() {
        return maxAssignments;
    }

    /**
     * Sets the value of the maxAssignments property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setMaxAssignments(Integer value) {
        this.maxAssignments = value;
    }

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
     * Gets the value of the lifetimeInSeconds property.
     * 
     */
    public long getLifetimeInSeconds() {
        return lifetimeInSeconds;
    }

    /**
     * Sets the value of the lifetimeInSeconds property.
     * 
     */
    public void setLifetimeInSeconds(long value) {
        this.lifetimeInSeconds = value;
    }

    /**
     * Gets the value of the assignmentDurationInSeconds property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getAssignmentDurationInSeconds() {
        return assignmentDurationInSeconds;
    }

    /**
     * Sets the value of the assignmentDurationInSeconds property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setAssignmentDurationInSeconds(Long value) {
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
     * Gets the value of the question property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getQuestion() {
        return question;
    }

    /**
     * Sets the value of the question property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setQuestion(String value) {
        this.question = value;
    }

    /**
     * Gets the value of the requesterAnnotation property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRequesterAnnotation() {
        return requesterAnnotation;
    }

    /**
     * Sets the value of the requesterAnnotation property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRequesterAnnotation(String value) {
        this.requesterAnnotation = value;
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
     * Gets the value of the assignmentReviewPolicy property.
     * 
     * @return
     *     possible object is
     *     {@link ReviewPolicy }
     *     
     */
    public ReviewPolicy getAssignmentReviewPolicy() {
        return assignmentReviewPolicy;
    }

    /**
     * Sets the value of the assignmentReviewPolicy property.
     * 
     * @param value
     *     allowed object is
     *     {@link ReviewPolicy }
     *     
     */
    public void setAssignmentReviewPolicy(ReviewPolicy value) {
        this.assignmentReviewPolicy = value;
    }

    /**
     * Gets the value of the hitReviewPolicy property.
     * 
     * @return
     *     possible object is
     *     {@link ReviewPolicy }
     *     
     */
    public ReviewPolicy getHITReviewPolicy() {
        return hitReviewPolicy;
    }

    /**
     * Sets the value of the hitReviewPolicy property.
     * 
     * @param value
     *     allowed object is
     *     {@link ReviewPolicy }
     *     
     */
    public void setHITReviewPolicy(ReviewPolicy value) {
        this.hitReviewPolicy = value;
    }

    /**
     * Gets the value of the hitLayoutId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHITLayoutId() {
        return hitLayoutId;
    }

    /**
     * Sets the value of the hitLayoutId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHITLayoutId(String value) {
        this.hitLayoutId = value;
    }

    /**
     * Gets the value of the hitLayoutParameter property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the hitLayoutParameter property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getHITLayoutParameter().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link HITLayoutParameter }
     * 
     * 
     */
    public List<HITLayoutParameter> getHITLayoutParameter() {
        if (hitLayoutParameter == null) {
            hitLayoutParameter = new ArrayList<HITLayoutParameter>();
        }
        return this.hitLayoutParameter;
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
