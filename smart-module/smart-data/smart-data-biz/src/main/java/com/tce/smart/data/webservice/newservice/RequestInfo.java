
package com.tce.smart.data.webservice.newservice;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>RequestInfo complex type的 Java 类。
 *
 * <p>以下模式片段指定包含在此类中的预期内容。
 *
 * <pre>
 * &lt;complexType name="RequestInfo">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="_Remark" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="creatorid" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="description" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="detailTableInfo" type="{http://request.workflow.soa.weaver}DetailTableInfo" minOccurs="0"/>
 *         &lt;element name="hostid" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="isNextFlow" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ispreadd" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="lastoperator" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="mainTableInfo" type="{http://request.workflow.soa.weaver}MainTableInfo" minOccurs="0"/>
 *         &lt;element name="objid" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="objtype" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="remindtype" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="requestLog" type="{http://request.workflow.soa.weaver}RequestLog" minOccurs="0"/>
 *         &lt;element name="requestManager" type="{http://request.workflow.weaver}RequestManager" minOccurs="0"/>
 *         &lt;element name="requestid" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="requestlevel" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="workflowid" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RequestInfo", namespace = "http://request.workflow.soa.weaver", propOrder = {
    "remark",
    "creatorid",
    "description",
    "detailTableInfo",
    "hostid",
    "isNextFlow",
    "ispreadd",
    "lastoperator",
    "mainTableInfo",
    "objid",
    "objtype",
    "remindtype",
    "requestLog",
    "requestManager",
    "requestid",
    "requestlevel",
    "workflowid"
})
public class RequestInfo {

    @XmlElementRef(name = "_Remark", namespace = "http://request.workflow.soa.weaver", type = JAXBElement.class, required = false)
    protected JAXBElement<String> remark;
    @XmlElementRef(name = "creatorid", namespace = "http://request.workflow.soa.weaver", type = JAXBElement.class, required = false)
    protected JAXBElement<String> creatorid;
    @XmlElementRef(name = "description", namespace = "http://request.workflow.soa.weaver", type = JAXBElement.class, required = false)
    protected JAXBElement<String> description;
    @XmlElementRef(name = "detailTableInfo", namespace = "http://request.workflow.soa.weaver", type = JAXBElement.class, required = false)
    protected JAXBElement<DetailTableInfo> detailTableInfo;
    @XmlElementRef(name = "hostid", namespace = "http://request.workflow.soa.weaver", type = JAXBElement.class, required = false)
    protected JAXBElement<String> hostid;
    @XmlElementRef(name = "isNextFlow", namespace = "http://request.workflow.soa.weaver", type = JAXBElement.class, required = false)
    protected JAXBElement<String> isNextFlow;
    @XmlElementRef(name = "ispreadd", namespace = "http://request.workflow.soa.weaver", type = JAXBElement.class, required = false)
    protected JAXBElement<String> ispreadd;
    @XmlElementRef(name = "lastoperator", namespace = "http://request.workflow.soa.weaver", type = JAXBElement.class, required = false)
    protected JAXBElement<String> lastoperator;
    @XmlElementRef(name = "mainTableInfo", namespace = "http://request.workflow.soa.weaver", type = JAXBElement.class, required = false)
    protected JAXBElement<MainTableInfo> mainTableInfo;
    protected Integer objid;
    protected Integer objtype;
    @XmlElementRef(name = "remindtype", namespace = "http://request.workflow.soa.weaver", type = JAXBElement.class, required = false)
    protected JAXBElement<String> remindtype;
    @XmlElementRef(name = "requestLog", namespace = "http://request.workflow.soa.weaver", type = JAXBElement.class, required = false)
    protected JAXBElement<RequestLog> requestLog;
    @XmlElementRef(name = "requestManager", namespace = "http://request.workflow.soa.weaver", type = JAXBElement.class, required = false)
    protected JAXBElement<RequestManager> requestManager;
    @XmlElementRef(name = "requestid", namespace = "http://request.workflow.soa.weaver", type = JAXBElement.class, required = false)
    protected JAXBElement<String> requestid;
    @XmlElementRef(name = "requestlevel", namespace = "http://request.workflow.soa.weaver", type = JAXBElement.class, required = false)
    protected JAXBElement<String> requestlevel;
    @XmlElementRef(name = "workflowid", namespace = "http://request.workflow.soa.weaver", type = JAXBElement.class, required = false)
    protected JAXBElement<String> workflowid;

