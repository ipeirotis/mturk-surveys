
package com.ipeirotis.mturk.requester;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for SearchQualificationTypesSortProperty.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="SearchQualificationTypesSortProperty">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="Name"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "SearchQualificationTypesSortProperty")
@XmlEnum
public enum SearchQualificationTypesSortProperty {

    @XmlEnumValue("Name")
    NAME("Name");
    private final String value;

    SearchQualificationTypesSortProperty(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static SearchQualificationTypesSortProperty fromValue(String v) {
        for (SearchQualificationTypesSortProperty c: SearchQualificationTypesSortProperty.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
