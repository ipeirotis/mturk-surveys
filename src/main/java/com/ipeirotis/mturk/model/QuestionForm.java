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
import javax.xml.bind.annotation.XmlElements;
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
 *         &lt;choice maxOccurs="unbounded">
 *           &lt;element name="Overview" type="{http://mechanicalturk.amazonaws.com/AWSMechanicalTurkDataSchemas/2005-10-01/QuestionForm.xsd}ContentType"/>
 *           &lt;element name="Question">
 *             &lt;complexType>
 *               &lt;complexContent>
 *                 &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                   &lt;sequence>
 *                     &lt;element name="QuestionIdentifier" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                     &lt;element name="DisplayName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                     &lt;element name="IsRequired" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *                     &lt;element name="QuestionContent" type="{http://mechanicalturk.amazonaws.com/AWSMechanicalTurkDataSchemas/2005-10-01/QuestionForm.xsd}ContentType"/>
 *                     &lt;element name="AnswerSpecification" type="{http://mechanicalturk.amazonaws.com/AWSMechanicalTurkDataSchemas/2005-10-01/QuestionForm.xsd}AnswerSpecificationType"/>
 *                   &lt;/sequence>
 *                 &lt;/restriction>
 *               &lt;/complexContent>
 *             &lt;/complexType>
 *           &lt;/element>
 *         &lt;/choice>
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
    "overviewOrQuestion"
})
@XmlRootElement(name = "QuestionForm")
public class QuestionForm {

    @XmlElements({
        @XmlElement(name = "Overview", type = ContentType.class),
        @XmlElement(name = "Question", type = Question.class)
    })
    protected List<Object> overviewOrQuestion;

    /**
     * Gets the value of the overviewOrQuestion property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the overviewOrQuestion property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getOverviewOrQuestion().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ContentType }
     * {@link Question }
     * 
     * 
     */
    public List<Object> getOverviewOrQuestion() {
        if (overviewOrQuestion == null) {
            overviewOrQuestion = new ArrayList<Object>();
        }
        return this.overviewOrQuestion;
    }

}
