
package ru.javaops.masterjava.xml.schema;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for cityType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="cityType">
 *   &lt;simpleContent>
 *     &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>string">
 *       &lt;attribute name="id" use="required" type="{http://www.w3.org/2001/XMLSchema}ID" />
 *     &lt;/extension>
 *   &lt;/simpleContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlType(name = "groupType", namespace = "http://javaops.ru")
@XmlEnum
public enum GroupType {

    @XmlEnumValue("REGISTERING")
    REGISTERING("REGISTERING"),
    @XmlEnumValue("CURRENT")
    CURRENT("CURRENT"),
    @XmlEnumValue("FINISHED")
    FINISHED("FINISHED");
    private final String value;

    GroupType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static GroupType fromValue(String v) {
        for (GroupType c: GroupType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }
}
