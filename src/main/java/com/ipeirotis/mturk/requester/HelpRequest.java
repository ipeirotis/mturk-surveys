
package com.ipeirotis.mturk.requester;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for HelpRequest complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="HelpRequest">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="About" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded"/>
 *         &lt;element name="HelpType" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;enumeration value="Operation"/>
 *               &lt;enumeration value="ResponseGroup"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
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
@XmlType(name = "HelpRequest", propOrder = {
    "about",
    "helpType",
    "responseGroup"
})
public class HelpRequest {

    @XmlElement(name = "About", required = true)
    protected List<String> about;
    @XmlElement(name = "HelpType")
    protected String helpType;
    @XmlElement(name = "ResponseGroup")
    protected List<String> responseGroup;

    /**
     * Gets the value of the about property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the about property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAbout().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getAbout() {
        if (about == null) {
            about = new ArrayList<String>();
        }
        return this.about;
    }

    /**
     * Gets the value of the helpType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHelpType() {
        return helpType;
    }

    /**
     * Sets the value of the helpType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHelpType(String value) {
        this.helpType = value;
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
