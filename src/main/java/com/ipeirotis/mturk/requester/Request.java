
package com.ipeirotis.mturk.requester;

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
 *         &lt;element name="IsValid" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="CreateHITRequest" type="{http://requester.mturk.amazonaws.com/doc/2013-11-15}CreateHITRequest" minOccurs="0"/>
 *         &lt;element name="RegisterHITTypeRequest" type="{http://requester.mturk.amazonaws.com/doc/2013-11-15}RegisterHITTypeRequest" minOccurs="0"/>
 *         &lt;element name="DisposeHITRequest" type="{http://requester.mturk.amazonaws.com/doc/2013-11-15}DisposeHITRequest" minOccurs="0"/>
 *         &lt;element name="DisableHITRequest" type="{http://requester.mturk.amazonaws.com/doc/2013-11-15}DisableHITRequest" minOccurs="0"/>
 *         &lt;element name="GetHITRequest" type="{http://requester.mturk.amazonaws.com/doc/2013-11-15}GetHITRequest" minOccurs="0"/>
 *         &lt;element name="GetAssignmentRequest" type="{http://requester.mturk.amazonaws.com/doc/2013-11-15}GetAssignmentRequest" minOccurs="0"/>
 *         &lt;element name="GetReviewResultsForHITRequest" type="{http://requester.mturk.amazonaws.com/doc/2013-11-15}GetReviewResultsForHITRequest" minOccurs="0"/>
 *         &lt;element name="GetReviewableHITsRequest" type="{http://requester.mturk.amazonaws.com/doc/2013-11-15}GetReviewableHITsRequest" minOccurs="0"/>
 *         &lt;element name="GetHITsForQualificationTypeRequest" type="{http://requester.mturk.amazonaws.com/doc/2013-11-15}GetHITsForQualificationTypeRequest" minOccurs="0"/>
 *         &lt;element name="GetQualificationsForQualificationTypeRequest" type="{http://requester.mturk.amazonaws.com/doc/2013-11-15}GetQualificationsForQualificationTypeRequest" minOccurs="0"/>
 *         &lt;element name="SetHITAsReviewingRequest" type="{http://requester.mturk.amazonaws.com/doc/2013-11-15}SetHITAsReviewingRequest" minOccurs="0"/>
 *         &lt;element name="SearchHITsRequest" type="{http://requester.mturk.amazonaws.com/doc/2013-11-15}SearchHITsRequest" minOccurs="0"/>
 *         &lt;element name="ExtendHITRequest" type="{http://requester.mturk.amazonaws.com/doc/2013-11-15}ExtendHITRequest" minOccurs="0"/>
 *         &lt;element name="ForceExpireHITRequest" type="{http://requester.mturk.amazonaws.com/doc/2013-11-15}ForceExpireHITRequest" minOccurs="0"/>
 *         &lt;element name="ChangeHITTypeOfHITRequest" type="{http://requester.mturk.amazonaws.com/doc/2013-11-15}ChangeHITTypeOfHITRequest" minOccurs="0"/>
 *         &lt;element name="CreateQualificationTypeRequest" type="{http://requester.mturk.amazonaws.com/doc/2013-11-15}CreateQualificationTypeRequest" minOccurs="0"/>
 *         &lt;element name="DisposeQualificationTypeRequest" type="{http://requester.mturk.amazonaws.com/doc/2013-11-15}DisposeQualificationTypeRequest" minOccurs="0"/>
 *         &lt;element name="GrantQualificationRequest" type="{http://requester.mturk.amazonaws.com/doc/2013-11-15}GrantQualificationRequest" minOccurs="0"/>
 *         &lt;element name="AssignQualificationRequest" type="{http://requester.mturk.amazonaws.com/doc/2013-11-15}AssignQualificationRequest" minOccurs="0"/>
 *         &lt;element name="RevokeQualificationRequest" type="{http://requester.mturk.amazonaws.com/doc/2013-11-15}RevokeQualificationRequest" minOccurs="0"/>
 *         &lt;element name="GetQualificationRequestsRequest" type="{http://requester.mturk.amazonaws.com/doc/2013-11-15}GetQualificationRequestsRequest" minOccurs="0"/>
 *         &lt;element name="RejectQualificationRequestRequest" type="{http://requester.mturk.amazonaws.com/doc/2013-11-15}RejectQualificationRequestRequest" minOccurs="0"/>
 *         &lt;element name="GetQualificationTypeRequest" type="{http://requester.mturk.amazonaws.com/doc/2013-11-15}GetQualificationTypeRequest" minOccurs="0"/>
 *         &lt;element name="SearchQualificationTypesRequest" type="{http://requester.mturk.amazonaws.com/doc/2013-11-15}SearchQualificationTypesRequest" minOccurs="0"/>
 *         &lt;element name="UpdateQualificationTypeRequest" type="{http://requester.mturk.amazonaws.com/doc/2013-11-15}UpdateQualificationTypeRequest" minOccurs="0"/>
 *         &lt;element name="ApproveAssignmentRequest" type="{http://requester.mturk.amazonaws.com/doc/2013-11-15}ApproveAssignmentRequest" minOccurs="0"/>
 *         &lt;element name="RejectAssignmentRequest" type="{http://requester.mturk.amazonaws.com/doc/2013-11-15}RejectAssignmentRequest" minOccurs="0"/>
 *         &lt;element name="ApproveRejectedAssignmentRequest" type="{http://requester.mturk.amazonaws.com/doc/2013-11-15}ApproveRejectedAssignmentRequest" minOccurs="0"/>
 *         &lt;element name="GetAssignmentsForHIT" type="{http://requester.mturk.amazonaws.com/doc/2013-11-15}GetAssignmentsForHITRequest" minOccurs="0"/>
 *         &lt;element name="GetFileUploadURL" type="{http://requester.mturk.amazonaws.com/doc/2013-11-15}GetFileUploadURLRequest" minOccurs="0"/>
 *         &lt;element name="GrantBonusRequest" type="{http://requester.mturk.amazonaws.com/doc/2013-11-15}GrantBonusRequest" minOccurs="0"/>
 *         &lt;element name="GetBonusPaymentsRequest" type="{http://requester.mturk.amazonaws.com/doc/2013-11-15}GetBonusPaymentsRequest" minOccurs="0"/>
 *         &lt;element name="GetAccountBalanceRequest" type="{http://requester.mturk.amazonaws.com/doc/2013-11-15}GetAccountBalanceRequest" minOccurs="0"/>
 *         &lt;element name="NotifyWorkersRequest" type="{http://requester.mturk.amazonaws.com/doc/2013-11-15}NotifyWorkersRequest" minOccurs="0"/>
 *         &lt;element name="GetBlockedWorkersRequest" type="{http://requester.mturk.amazonaws.com/doc/2013-11-15}GetBlockedWorkersRequest" minOccurs="0"/>
 *         &lt;element name="BlockWorkerRequest" type="{http://requester.mturk.amazonaws.com/doc/2013-11-15}BlockWorkerRequest" minOccurs="0"/>
 *         &lt;element name="UnblockWorkerRequest" type="{http://requester.mturk.amazonaws.com/doc/2013-11-15}UnblockWorkerRequest" minOccurs="0"/>
 *         &lt;element name="GetRequesterStatistic" type="{http://requester.mturk.amazonaws.com/doc/2013-11-15}GetRequesterStatisticRequest" minOccurs="0"/>
 *         &lt;element name="GetRequesterWorkerStatistic" type="{http://requester.mturk.amazonaws.com/doc/2013-11-15}GetRequesterWorkerStatisticRequest" minOccurs="0"/>
 *         &lt;element name="HelpRequest" type="{http://requester.mturk.amazonaws.com/doc/2013-11-15}HelpRequest" minOccurs="0"/>
 *         &lt;element ref="{http://requester.mturk.amazonaws.com/doc/2013-11-15}Errors" minOccurs="0"/>
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
    "isValid",
    "createHITRequest",
    "registerHITTypeRequest",
    "disposeHITRequest",
    "disableHITRequest",
    "getHITRequest",
    "getAssignmentRequest",
    "getReviewResultsForHITRequest",
    "getReviewableHITsRequest",
    "getHITsForQualificationTypeRequest",
    "getQualificationsForQualificationTypeRequest",
    "setHITAsReviewingRequest",
    "searchHITsRequest",
    "extendHITRequest",
    "forceExpireHITRequest",
    "changeHITTypeOfHITRequest",
    "createQualificationTypeRequest",
    "disposeQualificationTypeRequest",
    "grantQualificationRequest",
    "assignQualificationRequest",
    "revokeQualificationRequest",
    "getQualificationRequestsRequest",
    "rejectQualificationRequestRequest",
    "getQualificationTypeRequest",
    "searchQualificationTypesRequest",
    "updateQualificationTypeRequest",
    "approveAssignmentRequest",
    "rejectAssignmentRequest",
    "approveRejectedAssignmentRequest",
    "getAssignmentsForHIT",
    "getFileUploadURL",
    "grantBonusRequest",
    "getBonusPaymentsRequest",
    "getAccountBalanceRequest",
    "notifyWorkersRequest",
    "getBlockedWorkersRequest",
    "blockWorkerRequest",
    "unblockWorkerRequest",
    "getRequesterStatistic",
    "getRequesterWorkerStatistic",
    "helpRequest",
    "errors"
})
@XmlRootElement(name = "Request")
public class Request {

