
package com.ipeirotis.mturk.requester;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for HIT complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="HIT">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://requester.mturk.amazonaws.com/doc/2013-11-15}Request" minOccurs="0"/>
 *         &lt;element name="HITId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="HITTypeId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="HITGroupId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="HITLayoutId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="CreationTime" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="Title" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Description" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Question" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Keywords" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="HITStatus" type="{http://requester.mturk.amazonaws.com/doc/2013-11-15}HITStatus" minOccurs="0"/>
 *         &lt;element name="MaxAssignments" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="Reward" type="{http://requester.mturk.amazonaws.com/doc/2013-11-15}Price" minOccurs="0"/>
 *         &lt;element name="AutoApprovalDelayInSeconds" type="{http://www.w3.org/2001/XMLSchema}long" minOccurs="0"/>
 *         &lt;element name="Expiration" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="AssignmentDurationInSeconds" type="{http://www.w3.org/2001/XMLSchema}long" minOccurs="0"/>
 *         &lt;element name="RequesterAnnotation" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="QualificationRequirement" type="{http://requester.mturk.amazonaws.com/doc/2013-11-15}QualificationRequirement" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="HITReviewStatus" type="{http://requester.mturk.amazonaws.com/doc/2013-11-15}HITReviewStatus" minOccurs="0"/>
 *         &lt;element name="NumberOfAssignmentsPending" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="NumberOfAssignmentsAvailable" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="NumberOfAssignmentsCompleted" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "HIT", propOrder = {
    "request",
    "hitId",
    "hitTypeId",
    "hitGroupId",
    "hitLayoutId",
    "creationTime",
    "title",
    "description",
    "question",
    "keywords",
    "hitStatus",
    "maxAssignments",
    "reward",
    "autoApprovalDelayInSeconds",
    "expiration",
    "assignmentDurationInSeconds",
    "requesterAnnotation",
    "qualificationRequirement",
    "hitReviewStatus",
    "numberOfAssignmentsPending",
    "numberOfAssignmentsAvailable",
    "numberOfAssignmentsCompleted"
})
public class HIT {

    @XmlElement(name = "Request")
    protected Request request;
    @XmlElement(name = "HITId")
    protected String hitId;
    @XmlElement(name = "HITTypeId")
    protected String hitTypeId;
    @XmlElement(name = "HITGroupId")
    protected String hitGroupId;
    @XmlElement(name = "HITLayoutId")
    protected String hitLayoutId;
    @XmlElement(name = "CreationTime", type = String.class)
    @XmlJavaTypeAdapter(Adapter1 .class)
    @XmlSchemaType(name = "dateTime")
    protected Calendar creationTime;
    @XmlElement(name = "Title")
    protected String title;
    @XmlElement(name = "Description")
    protected String description;
    @XmlElement(name = "Question")
    protected String question;
    @XmlElement(name = "Keywords")
    protected String keywords;
    @XmlElement(name = "HITStatus")
    protected HITStatus hitStatus;
    @XmlElement(name = "MaxAssignments")
    protected Integer maxAssignments;
    @XmlElement(name = "Reward")
    protected Price reward;
    @XmlElement(name = "AutoApprovalDelayInSeconds")
    protected Long autoApprovalDelayInSeconds;
    @XmlElement(name = "Expiration", type = String.class)
    @XmlJavaTypeAdapter(Adapter1 .class)
    @XmlSchemaType(name = "dateTime")
    protected Calendar expiration;
    @XmlElement(name = "AssignmentDurationInSeconds")
    protected Long assignmentDurationInSeconds;
    @XmlElement(name = "RequesterAnnotation")
    protected String requesterAnnotation;
    @XmlElement(name = "QualificationRequirement")
    protected List<QualificationRequirement> qualificationRequirement;
    @XmlElement(name = "HITReviewStatus")
    protected HITReviewStatus hitReviewStatus;
    @XmlElement(name = "NumberOfAssignmentsPending")
    protected Integer numberOfAssignmentsPending;
    @XmlElement(name = "NumberOfAssignmentsAvailable")
    protected Integer numberOfAssignmentsAvailable;
    @XmlElement(name = "NumberOfAssignmentsCompleted")
    protected Integer numberOfAssignmentsCompleted;

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
     * Gets the value of the hitGroupId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHITGroupId() {
        return hitGroupId;
    }

    /**
     * Sets the value of the hitGroupId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHITGroupId(String value) {
        this.hitGroupId = value;
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
     * Gets the value of the creationTime property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public Calendar getCreationTime() {
        return creationTime;
    }

    /**
     * Sets the value of the creationTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCreationTime(Calendar value) {
        this.creationTime = value;
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
     * Gets the value of the hitStatus property.
     * 
     * @return
     *     possible object is
     *     {@link HITStatus }
     *     
     */
    public HITStatus getHITStatus() {
        return hitStatus;
    }

    /**
     * Sets the value of the hitStatus property.
     * 
     * @param value
     *     allowed object is
     *     {@link HITStatus }
     *     
     */
    public void setHITStatus(HITStatus value) {
        this.hitStatus = value;
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
     * Gets the value of the expiration property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public Calendar getExpiration() {
        return expiration;
    }

    /**
     * Sets the value of the expiration property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setExpiration(Calendar value) {
        this.expiration = value;
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
     * Gets the value of the hitReviewStatus property.
     * 
     * @return
     *     possible object is
     *     {@link HITReviewStatus }
     *     
     */
    public HITReviewStatus getHITReviewStatus() {
        return hitReviewStatus;
    }

    /**
     * Sets the value of the hitReviewStatus property.
     * 
     * @param value
     *     allowed object is
     *     {@link HITReviewStatus }
     *     
     */
    public void setHITReviewStatus(HITReviewStatus value) {
        this.hitReviewStatus = value;
    }

    /**
     * Gets the value of the numberOfAssignmentsPending property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getNumberOfAssignmentsPending() {
        return numberOfAssignmentsPending;
    }

    /**
     * Sets the value of the numberOfAssignmentsPending property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setNumberOfAssignmentsPending(Integer value) {
        this.numberOfAssignmentsPending = value;
    }

    /**
     * Gets the value of the numberOfAssignmentsAvailable property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getNumberOfAssignmentsAvailable() {
        return numberOfAssignmentsAvailable;
    }

    /**
     * Sets the value of the numberOfAssignmentsAvailable property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setNumberOfAssignmentsAvailable(Integer value) {
        this.numberOfAssignmentsAvailable = value;
    }

    /**
     * Gets the value of the numberOfAssignmentsCompleted property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getNumberOfAssignmentsCompleted() {
        return numberOfAssignmentsCompleted;
    }

    /**
     * Sets the value of the numberOfAssignmentsCompleted property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setNumberOfAssignmentsCompleted(Integer value) {
        this.numberOfAssignmentsCompleted = value;
    }

}
