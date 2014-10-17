
package com.ipeirotis.mturk.requester;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ReviewableHITStatus.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ReviewableHITStatus">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="Reviewable"/>
 *     &lt;enumeration value="Reviewing"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ReviewableHITStatus")
@XmlEnum
public enum ReviewableHITStatus {

    @XmlEnumValue("Reviewable")
    REVIEWABLE("Reviewable"),
    @XmlEnumValue("Reviewing")
    REVIEWING("Reviewing");
    private final String value;

    ReviewableHITStatus(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static ReviewableHITStatus fromValue(String v) {
        for (ReviewableHITStatus c: ReviewableHITStatus.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
