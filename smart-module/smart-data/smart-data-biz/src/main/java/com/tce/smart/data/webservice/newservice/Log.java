
package com.tce.smart.data.webservice.newservice;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Log complex type的 Java 类。
 *
 * <p>以下模式片段指定包含在此类中的预期内容。
 *
 * <pre>
 * &lt;complexType name="Log">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="agentorbyagentid" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="agenttype" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="annexdocids" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="comment" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="id" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="node" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="nodeid" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="opdate" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="operator" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="operatordept" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="operatorid" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="operatortype" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="optime" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="optype" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="receiver" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="requestLogId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="signdocids" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="signworkflowids" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Log", namespace = "http://request.workflow.soa.weaver", propOrder = {
    "agentorbyagentid",
    "agenttype",
    "annexdocids",
    "comment",
    "id",
    "node",
    "nodeid",
    "opdate",
    "operator",
    "operatordept",
    "operatorid",
    "operatortype",
    "optime",
    "optype",
    "receiver",
    "requestLogId",
    "signdocids",
    "signworkflowids"
})
public class Log {

    @XmlElementRef(name = "agentorbyagentid", namespace = "http://request.workflow.soa.weaver", type = JAXBElement.class, required = false)
    protected JAXBElement<String> agentorbyagentid;
    @XmlElementRef(name = "agenttype", namespace = "http://request.workflow.soa.weaver", type = JAXBElement.class, required = false)
    protected JAXBElement<String> agenttype;
    @XmlElementRef(name = "annexdocids", namespace = "http://request.workflow.soa.weaver", type = JAXBElement.class, required = false)
    protected JAXBElement<String> annexdocids;
    @XmlElementRef(name = "comment", namespace = "http://request.workflow.soa.weaver", type = JAXBElement.class, required = false)
    protected JAXBElement<String> comment;
    @XmlElementRef(name = "id", namespace = "http://request.workflow.soa.weaver", type = JAXBElement.class, required = false)
    protected JAXBElement<String> id;
    @XmlElementRef(name = "node", namespace = "http://request.workflow.soa.weaver", type = JAXBElement.class, required = false)
    protected JAXBElement<String> node;
    @XmlElementRef(name = "nodeid", namespace = "http://request.workflow.soa.weaver", type = JAXBElement.class, required = false)
    protected JAXBElement<String> nodeid;
    @XmlElementRef(name = "opdate", namespace = "http://request.workflow.soa.weaver", type = JAXBElement.class, required = false)
    protected JAXBElement<String> opdate;
    @XmlElementRef(name = "operator", namespace = "http://request.workflow.soa.weaver", type = JAXBElement.class, required = false)
    protected JAXBElement<String> operator;
    @XmlElementRef(name = "operatordept", namespace = "http://request.workflow.soa.weaver", type = JAXBElement.class, required = false)
    protected JAXBElement<String> operatordept;
    @XmlElementRef(name = "operatorid", namespace = "http://request.workflow.soa.weaver", type = JAXBElement.class, required = false)
    protected JAXBElement<String> operatorid;
    @XmlElementRef(name = "operatortype", namespace = "http://request.workflow.soa.weaver", type = JAXBElement.class, required = false)
    protected JAXBElement<String> operatortype;
    @XmlElementRef(name = "optime", namespace = "http://request.workflow.soa.weaver", type = JAXBElement.class, required = false)
    protected JAXBElement<String> optime;
    @XmlElementRef(name = "optype", namespace = "http://request.workflow.soa.weaver", type = JAXBElement.class, required = false)
    protected JAXBElement<String> optype;
    @XmlElementRef(name = "receiver", namespace = "http://request.workflow.soa.weaver", type = JAXBElement.class, required = false)
    protected JAXBElement<String> receiver;
    @XmlElementRef(name = "requestLogId", namespace = "http://request.workflow.soa.weaver", type = JAXBElement.class, required = false)
    protected JAXBElement<String> requestLogId;
    @XmlElementRef(name = "signdocids", namespace = "http://request.workflow.soa.weaver", type = JAXBElement.class, required = false)
    protected JAXBElement<String> signdocids;
    @XmlElementRef(name = "signworkflowids", namespace = "http://request.workflow.soa.weaver", type = JAXBElement.class, required = false)
    protected JAXBElement<String> signworkflowids;

