
package com.ipeirotis.mturk.requester;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for NotificationTransport.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="NotificationTransport">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="SOAP"/>
 *     &lt;enumeration value="REST"/>
 *     &lt;enumeration value="Email"/>
 *     &lt;enumeration value="SQS"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "NotificationTransport")
@XmlEnum
public enum NotificationTransport {

    SOAP("SOAP"),
    REST("REST"),
    @XmlEnumValue("Email")
    EMAIL("Email"),
    SQS("SQS");
    private final String value;

    NotificationTransport(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static NotificationTransport fromValue(String v) {
        for (NotificationTransport c: NotificationTransport.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