    @XmlElement(name = "IsValid")
    protected String isValid;
    @XmlElement(name = "CreateHITRequest")
    protected CreateHITRequest createHITRequest;
    @XmlElement(name = "RegisterHITTypeRequest")
    protected RegisterHITTypeRequest registerHITTypeRequest;
    @XmlElement(name = "DisposeHITRequest")
    protected DisposeHITRequest disposeHITRequest;
    @XmlElement(name = "DisableHITRequest")
    protected DisableHITRequest disableHITRequest;
    @XmlElement(name = "GetHITRequest")
    protected GetHITRequest getHITRequest;
    @XmlElement(name = "GetAssignmentRequest")
    protected GetAssignmentRequest getAssignmentRequest;
    @XmlElement(name = "GetReviewResultsForHITRequest")
    protected GetReviewResultsForHITRequest getReviewResultsForHITRequest;
    @XmlElement(name = "GetReviewableHITsRequest")
    protected GetReviewableHITsRequest getReviewableHITsRequest;
    @XmlElement(name = "GetHITsForQualificationTypeRequest")
    protected GetHITsForQualificationTypeRequest getHITsForQualificationTypeRequest;
    @XmlElement(name = "GetQualificationsForQualificationTypeRequest")
    protected GetQualificationsForQualificationTypeRequest getQualificationsForQualificationTypeRequest;
    @XmlElement(name = "SetHITAsReviewingRequest")
    protected SetHITAsReviewingRequest setHITAsReviewingRequest;
    @XmlElement(name = "SearchHITsRequest")
    protected SearchHITsRequest searchHITsRequest;
    @XmlElement(name = "ExtendHITRequest")
    protected ExtendHITRequest extendHITRequest;
    @XmlElement(name = "ForceExpireHITRequest")
    protected ForceExpireHITRequest forceExpireHITRequest;
    @XmlElement(name = "ChangeHITTypeOfHITRequest")
    protected ChangeHITTypeOfHITRequest changeHITTypeOfHITRequest;
    @XmlElement(name = "CreateQualificationTypeRequest")
    protected CreateQualificationTypeRequest createQualificationTypeRequest;
    @XmlElement(name = "DisposeQualificationTypeRequest")
    protected DisposeQualificationTypeRequest disposeQualificationTypeRequest;
    @XmlElement(name = "GrantQualificationRequest")
    protected GrantQualificationRequest grantQualificationRequest;
    @XmlElement(name = "AssignQualificationRequest")
    protected AssignQualificationRequest assignQualificationRequest;
    @XmlElement(name = "RevokeQualificationRequest")
    protected RevokeQualificationRequest revokeQualificationRequest;
    @XmlElement(name = "GetQualificationRequestsRequest")
    protected GetQualificationRequestsRequest getQualificationRequestsRequest;
    @XmlElement(name = "RejectQualificationRequestRequest")
    protected RejectQualificationRequestRequest rejectQualificationRequestRequest;
    @XmlElement(name = "GetQualificationTypeRequest")
    protected GetQualificationTypeRequest getQualificationTypeRequest;
    @XmlElement(name = "SearchQualificationTypesRequest")
    protected SearchQualificationTypesRequest searchQualificationTypesRequest;
    @XmlElement(name = "UpdateQualificationTypeRequest")
    protected UpdateQualificationTypeRequest updateQualificationTypeRequest;
    @XmlElement(name = "ApproveAssignmentRequest")
    protected ApproveAssignmentRequest approveAssignmentRequest;
    @XmlElement(name = "RejectAssignmentRequest")
    protected RejectAssignmentRequest rejectAssignmentRequest;
    @XmlElement(name = "ApproveRejectedAssignmentRequest")
    protected ApproveRejectedAssignmentRequest approveRejectedAssignmentRequest;
    @XmlElement(name = "GetAssignmentsForHIT")
    protected GetAssignmentsForHITRequest getAssignmentsForHIT;
    @XmlElement(name = "GetFileUploadURL")
    protected GetFileUploadURLRequest getFileUploadURL;
    @XmlElement(name = "GrantBonusRequest")
    protected GrantBonusRequest grantBonusRequest;
    @XmlElement(name = "GetBonusPaymentsRequest")
    protected GetBonusPaymentsRequest getBonusPaymentsRequest;
    @XmlElement(name = "GetAccountBalanceRequest")
    protected GetAccountBalanceRequest getAccountBalanceRequest;
    @XmlElement(name = "NotifyWorkersRequest")
    protected NotifyWorkersRequest notifyWorkersRequest;
    @XmlElement(name = "GetBlockedWorkersRequest")
    protected GetBlockedWorkersRequest getBlockedWorkersRequest;
    @XmlElement(name = "BlockWorkerRequest")
    protected BlockWorkerRequest blockWorkerRequest;
    @XmlElement(name = "UnblockWorkerRequest")
    protected UnblockWorkerRequest unblockWorkerRequest;
    @XmlElement(name = "GetRequesterStatistic")
    protected GetRequesterStatisticRequest getRequesterStatistic;
    @XmlElement(name = "GetRequesterWorkerStatistic")
    protected GetRequesterWorkerStatisticRequest getRequesterWorkerStatistic;
    @XmlElement(name = "HelpRequest")
    protected HelpRequest helpRequest;
    @XmlElement(name = "Errors")
    protected Errors errors;

