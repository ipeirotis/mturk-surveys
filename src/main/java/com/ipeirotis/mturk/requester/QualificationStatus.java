
package com.ipeirotis.mturk.requester;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for QualificationStatus.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="QualificationStatus">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="Granted"/>
 *     &lt;enumeration value="Revoked"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "QualificationStatus")
@XmlEnum
public enum QualificationStatus {

    @XmlEnumValue("Granted")
    GRANTED("Granted"),
    @XmlEnumValue("Revoked")
    REVOKED("Revoked");
    private final String value;

    QualificationStatus(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static QualificationStatus fromValue(String v) {
        for (QualificationStatus c: QualificationStatus.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
