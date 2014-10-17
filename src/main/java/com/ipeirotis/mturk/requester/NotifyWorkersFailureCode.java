
package com.ipeirotis.mturk.requester;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for NotifyWorkersFailureCode.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="NotifyWorkersFailureCode">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="SoftFailure"/>
 *     &lt;enumeration value="HardFailure"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "NotifyWorkersFailureCode")
@XmlEnum
public enum NotifyWorkersFailureCode {

    @XmlEnumValue("SoftFailure")
    SOFT_FAILURE("SoftFailure"),
    @XmlEnumValue("HardFailure")
    HARD_FAILURE("HardFailure");
    private final String value;

    NotifyWorkersFailureCode(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static NotifyWorkersFailureCode fromValue(String v) {
        for (NotifyWorkersFailureCode c: NotifyWorkersFailureCode.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
