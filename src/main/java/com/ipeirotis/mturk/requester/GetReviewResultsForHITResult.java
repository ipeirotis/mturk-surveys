
package com.ipeirotis.mturk.requester;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for GetReviewResultsForHITResult complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="GetReviewResultsForHITResult">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://requester.mturk.amazonaws.com/doc/2013-11-15}Request" minOccurs="0"/>
 *         &lt;element name="HITId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="AssignmentReviewPolicy" type="{http://requester.mturk.amazonaws.com/doc/2013-11-15}ReviewPolicy" minOccurs="0"/>
 *         &lt;element name="HITReviewPolicy" type="{http://requester.mturk.amazonaws.com/doc/2013-11-15}ReviewPolicy" minOccurs="0"/>
 *         &lt;element name="AssignmentReviewReport" type="{http://requester.mturk.amazonaws.com/doc/2013-11-15}ReviewReport" minOccurs="0"/>
 *         &lt;element name="HITReviewReport" type="{http://requester.mturk.amazonaws.com/doc/2013-11-15}ReviewReport" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GetReviewResultsForHITResult", propOrder = {
    "request",
    "hitId",
    "assignmentReviewPolicy",
    "hitReviewPolicy",
    "assignmentReviewReport",
    "hitReviewReport"
})
public class GetReviewResultsForHITResult {

    @XmlElement(name = "Request")
    protected Request request;
    @XmlElement(name = "HITId", required = true)
    protected String hitId;
    @XmlElement(name = "AssignmentReviewPolicy")
    protected ReviewPolicy assignmentReviewPolicy;
    @XmlElement(name = "HITReviewPolicy")
    protected ReviewPolicy hitReviewPolicy;
    @XmlElement(name = "AssignmentReviewReport")
    protected ReviewReport assignmentReviewReport;
    @XmlElement(name = "HITReviewReport")
    protected ReviewReport hitReviewReport;

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
     * Gets the value of the assignmentReviewReport property.
     * 
     * @return
     *     possible object is
     *     {@link ReviewReport }
     *     
     */
    public ReviewReport getAssignmentReviewReport() {
        return assignmentReviewReport;
    }

    /**
     * Sets the value of the assignmentReviewReport property.
     * 
     * @param value
     *     allowed object is
     *     {@link ReviewReport }
     *     
     */
    public void setAssignmentReviewReport(ReviewReport value) {
        this.assignmentReviewReport = value;
    }

    /**
     * Gets the value of the hitReviewReport property.
     * 
     * @return
     *     possible object is
     *     {@link ReviewReport }
     *     
     */
    public ReviewReport getHITReviewReport() {
        return hitReviewReport;
    }

    /**
     * Sets the value of the hitReviewReport property.
     * 
     * @param value
     *     allowed object is
     *     {@link ReviewReport }
     *     
     */
    public void setHITReviewReport(ReviewReport value) {
        this.hitReviewReport = value;
    }

}
