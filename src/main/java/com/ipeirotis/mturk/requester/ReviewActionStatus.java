
package com.ipeirotis.mturk.requester;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ReviewActionStatus.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ReviewActionStatus">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="Intended"/>
 *     &lt;enumeration value="Succeeded"/>
 *     &lt;enumeration value="Failed"/>
 *     &lt;enumeration value="Cancelled"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ReviewActionStatus")
@XmlEnum
public enum ReviewActionStatus {

    @XmlEnumValue("Intended")
    INTENDED("Intended"),
    @XmlEnumValue("Succeeded")
    SUCCEEDED("Succeeded"),
    @XmlEnumValue("Failed")
    FAILED("Failed"),
    @XmlEnumValue("Cancelled")
    CANCELLED("Cancelled");
    private final String value;

    ReviewActionStatus(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static ReviewActionStatus fromValue(String v) {
        for (ReviewActionStatus c: ReviewActionStatus.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
