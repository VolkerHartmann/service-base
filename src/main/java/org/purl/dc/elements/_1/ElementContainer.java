//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2019.06.05 um 12:27:41 PM CEST 
//
package org.purl.dc.elements._1;

import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.ElementCollection;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElementRef;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

/**
 *
 * This complexType is included as a convenience for schema authors who need to
 * define a root or container element for all of the DC elements.
 *
 *
 * <p>
 * Java-Klasse für elementContainer complex type.
 *
 * <p>
 * Das folgende Schemafragment gibt den erwarteten Content an, der in dieser
 * Klasse enthalten ist.
 *
 * <pre>
 * &lt;complexType name="elementContainer">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice>
 *         &lt;group ref="{http://purl.org/dc/elements/1.1/}elementsGroup"/>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "elementContainer", propOrder = {
  "any"
})
@XmlRootElement(name = "dc", namespace = "http://www.openarchives.org/OAI/2.0/oai_dc/")
public class ElementContainer{

  @XmlElementRef(name = "any", namespace = "http://purl.org/dc/elements/1.1/", type = JAXBElement.class, required = false)
  protected List<JAXBElement<SimpleLiteral>> any;

  /**
   * Gets the value of the any property.
   *
   * <p>
   * This accessor method returns a reference to the live list, not a snapshot.
   * Therefore any modification you make to the returned list will be present
   * inside the JAXB object. This is why there is not a <CODE>set</CODE> method
   * for the any property.
   *
   * <p>
   * For example, to add a new item, do as follows:
   * <pre>
   *    getAny().add(newItem);
   * </pre>
   *
   *
   * <p>
   * Objects of the following type(s) are allowed in the list null   {@link JAXBElement }{@code <}{@link SimpleLiteral }{@code >}
     * {@link JAXBElement }{@code <}{@link SimpleLiteral }{@code >}
   * {@link JAXBElement }{@code <}{@link SimpleLiteral }{@code >}
   * {@link JAXBElement }{@code <}{@link SimpleLiteral }{@code >}
   * {@link JAXBElement }{@code <}{@link SimpleLiteral }{@code >}
   * {@link JAXBElement }{@code <}{@link SimpleLiteral }{@code >}
   * {@link JAXBElement }{@code <}{@link SimpleLiteral }{@code >}
   * {@link JAXBElement }{@code <}{@link SimpleLiteral }{@code >}
   * {@link JAXBElement }{@code <}{@link SimpleLiteral }{@code >}
   * {@link JAXBElement }{@code <}{@link SimpleLiteral }{@code >}
   * {@link JAXBElement }{@code <}{@link SimpleLiteral }{@code >}
   * {@link JAXBElement }{@code <}{@link SimpleLiteral }{@code >}
   * {@link JAXBElement }{@code <}{@link SimpleLiteral }{@code >}
   * {@link JAXBElement }{@code <}{@link SimpleLiteral }{@code >}
   * {@link JAXBElement }{@code <}{@link SimpleLiteral }{@code >}
   * {@link JAXBElement }{@code <}{@link SimpleLiteral }{@code >}
   *
   *
   */
  public List<JAXBElement<SimpleLiteral>> getAny(){
    if(any == null){
      any = new ArrayList<JAXBElement<SimpleLiteral>>();
    }
    return this.any;
  }

}