    /**
     * Gets the value of the isValid property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIsValid() {
        return isValid;
    }

    /**
     * Sets the value of the isValid property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIsValid(String value) {
        this.isValid = value;
    }

    /**
     * Gets the value of the createHITRequest property.
     * 
     * @return
     *     possible object is
     *     {@link CreateHITRequest }
     *     
     */
    public CreateHITRequest getCreateHITRequest() {
        return createHITRequest;
    }

    /**
     * Sets the value of the createHITRequest property.
     * 
     * @param value
     *     allowed object is
     *     {@link CreateHITRequest }
     *     
     */
    public void setCreateHITRequest(CreateHITRequest value) {
        this.createHITRequest = value;
    }

    /**
     * Gets the value of the registerHITTypeRequest property.
     * 
     * @return
     *     possible object is
     *     {@link RegisterHITTypeRequest }
     *     
     */
    public RegisterHITTypeRequest getRegisterHITTypeRequest() {
        return registerHITTypeRequest;
    }

    /**
     * Sets the value of the registerHITTypeRequest property.
     * 
     * @param value
     *     allowed object is
     *     {@link RegisterHITTypeRequest }
     *     
     */
    public void setRegisterHITTypeRequest(RegisterHITTypeRequest value) {
        this.registerHITTypeRequest = value;
    }

