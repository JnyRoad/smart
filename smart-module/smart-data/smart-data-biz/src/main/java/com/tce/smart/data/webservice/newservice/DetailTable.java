
package com.tce.smart.data.webservice.newservice;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>DetailTable complex type的 Java 类。
 *
 * <p>以下模式片段指定包含在此类中的预期内容。
 *
 * <pre>
 * &lt;complexType name="DetailTable">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="id" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="row" type="{http://request.workflow.soa.weaver}ArrayOfRow" minOccurs="0"/>
 *         &lt;element name="rowCount" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="tableDBName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DetailTable", namespace = "http://request.workflow.soa.weaver", propOrder = {
    "id",
    "row",
    "rowCount",
    "tableDBName"
})
public class DetailTable {

    @XmlElementRef(name = "id", namespace = "http://request.workflow.soa.weaver", type = JAXBElement.class, required = false)
    protected JAXBElement<String> id;
    @XmlElementRef(name = "row", namespace = "http://request.workflow.soa.weaver", type = JAXBElement.class, required = false)
    protected JAXBElement<ArrayOfRow> row;
    protected Integer rowCount;
    @XmlElementRef(name = "tableDBName", namespace = "http://request.workflow.soa.weaver", type = JAXBElement.class, required = false)
    protected JAXBElement<String> tableDBName;

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
     * 获取row属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link ArrayOfRow }{@code >}
     *
     */
    public JAXBElement<ArrayOfRow> getRow() {
        return row;
    }

    /**
     * 设置row属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link ArrayOfRow }{@code >}
     *
     */
    public void setRow(JAXBElement<ArrayOfRow> value) {
        this.row = value;
    }

    /**
     * 获取rowCount属性的值。
     *
     * @return
     *     possible object is
     *     {@link Integer }
     *
     */
    public Integer getRowCount() {
        return rowCount;
    }

    /**
     * 设置rowCount属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link Integer }
     *
     */
    public void setRowCount(Integer value) {
        this.rowCount = value;
    }

    /**
     * 获取tableDBName属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getTableDBName() {
        return tableDBName;
    }

    /**
     * 设置tableDBName属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setTableDBName(JAXBElement<String> value) {
        this.tableDBName = value;
    }

}