    /**
     * 获取remark属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getRemark() {
        return remark;
    }

    /**
     * 设置remark属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setRemark(JAXBElement<String> value) {
        this.remark = value;
    }

    /**
     * 获取creatorid属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getCreatorid() {
        return creatorid;
    }

    /**
     * 设置creatorid属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setCreatorid(JAXBElement<String> value) {
        this.creatorid = value;
    }

    /**
     * 获取description属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getDescription() {
        return description;
    }

    /**
     * 设置description属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setDescription(JAXBElement<String> value) {
        this.description = value;
    }

    /**
     * 获取detailTableInfo属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link DetailTableInfo }{@code >}
     *
     */
    public JAXBElement<DetailTableInfo> getDetailTableInfo() {
        return detailTableInfo;
    }

    /**
     * 设置detailTableInfo属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link DetailTableInfo }{@code >}
     *
     */
    public void setDetailTableInfo(JAXBElement<DetailTableInfo> value) {
        this.detailTableInfo = value;
    }

    /**
     * 获取hostid属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getHostid() {
        return hostid;
    }

    /**
     * 设置hostid属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setHostid(JAXBElement<String> value) {
        this.hostid = value;
    }

    /**
     * 获取isNextFlow属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getIsNextFlow() {
        return isNextFlow;
    }

    /**
     * 设置isNextFlow属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setIsNextFlow(JAXBElement<String> value) {
        this.isNextFlow = value;
    }

    /**
     * 获取ispreadd属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getIspreadd() {
        return ispreadd;
    }

    /**
     * 设置ispreadd属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setIspreadd(JAXBElement<String> value) {
        this.ispreadd = value;
    }

    /**
     * 获取lastoperator属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getLastoperator() {
        return lastoperator;
    }

    /**
     * 设置lastoperator属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setLastoperator(JAXBElement<String> value) {
        this.lastoperator = value;
    }

    /**
     * 获取mainTableInfo属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link MainTableInfo }{@code >}
     *
     */
    public JAXBElement<MainTableInfo> getMainTableInfo() {
        return mainTableInfo;
    }

    /**
     * 设置mainTableInfo属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link MainTableInfo }{@code >}
     *
     */
    public void setMainTableInfo(JAXBElement<MainTableInfo> value) {
        this.mainTableInfo = value;
    }

    /**
     * 获取objid属性的值。
     *
     * @return
     *     possible object is
     *     {@link Integer }
     *
     */
    public Integer getObjid() {
        return objid;
    }

    /**
     * 设置objid属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link Integer }
     *
     */
    public void setObjid(Integer value) {
        this.objid = value;
    }

    /**
     * 获取objtype属性的值。
     *
     * @return
     *     possible object is
     *     {@link Integer }
     *
     */
    public Integer getObjtype() {
        return objtype;
    }

    /**
     * 设置objtype属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link Integer }
     *
     */
    public void setObjtype(Integer value) {
        this.objtype = value;
    }

    /**
     * 获取remindtype属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getRemindtype() {
        return remindtype;
    }

    /**
     * 设置remindtype属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setRemindtype(JAXBElement<String> value) {
        this.remindtype = value;
    }

    /**
     * 获取requestLog属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link RequestLog }{@code >}
     *
     */
    public JAXBElement<RequestLog> getRequestLog() {
        return requestLog;
    }

    /**
     * 设置requestLog属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link RequestLog }{@code >}
     *
     */
    public void setRequestLog(JAXBElement<RequestLog> value) {
        this.requestLog = value;
    }

    /**
     * 获取requestManager属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link RequestManager }{@code >}
     *
     */
    public JAXBElement<RequestManager> getRequestManager() {
        return requestManager;
    }

    /**
     * 设置requestManager属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link RequestManager }{@code >}
     *
     */
    public void setRequestManager(JAXBElement<RequestManager> value) {
        this.requestManager = value;
    }

    /**
     * 获取requestid属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getRequestid() {
        return requestid;
    }

    /**
     * 设置requestid属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setRequestid(JAXBElement<String> value) {
        this.requestid = value;
    }

    /**
     * 获取requestlevel属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getRequestlevel() {
        return requestlevel;
    }

    /**
     * 设置requestlevel属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setRequestlevel(JAXBElement<String> value) {
        this.requestlevel = value;
    }

    /**
     * 获取workflowid属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getWorkflowid() {
        return workflowid;
    }

    /**
     * 设置workflowid属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setWorkflowid(JAXBElement<String> value) {
        this.workflowid = value;
    }

}
