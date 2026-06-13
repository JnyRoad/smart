
package com.tce.smart.data.webservice.newservice;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Cookie complex type的 Java 类。
 *
 * <p>以下模式片段指定包含在此类中的预期内容。
 *
 * <pre>
 * &lt;complexType name="Cookie">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="comment" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="domain" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="maxAge" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="path" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="secure" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="value" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="version" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Cookie", namespace = "http://http.servlet.javax", propOrder = {
    "comment",
    "domain",
    "maxAge",
    "name",
    "path",
    "secure",
    "value",
    "version"
})
public class Cookie {

    @XmlElementRef(name = "comment", namespace = "http://http.servlet.javax", type = JAXBElement.class, required = false)
    protected JAXBElement<String> comment;
    @XmlElementRef(name = "domain", namespace = "http://http.servlet.javax", type = JAXBElement.class, required = false)
    protected JAXBElement<String> domain;
    protected Integer maxAge;
    @XmlElementRef(name = "name", namespace = "http://http.servlet.javax", type = JAXBElement.class, required = false)
    protected JAXBElement<String> name;
    @XmlElementRef(name = "path", namespace = "http://http.servlet.javax", type = JAXBElement.class, required = false)
    protected JAXBElement<String> path;
    protected Boolean secure;
    @XmlElementRef(name = "value", namespace = "http://http.servlet.javax", type = JAXBElement.class, required = false)
    protected JAXBElement<String> value;
    protected Integer version;

    /**
     * 获取comment属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getComment() {
        return comment;
    }

    /**
     * 设置comment属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setComment(JAXBElement<String> value) {
        this.comment = value;
    }

    /**
     * 获取domain属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getDomain() {
        return domain;
    }

    /**
     * 设置domain属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setDomain(JAXBElement<String> value) {
        this.domain = value;
    }

    /**
     * 获取maxAge属性的值。
     *
     * @return
     *     possible object is
     *     {@link Integer }
     *
     */
    public Integer getMaxAge() {
        return maxAge;
    }

    /**
     * 设置maxAge属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link Integer }
     *
     */
    public void setMaxAge(Integer value) {
        this.maxAge = value;
    }

    /**
     * 获取name属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getName() {
        return name;
    }

    /**
     * 设置name属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setName(JAXBElement<String> value) {
        this.name = value;
    }

    /**
     * 获取path属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getPath() {
        return path;
    }

    /**
     * 设置path属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setPath(JAXBElement<String> value) {
        this.path = value;
    }

    /**
     * 获取secure属性的值。
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public Boolean isSecure() {
        return secure;
    }

    /**
     * 设置secure属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *
     */
    public void setSecure(Boolean value) {
        this.secure = value;
    }

    /**
     * 获取value属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getValue() {
        return value;
    }

    /**
     * 设置value属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setValue(JAXBElement<String> value) {
        this.value = value;
    }

    /**
     * 获取version属性的值。
     *
     * @return
     *     possible object is
     *     {@link Integer }
     *
     */
    public Integer getVersion() {
        return version;
    }

    /**
     * 设置version属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link Integer }
     *
     */
    public void setVersion(Integer value) {
        this.version = value;
    }

}