    /**
     * 获取agentorbyagentid属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getAgentorbyagentid() {
        return agentorbyagentid;
    }

    /**
     * 设置agentorbyagentid属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setAgentorbyagentid(JAXBElement<String> value) {
        this.agentorbyagentid = value;
    }

    /**
     * 获取agenttype属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getAgenttype() {
        return agenttype;
    }

    /**
     * 设置agenttype属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setAgenttype(JAXBElement<String> value) {
        this.agenttype = value;
    }

    /**
     * 获取annexdocids属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getAnnexdocids() {
        return annexdocids;
    }

    /**
     * 设置annexdocids属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setAnnexdocids(JAXBElement<String> value) {
        this.annexdocids = value;
    }

    /**
     * 获取comment属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getComment() {
        return comment;
    }

    /**
     * 设置comment属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setComment(JAXBElement<String> value) {
        this.comment = value;
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
     * 获取node属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getNode() {
        return node;
    }

    /**
     * 设置node属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setNode(JAXBElement<String> value) {
        this.node = value;
    }

    /**
     * 获取nodeid属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getNodeid() {
        return nodeid;
    }

    /**
     * 设置nodeid属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setNodeid(JAXBElement<String> value) {
        this.nodeid = value;
    }

    /**
     * 获取opdate属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getOpdate() {
        return opdate;
    }

    /**
     * 设置opdate属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setOpdate(JAXBElement<String> value) {
        this.opdate = value;
    }

    /**
     * 获取operator属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getOperator() {
        return operator;
    }

    /**
     * 设置operator属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setOperator(JAXBElement<String> value) {
        this.operator = value;
    }

    /**
     * 获取operatordept属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getOperatordept() {
        return operatordept;
    }

    /**
     * 设置operatordept属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setOperatordept(JAXBElement<String> value) {
        this.operatordept = value;
    }

    /**
     * 获取operatorid属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getOperatorid() {
        return operatorid;
    }

    /**
     * 设置operatorid属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setOperatorid(JAXBElement<String> value) {
        this.operatorid = value;
    }

    /**
     * 获取operatortype属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getOperatortype() {
        return operatortype;
    }

    /**
     * 设置operatortype属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setOperatortype(JAXBElement<String> value) {
        this.operatortype = value;
    }

    /**
     * 获取optime属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getOptime() {
        return optime;
    }

    /**
     * 设置optime属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setOptime(JAXBElement<String> value) {
        this.optime = value;
    }

    /**
     * 获取optype属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getOptype() {
        return optype;
    }

    /**
     * 设置optype属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setOptype(JAXBElement<String> value) {
        this.optype = value;
    }

    /**
     * 获取receiver属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getReceiver() {
        return receiver;
    }

    /**
     * 设置receiver属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setReceiver(JAXBElement<String> value) {
        this.receiver = value;
    }

    /**
     * 获取requestLogId属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getRequestLogId() {
        return requestLogId;
    }

    /**
     * 设置requestLogId属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setRequestLogId(JAXBElement<String> value) {
        this.requestLogId = value;
    }

    /**
     * 获取signdocids属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getSigndocids() {
        return signdocids;
    }

    /**
     * 设置signdocids属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setSigndocids(JAXBElement<String> value) {
        this.signdocids = value;
    }

    /**
     * 获取signworkflowids属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getSignworkflowids() {
        return signworkflowids;
    }

    /**
     * 设置signworkflowids属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setSignworkflowids(JAXBElement<String> value) {
        this.signworkflowids = value;
    }

}
