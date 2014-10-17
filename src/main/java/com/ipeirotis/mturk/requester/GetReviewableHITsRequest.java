
package com.ipeirotis.mturk.requester;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for GetReviewableHITsRequest complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="GetReviewableHITsRequest">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="HITTypeId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Status" type="{http://requester.mturk.amazonaws.com/doc/2013-11-15}ReviewableHITStatus" minOccurs="0"/>
 *         &lt;element name="SortDirection" type="{http://requester.mturk.amazonaws.com/doc/2013-11-15}SortDirection" minOccurs="0"/>
 *         &lt;element name="SortProperty" type="{http://requester.mturk.amazonaws.com/doc/2013-11-15}GetReviewableHITsSortProperty" minOccurs="0"/>
 *         &lt;element name="PageNumber" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="PageSize" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
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
@XmlType(name = "GetReviewableHITsRequest", propOrder = {
    "hitTypeId",
    "status",
    "sortDirection",
    "sortProperty",
    "pageNumber",
    "pageSize",
    "responseGroup"
})
public class GetReviewableHITsRequest {

    @XmlElement(name = "HITTypeId")
    protected String hitTypeId;
    @XmlElement(name = "Status")
    protected ReviewableHITStatus status;
    @XmlElement(name = "SortDirection")
    protected SortDirection sortDirection;
    @XmlElement(name = "SortProperty")
    protected GetReviewableHITsSortProperty sortProperty;
    @XmlElement(name = "PageNumber")
    protected Integer pageNumber;
    @XmlElement(name = "PageSize")
    protected Integer pageSize;
    @XmlElement(name = "ResponseGroup")
    protected List<String> responseGroup;

    /**
     * Gets the value of the hitTypeId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHITTypeId() {
        return hitTypeId;
    }

    /**
     * Sets the value of the hitTypeId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHITTypeId(String value) {
        this.hitTypeId = value;
    }

    /**
     * Gets the value of the status property.
     * 
     * @return
     *     possible object is
     *     {@link ReviewableHITStatus }
     *     
     */
    public ReviewableHITStatus getStatus() {
        return status;
    }

    /**
     * Sets the value of the status property.
     * 
     * @param value
     *     allowed object is
     *     {@link ReviewableHITStatus }
     *     
     */
    public void setStatus(ReviewableHITStatus value) {
        this.status = value;
    }

    /**
     * Gets the value of the sortDirection property.
     * 
     * @return
     *     possible object is
     *     {@link SortDirection }
     *     
     */
    public SortDirection getSortDirection() {
        return sortDirection;
    }

    /**
     * Sets the value of the sortDirection property.
     * 
     * @param value
     *     allowed object is
     *     {@link SortDirection }
     *     
     */
    public void setSortDirection(SortDirection value) {
        this.sortDirection = value;
    }

    /**
     * Gets the value of the sortProperty property.
     * 
     * @return
     *     possible object is
     *     {@link GetReviewableHITsSortProperty }
     *     
     */
    public GetReviewableHITsSortProperty getSortProperty() {
        return sortProperty;
    }

    /**
     * Sets the value of the sortProperty property.
     * 
     * @param value
     *     allowed object is
     *     {@link GetReviewableHITsSortProperty }
     *     
     */
    public void setSortProperty(GetReviewableHITsSortProperty value) {
        this.sortProperty = value;
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
     * Gets the value of the pageSize property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getPageSize() {
        return pageSize;
    }

    /**
     * Sets the value of the pageSize property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setPageSize(Integer value) {
        this.pageSize = value;
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
