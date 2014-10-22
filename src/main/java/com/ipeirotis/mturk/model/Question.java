//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.10.22 at 11:24:01 AM GMT+03:00 
//


package com.ipeirotis.mturk.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
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
 *         &lt;element name="QuestionIdentifier" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="DisplayName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="IsRequired" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="QuestionContent" type="{http://mechanicalturk.amazonaws.com/AWSMechanicalTurkDataSchemas/2005-10-01/QuestionForm.xsd}ContentType"/>
 *         &lt;element name="AnswerSpecification" type="{http://mechanicalturk.amazonaws.com/AWSMechanicalTurkDataSchemas/2005-10-01/QuestionForm.xsd}AnswerSpecificationType"/>
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
    "questionIdentifier",
    "displayName",
    "isRequired",
    "questionContent",
    "answerSpecification"
})
public class Question {

    @XmlElement(name = "QuestionIdentifier", namespace = "http://mechanicalturk.amazonaws.com/AWSMechanicalTurkDataSchemas/2005-10-01/QuestionForm.xsd", required = true)
    protected String questionIdentifier;
    @XmlElement(name = "DisplayName", namespace = "http://mechanicalturk.amazonaws.com/AWSMechanicalTurkDataSchemas/2005-10-01/QuestionForm.xsd")
    protected String displayName;
    @XmlElement(name = "IsRequired", namespace = "http://mechanicalturk.amazonaws.com/AWSMechanicalTurkDataSchemas/2005-10-01/QuestionForm.xsd")
    protected Boolean isRequired;
    @XmlElement(name = "QuestionContent", namespace = "http://mechanicalturk.amazonaws.com/AWSMechanicalTurkDataSchemas/2005-10-01/QuestionForm.xsd", required = true)
    protected ContentType questionContent;
    @XmlElement(name = "AnswerSpecification", namespace = "http://mechanicalturk.amazonaws.com/AWSMechanicalTurkDataSchemas/2005-10-01/QuestionForm.xsd", required = true)
    protected AnswerSpecificationType answerSpecification;

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

    /**
     * Gets the value of the displayName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Sets the value of the displayName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDisplayName(String value) {
        this.displayName = value;
    }

    /**
     * Gets the value of the isRequired property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isIsRequired() {
        return isRequired;
    }

    /**
     * Sets the value of the isRequired property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIsRequired(Boolean value) {
        this.isRequired = value;
    }

    /**
     * Gets the value of the questionContent property.
     * 
     * @return
     *     possible object is
     *     {@link ContentType }
     *     
     */
    public ContentType getQuestionContent() {
        return questionContent;
    }

    /**
     * Sets the value of the questionContent property.
     * 
     * @param value
     *     allowed object is
     *     {@link ContentType }
     *     
     */
    public void setQuestionContent(ContentType value) {
        this.questionContent = value;
    }

    /**
     * Gets the value of the answerSpecification property.
     * 
     * @return
     *     possible object is
     *     {@link AnswerSpecificationType }
     *     
     */
    public AnswerSpecificationType getAnswerSpecification() {
        return answerSpecification;
    }

    /**
     * Sets the value of the answerSpecification property.
     * 
     * @param value
     *     allowed object is
     *     {@link AnswerSpecificationType }
     *     
     */
    public void setAnswerSpecification(AnswerSpecificationType value) {
        this.answerSpecification = value;
    }

}
