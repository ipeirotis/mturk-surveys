
package com.ipeirotis.mturk.requester;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://requester.mturk.amazonaws.com/doc/2013-11-15}OperationRequest" minOccurs="0"/>
 *         &lt;element name="ApproveRejectedAssignmentResult" type="{http://requester.mturk.amazonaws.com/doc/2013-11-15}ApproveRejectedAssignmentResult" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "operationRequest",
    "approveRejectedAssignmentResult"
})
@XmlRootElement(name = "ApproveRejectedAssignmentResponse")
public class ApproveRejectedAssignmentResponse {

    @XmlElement(name = "OperationRequest")
    protected OperationRequest operationRequest;
    @XmlElement(name = "ApproveRejectedAssignmentResult")
    protected List<ApproveRejectedAssignmentResult> approveRejectedAssignmentResult;

    /**
     * Gets the value of the operationRequest property.
     * 
     * @return
     *     possible object is
     *     {@link OperationRequest }
     *     
     */
    public OperationRequest getOperationRequest() {
        return operationRequest;
    }

    /**
     * Sets the value of the operationRequest property.
     * 
     * @param value
     *     allowed object is
     *     {@link OperationRequest }
     *     
     */
    public void setOperationRequest(OperationRequest value) {
        this.operationRequest = value;
    }

    /**
     * Gets the value of the approveRejectedAssignmentResult property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the approveRejectedAssignmentResult property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getApproveRejectedAssignmentResult().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ApproveRejectedAssignmentResult }
     * 
     * 
     */
    public List<ApproveRejectedAssignmentResult> getApproveRejectedAssignmentResult() {
        if (approveRejectedAssignmentResult == null) {
            approveRejectedAssignmentResult = new ArrayList<ApproveRejectedAssignmentResult>();
        }
        return this.approveRejectedAssignmentResult;
    }

}
