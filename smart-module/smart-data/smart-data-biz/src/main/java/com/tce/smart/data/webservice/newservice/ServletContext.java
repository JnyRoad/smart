
package com.tce.smart.data.webservice.newservice;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>ServletContext complex type的 Java 类。
 *
 * <p>以下模式片段指定包含在此类中的预期内容。
 *
 * <pre>
 * &lt;complexType name="ServletContext">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="attributeNames" type="{http://util.java}Enumeration" minOccurs="0"/>
 *         &lt;element name="contextPath" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="initParameterNames" type="{http://util.java}Enumeration" minOccurs="0"/>
 *         &lt;element name="majorVersion" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="minorVersion" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="serverInfo" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="servletContextName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="servletNames" type="{http://util.java}Enumeration" minOccurs="0"/>
 *         &lt;element name="servlets" type="{http://util.java}Enumeration" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ServletContext", namespace = "http://servlet.javax", propOrder = {
    "attributeNames",
    "contextPath",
    "initParameterNames",
    "majorVersion",
    "minorVersion",
    "serverInfo",
    "servletContextName",
    "servletNames",
    "servlets"
})
public class ServletContext {

    @XmlElementRef(name = "attributeNames", namespace = "http://servlet.javax", type = JAXBElement.class, required = false)
    protected JAXBElement<Enumeration> attributeNames;
    @XmlElementRef(name = "contextPath", namespace = "http://servlet.javax", type = JAXBElement.class, required = false)
    protected JAXBElement<String> contextPath;
    @XmlElementRef(name = "initParameterNames", namespace = "http://servlet.javax", type = JAXBElement.class, required = false)
    protected JAXBElement<Enumeration> initParameterNames;
    protected Integer majorVersion;
    protected Integer minorVersion;
    @XmlElementRef(name = "serverInfo", namespace = "http://servlet.javax", type = JAXBElement.class, required = false)
    protected JAXBElement<String> serverInfo;
    @XmlElementRef(name = "servletContextName", namespace = "http://servlet.javax", type = JAXBElement.class, required = false)
    protected JAXBElement<String> servletContextName;
    @XmlElementRef(name = "servletNames", namespace = "http://servlet.javax", type = JAXBElement.class, required = false)
    protected JAXBElement<Enumeration> servletNames;
    @XmlElementRef(name = "servlets", namespace = "http://servlet.javax", type = JAXBElement.class, required = false)
    protected JAXBElement<Enumeration> servlets;

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
     * 获取contextPath属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getContextPath() {
        return contextPath;
    }

    /**
     * 设置contextPath属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setContextPath(JAXBElement<String> value) {
        this.contextPath = value;
    }

    /**
     * 获取initParameterNames属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Enumeration }{@code >}
     *
     */
    public JAXBElement<Enumeration> getInitParameterNames() {
        return initParameterNames;
    }

    /**
     * 设置initParameterNames属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Enumeration }{@code >}
     *
     */
    public void setInitParameterNames(JAXBElement<Enumeration> value) {
        this.initParameterNames = value;
    }

    /**
     * 获取majorVersion属性的值。
     *
     * @return
     *     possible object is
     *     {@link Integer }
     *
     */
    public Integer getMajorVersion() {
        return majorVersion;
    }

    /**
     * 设置majorVersion属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link Integer }
     *
     */
    public void setMajorVersion(Integer value) {
        this.majorVersion = value;
    }

    /**
     * 获取minorVersion属性的值。
     *
     * @return
     *     possible object is
     *     {@link Integer }
     *
     */
    public Integer getMinorVersion() {
        return minorVersion;
    }

    /**
     * 设置minorVersion属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link Integer }
     *
     */
    public void setMinorVersion(Integer value) {
        this.minorVersion = value;
    }

    /**
     * 获取serverInfo属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getServerInfo() {
        return serverInfo;
    }

    /**
     * 设置serverInfo属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setServerInfo(JAXBElement<String> value) {
        this.serverInfo = value;
    }

    /**
     * 获取servletContextName属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getServletContextName() {
        return servletContextName;
    }

    /**
     * 设置servletContextName属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setServletContextName(JAXBElement<String> value) {
        this.servletContextName = value;
    }

    /**
     * 获取servletNames属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Enumeration }{@code >}
     *
     */
    public JAXBElement<Enumeration> getServletNames() {
        return servletNames;
    }

    /**
     * 设置servletNames属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Enumeration }{@code >}
     *
     */
    public void setServletNames(JAXBElement<Enumeration> value) {
        this.servletNames = value;
    }

    /**
     * 获取servlets属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Enumeration }{@code >}
     *
     */
    public JAXBElement<Enumeration> getServlets() {
        return servlets;
    }

    /**
     * 设置servlets属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Enumeration }{@code >}
     *
     */
    public void setServlets(JAXBElement<Enumeration> value) {
        this.servlets = value;
    }

}
