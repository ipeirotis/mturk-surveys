
package com.ipeirotis.mturk.requester;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for TimePeriod.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="TimePeriod">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="OneDay"/>
 *     &lt;enumeration value="SevenDays"/>
 *     &lt;enumeration value="ThirtyDays"/>
 *     &lt;enumeration value="LifeToDate"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "TimePeriod")
@XmlEnum
public enum TimePeriod {

    @XmlEnumValue("OneDay")
    ONE_DAY("OneDay"),
    @XmlEnumValue("SevenDays")
    SEVEN_DAYS("SevenDays"),
    @XmlEnumValue("ThirtyDays")
    THIRTY_DAYS("ThirtyDays"),
    @XmlEnumValue("LifeToDate")
    LIFE_TO_DATE("LifeToDate");
    private final String value;

    TimePeriod(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static TimePeriod fromValue(String v) {
        for (TimePeriod c: TimePeriod.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
