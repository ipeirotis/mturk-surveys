
package com.ipeirotis.mturk.requester;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for Comparator.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="Comparator">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="LessThan"/>
 *     &lt;enumeration value="LessThanOrEqualTo"/>
 *     &lt;enumeration value="GreaterThan"/>
 *     &lt;enumeration value="GreaterThanOrEqualTo"/>
 *     &lt;enumeration value="EqualTo"/>
 *     &lt;enumeration value="NotEqualTo"/>
 *     &lt;enumeration value="Exists"/>
 *     &lt;enumeration value="DoesNotExist"/>
 *     &lt;enumeration value="In"/>
 *     &lt;enumeration value="NotIn"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "Comparator")
@XmlEnum
public enum Comparator {

    @XmlEnumValue("LessThan")
    LESS_THAN("LessThan"),
    @XmlEnumValue("LessThanOrEqualTo")
    LESS_THAN_OR_EQUAL_TO("LessThanOrEqualTo"),
    @XmlEnumValue("GreaterThan")
    GREATER_THAN("GreaterThan"),
    @XmlEnumValue("GreaterThanOrEqualTo")
    GREATER_THAN_OR_EQUAL_TO("GreaterThanOrEqualTo"),
    @XmlEnumValue("EqualTo")
    EQUAL_TO("EqualTo"),
    @XmlEnumValue("NotEqualTo")
    NOT_EQUAL_TO("NotEqualTo"),
    @XmlEnumValue("Exists")
    EXISTS("Exists"),
    @XmlEnumValue("DoesNotExist")
    DOES_NOT_EXIST("DoesNotExist"),
    @XmlEnumValue("In")
    IN("In"),
    @XmlEnumValue("NotIn")
    NOT_IN("NotIn");
    private final String value;

    Comparator(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static Comparator fromValue(String v) {
        for (Comparator c: Comparator.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
