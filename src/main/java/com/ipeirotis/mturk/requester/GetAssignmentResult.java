
package com.ipeirotis.mturk.requester;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for GetAssignmentResult complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="GetAssignmentResult">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://requester.mturk.amazonaws.com/doc/2013-11-15}Request" minOccurs="0"/>
 *         &lt;element name="Assignment" type="{http://requester.mturk.amazonaws.com/doc/2013-11-15}Assignment" minOccurs="0"/>
 *         &lt;element name="HIT" type="{http://requester.mturk.amazonaws.com/doc/2013-11-15}HIT" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GetAssignmentResult", propOrder = {
    "request",
    "assignment",
    "hit"
})
public class GetAssignmentResult {

    @XmlElement(name = "Request")
    protected Request request;
    @XmlElement(name = "Assignment")
    protected Assignment assignment;
    @XmlElement(name = "HIT")
    protected HIT hit;

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
     * Gets the value of the assignment property.
     * 
     * @return
     *     possible object is
     *     {@link Assignment }
     *     
     */
    public Assignment getAssignment() {
        return assignment;
    }

    /**
     * Sets the value of the assignment property.
     * 
     * @param value
     *     allowed object is
     *     {@link Assignment }
     *     
     */
    public void setAssignment(Assignment value) {
        this.assignment = value;
    }

    /**
     * Gets the value of the hit property.
     * 
     * @return
     *     possible object is
     *     {@link HIT }
     *     
     */
    public HIT getHIT() {
        return hit;
    }

    /**
     * Sets the value of the hit property.
     * 
     * @param value
     *     allowed object is
     *     {@link HIT }
     *     
     */
    public void setHIT(HIT value) {
        this.hit = value;
    }

}
