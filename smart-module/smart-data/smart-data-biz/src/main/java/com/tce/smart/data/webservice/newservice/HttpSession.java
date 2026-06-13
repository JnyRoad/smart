
package com.tce.smart.data.webservice.newservice;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>HttpSession complex type的 Java 类。
 *
 * <p>以下模式片段指定包含在此类中的预期内容。
 *
 * <pre>
 * &lt;complexType name="HttpSession">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="attributeNames" type="{http://util.java}Enumeration" minOccurs="0"/>
 *         &lt;element name="creationTime" type="{http://www.w3.org/2001/XMLSchema}long" minOccurs="0"/>
 *         &lt;element name="id" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="lastAccessedTime" type="{http://www.w3.org/2001/XMLSchema}long" minOccurs="0"/>
 *         &lt;element name="maxInactiveInterval" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="new" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="servletContext" type="{http://servlet.javax}ServletContext" minOccurs="0"/>
 *         &lt;element name="sessionContext" type="{http://http.servlet.javax}HttpSessionContext" minOccurs="0"/>
 *         &lt;element name="valueNames" type="{http://localhost/services/RequestService}ArrayOfString" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "HttpSession", namespace = "http://http.servlet.javax", propOrder = {
    "attributeNames",
    "creationTime",
    "id",
    "lastAccessedTime",
    "maxInactiveInterval",
    "_new",
    "servletContext",
    "sessionContext",
    "valueNames"
})
public class HttpSession {

    @XmlElementRef(name = "attributeNames", namespace = "http://http.servlet.javax", type = JAXBElement.class, required = false)
    protected JAXBElement<Enumeration> attributeNames;
    protected Long creationTime;
    @XmlElementRef(name = "id", namespace = "http://http.servlet.javax", type = JAXBElement.class, required = false)
    protected JAXBElement<String> id;
    protected Long lastAccessedTime;
    protected Integer maxInactiveInterval;
    @XmlElement(name = "new")
    protected Boolean _new;
    @XmlElementRef(name = "servletContext", namespace = "http://http.servlet.javax", type = JAXBElement.class, required = false)
    protected JAXBElement<ServletContext> servletContext;
    @XmlElementRef(name = "sessionContext", namespace = "http://http.servlet.javax", type = JAXBElement.class, required = false)
    protected JAXBElement<HttpSessionContext> sessionContext;
    @XmlElementRef(name = "valueNames", namespace = "http://http.servlet.javax", type = JAXBElement.class, required = false)
    protected JAXBElement<ArrayOfString> valueNames;

    /**
     * 获取attributeNames属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Enumeration }{@code >}
     *
     */
    public JAXBElement<Enumeration> getAttributeNames() {
        return attributeNames;
    }

    /**
     * 设置attributeNames属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Enumeration }{@code >}
     *
     */
    public void setAttributeNames(JAXBElement<Enumeration> value) {
        this.attributeNames = value;
    }

    /**
     * 获取creationTime属性的值。
     *
     * @return
     *     possible object is
     *     {@link Long }
     *
     */
    public Long getCreationTime() {
        return creationTime;
    }

    /**
     * 设置creationTime属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link Long }
     *
     */
    public void setCreationTime(Long value) {
        this.creationTime = value;
    }

    /**
     * 获取id属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getId() {
        return id;
    }

    /**
     * 设置id属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setId(JAXBElement<String> value) {
        this.id = value;
    }

    /**
     * 获取lastAccessedTime属性的值。
     *
     * @return
     *     possible object is
     *     {@link Long }
     *
     */
    public Long getLastAccessedTime() {
        return lastAccessedTime;
    }

    /**
     * 设置lastAccessedTime属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link Long }
     *
     */
    public void setLastAccessedTime(Long value) {
        this.lastAccessedTime = value;
    }

    /**
     * 获取maxInactiveInterval属性的值。
     *
     * @return
     *     possible object is
     *     {@link Integer }
     *
     */
    public Integer getMaxInactiveInterval() {
        return maxInactiveInterval;
    }

    /**
     * 设置maxInactiveInterval属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link Integer }
     *
     */
    public void setMaxInactiveInterval(Integer value) {
        this.maxInactiveInterval = value;
    }

    /**
     * 获取new属性的值。
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public Boolean isNew() {
        return _new;
    }

    /**
     * 设置new属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *
     */
    public void setNew(Boolean value) {
        this._new = value;
    }

    /**
     * 获取servletContext属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link ServletContext }{@code >}
     *
     */
    public JAXBElement<ServletContext> getServletContext() {
        return servletContext;
    }

    /**
     * 设置servletContext属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link ServletContext }{@code >}
     *
     */
    public void setServletContext(JAXBElement<ServletContext> value) {
        this.servletContext = value;
    }

    /**
     * 获取sessionContext属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link HttpSessionContext }{@code >}
     *
     */
    public JAXBElement<HttpSessionContext> getSessionContext() {
        return sessionContext;
    }

    /**
     * 设置sessionContext属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link HttpSessionContext }{@code >}
     *
     */
    public void setSessionContext(JAXBElement<HttpSessionContext> value) {
        this.sessionContext = value;
    }

    /**
     * 获取valueNames属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link ArrayOfString }{@code >}
     *
     */
    public JAXBElement<ArrayOfString> getValueNames() {
        return valueNames;
    }

    /**
     * 设置valueNames属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link ArrayOfString }{@code >}
     *
     */
    public void setValueNames(JAXBElement<ArrayOfString> value) {
        this.valueNames = value;
    }

}
