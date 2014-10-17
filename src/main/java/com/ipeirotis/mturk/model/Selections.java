//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.10.14 at 07:51:40 PM GMT+03:00 
//


package com.ipeirotis.mturk.model;

import java.util.ArrayList;
import java.util.List;
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
 *         &lt;element name="Selection" maxOccurs="unbounded">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="SelectionIdentifier" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                   &lt;choice>
 *                     &lt;element name="Text" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                     &lt;element name="Binary" type="{http://mechanicalturk.amazonaws.com/AWSMechanicalTurkDataSchemas/2005-10-01/QuestionForm.xsd}BinaryContentType"/>
 *                     &lt;element name="FormattedContent" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                   &lt;/choice>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="OtherSelection" type="{http://mechanicalturk.amazonaws.com/AWSMechanicalTurkDataSchemas/2005-10-01/QuestionForm.xsd}FreeTextAnswerType" minOccurs="0"/>
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
    "selection",
    "otherSelection"
})
public class Selections {

    @XmlElement(name = "Selection", required = true)
    protected List<Selection> selection;
    @XmlElement(name = "OtherSelection")
    protected FreeTextAnswerType otherSelection;

    /**
     * Gets the value of the selection property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the selection property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSelection().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Selection }
     * 
     * 
     */
    public List<Selection> getSelection() {
        if (selection == null) {
            selection = new ArrayList<Selection>();
        }
        return this.selection;
    }

    /**
     * Gets the value of the otherSelection property.
     * 
     * @return
     *     possible object is
     *     {@link FreeTextAnswerType }
     *     
     */
    public FreeTextAnswerType getOtherSelection() {
        return otherSelection;
    }

    /**
     * Sets the value of the otherSelection property.
     * 
     * @param value
     *     allowed object is
     *     {@link FreeTextAnswerType }
     *     
     */
    public void setOtherSelection(FreeTextAnswerType value) {
        this.otherSelection = value;
    }

}
