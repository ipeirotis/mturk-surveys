
package com.ipeirotis.mturk.requester;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for QualificationRequirement complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="QualificationRequirement">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="QualificationTypeId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Comparator" type="{http://requester.mturk.amazonaws.com/doc/2013-11-15}Comparator"/>
 *         &lt;element name="IntegerValue" type="{http://www.w3.org/2001/XMLSchema}int" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="LocaleValue" type="{http://requester.mturk.amazonaws.com/doc/2013-11-15}Locale" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="RequiredToPreview" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "QualificationRequirement", propOrder = {
    "qualificationTypeId",
    "comparator",
    "integerValue",
    "localeValue",
    "requiredToPreview"
})
public class QualificationRequirement {

    @XmlElement(name = "QualificationTypeId", required = true)
    protected String qualificationTypeId;
    @XmlElement(name = "Comparator", required = true)
    protected Comparator comparator;
    @XmlElement(name = "IntegerValue", type = Integer.class)
    protected List<Integer> integerValue;
    @XmlElement(name = "LocaleValue")
    protected List<Locale> localeValue;
    @XmlElement(name = "RequiredToPreview")
    protected Boolean requiredToPreview;

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
     * Gets the value of the comparator property.
     * 
     * @return
     *     possible object is
     *     {@link Comparator }
     *     
     */
    public Comparator getComparator() {
        return comparator;
    }

    /**
     * Sets the value of the comparator property.
     * 
     * @param value
     *     allowed object is
     *     {@link Comparator }
     *     
     */
    public void setComparator(Comparator value) {
        this.comparator = value;
    }

    /**
     * Gets the value of the integerValue property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the integerValue property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getIntegerValue().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Integer }
     * 
     * 
     */
    public List<Integer> getIntegerValue() {
        if (integerValue == null) {
            integerValue = new ArrayList<Integer>();
        }
        return this.integerValue;
    }

    /**
     * Gets the value of the localeValue property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the localeValue property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getLocaleValue().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Locale }
     * 
     * 
     */
    public List<Locale> getLocaleValue() {
        if (localeValue == null) {
            localeValue = new ArrayList<Locale>();
        }
        return this.localeValue;
    }

    /**
     * Gets the value of the requiredToPreview property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isRequiredToPreview() {
        return requiredToPreview;
    }

    /**
     * Sets the value of the requiredToPreview property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setRequiredToPreview(Boolean value) {
        this.requiredToPreview = value;
    }

}