    /**
     * Gets the value of the disposeHITRequest property.
     * 
     * @return
     *     possible object is
     *     {@link DisposeHITRequest }
     *     
     */
    public DisposeHITRequest getDisposeHITRequest() {
        return disposeHITRequest;
    }

    /**
     * Sets the value of the disposeHITRequest property.
     * 
     * @param value
     *     allowed object is
     *     {@link DisposeHITRequest }
     *     
     */
    public void setDisposeHITRequest(DisposeHITRequest value) {
        this.disposeHITRequest = value;
    }

    /**
     * Gets the value of the disableHITRequest property.
     * 
     * @return
     *     possible object is
     *     {@link DisableHITRequest }
     *     
     */
    public DisableHITRequest getDisableHITRequest() {
        return disableHITRequest;
    }

    /**
     * Sets the value of the disableHITRequest property.
     * 
     * @param value
     *     allowed object is
     *     {@link DisableHITRequest }
     *     
     */
    public void setDisableHITRequest(DisableHITRequest value) {
        this.disableHITRequest = value;
    }

    /**
     * Gets the value of the getHITRequest property.
     * 
     * @return
     *     possible object is
     *     {@link GetHITRequest }
     *     
     */
    public GetHITRequest getGetHITRequest() {
        return getHITRequest;
    }

    /**
     * Sets the value of the getHITRequest property.
     * 
     * @param value
     *     allowed object is
     *     {@link GetHITRequest }
     *     
     */
    public void setGetHITRequest(GetHITRequest value) {
        this.getHITRequest = value;
    }

    /**
     * Gets the value of the getAssignmentRequest property.
     * 
     * @return
     *     possible object is
     *     {@link GetAssignmentRequest }
     *     
     */
    public GetAssignmentRequest getGetAssignmentRequest() {
        return getAssignmentRequest;
    }

    /**
     * Sets the value of the getAssignmentRequest property.
     * 
     * @param value
     *     allowed object is
     *     {@link GetAssignmentRequest }
     *     
     */
    public void setGetAssignmentRequest(GetAssignmentRequest value) {
        this.getAssignmentRequest = value;
    }

    /**
     * Gets the value of the getReviewResultsForHITRequest property.
     * 
     * @return
     *     possible object is
     *     {@link GetReviewResultsForHITRequest }
     *     
     */
    public GetReviewResultsForHITRequest getGetReviewResultsForHITRequest() {
        return getReviewResultsForHITRequest;
    }

    /**
     * Sets the value of the getReviewResultsForHITRequest property.
     * 
     * @param value
     *     allowed object is
     *     {@link GetReviewResultsForHITRequest }
     *     
     */
    public void setGetReviewResultsForHITRequest(GetReviewResultsForHITRequest value) {
        this.getReviewResultsForHITRequest = value;
    }

    /**
     * Gets the value of the getReviewableHITsRequest property.
     * 
     * @return
     *     possible object is
     *     {@link GetReviewableHITsRequest }
     *     
     */
    public GetReviewableHITsRequest getGetReviewableHITsRequest() {
        return getReviewableHITsRequest;
    }

    /**
     * Sets the value of the getReviewableHITsRequest property.
     * 
     * @param value
     *     allowed object is
     *     {@link GetReviewableHITsRequest }
     *     
     */
    public void setGetReviewableHITsRequest(GetReviewableHITsRequest value) {
        this.getReviewableHITsRequest = value;
    }

    /**
     * Gets the value of the getHITsForQualificationTypeRequest property.
     * 
     * @return
     *     possible object is
     *     {@link GetHITsForQualificationTypeRequest }
     *     
     */
    public GetHITsForQualificationTypeRequest getGetHITsForQualificationTypeRequest() {
        return getHITsForQualificationTypeRequest;
    }

