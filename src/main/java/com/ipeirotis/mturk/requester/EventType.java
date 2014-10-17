
package com.ipeirotis.mturk.requester;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for EventType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="EventType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="AssignmentAccepted"/>
 *     &lt;enumeration value="AssignmentAbandoned"/>
 *     &lt;enumeration value="AssignmentReturned"/>
 *     &lt;enumeration value="AssignmentSubmitted"/>
 *     &lt;enumeration value="HITReviewable"/>
 *     &lt;enumeration value="HITExpired"/>
 *     &lt;enumeration value="Ping"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "EventType")
@XmlEnum
public enum EventType {

    @XmlEnumValue("AssignmentAccepted")
    ASSIGNMENT_ACCEPTED("AssignmentAccepted"),
    @XmlEnumValue("AssignmentAbandoned")
    ASSIGNMENT_ABANDONED("AssignmentAbandoned"),
    @XmlEnumValue("AssignmentReturned")
    ASSIGNMENT_RETURNED("AssignmentReturned"),
    @XmlEnumValue("AssignmentSubmitted")
    ASSIGNMENT_SUBMITTED("AssignmentSubmitted"),
    @XmlEnumValue("HITReviewable")
    HIT_REVIEWABLE("HITReviewable"),
    @XmlEnumValue("HITExpired")
    HIT_EXPIRED("HITExpired"),
    @XmlEnumValue("Ping")
    PING("Ping");
    private final String value;

    EventType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static EventType fromValue(String v) {
        for (EventType c: EventType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
