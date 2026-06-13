
package com.tce.smart.data.webservice.newservice;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>HttpSessionContext complex type的 Java 类。
 *
 * <p>以下模式片段指定包含在此类中的预期内容。
 *
 * <pre>
 * &lt;complexType name="HttpSessionContext">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="ids" type="{http://util.java}Enumeration" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "HttpSessionContext", namespace = "http://http.servlet.javax", propOrder = {
    "ids"
})
public class HttpSessionContext {

    @XmlElementRef(name = "ids", namespace = "http://http.servlet.javax", type = JAXBElement.class, required = false)
    protected JAXBElement<Enumeration> ids;

    /**
     * 获取ids属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Enumeration }{@code >}
     *
     */
    public JAXBElement<Enumeration> getIds() {
        return ids;
    }

    /**
     * 设置ids属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Enumeration }{@code >}
     *
     */
    public void setIds(JAXBElement<Enumeration> value) {
        this.ids = value;
    }

}
