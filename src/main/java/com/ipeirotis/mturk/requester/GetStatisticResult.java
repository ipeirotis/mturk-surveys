
package com.ipeirotis.mturk.requester;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for GetStatisticResult complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="GetStatisticResult">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://requester.mturk.amazonaws.com/doc/2013-11-15}Request" minOccurs="0"/>
 *         &lt;element name="DataPoint" type="{http://requester.mturk.amazonaws.com/doc/2013-11-15}DataPoint" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="Statistic" type="{http://requester.mturk.amazonaws.com/doc/2013-11-15}RequesterStatistic" minOccurs="0"/>
 *         &lt;element name="TimePeriod" type="{http://requester.mturk.amazonaws.com/doc/2013-11-15}TimePeriod" minOccurs="0"/>
 *         &lt;element name="WorkerId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GetStatisticResult", propOrder = {
    "request",
    "dataPoint",
    "statistic",
    "timePeriod",
    "workerId"
})
public class GetStatisticResult {

    @XmlElement(name = "Request")
    protected Request request;
    @XmlElement(name = "DataPoint")
    protected List<DataPoint> dataPoint;
    @XmlElement(name = "Statistic")
    protected RequesterStatistic statistic;
    @XmlElement(name = "TimePeriod")
    protected TimePeriod timePeriod;
    @XmlElement(name = "WorkerId")
    protected String workerId;

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
     * Gets the value of the dataPoint property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the dataPoint property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDataPoint().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link DataPoint }
     * 
     * 
     */
    public List<DataPoint> getDataPoint() {
        if (dataPoint == null) {
            dataPoint = new ArrayList<DataPoint>();
        }
        return this.dataPoint;
    }

    /**
     * Gets the value of the statistic property.
     * 
     * @return
     *     possible object is
     *     {@link RequesterStatistic }
     *     
     */
    public RequesterStatistic getStatistic() {
        return statistic;
    }

    /**
     * Sets the value of the statistic property.
     * 
     * @param value
     *     allowed object is
     *     {@link RequesterStatistic }
     *     
     */
    public void setStatistic(RequesterStatistic value) {
        this.statistic = value;
    }

    /**
     * Gets the value of the timePeriod property.
     * 
     * @return
     *     possible object is
     *     {@link TimePeriod }
     *     
     */
    public TimePeriod getTimePeriod() {
        return timePeriod;
    }

    /**
     * Sets the value of the timePeriod property.
     * 
     * @param value
     *     allowed object is
     *     {@link TimePeriod }
     *     
     */
    public void setTimePeriod(TimePeriod value) {
        this.timePeriod = value;
    }

    /**
     * Gets the value of the workerId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getWorkerId() {
        return workerId;
    }

    /**
     * Sets the value of the workerId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setWorkerId(String value) {
        this.workerId = value;
    }

}
