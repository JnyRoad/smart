
package com.tce.smart.data.webservice.newservice;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>DetailTableInfo complex type的 Java 类。
 *
 * <p>以下模式片段指定包含在此类中的预期内容。
 *
 * <pre>
 * &lt;complexType name="DetailTableInfo">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="detailTable" type="{http://request.workflow.soa.weaver}ArrayOfDetailTable" minOccurs="0"/>
 *         &lt;element name="detailTableCount" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DetailTableInfo", namespace = "http://request.workflow.soa.weaver", propOrder = {
    "detailTable",
    "detailTableCount"
})
public class DetailTableInfo {

    @XmlElementRef(name = "detailTable", namespace = "http://request.workflow.soa.weaver", type = JAXBElement.class, required = false)
    protected JAXBElement<ArrayOfDetailTable> detailTable;
    protected Integer detailTableCount;

    /**
     * 获取detailTable属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link ArrayOfDetailTable }{@code >}
     *
     */
    public JAXBElement<ArrayOfDetailTable> getDetailTable() {
        return detailTable;
    }

    /**
     * 设置detailTable属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link ArrayOfDetailTable }{@code >}
     *
     */
    public void setDetailTable(JAXBElement<ArrayOfDetailTable> value) {
        this.detailTable = value;
    }

    /**
     * 获取detailTableCount属性的值。
     *
     * @return
     *     possible object is
     *     {@link Integer }
     *
     */
    public Integer getDetailTableCount() {
        return detailTableCount;
    }

    /**
     * 设置detailTableCount属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link Integer }
     *
     */
    public void setDetailTableCount(Integer value) {
        this.detailTableCount = value;
    }

}
