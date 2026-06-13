
package com.tce.smart.data.webservice.newservice;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>ArrayOfRequestBase complex type的 Java 类。
 *
 * <p>以下模式片段指定包含在此类中的预期内容。
 *
 * <pre>
 * &lt;complexType name="ArrayOfRequestBase">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="RequestBase" type="{http://request.workflow.soa.weaver}RequestBase" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ArrayOfRequestBase", namespace = "http://request.workflow.soa.weaver", propOrder = {
    "requestBase"
})
public class ArrayOfRequestBase {

    @XmlElement(name = "RequestBase", nillable = true)
    protected List<RequestBase> requestBase;

    /**
     * Gets the value of the requestBase property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the requestBase property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRequestBase().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link RequestBase }
     *
     *
     */
    public List<RequestBase> getRequestBase() {
        if (requestBase == null) {
            requestBase = new ArrayList<RequestBase>();
        }
        return this.requestBase;
    }

}
