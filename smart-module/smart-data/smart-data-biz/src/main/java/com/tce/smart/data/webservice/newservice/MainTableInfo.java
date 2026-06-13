
package com.tce.smart.data.webservice.newservice;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>MainTableInfo complex type的 Java 类。
 *
 * <p>以下模式片段指定包含在此类中的预期内容。
 *
 * <pre>
 * &lt;complexType name="MainTableInfo">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="property" type="{http://request.workflow.soa.weaver}ArrayOfProperty" minOccurs="0"/>
 *         &lt;element name="propertyCount" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MainTableInfo", namespace = "http://request.workflow.soa.weaver", propOrder = {
    "property",
    "propertyCount"
})
public class MainTableInfo {

    @XmlElementRef(name = "property", namespace = "http://request.workflow.soa.weaver", type = JAXBElement.class, required = false)
    protected JAXBElement<ArrayOfProperty> property;
    protected Integer propertyCount;

    /**
     * 获取property属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link ArrayOfProperty }{@code >}
     *
     */
    public JAXBElement<ArrayOfProperty> getProperty() {
        return property;
    }

    /**
     * 设置property属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link ArrayOfProperty }{@code >}
     *
     */
    public void setProperty(JAXBElement<ArrayOfProperty> value) {
        this.property = value;
    }

    /**
     * 获取propertyCount属性的值。
     *
     * @return
     *     possible object is
     *     {@link Integer }
     *
     */
    public Integer getPropertyCount() {
        return propertyCount;
    }

    /**
     * 设置propertyCount属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link Integer }
     *
     */
    public void setPropertyCount(Integer value) {
        this.propertyCount = value;
    }

}