    /**
     * Sets the value of the getHITsForQualificationTypeRequest property.
     * 
     * @param value
     *     allowed object is
     *     {@link GetHITsForQualificationTypeRequest }
     *     
     */
    public void setGetHITsForQualificationTypeRequest(GetHITsForQualificationTypeRequest value) {
        this.getHITsForQualificationTypeRequest = value;
    }

    /**
     * Gets the value of the getQualificationsForQualificationTypeRequest property.
     * 
     * @return
     *     possible object is
     *     {@link GetQualificationsForQualificationTypeRequest }
     *     
     */
    public GetQualificationsForQualificationTypeRequest getGetQualificationsForQualificationTypeRequest() {
        return getQualificationsForQualificationTypeRequest;
    }

    /**
     * Sets the value of the getQualificationsForQualificationTypeRequest property.
     * 
     * @param value
     *     allowed object is
     *     {@link GetQualificationsForQualificationTypeRequest }
     *     
     */
    public void setGetQualificationsForQualificationTypeRequest(GetQualificationsForQualificationTypeRequest value) {
        this.getQualificationsForQualificationTypeRequest = value;
    }

    /**
     * Gets the value of the setHITAsReviewingRequest property.
     * 
     * @return
     *     possible object is
     *     {@link SetHITAsReviewingRequest }
     *     
     */
    public SetHITAsReviewingRequest getSetHITAsReviewingRequest() {
        return setHITAsReviewingRequest;
    }

    /**
     * Sets the value of the setHITAsReviewingRequest property.
     * 
     * @param value
     *     allowed object is
     *     {@link SetHITAsReviewingRequest }
     *     
     */
    public void setSetHITAsReviewingRequest(SetHITAsReviewingRequest value) {
        this.setHITAsReviewingRequest = value;
    }

    /**
     * Gets the value of the searchHITsRequest property.
     * 
     * @return
     *     possible object is
     *     {@link SearchHITsRequest }
     *     
     */
    public SearchHITsRequest getSearchHITsRequest() {
        return searchHITsRequest;
    }

    /**
     * Sets the value of the searchHITsRequest property.
     * 
     * @param value
     *     allowed object is
     *     {@link SearchHITsRequest }
     *     
     */
    public void setSearchHITsRequest(SearchHITsRequest value) {
        this.searchHITsRequest = value;
    }

    /**
     * Gets the value of the extendHITRequest property.
     * 
     * @return
     *     possible object is
     *     {@link ExtendHITRequest }
     *     
     */
    public ExtendHITRequest getExtendHITRequest() {
        return extendHITRequest;
    }

    /**
     * Sets the value of the extendHITRequest property.
     * 
     * @param value
     *     allowed object is
     *     {@link ExtendHITRequest }
     *     
     */
    public void setExtendHITRequest(ExtendHITRequest value) {
        this.extendHITRequest = value;
    }

    /**
     * Gets the value of the forceExpireHITRequest property.
     * 
     * @return
     *     possible object is
     *     {@link ForceExpireHITRequest }
     *     
     */
    public ForceExpireHITRequest getForceExpireHITRequest() {
        return forceExpireHITRequest;
    }

    /**
     * Sets the value of the forceExpireHITRequest property.
     * 
     * @param value
     *     allowed object is
     *     {@link ForceExpireHITRequest }
     *     
     */
    public void setForceExpireHITRequest(ForceExpireHITRequest value) {
        this.forceExpireHITRequest = value;
    }

    /**
     * Gets the value of the changeHITTypeOfHITRequest property.
     * 
     * @return
     *     possible object is
     *     {@link ChangeHITTypeOfHITRequest }
     *     
     */
    public ChangeHITTypeOfHITRequest getChangeHITTypeOfHITRequest() {
        return changeHITTypeOfHITRequest;
    }

    /**
     * Sets the value of the changeHITTypeOfHITRequest property.
     * 
     * @param value
     *     allowed object is
     *     {@link ChangeHITTypeOfHITRequest }
     *     
     */
    public void setChangeHITTypeOfHITRequest(ChangeHITTypeOfHITRequest value) {
        this.changeHITTypeOfHITRequest = value;
    }

    /**
     * Gets the value of the createQualificationTypeRequest property.
     * 
     * @return
     *     possible object is
     *     {@link CreateQualificationTypeRequest }
     *     
     */
    public CreateQualificationTypeRequest getCreateQualificationTypeRequest() {
        return createQualificationTypeRequest;
    }

    /**
     * Sets the value of the createQualificationTypeRequest property.
     * 
     * @param value
     *     allowed object is
     *     {@link CreateQualificationTypeRequest }
     *     
     */
    public void setCreateQualificationTypeRequest(CreateQualificationTypeRequest value) {
        this.createQualificationTypeRequest = value;
    }

    /**
     * Gets the value of the disposeQualificationTypeRequest property.
     * 
     * @return
     *     possible object is
     *     {@link DisposeQualificationTypeRequest }
     *     
     */
    public DisposeQualificationTypeRequest getDisposeQualificationTypeRequest() {
        return disposeQualificationTypeRequest;
    }

