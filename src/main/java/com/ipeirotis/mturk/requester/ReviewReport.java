
package com.ipeirotis.mturk.requester;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ReviewReport complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ReviewReport">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="PageNumber" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="NumResults" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="TotalNumResults" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="ReviewResult" type="{http://requester.mturk.amazonaws.com/doc/2013-11-15}ReviewResultDetail" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="ReviewAction" type="{http://requester.mturk.amazonaws.com/doc/2013-11-15}ReviewActionDetail" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ReviewReport", propOrder = {
    "pageNumber",
    "numResults",
    "totalNumResults",
    "reviewResult",
    "reviewAction"
})
public class ReviewReport {

    @XmlElement(name = "PageNumber")
    protected Integer pageNumber;
    @XmlElement(name = "NumResults")
    protected Integer numResults;
    @XmlElement(name = "TotalNumResults")
    protected Integer totalNumResults;
    @XmlElement(name = "ReviewResult")
    protected List<ReviewResultDetail> reviewResult;
    @XmlElement(name = "ReviewAction")
    protected List<ReviewActionDetail> reviewAction;

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
     * Gets the value of the reviewResult property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the reviewResult property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getReviewResult().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ReviewResultDetail }
     * 
     * 
     */
    public List<ReviewResultDetail> getReviewResult() {
        if (reviewResult == null) {
            reviewResult = new ArrayList<ReviewResultDetail>();
        }
        return this.reviewResult;
    }

    /**
     * Gets the value of the reviewAction property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the reviewAction property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getReviewAction().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ReviewActionDetail }
     * 
     * 
     */
    public List<ReviewActionDetail> getReviewAction() {
        if (reviewAction == null) {
            reviewAction = new ArrayList<ReviewActionDetail>();
        }
        return this.reviewAction;
    }

}
