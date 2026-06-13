
package com.tce.smart.data.webservice.newservice;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>ArrayOfDetailTable complex type的 Java 类。
 *
 * <p>以下模式片段指定包含在此类中的预期内容。
 *
 * <pre>
 * &lt;complexType name="ArrayOfDetailTable">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="DetailTable" type="{http://request.workflow.soa.weaver}DetailTable" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ArrayOfDetailTable", namespace = "http://request.workflow.soa.weaver", propOrder = {
    "detailTable"
})
public class ArrayOfDetailTable {

    @XmlElement(name = "DetailTable", nillable = true)
    protected List<DetailTable> detailTable;

    /**
     * Gets the value of the detailTable property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the detailTable property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDetailTable().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link DetailTable }
     *
     *
     */
    public List<DetailTable> getDetailTable() {
        if (detailTable == null) {
            detailTable = new ArrayList<DetailTable>();
        }
        return this.detailTable;
    }

}