    /**
     * Sets the value of the disposeQualificationTypeRequest property.
     * 
     * @param value
     *     allowed object is
     *     {@link DisposeQualificationTypeRequest }
     *     
     */
    public void setDisposeQualificationTypeRequest(DisposeQualificationTypeRequest value) {
        this.disposeQualificationTypeRequest = value;
    }

    /**
     * Gets the value of the grantQualificationRequest property.
     * 
     * @return
     *     possible object is
     *     {@link GrantQualificationRequest }
     *     
     */
    public GrantQualificationRequest getGrantQualificationRequest() {
        return grantQualificationRequest;
    }

    /**
     * Sets the value of the grantQualificationRequest property.
     * 
     * @param value
     *     allowed object is
     *     {@link GrantQualificationRequest }
     *     
     */
    public void setGrantQualificationRequest(GrantQualificationRequest value) {
        this.grantQualificationRequest = value;
    }

    /**
     * Gets the value of the assignQualificationRequest property.
     * 
     * @return
     *     possible object is
     *     {@link AssignQualificationRequest }
     *     
     */
    public AssignQualificationRequest getAssignQualificationRequest() {
        return assignQualificationRequest;
    }

    /**
     * Sets the value of the assignQualificationRequest property.
     * 
     * @param value
     *     allowed object is
     *     {@link AssignQualificationRequest }
     *     
     */
    public void setAssignQualificationRequest(AssignQualificationRequest value) {
        this.assignQualificationRequest = value;
    }

    /**
     * Gets the value of the revokeQualificationRequest property.
     * 
     * @return
     *     possible object is
     *     {@link RevokeQualificationRequest }
     *     
     */
    public RevokeQualificationRequest getRevokeQualificationRequest() {
        return revokeQualificationRequest;
    }

    /**
     * Sets the value of the revokeQualificationRequest property.
     * 
     * @param value
     *     allowed object is
     *     {@link RevokeQualificationRequest }
     *     
     */
    public void setRevokeQualificationRequest(RevokeQualificationRequest value) {
        this.revokeQualificationRequest = value;
    }

    /**
     * Gets the value of the getQualificationRequestsRequest property.
     * 
     * @return
     *     possible object is
     *     {@link GetQualificationRequestsRequest }
     *     
     */
    public GetQualificationRequestsRequest getGetQualificationRequestsRequest() {
        return getQualificationRequestsRequest;
    }

    /**
     * Sets the value of the getQualificationRequestsRequest property.
     * 
     * @param value
     *     allowed object is
     *     {@link GetQualificationRequestsRequest }
     *     
     */
    public void setGetQualificationRequestsRequest(GetQualificationRequestsRequest value) {
        this.getQualificationRequestsRequest = value;
    }

    /**
     * Gets the value of the rejectQualificationRequestRequest property.
     * 
     * @return
     *     possible object is
     *     {@link RejectQualificationRequestRequest }
     *     
     */
    public RejectQualificationRequestRequest getRejectQualificationRequestRequest() {
        return rejectQualificationRequestRequest;
    }

    /**
     * Sets the value of the rejectQualificationRequestRequest property.
     * 
     * @param value
     *     allowed object is
     *     {@link RejectQualificationRequestRequest }
     *     
     */
    public void setRejectQualificationRequestRequest(RejectQualificationRequestRequest value) {
        this.rejectQualificationRequestRequest = value;
    }

    /**
     * Gets the value of the getQualificationTypeRequest property.
     * 
     * @return
     *     possible object is
     *     {@link GetQualificationTypeRequest }
     *     
     */
    public GetQualificationTypeRequest getGetQualificationTypeRequest() {
        return getQualificationTypeRequest;
    }

    /**
     * Sets the value of the getQualificationTypeRequest property.
     * 
     * @param value
     *     allowed object is
     *     {@link GetQualificationTypeRequest }
     *     
     */
    public void setGetQualificationTypeRequest(GetQualificationTypeRequest value) {
        this.getQualificationTypeRequest = value;
    }

    /**
     * Gets the value of the searchQualificationTypesRequest property.
     * 
     * @return
     *     possible object is
     *     {@link SearchQualificationTypesRequest }
     *     
     */
    public SearchQualificationTypesRequest getSearchQualificationTypesRequest() {
        return searchQualificationTypesRequest;
    }

    /**
     * Sets the value of the searchQualificationTypesRequest property.
     * 
     * @param value
     *     allowed object is
     *     {@link SearchQualificationTypesRequest }
     *     
     */
    public void setSearchQualificationTypesRequest(SearchQualificationTypesRequest value) {
        this.searchQualificationTypesRequest = value;
    }

    /**
     * Gets the value of the updateQualificationTypeRequest property.
     * 
     * @return
     *     possible object is
     *     {@link UpdateQualificationTypeRequest }
     *     
     */
    public UpdateQualificationTypeRequest getUpdateQualificationTypeRequest() {
        return updateQualificationTypeRequest;
    }

