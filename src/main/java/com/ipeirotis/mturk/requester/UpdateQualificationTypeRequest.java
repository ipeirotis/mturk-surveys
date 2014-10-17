
package com.ipeirotis.mturk.requester;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for UpdateQualificationTypeRequest complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="UpdateQualificationTypeRequest">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="QualificationTypeId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Description" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="QualificationTypeStatus" type="{http://requester.mturk.amazonaws.com/doc/2013-11-15}QualificationTypeStatus" minOccurs="0"/>
 *         &lt;element name="Test" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="AnswerKey" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="TestDurationInSeconds" type="{http://www.w3.org/2001/XMLSchema}long" minOccurs="0"/>
 *         &lt;element name="RetryDelayInSeconds" type="{http://www.w3.org/2001/XMLSchema}long" minOccurs="0"/>
 *         &lt;element name="AutoGranted" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="AutoGrantedValue" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
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
@XmlType(name = "UpdateQualificationTypeRequest", propOrder = {
    "qualificationTypeId",
    "description",
    "qualificationTypeStatus",
    "test",
    "answerKey",
    "testDurationInSeconds",
    "retryDelayInSeconds",
    "autoGranted",
    "autoGrantedValue",
    "responseGroup"
})
public class UpdateQualificationTypeRequest {

    @XmlElement(name = "QualificationTypeId", required = true)
    protected String qualificationTypeId;
    @XmlElement(name = "Description")
    protected String description;
    @XmlElement(name = "QualificationTypeStatus")
    protected QualificationTypeStatus qualificationTypeStatus;
    @XmlElement(name = "Test")
    protected String test;
    @XmlElement(name = "AnswerKey")
    protected String answerKey;
    @XmlElement(name = "TestDurationInSeconds")
    protected Long testDurationInSeconds;
    @XmlElement(name = "RetryDelayInSeconds")
    protected Long retryDelayInSeconds;
    @XmlElement(name = "AutoGranted")
    protected Boolean autoGranted;
    @XmlElement(name = "AutoGrantedValue")
    protected Integer autoGrantedValue;
    @XmlElement(name = "ResponseGroup")
    protected List<String> responseGroup;

    /**
     * Gets the value of the qualificationTypeId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getQualificationTypeId() {
        return qualificationTypeId;
    }

    /**
     * Sets the value of the qualificationTypeId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setQualificationTypeId(String value) {
        this.qualificationTypeId = value;
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
     * Gets the value of the qualificationTypeStatus property.
     * 
     * @return
     *     possible object is
     *     {@link QualificationTypeStatus }
     *     
     */
    public QualificationTypeStatus getQualificationTypeStatus() {
        return qualificationTypeStatus;
    }

    /**
     * Sets the value of the qualificationTypeStatus property.
     * 
     * @param value
     *     allowed object is
     *     {@link QualificationTypeStatus }
     *     
     */
    public void setQualificationTypeStatus(QualificationTypeStatus value) {
        this.qualificationTypeStatus = value;
    }

    /**
     * Gets the value of the test property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTest() {
        return test;
    }

    /**
     * Sets the value of the test property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTest(String value) {
        this.test = value;
    }

    /**
     * Gets the value of the answerKey property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAnswerKey() {
        return answerKey;
    }

    /**
     * Sets the value of the answerKey property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAnswerKey(String value) {
        this.answerKey = value;
    }

    /**
     * Gets the value of the testDurationInSeconds property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getTestDurationInSeconds() {
        return testDurationInSeconds;
    }

    /**
     * Sets the value of the testDurationInSeconds property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setTestDurationInSeconds(Long value) {
        this.testDurationInSeconds = value;
    }

    /**
     * Gets the value of the retryDelayInSeconds property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getRetryDelayInSeconds() {
        return retryDelayInSeconds;
    }

    /**
     * Sets the value of the retryDelayInSeconds property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setRetryDelayInSeconds(Long value) {
        this.retryDelayInSeconds = value;
    }

    /**
     * Gets the value of the autoGranted property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isAutoGranted() {
        return autoGranted;
    }

    /**
     * Sets the value of the autoGranted property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setAutoGranted(Boolean value) {
        this.autoGranted = value;
    }

    /**
     * Gets the value of the autoGrantedValue property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getAutoGrantedValue() {
        return autoGrantedValue;
    }

    /**
     * Sets the value of the autoGrantedValue property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setAutoGrantedValue(Integer value) {
        this.autoGrantedValue = value;
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
