//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.10.22 at 11:24:01 AM GMT+03:00 
//


package com.ipeirotis.mturk.model;

import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for SelectionAnswerType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="SelectionAnswerType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="MinSelectionCount" type="{http://www.w3.org/2001/XMLSchema}nonNegativeInteger" minOccurs="0"/>
 *         &lt;element name="MaxSelectionCount" type="{http://www.w3.org/2001/XMLSchema}positiveInteger" minOccurs="0"/>
 *         &lt;element name="StyleSuggestion" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;enumeration value="radiobutton"/>
 *               &lt;enumeration value="checkbox"/>
 *               &lt;enumeration value="list"/>
 *               &lt;enumeration value="dropdown"/>
 *               &lt;enumeration value="combobox"/>
 *               &lt;enumeration value="multichooser"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="Selections">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="Selection" maxOccurs="unbounded">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;sequence>
 *                             &lt;element name="SelectionIdentifier" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                             &lt;choice>
 *                               &lt;element name="Text" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                               &lt;element name="Binary" type="{http://mechanicalturk.amazonaws.com/AWSMechanicalTurkDataSchemas/2005-10-01/QuestionForm.xsd}BinaryContentType"/>
 *                               &lt;element name="FormattedContent" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                             &lt;/choice>
 *                           &lt;/sequence>
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                   &lt;element name="OtherSelection" type="{http://mechanicalturk.amazonaws.com/AWSMechanicalTurkDataSchemas/2005-10-01/QuestionForm.xsd}FreeTextAnswerType" minOccurs="0"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SelectionAnswerType", namespace = "http://mechanicalturk.amazonaws.com/AWSMechanicalTurkDataSchemas/2005-10-01/QuestionForm.xsd", propOrder = {
    "minSelectionCount",
    "maxSelectionCount",
    "styleSuggestion",
    "selections"
})
public class SelectionAnswerType {

    @XmlElement(name = "MinSelectionCount", namespace = "http://mechanicalturk.amazonaws.com/AWSMechanicalTurkDataSchemas/2005-10-01/QuestionForm.xsd")
    @XmlSchemaType(name = "nonNegativeInteger")
    protected BigInteger minSelectionCount;
    @XmlElement(name = "MaxSelectionCount", namespace = "http://mechanicalturk.amazonaws.com/AWSMechanicalTurkDataSchemas/2005-10-01/QuestionForm.xsd")
    @XmlSchemaType(name = "positiveInteger")
    protected BigInteger maxSelectionCount;
    @XmlElement(name = "StyleSuggestion", namespace = "http://mechanicalturk.amazonaws.com/AWSMechanicalTurkDataSchemas/2005-10-01/QuestionForm.xsd")
    protected String styleSuggestion;
    @XmlElement(name = "Selections", namespace = "http://mechanicalturk.amazonaws.com/AWSMechanicalTurkDataSchemas/2005-10-01/QuestionForm.xsd", required = true)
    protected Selections selections;

    /**
     * Gets the value of the minSelectionCount property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getMinSelectionCount() {
        return minSelectionCount;
    }

    /**
     * Sets the value of the minSelectionCount property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setMinSelectionCount(BigInteger value) {
        this.minSelectionCount = value;
    }

    /**
     * Gets the value of the maxSelectionCount property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getMaxSelectionCount() {
        return maxSelectionCount;
    }

    /**
     * Sets the value of the maxSelectionCount property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setMaxSelectionCount(BigInteger value) {
        this.maxSelectionCount = value;
    }

    /**
     * Gets the value of the styleSuggestion property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStyleSuggestion() {
        return styleSuggestion;
    }

    /**
     * Sets the value of the styleSuggestion property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStyleSuggestion(String value) {
        this.styleSuggestion = value;
    }

    /**
     * Gets the value of the selections property.
     * 
     * @return
     *     possible object is
     *     {@link Selections }
     *     
     */
    public Selections getSelections() {
        return selections;
    }

    /**
     * Sets the value of the selections property.
     * 
     * @param value
     *     allowed object is
     *     {@link Selections }
     *     
     */
    public void setSelections(Selections value) {
        this.selections = value;
    }

}