    /**
     * Sets the value of the updateQualificationTypeRequest property.
     * 
     * @param value
     *     allowed object is
     *     {@link UpdateQualificationTypeRequest }
     *     
     */
    public void setUpdateQualificationTypeRequest(UpdateQualificationTypeRequest value) {
        this.updateQualificationTypeRequest = value;
    }

    /**
     * Gets the value of the approveAssignmentRequest property.
     * 
     * @return
     *     possible object is
     *     {@link ApproveAssignmentRequest }
     *     
     */
    public ApproveAssignmentRequest getApproveAssignmentRequest() {
        return approveAssignmentRequest;
    }

    /**
     * Sets the value of the approveAssignmentRequest property.
     * 
     * @param value
     *     allowed object is
     *     {@link ApproveAssignmentRequest }
     *     
     */
    public void setApproveAssignmentRequest(ApproveAssignmentRequest value) {
        this.approveAssignmentRequest = value;
    }

    /**
     * Gets the value of the rejectAssignmentRequest property.
     * 
     * @return
     *     possible object is
     *     {@link RejectAssignmentRequest }
     *     
     */
    public RejectAssignmentRequest getRejectAssignmentRequest() {
        return rejectAssignmentRequest;
    }

    /**
     * Sets the value of the rejectAssignmentRequest property.
     * 
     * @param value
     *     allowed object is
     *     {@link RejectAssignmentRequest }
     *     
     */
    public void setRejectAssignmentRequest(RejectAssignmentRequest value) {
        this.rejectAssignmentRequest = value;
    }

    /**
     * Gets the value of the approveRejectedAssignmentRequest property.
     * 
     * @return
     *     possible object is
     *     {@link ApproveRejectedAssignmentRequest }
     *     
     */
    public ApproveRejectedAssignmentRequest getApproveRejectedAssignmentRequest() {
        return approveRejectedAssignmentRequest;
    }

    /**
     * Sets the value of the approveRejectedAssignmentRequest property.
     * 
     * @param value
     *     allowed object is
     *     {@link ApproveRejectedAssignmentRequest }
     *     
     */
    public void setApproveRejectedAssignmentRequest(ApproveRejectedAssignmentRequest value) {
        this.approveRejectedAssignmentRequest = value;
    }

    /**
     * Gets the value of the getAssignmentsForHIT property.
     * 
     * @return
     *     possible object is
     *     {@link GetAssignmentsForHITRequest }
     *     
     */
    public GetAssignmentsForHITRequest getGetAssignmentsForHIT() {
        return getAssignmentsForHIT;
    }

    /**
     * Sets the value of the getAssignmentsForHIT property.
     * 
     * @param value
     *     allowed object is
     *     {@link GetAssignmentsForHITRequest }
     *     
     */
    public void setGetAssignmentsForHIT(GetAssignmentsForHITRequest value) {
        this.getAssignmentsForHIT = value;
    }

    /**
     * Gets the value of the getFileUploadURL property.
     * 
     * @return
     *     possible object is
     *     {@link GetFileUploadURLRequest }
     *     
     */
    public GetFileUploadURLRequest getGetFileUploadURL() {
        return getFileUploadURL;
    }

    /**
     * Sets the value of the getFileUploadURL property.
     * 
     * @param value
     *     allowed object is
     *     {@link GetFileUploadURLRequest }
     *     
     */
    public void setGetFileUploadURL(GetFileUploadURLRequest value) {
        this.getFileUploadURL = value;
    }

    /**
     * Gets the value of the grantBonusRequest property.
     * 
     * @return
     *     possible object is
     *     {@link GrantBonusRequest }
     *     
     */
    public GrantBonusRequest getGrantBonusRequest() {
        return grantBonusRequest;
    }

    /**
     * Sets the value of the grantBonusRequest property.
     * 
     * @param value
     *     allowed object is
     *     {@link GrantBonusRequest }
     *     
     */
    public void setGrantBonusRequest(GrantBonusRequest value) {
        this.grantBonusRequest = value;
    }

    /**
     * Gets the value of the getBonusPaymentsRequest property.
     * 
     * @return
     *     possible object is
     *     {@link GetBonusPaymentsRequest }
     *     
     */
    public GetBonusPaymentsRequest getGetBonusPaymentsRequest() {
        return getBonusPaymentsRequest;
    }

    /**
     * Sets the value of the getBonusPaymentsRequest property.
     * 
     * @param value
     *     allowed object is
     *     {@link GetBonusPaymentsRequest }
     *     
     */
    public void setGetBonusPaymentsRequest(GetBonusPaymentsRequest value) {
        this.getBonusPaymentsRequest = value;
    }

    /**
     * Gets the value of the getAccountBalanceRequest property.
     * 
     * @return
     *     possible object is
     *     {@link GetAccountBalanceRequest }
     *     
     */
    public GetAccountBalanceRequest getGetAccountBalanceRequest() {
        return getAccountBalanceRequest;
    }

