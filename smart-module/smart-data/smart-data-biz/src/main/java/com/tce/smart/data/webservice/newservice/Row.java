
package com.tce.smart.data.webservice.newservice;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Row complex type的 Java 类。
 *
 * <p>以下模式片段指定包含在此类中的预期内容。
 *
 * <pre>
 * &lt;complexType name="Row">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="cell" type="{http://request.workflow.soa.weaver}ArrayOfCell" minOccurs="0"/>
 *         &lt;element name="cellCount" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="id" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Row", namespace = "http://request.workflow.soa.weaver", propOrder = {
    "cell",
    "cellCount",
    "id"
})
public class Row {

    @XmlElementRef(name = "cell", namespace = "http://request.workflow.soa.weaver", type = JAXBElement.class, required = false)
    protected JAXBElement<ArrayOfCell> cell;
    protected Integer cellCount;
    @XmlElementRef(name = "id", namespace = "http://request.workflow.soa.weaver", type = JAXBElement.class, required = false)
    protected JAXBElement<String> id;

    /**
     * 获取cell属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link ArrayOfCell }{@code >}
     *
     */
    public JAXBElement<ArrayOfCell> getCell() {
        return cell;
    }

    /**
     * 设置cell属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link ArrayOfCell }{@code >}
     *
     */
    public void setCell(JAXBElement<ArrayOfCell> value) {
        this.cell = value;
    }

    /**
     * 获取cellCount属性的值。
     *
     * @return
     *     possible object is
     *     {@link Integer }
     *
     */
    public Integer getCellCount() {
        return cellCount;
    }

    /**
     * 设置cellCount属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link Integer }
     *
     */
    public void setCellCount(Integer value) {
        this.cellCount = value;
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

}
