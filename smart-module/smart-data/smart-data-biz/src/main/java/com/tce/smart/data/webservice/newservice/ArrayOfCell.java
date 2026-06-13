
package com.tce.smart.data.webservice.newservice;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>ArrayOfCell complex type的 Java 类。
 *
 * <p>以下模式片段指定包含在此类中的预期内容。
 *
 * <pre>
 * &lt;complexType name="ArrayOfCell">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Cell" type="{http://request.workflow.soa.weaver}Cell" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ArrayOfCell", namespace = "http://request.workflow.soa.weaver", propOrder = {
    "cell"
})
public class ArrayOfCell {

    @XmlElement(name = "Cell", nillable = true)
    protected List<Cell> cell;

    /**
     * Gets the value of the cell property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the cell property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCell().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Cell }
     *
     *
     */
    public List<Cell> getCell() {
        if (cell == null) {
            cell = new ArrayList<Cell>();
        }
        return this.cell;
    }

}
