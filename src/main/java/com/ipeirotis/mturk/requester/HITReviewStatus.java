
package com.ipeirotis.mturk.requester;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for HITReviewStatus.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="HITReviewStatus">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="NotReviewed"/>
 *     &lt;enumeration value="MarkedForReview"/>
 *     &lt;enumeration value="ReviewedAppropriate"/>
 *     &lt;enumeration value="ReviewedInappropriate"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "HITReviewStatus")
@XmlEnum
public enum HITReviewStatus {

    @XmlEnumValue("NotReviewed")
    NOT_REVIEWED("NotReviewed"),
    @XmlEnumValue("MarkedForReview")
    MARKED_FOR_REVIEW("MarkedForReview"),
    @XmlEnumValue("ReviewedAppropriate")
    REVIEWED_APPROPRIATE("ReviewedAppropriate"),
    @XmlEnumValue("ReviewedInappropriate")
    REVIEWED_INAPPROPRIATE("ReviewedInappropriate");
    private final String value;

    HITReviewStatus(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static HITReviewStatus fromValue(String v) {
        for (HITReviewStatus c: HITReviewStatus.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
