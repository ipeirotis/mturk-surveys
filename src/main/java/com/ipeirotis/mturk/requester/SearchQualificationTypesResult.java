
package com.ipeirotis.mturk.requester;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for SearchQualificationTypesResult complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="SearchQualificationTypesResult">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://requester.mturk.amazonaws.com/doc/2013-11-15}Request" minOccurs="0"/>
 *         &lt;element name="NumResults" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="TotalNumResults" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="PageNumber" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="QualificationType" type="{http://requester.mturk.amazonaws.com/doc/2013-11-15}QualificationType" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SearchQualificationTypesResult", propOrder = {
    "request",
    "numResults",
    "totalNumResults",
    "pageNumber",
    "qualificationType"
})
public class SearchQualificationTypesResult {

    @XmlElement(name = "Request")
    protected Request request;
    @XmlElement(name = "NumResults")
    protected Integer numResults;
    @XmlElement(name = "TotalNumResults")
    protected Integer totalNumResults;
    @XmlElement(name = "PageNumber")
    protected Integer pageNumber;
    @XmlElement(name = "QualificationType")
    protected List<QualificationType> qualificationType;

    /**
     * Gets the value of the request property.
     * 
     * @return
     *     possible object is
     *     {@link Request }
     *     
     */
    public Request getRequest() {
        return request;
    }

    /**
     * Sets the value of the request property.
     * 
     * @param value
     *     allowed object is
     *     {@link Request }
     *     
     */
    public void setRequest(Request value) {
        this.request = value;
    }

    /**
     * Gets the value of the numResults property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getNumResults() {
        return numResults;
    }

    /**
     * Sets the value of the numResults property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setNumResults(Integer value) {
        this.numResults = value;
    }

    /**
     * Gets the value of the totalNumResults property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getTotalNumResults() {
        return totalNumResults;
    }

    /**
     * Sets the value of the totalNumResults property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setTotalNumResults(Integer value) {
        this.totalNumResults = value;
    }

    /**
     * Gets the value of the pageNumber property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getPageNumber() {
        return pageNumber;
    }

    /**
     * Sets the value of the pageNumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setPageNumber(Integer value) {
        this.pageNumber = value;
    }

    /**
     * Gets the value of the qualificationType property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the qualificationType property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getQualificationType().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link QualificationType }
     * 
     * 
     */
    public List<QualificationType> getQualificationType() {
        if (qualificationType == null) {
            qualificationType = new ArrayList<QualificationType>();
        }
        return this.qualificationType;
    }

}
