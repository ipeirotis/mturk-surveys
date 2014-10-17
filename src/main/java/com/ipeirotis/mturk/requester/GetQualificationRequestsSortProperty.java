
package com.ipeirotis.mturk.requester;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for GetQualificationRequestsSortProperty.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="GetQualificationRequestsSortProperty">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="QualificationTypeId"/>
 *     &lt;enumeration value="SubmitTime"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "GetQualificationRequestsSortProperty")
@XmlEnum
public enum GetQualificationRequestsSortProperty {

    @XmlEnumValue("QualificationTypeId")
    QUALIFICATION_TYPE_ID("QualificationTypeId"),
    @XmlEnumValue("SubmitTime")
    SUBMIT_TIME("SubmitTime");
    private final String value;

    GetQualificationRequestsSortProperty(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static GetQualificationRequestsSortProperty fromValue(String v) {
        for (GetQualificationRequestsSortProperty c: GetQualificationRequestsSortProperty.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
