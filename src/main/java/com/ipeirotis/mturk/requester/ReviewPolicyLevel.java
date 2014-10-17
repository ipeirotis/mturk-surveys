
package com.ipeirotis.mturk.requester;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ReviewPolicyLevel.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ReviewPolicyLevel">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="Assignment"/>
 *     &lt;enumeration value="HIT"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ReviewPolicyLevel")
@XmlEnum
public enum ReviewPolicyLevel {

    @XmlEnumValue("Assignment")
    ASSIGNMENT("Assignment"),
    HIT("HIT");
    private final String value;

    ReviewPolicyLevel(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static ReviewPolicyLevel fromValue(String v) {
        for (ReviewPolicyLevel c: ReviewPolicyLevel.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
