
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
 *         &lt;element name="Name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Description" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="RequiredParameters" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="Parameter" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="AvailableParameters" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="Parameter" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="DefaultResponseGroups" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="ResponseGroup" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="AvailableResponseGroups" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="ResponseGroup" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded"/>
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
@XmlType(name = "", propOrder = {
    "name",
    "description",
    "requiredParameters",
    "availableParameters",
    "defaultResponseGroups",
    "availableResponseGroups"
})
@XmlRootElement(name = "OperationInformation")
public class OperationInformation {

    @XmlElement(name = "Name")
    protected String name;
    @XmlElement(name = "Description")
    protected String description;
    @XmlElement(name = "RequiredParameters")
    protected RequiredParameters requiredParameters;
    @XmlElement(name = "AvailableParameters")
    protected AvailableParameters availableParameters;
    @XmlElement(name = "DefaultResponseGroups")
    protected DefaultResponseGroups defaultResponseGroups;
    @XmlElement(name = "AvailableResponseGroups")
    protected AvailableResponseGroups availableResponseGroups;

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
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
     * Gets the value of the requiredParameters property.
     * 
     * @return
     *     possible object is
     *     {@link RequiredParameters }
     *     
     */
    public RequiredParameters getRequiredParameters() {
        return requiredParameters;
    }

    /**
     * Sets the value of the requiredParameters property.
     * 
     * @param value
     *     allowed object is
     *     {@link RequiredParameters }
     *     
     */
    public void setRequiredParameters(RequiredParameters value) {
        this.requiredParameters = value;
    }

    /**
     * Gets the value of the availableParameters property.
     * 
     * @return
     *     possible object is
     *     {@link AvailableParameters }
     *     
     */
    public AvailableParameters getAvailableParameters() {
        return availableParameters;
    }

    /**
     * Sets the value of the availableParameters property.
     * 
     * @param value
     *     allowed object is
     *     {@link AvailableParameters }
     *     
     */
    public void setAvailableParameters(AvailableParameters value) {
        this.availableParameters = value;
    }

    /**
     * Gets the value of the defaultResponseGroups property.
     * 
     * @return
     *     possible object is
     *     {@link DefaultResponseGroups }
     *     
     */
    public DefaultResponseGroups getDefaultResponseGroups() {
        return defaultResponseGroups;
    }

    /**
     * Sets the value of the defaultResponseGroups property.
     * 
     * @param value
     *     allowed object is
     *     {@link DefaultResponseGroups }
     *     
     */
    public void setDefaultResponseGroups(DefaultResponseGroups value) {
        this.defaultResponseGroups = value;
    }

    /**
     * Gets the value of the availableResponseGroups property.
     * 
     * @return
     *     possible object is
     *     {@link AvailableResponseGroups }
     *     
     */
    public AvailableResponseGroups getAvailableResponseGroups() {
        return availableResponseGroups;
    }

    /**
     * Sets the value of the availableResponseGroups property.
     * 
     * @param value
     *     allowed object is
     *     {@link AvailableResponseGroups }
     *     
     */
    public void setAvailableResponseGroups(AvailableResponseGroups value) {
        this.availableResponseGroups = value;
    }

}
