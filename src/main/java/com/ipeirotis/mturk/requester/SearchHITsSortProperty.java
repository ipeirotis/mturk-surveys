
package com.ipeirotis.mturk.requester;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for SearchHITsSortProperty.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="SearchHITsSortProperty">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="Title"/>
 *     &lt;enumeration value="Reward"/>
 *     &lt;enumeration value="Expiration"/>
 *     &lt;enumeration value="CreationTime"/>
 *     &lt;enumeration value="Enumeration"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "SearchHITsSortProperty")
@XmlEnum
public enum SearchHITsSortProperty {

    @XmlEnumValue("Title")
    TITLE("Title"),
    @XmlEnumValue("Reward")
    REWARD("Reward"),
    @XmlEnumValue("Expiration")
    EXPIRATION("Expiration"),
    @XmlEnumValue("CreationTime")
    CREATION_TIME("CreationTime"),
    @XmlEnumValue("Enumeration")
    ENUMERATION("Enumeration");
    private final String value;

    SearchHITsSortProperty(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static SearchHITsSortProperty fromValue(String v) {
        for (SearchHITsSortProperty c: SearchHITsSortProperty.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
