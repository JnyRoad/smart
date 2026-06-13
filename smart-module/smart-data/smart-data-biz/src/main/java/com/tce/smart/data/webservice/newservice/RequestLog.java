
package com.tce.smart.data.webservice.newservice;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>RequestLog complex type的 Java 类。
 *
 * <p>以下模式片段指定包含在此类中的预期内容。
 *
 * <pre>
 * &lt;complexType name="RequestLog">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="log" type="{http://request.workflow.soa.weaver}ArrayOfLog" minOccurs="0"/>
 *         &lt;element name="logCount" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RequestLog", namespace = "http://request.workflow.soa.weaver", propOrder = {
    "log",
    "logCount"
})
public class RequestLog {

    @XmlElementRef(name = "log", namespace = "http://request.workflow.soa.weaver", type = JAXBElement.class, required = false)
    protected JAXBElement<ArrayOfLog> log;
    protected Integer logCount;

    /**
     * 获取log属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link ArrayOfLog }{@code >}
     *
     */
    public JAXBElement<ArrayOfLog> getLog() {
        return log;
    }

    /**
     * 设置log属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link ArrayOfLog }{@code >}
     *
     */
    public void setLog(JAXBElement<ArrayOfLog> value) {
        this.log = value;
    }

    /**
     * 获取logCount属性的值。
     *
     * @return
     *     possible object is
     *     {@link Integer }
     *
     */
    public Integer getLogCount() {
        return logCount;
    }

    /**
     * 设置logCount属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link Integer }
     *
     */
    public void setLogCount(Integer value) {
        this.logCount = value;
    }

}
