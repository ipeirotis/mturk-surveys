
package com.ipeirotis.mturk.requester;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for GetFileUploadURLRequest complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="GetFileUploadURLRequest">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="AssignmentId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="QuestionIdentifier" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GetFileUploadURLRequest", propOrder = {
    "assignmentId",
    "questionIdentifier"
})
public class GetFileUploadURLRequest {

    @XmlElement(name = "AssignmentId", required = true)
    protected String assignmentId;
    @XmlElement(name = "QuestionIdentifier", required = true)
    protected String questionIdentifier;

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
     * Gets the value of the questionIdentifier property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getQuestionIdentifier() {
        return questionIdentifier;
    }

    /**
     * Sets the value of the questionIdentifier property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setQuestionIdentifier(String value) {
        this.questionIdentifier = value;
    }

}
