
package com.tce.smart.data.webservice.newservice;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>RequestManager complex type的 Java 类。
 *
 * <p>以下模式片段指定包含在此类中的预期内容。
 *
 * <pre>
 * &lt;complexType name="RequestManager">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="autoApproveNodeCache" type="{http://localhost/services/RequestService}ArrayOfInt" minOccurs="0"/>
 *         &lt;element name="beAgenter" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="billTableName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="billid" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="canModify" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="chatsType" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="clientType" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="coadsigntype" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="createdate" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="creater" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="creatertype" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="createtime" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="currentDate" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="currentOperator" type="{http://localhost/services/RequestService}anyType2anyType2anyTypeMapMap" minOccurs="0"/>
 *         &lt;element name="currentTime" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="doAutoApprove" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="docrowindex" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="enableIntervenor" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="formid" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="fromWebservice" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="handWrittenSign" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="hasEflowToAssignNode" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="hasTriggeredSubwf" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="intervenorid" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="intervenoridType" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ip" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="isAutoApprove" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="isAutoCommit" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="isAutoRemark" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="isFirstSubmit" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="isFromEditDocument" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="isMulSubmit" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="isPending" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="isagentCreater" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="isbill" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="iscreate" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="isremark" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="istest" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="lastNodeid" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="lastnodetype" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="lastoperatedate" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="lastoperatetime" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="lastoperator" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="lastoperatortype" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="logdate" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="logtime" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="message" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="messageType" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="messagecontent" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="messageid" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="needChooseOperator" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="needwfback" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="newAddDetailRowPerInfo" type="{http://localhost/services/RequestService}anyType2anyTypeMap" minOccurs="0"/>
 *         &lt;element name="nextNodeid" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="nextNodetype" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="nodeattribute" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="nodeid" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="nodelefttime" type="{http://www.w3.org/2001/XMLSchema}float" minOccurs="0"/>
 *         &lt;element name="nodepasstime" type="{http://www.w3.org/2001/XMLSchema}float" minOccurs="0"/>
 *         &lt;element name="nodetype" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="passedGroups" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="remark" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="remarkLocation" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="request" type="{http://http.servlet.javax}HttpServletRequest" minOccurs="0"/>
 *         &lt;element name="requestKey" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="requestid" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="requestlevel" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="requestname" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="signatureAppendfix" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="signdocids" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="signworkflowids" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="speechAttachment" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="src" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="status" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="submitNodeId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="totalGroups" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="user" type="{http://hrm.weaver}User" minOccurs="0"/>
 *         &lt;element name="userId" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="userType" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="workflowid" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="workflowtype" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RequestManager", namespace = "http://request.workflow.weaver", propOrder = {
    "autoApproveNodeCache",
    "beAgenter",
    "billTableName",
    "billid",
    "canModify",
    "chatsType",
    "clientType",
    "coadsigntype",
    "createdate",
    "creater",
    "creatertype",
    "createtime",
    "currentDate",
    "currentOperator",
    "currentTime",
    "doAutoApprove",
    "docrowindex",
    "enableIntervenor",
    "formid",
    "fromWebservice",
    "handWrittenSign",
    "hasEflowToAssignNode",
    "hasTriggeredSubwf",
    "intervenorid",
    "intervenoridType",
    "ip",
    "isAutoApprove",
    "isAutoCommit",
    "isAutoRemark",
    "isFirstSubmit",
    "isFromEditDocument",
    "isMulSubmit",
    "isPending",
    "isagentCreater",
    "isbill",
    "iscreate",
    "isremark",
    "istest",
    "lastNodeid",
    "lastnodetype",
    "lastoperatedate",
    "lastoperatetime",
    "lastoperator",
    "lastoperatortype",
    "logdate",
    "logtime",
    "message",
    "messageType",
    "messagecontent",
    "messageid",
    "needChooseOperator",
    "needwfback",
    "newAddDetailRowPerInfo",
    "nextNodeid",
    "nextNodetype",
    "nodeattribute",
    "nodeid",
    "nodelefttime",
    "nodepasstime",
    "nodetype",
    "passedGroups",
    "remark",
    "remarkLocation",
    "request",
    "requestKey",
    "requestid",
    "requestlevel",
    "requestname",
    "signatureAppendfix",
    "signdocids",
    "signworkflowids",
    "speechAttachment",
    "src",
    "status",
    "submitNodeId",
    "totalGroups",
    "user",
    "userId",
    "userType",
    "workflowid",
    "workflowtype"
})
public class RequestManager {

    @XmlElementRef(name = "autoApproveNodeCache", namespace = "http://request.workflow.weaver", type = JAXBElement.class, required = false)
    protected JAXBElement<ArrayOfInt> autoApproveNodeCache;
    protected Integer beAgenter;
    @XmlElementRef(name = "billTableName", namespace = "http://request.workflow.weaver", type = JAXBElement.class, required = false)
    protected JAXBElement<String> billTableName;
    protected Integer billid;
    protected Boolean canModify;
    @XmlElementRef(name = "chatsType", namespace = "http://request.workflow.weaver", type = JAXBElement.class, required = false)
    protected JAXBElement<String> chatsType;
    @XmlElementRef(name = "clientType", namespace = "http://request.workflow.weaver", type = JAXBElement.class, required = false)
    protected JAXBElement<String> clientType;
    @XmlElementRef(name = "coadsigntype", namespace = "http://request.workflow.weaver", type = JAXBElement.class, required = false)
    protected JAXBElement<String> coadsigntype;
    @XmlElementRef(name = "createdate", namespace = "http://request.workflow.weaver", type = JAXBElement.class, required = false)
    protected JAXBElement<String> createdate;
    protected Integer creater;
    protected Integer creatertype;
    @XmlElementRef(name = "createtime", namespace = "http://request.workflow.weaver", type = JAXBElement.class, required = false)
    protected JAXBElement<String> createtime;
    @XmlElementRef(name = "currentDate", namespace = "http://request.workflow.weaver", type = JAXBElement.class, required = false)
    protected JAXBElement<String> currentDate;
    @XmlElementRef(name = "currentOperator", namespace = "http://request.workflow.weaver", type = JAXBElement.class, required = false)
    protected JAXBElement<AnyType2AnyType2AnyTypeMapMap> currentOperator;
    @XmlElementRef(name = "currentTime", namespace = "http://request.workflow.weaver", type = JAXBElement.class, required = false)
    protected JAXBElement<String> currentTime;
    protected Boolean doAutoApprove;
    @XmlElementRef(name = "docrowindex", namespace = "http://request.workflow.weaver", type = JAXBElement.class, required = false)
    protected JAXBElement<String> docrowindex;
    protected Integer enableIntervenor;
    protected Integer formid;
    protected Boolean fromWebservice;
    protected Integer handWrittenSign;
    protected Boolean hasEflowToAssignNode;
    @XmlElementRef(name = "hasTriggeredSubwf", namespace = "http://request.workflow.weaver", type = JAXBElement.class, required = false)
    protected JAXBElement<String> hasTriggeredSubwf;
    @XmlElementRef(name = "intervenorid", namespace = "http://request.workflow.weaver", type = JAXBElement.class, required = false)
    protected JAXBElement<String> intervenorid;
    @XmlElementRef(name = "intervenoridType", namespace = "http://request.workflow.weaver", type = JAXBElement.class, required = false)
    protected JAXBElement<String> intervenoridType;
    @XmlElementRef(name = "ip", namespace = "http://request.workflow.weaver", type = JAXBElement.class, required = false)
    protected JAXBElement<String> ip;
    @XmlElementRef(name = "isAutoApprove", namespace = "http://request.workflow.weaver", type = JAXBElement.class, required = false)
    protected JAXBElement<String> isAutoApprove;
    @XmlElementRef(name = "isAutoCommit", namespace = "http://request.workflow.weaver", type = JAXBElement.class, required = false)
    protected JAXBElement<String> isAutoCommit;
    @XmlElementRef(name = "isAutoRemark", namespace = "http://request.workflow.weaver", type = JAXBElement.class, required = false)
    protected JAXBElement<String> isAutoRemark;
    @XmlElementRef(name = "isFirstSubmit", namespace = "http://request.workflow.weaver", type = JAXBElement.class, required = false)
    protected JAXBElement<String> isFirstSubmit;
    @XmlElementRef(name = "isFromEditDocument", namespace = "http://request.workflow.weaver", type = JAXBElement.class, required = false)
    protected JAXBElement<String> isFromEditDocument;
    protected Integer isMulSubmit;
    protected Integer isPending;
    protected Integer isagentCreater;
    protected Integer isbill;
    @XmlElementRef(name = "iscreate", namespace = "http://request.workflow.weaver", type = JAXBElement.class, required = false)
    protected JAXBElement<String> iscreate;
    protected Integer isremark;
    protected Integer istest;
    protected Integer lastNodeid;
    @XmlElementRef(name = "lastnodetype", namespace = "http://request.workflow.weaver", type = JAXBElement.class, required = false)
    protected JAXBElement<String> lastnodetype;
    @XmlElementRef(name = "lastoperatedate", namespace = "http://request.workflow.weaver", type = JAXBElement.class, required = false)
    protected JAXBElement<String> lastoperatedate;
    @XmlElementRef(name = "lastoperatetime", namespace = "http://request.workflow.weaver", type = JAXBElement.class, required = false)
    protected JAXBElement<String> lastoperatetime;
    protected Integer lastoperator;
    protected Integer lastoperatortype;
    @XmlElementRef(name = "logdate", namespace = "http://request.workflow.weaver", type = JAXBElement.class, required = false)
    protected JAXBElement<String> logdate;
    @XmlElementRef(name = "logtime", namespace = "http://request.workflow.weaver", type = JAXBElement.class, required = false)
    protected JAXBElement<String> logtime;
    @XmlElementRef(name = "message", namespace = "http://request.workflow.weaver", type = JAXBElement.class, required = false)
    protected JAXBElement<String> message;
    @XmlElementRef(name = "messageType", namespace = "http://request.workflow.weaver", type = JAXBElement.class, required = false)
    protected JAXBElement<String> messageType;
    @XmlElementRef(name = "messagecontent", namespace = "http://request.workflow.weaver", type = JAXBElement.class, required = false)
    protected JAXBElement<String> messagecontent;
    @XmlElementRef(name = "messageid", namespace = "http://request.workflow.weaver", type = JAXBElement.class, required = false)
    protected JAXBElement<String> messageid;
    protected Boolean needChooseOperator;
    @XmlElementRef(name = "needwfback", namespace = "http://request.workflow.weaver", type = JAXBElement.class, required = false)
    protected JAXBElement<String> needwfback;
    @XmlElementRef(name = "newAddDetailRowPerInfo", namespace = "http://request.workflow.weaver", type = JAXBElement.class, required = false)
    protected JAXBElement<AnyType2AnyTypeMap> newAddDetailRowPerInfo;
    protected Integer nextNodeid;
    @XmlElementRef(name = "nextNodetype", namespace = "http://request.workflow.weaver", type = JAXBElement.class, required = false)
    protected JAXBElement<String> nextNodetype;
    protected Integer nodeattribute;
    protected Integer nodeid;
    protected Float nodelefttime;
    protected Float nodepasstime;
    @XmlElementRef(name = "nodetype", namespace = "http://request.workflow.weaver", type = JAXBElement.class, required = false)
    protected JAXBElement<String> nodetype;
    protected Integer passedGroups;
    @XmlElementRef(name = "remark", namespace = "http://request.workflow.weaver", type = JAXBElement.class, required = false)
    protected JAXBElement<String> remark;
    @XmlElementRef(name = "remarkLocation", namespace = "http://request.workflow.weaver", type = JAXBElement.class, required = false)
    protected JAXBElement<String> remarkLocation;
    @XmlElementRef(name = "request", namespace = "http://request.workflow.weaver", type = JAXBElement.class, required = false)
    protected JAXBElement<HttpServletRequest> request;
    protected Integer requestKey;
    protected Integer requestid;
    @XmlElementRef(name = "requestlevel", namespace = "http://request.workflow.weaver", type = JAXBElement.class, required = false)
    protected JAXBElement<String> requestlevel;
    @XmlElementRef(name = "requestname", namespace = "http://request.workflow.weaver", type = JAXBElement.class, required = false)
    protected JAXBElement<String> requestname;
    @XmlElementRef(name = "signatureAppendfix", namespace = "http://request.workflow.weaver", type = JAXBElement.class, required = false)
    protected JAXBElement<String> signatureAppendfix;
    @XmlElementRef(name = "signdocids", namespace = "http://request.workflow.weaver", type = JAXBElement.class, required = false)
    protected JAXBElement<String> signdocids;
    @XmlElementRef(name = "signworkflowids", namespace = "http://request.workflow.weaver", type = JAXBElement.class, required = false)
    protected JAXBElement<String> signworkflowids;
    protected Integer speechAttachment;
    @XmlElementRef(name = "src", namespace = "http://request.workflow.weaver", type = JAXBElement.class, required = false)
    protected JAXBElement<String> src;
    @XmlElementRef(name = "status", namespace = "http://request.workflow.weaver", type = JAXBElement.class, required = false)
    protected JAXBElement<String> status;
    @XmlElementRef(name = "submitNodeId", namespace = "http://request.workflow.weaver", type = JAXBElement.class, required = false)
    protected JAXBElement<String> submitNodeId;
    protected Integer totalGroups;
    @XmlElementRef(name = "user", namespace = "http://request.workflow.weaver", type = JAXBElement.class, required = false)
    protected JAXBElement<User> user;
    protected Integer userId;
    protected Integer userType;
    protected Integer workflowid;
    @XmlElementRef(name = "workflowtype", namespace = "http://request.workflow.weaver", type = JAXBElement.class, required = false)
    protected JAXBElement<String> workflowtype;

    /**
     * 获取autoApproveNodeCache属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link ArrayOfInt }{@code >}
     *
     */
    public JAXBElement<ArrayOfInt> getAutoApproveNodeCache() {
        return autoApproveNodeCache;
    }

    /**
     * 设置autoApproveNodeCache属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link ArrayOfInt }{@code >}
     *
     */
    public void setAutoApproveNodeCache(JAXBElement<ArrayOfInt> value) {
        this.autoApproveNodeCache = value;
    }

    /**
     * 获取beAgenter属性的值。
     *
     * @return
     *     possible object is
     *     {@link Integer }
     *
     */
    public Integer getBeAgenter() {
        return beAgenter;
    }

    /**
     * 设置beAgenter属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link Integer }
     *
     */
    public void setBeAgenter(Integer value) {
        this.beAgenter = value;
    }

    /**
     * 获取billTableName属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getBillTableName() {
        return billTableName;
    }

    /**
     * 设置billTableName属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setBillTableName(JAXBElement<String> value) {
        this.billTableName = value;
    }

    /**
     * 获取billid属性的值。
     *
     * @return
     *     possible object is
     *     {@link Integer }
     *
     */
    public Integer getBillid() {
        return billid;
    }

    /**
     * 设置billid属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link Integer }
     *
     */
    public void setBillid(Integer value) {
        this.billid = value;
    }

    /**
     * 获取canModify属性的值。
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public Boolean isCanModify() {
        return canModify;
    }

    /**
     * 设置canModify属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *
     */
    public void setCanModify(Boolean value) {
        this.canModify = value;
    }

    /**
     * 获取chatsType属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getChatsType() {
        return chatsType;
    }

    /**
     * 设置chatsType属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setChatsType(JAXBElement<String> value) {
        this.chatsType = value;
    }

    /**
     * 获取clientType属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getClientType() {
        return clientType;
    }

    /**
     * 设置clientType属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setClientType(JAXBElement<String> value) {
        this.clientType = value;
    }

    /**
     * 获取coadsigntype属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getCoadsigntype() {
        return coadsigntype;
    }

    /**
     * 设置coadsigntype属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setCoadsigntype(JAXBElement<String> value) {
        this.coadsigntype = value;
    }

    /**
     * 获取createdate属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getCreatedate() {
        return createdate;
    }

    /**
     * 设置createdate属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setCreatedate(JAXBElement<String> value) {
        this.createdate = value;
    }

    /**
     * 获取creater属性的值。
     *
     * @return
     *     possible object is
     *     {@link Integer }
     *
     */
    public Integer getCreater() {
        return creater;
    }

    /**
     * 设置creater属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link Integer }
     *
     */
    public void setCreater(Integer value) {
        this.creater = value;
    }

    /**
     * 获取creatertype属性的值。
     *
     * @return
     *     possible object is
     *     {@link Integer }
     *
     */
    public Integer getCreatertype() {
        return creatertype;
    }

    /**
     * 设置creatertype属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link Integer }
     *
     */
    public void setCreatertype(Integer value) {
        this.creatertype = value;
    }

    /**
     * 获取createtime属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getCreatetime() {
        return createtime;
    }

    /**
     * 设置createtime属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setCreatetime(JAXBElement<String> value) {
        this.createtime = value;
    }

    /**
     * 获取currentDate属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getCurrentDate() {
        return currentDate;
    }

    /**
     * 设置currentDate属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setCurrentDate(JAXBElement<String> value) {
        this.currentDate = value;
    }

    /**
     * 获取currentOperator属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link AnyType2AnyType2AnyTypeMapMap }{@code >}
     *
     */
    public JAXBElement<AnyType2AnyType2AnyTypeMapMap> getCurrentOperator() {
        return currentOperator;
    }

    /**
     * 设置currentOperator属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link AnyType2AnyType2AnyTypeMapMap }{@code >}
     *
     */
    public void setCurrentOperator(JAXBElement<AnyType2AnyType2AnyTypeMapMap> value) {
        this.currentOperator = value;
    }

    /**
     * 获取currentTime属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getCurrentTime() {
        return currentTime;
    }

    /**
     * 设置currentTime属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setCurrentTime(JAXBElement<String> value) {
        this.currentTime = value;
    }

    /**
     * 获取doAutoApprove属性的值。
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public Boolean isDoAutoApprove() {
        return doAutoApprove;
    }

    /**
     * 设置doAutoApprove属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *
     */
    public void setDoAutoApprove(Boolean value) {
        this.doAutoApprove = value;
    }

    /**
     * 获取docrowindex属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getDocrowindex() {
        return docrowindex;
    }

    /**
     * 设置docrowindex属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setDocrowindex(JAXBElement<String> value) {
        this.docrowindex = value;
    }

    /**
     * 获取enableIntervenor属性的值。
     *
     * @return
     *     possible object is
     *     {@link Integer }
     *
     */
    public Integer getEnableIntervenor() {
        return enableIntervenor;
    }

    /**
     * 设置enableIntervenor属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link Integer }
     *
     */
    public void setEnableIntervenor(Integer value) {
        this.enableIntervenor = value;
    }

    /**
     * 获取formid属性的值。
     *
     * @return
     *     possible object is
     *     {@link Integer }
     *
     */
    public Integer getFormid() {
        return formid;
    }

    /**
     * 设置formid属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link Integer }
     *
     */
    public void setFormid(Integer value) {
        this.formid = value;
    }

    /**
     * 获取fromWebservice属性的值。
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public Boolean isFromWebservice() {
        return fromWebservice;
    }

    /**
     * 设置fromWebservice属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *
     */
    public void setFromWebservice(Boolean value) {
        this.fromWebservice = value;
    }

    /**
     * 获取handWrittenSign属性的值。
     *
     * @return
     *     possible object is
     *     {@link Integer }
     *
     */
    public Integer getHandWrittenSign() {
        return handWrittenSign;
    }

    /**
     * 设置handWrittenSign属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link Integer }
     *
     */
    public void setHandWrittenSign(Integer value) {
        this.handWrittenSign = value;
    }

    /**
     * 获取hasEflowToAssignNode属性的值。
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public Boolean isHasEflowToAssignNode() {
        return hasEflowToAssignNode;
    }

    /**
     * 设置hasEflowToAssignNode属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *
     */
    public void setHasEflowToAssignNode(Boolean value) {
        this.hasEflowToAssignNode = value;
    }

    /**
     * 获取hasTriggeredSubwf属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getHasTriggeredSubwf() {
        return hasTriggeredSubwf;
    }

    /**
     * 设置hasTriggeredSubwf属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setHasTriggeredSubwf(JAXBElement<String> value) {
        this.hasTriggeredSubwf = value;
    }

    /**
     * 获取intervenorid属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getIntervenorid() {
        return intervenorid;
    }

    /**
     * 设置intervenorid属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setIntervenorid(JAXBElement<String> value) {
        this.intervenorid = value;
    }

    /**
     * 获取intervenoridType属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getIntervenoridType() {
        return intervenoridType;
    }

    /**
     * 设置intervenoridType属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setIntervenoridType(JAXBElement<String> value) {
        this.intervenoridType = value;
    }

    /**
     * 获取ip属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getIp() {
        return ip;
    }

    /**
     * 设置ip属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setIp(JAXBElement<String> value) {
        this.ip = value;
    }

    /**
     * 获取isAutoApprove属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getIsAutoApprove() {
        return isAutoApprove;
    }

    /**
     * 设置isAutoApprove属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setIsAutoApprove(JAXBElement<String> value) {
        this.isAutoApprove = value;
    }

    /**
     * 获取isAutoCommit属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getIsAutoCommit() {
        return isAutoCommit;
    }

    /**
     * 设置isAutoCommit属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setIsAutoCommit(JAXBElement<String> value) {
        this.isAutoCommit = value;
    }

    /**
     * 获取isAutoRemark属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getIsAutoRemark() {
        return isAutoRemark;
    }

    /**
     * 设置isAutoRemark属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setIsAutoRemark(JAXBElement<String> value) {
        this.isAutoRemark = value;
    }

    /**
     * 获取isFirstSubmit属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getIsFirstSubmit() {
        return isFirstSubmit;
    }

    /**
     * 设置isFirstSubmit属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setIsFirstSubmit(JAXBElement<String> value) {
        this.isFirstSubmit = value;
    }

    /**
     * 获取isFromEditDocument属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getIsFromEditDocument() {
        return isFromEditDocument;
    }

    /**
     * 设置isFromEditDocument属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setIsFromEditDocument(JAXBElement<String> value) {
        this.isFromEditDocument = value;
    }

    /**
     * 获取isMulSubmit属性的值。
     *
     * @return
     *     possible object is
     *     {@link Integer }
     *
     */
    public Integer getIsMulSubmit() {
        return isMulSubmit;
    }

    /**
     * 设置isMulSubmit属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link Integer }
     *
     */
    public void setIsMulSubmit(Integer value) {
        this.isMulSubmit = value;
    }

    /**
     * 获取isPending属性的值。
     *
     * @return
     *     possible object is
     *     {@link Integer }
     *
     */
    public Integer getIsPending() {
        return isPending;
    }

    /**
     * 设置isPending属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link Integer }
     *
     */
    public void setIsPending(Integer value) {
        this.isPending = value;
    }

    /**
     * 获取isagentCreater属性的值。
     *
     * @return
     *     possible object is
     *     {@link Integer }
     *
     */
    public Integer getIsagentCreater() {
        return isagentCreater;
    }

    /**
     * 设置isagentCreater属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link Integer }
     *
     */
    public void setIsagentCreater(Integer value) {
        this.isagentCreater = value;
    }

    /**
     * 获取isbill属性的值。
     *
     * @return
     *     possible object is
     *     {@link Integer }
     *
     */
    public Integer getIsbill() {
        return isbill;
    }

    /**
     * 设置isbill属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link Integer }
     *
     */
    public void setIsbill(Integer value) {
        this.isbill = value;
    }

    /**
     * 获取iscreate属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getIscreate() {
        return iscreate;
    }

    /**
     * 设置iscreate属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setIscreate(JAXBElement<String> value) {
        this.iscreate = value;
    }

    /**
     * 获取isremark属性的值。
     *
     * @return
     *     possible object is
     *     {@link Integer }
     *
     */
    public Integer getIsremark() {
        return isremark;
    }

    /**
     * 设置isremark属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link Integer }
     *
     */
    public void setIsremark(Integer value) {
        this.isremark = value;
    }

    /**
     * 获取istest属性的值。
     *
     * @return
     *     possible object is
     *     {@link Integer }
     *
     */
    public Integer getIstest() {
        return istest;
    }

    /**
     * 设置istest属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link Integer }
     *
     */
    public void setIstest(Integer value) {
        this.istest = value;
    }

    /**
     * 获取lastNodeid属性的值。
     *
     * @return
     *     possible object is
     *     {@link Integer }
     *
     */
    public Integer getLastNodeid() {
        return lastNodeid;
    }

    /**
     * 设置lastNodeid属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link Integer }
     *
     */
    public void setLastNodeid(Integer value) {
        this.lastNodeid = value;
    }

    /**
     * 获取lastnodetype属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getLastnodetype() {
        return lastnodetype;
    }

    /**
     * 设置lastnodetype属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setLastnodetype(JAXBElement<String> value) {
        this.lastnodetype = value;
    }

    /**
     * 获取lastoperatedate属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getLastoperatedate() {
        return lastoperatedate;
    }

    /**
     * 设置lastoperatedate属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setLastoperatedate(JAXBElement<String> value) {
        this.lastoperatedate = value;
    }

    /**
     * 获取lastoperatetime属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getLastoperatetime() {
        return lastoperatetime;
    }

    /**
     * 设置lastoperatetime属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setLastoperatetime(JAXBElement<String> value) {
        this.lastoperatetime = value;
    }

    /**
     * 获取lastoperator属性的值。
     *
     * @return
     *     possible object is
     *     {@link Integer }
     *
     */
    public Integer getLastoperator() {
        return lastoperator;
    }

    /**
     * 设置lastoperator属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link Integer }
     *
     */
    public void setLastoperator(Integer value) {
        this.lastoperator = value;
    }

    /**
     * 获取lastoperatortype属性的值。
     *
     * @return
     *     possible object is
     *     {@link Integer }
     *
     */
    public Integer getLastoperatortype() {
        return lastoperatortype;
    }

    /**
     * 设置lastoperatortype属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link Integer }
     *
     */
    public void setLastoperatortype(Integer value) {
        this.lastoperatortype = value;
    }

    /**
     * 获取logdate属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getLogdate() {
        return logdate;
    }

    /**
     * 设置logdate属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setLogdate(JAXBElement<String> value) {
        this.logdate = value;
    }

    /**
     * 获取logtime属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getLogtime() {
        return logtime;
    }

    /**
     * 设置logtime属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setLogtime(JAXBElement<String> value) {
        this.logtime = value;
    }

    /**
     * 获取message属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getMessage() {
        return message;
    }

    /**
     * 设置message属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setMessage(JAXBElement<String> value) {
        this.message = value;
    }

    /**
     * 获取messageType属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getMessageType() {
        return messageType;
    }

    /**
     * 设置messageType属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setMessageType(JAXBElement<String> value) {
        this.messageType = value;
    }

    /**
     * 获取messagecontent属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getMessagecontent() {
        return messagecontent;
    }

    /**
     * 设置messagecontent属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setMessagecontent(JAXBElement<String> value) {
        this.messagecontent = value;
    }

    /**
     * 获取messageid属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getMessageid() {
        return messageid;
    }

    /**
     * 设置messageid属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setMessageid(JAXBElement<String> value) {
        this.messageid = value;
    }

    /**
     * 获取needChooseOperator属性的值。
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public Boolean isNeedChooseOperator() {
        return needChooseOperator;
    }

    /**
     * 设置needChooseOperator属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *
     */
    public void setNeedChooseOperator(Boolean value) {
        this.needChooseOperator = value;
    }

    /**
     * 获取needwfback属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getNeedwfback() {
        return needwfback;
    }

    /**
     * 设置needwfback属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setNeedwfback(JAXBElement<String> value) {
        this.needwfback = value;
    }

    /**
     * 获取newAddDetailRowPerInfo属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link AnyType2AnyTypeMap }{@code >}
     *
     */
    public JAXBElement<AnyType2AnyTypeMap> getNewAddDetailRowPerInfo() {
        return newAddDetailRowPerInfo;
    }

    /**
     * 设置newAddDetailRowPerInfo属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link AnyType2AnyTypeMap }{@code >}
     *
     */
    public void setNewAddDetailRowPerInfo(JAXBElement<AnyType2AnyTypeMap> value) {
        this.newAddDetailRowPerInfo = value;
    }

    /**
     * 获取nextNodeid属性的值。
     *
     * @return
     *     possible object is
     *     {@link Integer }
     *
     */
    public Integer getNextNodeid() {
        return nextNodeid;
    }

    /**
     * 设置nextNodeid属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link Integer }
     *
     */
    public void setNextNodeid(Integer value) {
        this.nextNodeid = value;
    }

    /**
     * 获取nextNodetype属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getNextNodetype() {
        return nextNodetype;
    }

    /**
     * 设置nextNodetype属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setNextNodetype(JAXBElement<String> value) {
        this.nextNodetype = value;
    }

    /**
     * 获取nodeattribute属性的值。
     *
     * @return
     *     possible object is
     *     {@link Integer }
     *
     */
    public Integer getNodeattribute() {
        return nodeattribute;
    }

    /**
     * 设置nodeattribute属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link Integer }
     *
     */
    public void setNodeattribute(Integer value) {
        this.nodeattribute = value;
    }

    /**
     * 获取nodeid属性的值。
     *
     * @return
     *     possible object is
     *     {@link Integer }
     *
     */
    public Integer getNodeid() {
        return nodeid;
    }

    /**
     * 设置nodeid属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link Integer }
     *
     */
    public void setNodeid(Integer value) {
        this.nodeid = value;
    }

    /**
     * 获取nodelefttime属性的值。
     *
     * @return
     *     possible object is
     *     {@link Float }
     *
     */
    public Float getNodelefttime() {
        return nodelefttime;
    }

    /**
     * 设置nodelefttime属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link Float }
     *
     */
    public void setNodelefttime(Float value) {
        this.nodelefttime = value;
    }

    /**
     * 获取nodepasstime属性的值。
     *
     * @return
     *     possible object is
     *     {@link Float }
     *
     */
    public Float getNodepasstime() {
        return nodepasstime;
    }

    /**
     * 设置nodepasstime属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link Float }
     *
     */
    public void setNodepasstime(Float value) {
        this.nodepasstime = value;
    }

    /**
     * 获取nodetype属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getNodetype() {
        return nodetype;
    }

    /**
     * 设置nodetype属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setNodetype(JAXBElement<String> value) {
        this.nodetype = value;
    }

    /**
     * 获取passedGroups属性的值。
     *
     * @return
     *     possible object is
     *     {@link Integer }
     *
     */
    public Integer getPassedGroups() {
        return passedGroups;
    }

    /**
     * 设置passedGroups属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link Integer }
     *
     */
    public void setPassedGroups(Integer value) {
        this.passedGroups = value;
    }

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
     * 获取remarkLocation属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getRemarkLocation() {
        return remarkLocation;
    }

    /**
     * 设置remarkLocation属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setRemarkLocation(JAXBElement<String> value) {
        this.remarkLocation = value;
    }

    /**
     * 获取request属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link HttpServletRequest }{@code >}
     *
     */
    public JAXBElement<HttpServletRequest> getRequest() {
        return request;
    }

    /**
     * 设置request属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link HttpServletRequest }{@code >}
     *
     */
    public void setRequest(JAXBElement<HttpServletRequest> value) {
        this.request = value;
    }

    /**
     * 获取requestKey属性的值。
     *
     * @return
     *     possible object is
     *     {@link Integer }
     *
     */
    public Integer getRequestKey() {
        return requestKey;
    }

    /**
     * 设置requestKey属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link Integer }
     *
     */
    public void setRequestKey(Integer value) {
        this.requestKey = value;
    }

    /**
     * 获取requestid属性的值。
     *
     * @return
     *     possible object is
     *     {@link Integer }
     *
     */
    public Integer getRequestid() {
        return requestid;
    }

    /**
     * 设置requestid属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link Integer }
     *
     */
    public void setRequestid(Integer value) {
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
     * 获取requestname属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getRequestname() {
        return requestname;
    }

    /**
     * 设置requestname属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setRequestname(JAXBElement<String> value) {
        this.requestname = value;
    }

    /**
     * 获取signatureAppendfix属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getSignatureAppendfix() {
        return signatureAppendfix;
    }

    /**
     * 设置signatureAppendfix属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setSignatureAppendfix(JAXBElement<String> value) {
        this.signatureAppendfix = value;
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

    /**
     * 获取speechAttachment属性的值。
     *
     * @return
     *     possible object is
     *     {@link Integer }
     *
     */
    public Integer getSpeechAttachment() {
        return speechAttachment;
    }

    /**
     * 设置speechAttachment属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link Integer }
     *
     */
    public void setSpeechAttachment(Integer value) {
        this.speechAttachment = value;
    }

    /**
     * 获取src属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getSrc() {
        return src;
    }

    /**
     * 设置src属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setSrc(JAXBElement<String> value) {
        this.src = value;
    }

    /**
     * 获取status属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getStatus() {
        return status;
    }

    /**
     * 设置status属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setStatus(JAXBElement<String> value) {
        this.status = value;
    }

    /**
     * 获取submitNodeId属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getSubmitNodeId() {
        return submitNodeId;
    }

    /**
     * 设置submitNodeId属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setSubmitNodeId(JAXBElement<String> value) {
        this.submitNodeId = value;
    }

    /**
     * 获取totalGroups属性的值。
     *
     * @return
     *     possible object is
     *     {@link Integer }
     *
     */
    public Integer getTotalGroups() {
        return totalGroups;
    }

    /**
     * 设置totalGroups属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link Integer }
     *
     */
    public void setTotalGroups(Integer value) {
        this.totalGroups = value;
    }

    /**
     * 获取user属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link User }{@code >}
     *
     */
    public JAXBElement<User> getUser() {
        return user;
    }

    /**
     * 设置user属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link User }{@code >}
     *
     */
    public void setUser(JAXBElement<User> value) {
        this.user = value;
    }

    /**
     * 获取userId属性的值。
     *
     * @return
     *     possible object is
     *     {@link Integer }
     *
     */
    public Integer getUserId() {
        return userId;
    }

    /**
     * 设置userId属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link Integer }
     *
     */
    public void setUserId(Integer value) {
        this.userId = value;
    }

    /**
     * 获取userType属性的值。
     *
     * @return
     *     possible object is
     *     {@link Integer }
     *
     */
    public Integer getUserType() {
        return userType;
    }

    /**
     * 设置userType属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link Integer }
     *
     */
    public void setUserType(Integer value) {
        this.userType = value;
    }

    /**
     * 获取workflowid属性的值。
     *
     * @return
     *     possible object is
     *     {@link Integer }
     *
     */
    public Integer getWorkflowid() {
        return workflowid;
    }

    /**
     * 设置workflowid属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link Integer }
     *
     */
    public void setWorkflowid(Integer value) {
        this.workflowid = value;
    }

    /**
     * 获取workflowtype属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getWorkflowtype() {
        return workflowtype;
    }

    /**
     * 设置workflowtype属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setWorkflowtype(JAXBElement<String> value) {
        this.workflowtype = value;
    }

}
