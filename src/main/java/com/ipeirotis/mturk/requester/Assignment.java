
package com.ipeirotis.mturk.requester;

import java.util.Calendar;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for Assignment complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Assignment">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://requester.mturk.amazonaws.com/doc/2013-11-15}Request" minOccurs="0"/>
 *         &lt;element name="AssignmentId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="WorkerId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="HITId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="AssignmentStatus" type="{http://requester.mturk.amazonaws.com/doc/2013-11-15}AssignmentStatus" minOccurs="0"/>
 *         &lt;element name="AutoApprovalTime" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="AcceptTime" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="SubmitTime" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="ApprovalTime" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="RejectionTime" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="Deadline" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="Answer" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="RequesterFeedback" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Assignment", propOrder = {
    "request",
    "assignmentId",
    "workerId",
    "hitId",
    "assignmentStatus",
    "autoApprovalTime",
    "acceptTime",
    "submitTime",
    "approvalTime",
    "rejectionTime",
    "deadline",
    "answer",
    "requesterFeedback"
})
public class Assignment {

    @XmlElement(name = "Request")
    protected Request request;
    @XmlElement(name = "AssignmentId")
    protected String assignmentId;
    @XmlElement(name = "WorkerId")
    protected String workerId;
    @XmlElement(name = "HITId")
    protected String hitId;
    @XmlElement(name = "AssignmentStatus")
    protected AssignmentStatus assignmentStatus;
    @XmlElement(name = "AutoApprovalTime", type = String.class)
    @XmlJavaTypeAdapter(Adapter1 .class)
    @XmlSchemaType(name = "dateTime")
    protected Calendar autoApprovalTime;
    @XmlElement(name = "AcceptTime", type = String.class)
    @XmlJavaTypeAdapter(Adapter1 .class)
    @XmlSchemaType(name = "dateTime")
    protected Calendar acceptTime;
    @XmlElement(name = "SubmitTime", type = String.class)
    @XmlJavaTypeAdapter(Adapter1 .class)
    @XmlSchemaType(name = "dateTime")
    protected Calendar submitTime;
    @XmlElement(name = "ApprovalTime", type = String.class)
    @XmlJavaTypeAdapter(Adapter1 .class)
    @XmlSchemaType(name = "dateTime")
    protected Calendar approvalTime;
    @XmlElement(name = "RejectionTime", type = String.class)
    @XmlJavaTypeAdapter(Adapter1 .class)
    @XmlSchemaType(name = "dateTime")
    protected Calendar rejectionTime;
    @XmlElement(name = "Deadline", type = String.class)
    @XmlJavaTypeAdapter(Adapter1 .class)
    @XmlSchemaType(name = "dateTime")
    protected Calendar deadline;
    @XmlElement(name = "Answer")
    protected String answer;
    @XmlElement(name = "RequesterFeedback")
    protected String requesterFeedback;

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
     * Gets the value of the assignmentStatus property.
     * 
     * @return
     *     possible object is
     *     {@link AssignmentStatus }
     *     
     */
    public AssignmentStatus getAssignmentStatus() {
        return assignmentStatus;
    }

    /**
     * Sets the value of the assignmentStatus property.
     * 
     * @param value
     *     allowed object is
     *     {@link AssignmentStatus }
     *     
     */
    public void setAssignmentStatus(AssignmentStatus value) {
        this.assignmentStatus = value;
    }

    /**
     * Gets the value of the autoApprovalTime property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public Calendar getAutoApprovalTime() {
        return autoApprovalTime;
    }

    /**
     * Sets the value of the autoApprovalTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAutoApprovalTime(Calendar value) {
        this.autoApprovalTime = value;
    }

    /**
     * Gets the value of the acceptTime property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public Calendar getAcceptTime() {
        return acceptTime;
    }

    /**
     * Sets the value of the acceptTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAcceptTime(Calendar value) {
        this.acceptTime = value;
    }

    /**
     * Gets the value of the submitTime property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public Calendar getSubmitTime() {
        return submitTime;
    }

    /**
     * Sets the value of the submitTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSubmitTime(Calendar value) {
        this.submitTime = value;
    }

    /**
     * Gets the value of the approvalTime property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public Calendar getApprovalTime() {
        return approvalTime;
    }

    /**
     * Sets the value of the approvalTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setApprovalTime(Calendar value) {
        this.approvalTime = value;
    }

    /**
     * Gets the value of the rejectionTime property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public Calendar getRejectionTime() {
        return rejectionTime;
    }

    /**
     * Sets the value of the rejectionTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRejectionTime(Calendar value) {
        this.rejectionTime = value;
    }

    /**
     * Gets the value of the deadline property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public Calendar getDeadline() {
        return deadline;
    }

    /**
     * Sets the value of the deadline property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDeadline(Calendar value) {
        this.deadline = value;
    }

    /**
     * Gets the value of the answer property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAnswer() {
        return answer;
    }

    /**
     * Sets the value of the answer property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAnswer(String value) {
        this.answer = value;
    }

    /**
     * Gets the value of the requesterFeedback property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRequesterFeedback() {
        return requesterFeedback;
    }

    /**
     * Sets the value of the requesterFeedback property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRequesterFeedback(String value) {
        this.requesterFeedback = value;
    }

}
