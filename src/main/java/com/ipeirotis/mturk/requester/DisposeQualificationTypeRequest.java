
package com.ipeirotis.mturk.requester;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for DisposeQualificationTypeRequest complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="DisposeQualificationTypeRequest">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="QualificationTypeId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DisposeQualificationTypeRequest", propOrder = {
    "qualificationTypeId"
})
public class DisposeQualificationTypeRequest {

    @XmlElement(name = "QualificationTypeId")
    protected String qualificationTypeId;

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

}
