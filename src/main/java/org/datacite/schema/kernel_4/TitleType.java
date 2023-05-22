//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2018.01.10 um 07:42:46 AM CET 
//


package org.datacite.schema.kernel_4;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für titleType.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * <p>
 * <pre>
 * &lt;simpleType name="titleType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="AlternativeTitle"/>
 *     &lt;enumeration value="Subtitle"/>
 *     &lt;enumeration value="TranslatedTitle"/>
 *     &lt;enumeration value="Other"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "titleType")
@XmlEnum
public enum TitleType {

    @XmlEnumValue("AlternativeTitle")
    ALTERNATIVE_TITLE("AlternativeTitle"),
    @XmlEnumValue("Subtitle")
    SUBTITLE("Subtitle"),
    @XmlEnumValue("TranslatedTitle")
    TRANSLATED_TITLE("TranslatedTitle"),
    @XmlEnumValue("Other")
    OTHER("Other");
    private final String value;

    TitleType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static TitleType fromValue(String v) {
        for (TitleType c: TitleType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
