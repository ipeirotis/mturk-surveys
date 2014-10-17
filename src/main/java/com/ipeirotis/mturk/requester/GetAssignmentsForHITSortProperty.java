
package com.ipeirotis.mturk.requester;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for GetAssignmentsForHITSortProperty.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="GetAssignmentsForHITSortProperty">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="AcceptTime"/>
 *     &lt;enumeration value="SubmitTime"/>
 *     &lt;enumeration value="Answer"/>
 *     &lt;enumeration value="AssignmentStatus"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "GetAssignmentsForHITSortProperty")
@XmlEnum
public enum GetAssignmentsForHITSortProperty {

    @XmlEnumValue("AcceptTime")
    ACCEPT_TIME("AcceptTime"),
    @XmlEnumValue("SubmitTime")
    SUBMIT_TIME("SubmitTime"),
    @XmlEnumValue("Answer")
    ANSWER("Answer"),
    @XmlEnumValue("AssignmentStatus")
    ASSIGNMENT_STATUS("AssignmentStatus");
    private final String value;

    GetAssignmentsForHITSortProperty(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static GetAssignmentsForHITSortProperty fromValue(String v) {
        for (GetAssignmentsForHITSortProperty c: GetAssignmentsForHITSortProperty.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
