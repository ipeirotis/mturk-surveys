
package com.ipeirotis.mturk.requester;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for HITStatus.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="HITStatus">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="Assignable"/>
 *     &lt;enumeration value="Unassignable"/>
 *     &lt;enumeration value="Reviewable"/>
 *     &lt;enumeration value="Reviewing"/>
 *     &lt;enumeration value="Disposed"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "HITStatus")
@XmlEnum
public enum HITStatus {

    @XmlEnumValue("Assignable")
    ASSIGNABLE("Assignable"),
    @XmlEnumValue("Unassignable")
    UNASSIGNABLE("Unassignable"),
    @XmlEnumValue("Reviewable")
    REVIEWABLE("Reviewable"),
    @XmlEnumValue("Reviewing")
    REVIEWING("Reviewing"),
    @XmlEnumValue("Disposed")
    DISPOSED("Disposed");
    private final String value;

    HITStatus(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static HITStatus fromValue(String v) {
        for (HITStatus c: HITStatus.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
