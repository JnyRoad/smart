
package com.tce.smart.data.webservice.newservice;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>ArrayOfLog complex type的 Java 类。
 *
 * <p>以下模式片段指定包含在此类中的预期内容。
 *
 * <pre>
 * &lt;complexType name="ArrayOfLog">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Log" type="{http://request.workflow.soa.weaver}Log" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ArrayOfLog", namespace = "http://request.workflow.soa.weaver", propOrder = {
    "log"
})
public class ArrayOfLog {

    @XmlElement(name = "Log", nillable = true)
    protected List<Log> log;

    /**
     * Gets the value of the log property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the log property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getLog().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Log }
     *
     *
     */
    public List<Log> getLog() {
        if (log == null) {
            log = new ArrayList<Log>();
        }
        return this.log;
    }

}