    /**
     * Sets the value of the getAccountBalanceRequest property.
     * 
     * @param value
     *     allowed object is
     *     {@link GetAccountBalanceRequest }
     *     
     */
    public void setGetAccountBalanceRequest(GetAccountBalanceRequest value) {
        this.getAccountBalanceRequest = value;
    }

    /**
     * Gets the value of the notifyWorkersRequest property.
     * 
     * @return
     *     possible object is
     *     {@link NotifyWorkersRequest }
     *     
     */
    public NotifyWorkersRequest getNotifyWorkersRequest() {
        return notifyWorkersRequest;
    }

    /**
     * Sets the value of the notifyWorkersRequest property.
     * 
     * @param value
     *     allowed object is
     *     {@link NotifyWorkersRequest }
     *     
     */
    public void setNotifyWorkersRequest(NotifyWorkersRequest value) {
        this.notifyWorkersRequest = value;
    }

    /**
     * Gets the value of the getBlockedWorkersRequest property.
     * 
     * @return
     *     possible object is
     *     {@link GetBlockedWorkersRequest }
     *     
     */
    public GetBlockedWorkersRequest getGetBlockedWorkersRequest() {
        return getBlockedWorkersRequest;
    }

    /**
     * Sets the value of the getBlockedWorkersRequest property.
     * 
     * @param value
     *     allowed object is
     *     {@link GetBlockedWorkersRequest }
     *     
     */
    public void setGetBlockedWorkersRequest(GetBlockedWorkersRequest value) {
        this.getBlockedWorkersRequest = value;
    }

    /**
     * Gets the value of the blockWorkerRequest property.
     * 
     * @return
     *     possible object is
     *     {@link BlockWorkerRequest }
     *     
     */
    public BlockWorkerRequest getBlockWorkerRequest() {
        return blockWorkerRequest;
    }

    /**
     * Sets the value of the blockWorkerRequest property.
     * 
     * @param value
     *     allowed object is
     *     {@link BlockWorkerRequest }
     *     
     */
    public void setBlockWorkerRequest(BlockWorkerRequest value) {
        this.blockWorkerRequest = value;
    }

    /**
     * Gets the value of the unblockWorkerRequest property.
     * 
     * @return
     *     possible object is
     *     {@link UnblockWorkerRequest }
     *     
     */
    public UnblockWorkerRequest getUnblockWorkerRequest() {
        return unblockWorkerRequest;
    }

    /**
     * Sets the value of the unblockWorkerRequest property.
     * 
     * @param value
     *     allowed object is
     *     {@link UnblockWorkerRequest }
     *     
     */
    public void setUnblockWorkerRequest(UnblockWorkerRequest value) {
        this.unblockWorkerRequest = value;
    }

    /**
     * Gets the value of the getRequesterStatistic property.
     * 
     * @return
     *     possible object is
     *     {@link GetRequesterStatisticRequest }
     *     
     */
    public GetRequesterStatisticRequest getGetRequesterStatistic() {
        return getRequesterStatistic;
    }

    /**
     * Sets the value of the getRequesterStatistic property.
     * 
     * @param value
     *     allowed object is
     *     {@link GetRequesterStatisticRequest }
     *     
     */
    public void setGetRequesterStatistic(GetRequesterStatisticRequest value) {
        this.getRequesterStatistic = value;
    }

    /**
     * Gets the value of the getRequesterWorkerStatistic property.
     * 
     * @return
     *     possible object is
     *     {@link GetRequesterWorkerStatisticRequest }
     *     
     */
    public GetRequesterWorkerStatisticRequest getGetRequesterWorkerStatistic() {
        return getRequesterWorkerStatistic;
    }

    /**
     * Sets the value of the getRequesterWorkerStatistic property.
     * 
     * @param value
     *     allowed object is
     *     {@link GetRequesterWorkerStatisticRequest }
     *     
     */
    public void setGetRequesterWorkerStatistic(GetRequesterWorkerStatisticRequest value) {
        this.getRequesterWorkerStatistic = value;
    }

    /**
     * Gets the value of the helpRequest property.
     * 
     * @return
     *     possible object is
     *     {@link HelpRequest }
     *     
     */
    public HelpRequest getHelpRequest() {
        return helpRequest;
    }

    /**
     * Sets the value of the helpRequest property.
     * 
     * @param value
     *     allowed object is
     *     {@link HelpRequest }
     *     
     */
    public void setHelpRequest(HelpRequest value) {
        this.helpRequest = value;
    }

    /**
     * Gets the value of the errors property.
     * 
     * @return
     *     possible object is
     *     {@link Errors }
     *     
     */
    public Errors getErrors() {
        return errors;
    }

    /**
     * Sets the value of the errors property.
     * 
     * @param value
     *     allowed object is
     *     {@link Errors }
     *     
     */
    public void setErrors(Errors value) {
        this.errors = value;
    }

}
