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
 * <p>Java class for ApplicationContentType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ApplicationContentType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice>
 *         &lt;element name="Flash" type="{http://mechanicalturk.amazonaws.com/AWSMechanicalTurkDataSchemas/2005-10-01/QuestionForm.xsd}FlashContentType"/>
 *         &lt;element name="JavaApplet" type="{http://mechanicalturk.amazonaws.com/AWSMechanicalTurkDataSchemas/2005-10-01/QuestionForm.xsd}JavaAppletContentType"/>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ApplicationContentType", namespace = "http://mechanicalturk.amazonaws.com/AWSMechanicalTurkDataSchemas/2005-10-01/QuestionForm.xsd", propOrder = {
    "flash",
    "javaApplet"
})
public class ApplicationContentType {

    @XmlElement(name = "Flash", namespace = "http://mechanicalturk.amazonaws.com/AWSMechanicalTurkDataSchemas/2005-10-01/QuestionForm.xsd")
    protected FlashContentType flash;
    @XmlElement(name = "JavaApplet", namespace = "http://mechanicalturk.amazonaws.com/AWSMechanicalTurkDataSchemas/2005-10-01/QuestionForm.xsd")
    protected JavaAppletContentType javaApplet;

    /**
     * Gets the value of the flash property.
     * 
     * @return
     *     possible object is
     *     {@link FlashContentType }
     *     
     */
    public FlashContentType getFlash() {
        return flash;
    }

    /**
     * Sets the value of the flash property.
     * 
     * @param value
     *     allowed object is
     *     {@link FlashContentType }
     *     
     */
    public void setFlash(FlashContentType value) {
        this.flash = value;
    }

    /**
     * Gets the value of the javaApplet property.
     * 
     * @return
     *     possible object is
     *     {@link JavaAppletContentType }
     *     
     */
    public JavaAppletContentType getJavaApplet() {
        return javaApplet;
    }

    /**
     * Sets the value of the javaApplet property.
     * 
     * @param value
     *     allowed object is
     *     {@link JavaAppletContentType }
     *     
     */
    public void setJavaApplet(JavaAppletContentType value) {
        this.javaApplet = value;
    }

}
