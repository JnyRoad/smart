
package com.tce.smart.data.webservice.newservice;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>User complex type的 Java 类。
 *
 * <p>以下模式片段指定包含在此类中的预期内容。
 *
 * <pre>
 * &lt;complexType name="User">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="UID" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="account" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="account_type" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="admin" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="agent" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="aliasname" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="assistantid" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="belongtoids" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="city" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="contractdate" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="costcenterid" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="countryid" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="currencyid" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="email" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="enddate" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="firstname" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="jobactivity" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="jobgroup" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="joblevel" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="jobtitle" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="language" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="lastlogindate" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="lastname" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="locationid" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="loginid" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="loginip" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="logintype" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="managerid" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="mergeFlag" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="mobile" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="mobilecall" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="needusb" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="parentid" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="postcode" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="province" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="purchaselimit" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="pwd" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="receiveaddress" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="remark" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="resourcetype" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="seclevel" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="serial" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="sex" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="startdate" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="status" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="telephone" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="title" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="titlelocation" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="type" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="userDepartment" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="userSubCompany1" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="userSubCompany2" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="userSubCompany3" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="userSubCompany4" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="username" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "User", namespace = "http://hrm.weaver", propOrder = {
    "uid",
    "account",
    "accountType",
    "admin",
    "agent",
    "aliasname",
    "assistantid",
    "belongtoids",
    "city",
    "contractdate",
    "costcenterid",
    "countryid",
    "currencyid",
    "email",
    "enddate",
    "firstname",
    "jobactivity",
    "jobgroup",
    "joblevel",
    "jobtitle",
    "language",
    "lastlogindate",
    "lastname",
    "locationid",
    "loginid",
    "loginip",
    "logintype",
    "managerid",
    "mergeFlag",
    "mobile",
    "mobilecall",
    "needusb",
    "parentid",
    "postcode",
    "province",
    "purchaselimit",
    "pwd",
    "receiveaddress",
    "remark",
    "resourcetype",
    "seclevel",
    "serial",
    "sex",
    "startdate",
    "status",
    "telephone",
    "title",
    "titlelocation",
    "type",
    "userDepartment",
    "userSubCompany1",
    "userSubCompany2",
    "userSubCompany3",
    "userSubCompany4",
    "username"
})
public class User {

    @XmlElement(name = "UID")
    protected Integer uid;
    @XmlElementRef(name = "account", namespace = "http://hrm.weaver", type = JAXBElement.class, required = false)
    protected JAXBElement<String> account;
    @XmlElementRef(name = "account_type", namespace = "http://hrm.weaver", type = JAXBElement.class, required = false)
    protected JAXBElement<String> accountType;
    protected Boolean admin;
    protected Integer agent;
    @XmlElementRef(name = "aliasname", namespace = "http://hrm.weaver", type = JAXBElement.class, required = false)
    protected JAXBElement<String> aliasname;
    @XmlElementRef(name = "assistantid", namespace = "http://hrm.weaver", type = JAXBElement.class, required = false)
    protected JAXBElement<String> assistantid;
    @XmlElementRef(name = "belongtoids", namespace = "http://hrm.weaver", type = JAXBElement.class, required = false)
    protected JAXBElement<String> belongtoids;
    protected Integer city;
    @XmlElementRef(name = "contractdate", namespace = "http://hrm.weaver", type = JAXBElement.class, required = false)
    protected JAXBElement<String> contractdate;
    @XmlElementRef(name = "costcenterid", namespace = "http://hrm.weaver", type = JAXBElement.class, required = false)
    protected JAXBElement<String> costcenterid;
    @XmlElementRef(name = "countryid", namespace = "http://hrm.weaver", type = JAXBElement.class, required = false)
    protected JAXBElement<String> countryid;
    @XmlElementRef(name = "currencyid", namespace = "http://hrm.weaver", type = JAXBElement.class, required = false)
    protected JAXBElement<String> currencyid;
    @XmlElementRef(name = "email", namespace = "http://hrm.weaver", type = JAXBElement.class, required = false)
    protected JAXBElement<String> email;
    @XmlElementRef(name = "enddate", namespace = "http://hrm.weaver", type = JAXBElement.class, required = false)
    protected JAXBElement<String> enddate;
    @XmlElementRef(name = "firstname", namespace = "http://hrm.weaver", type = JAXBElement.class, required = false)
    protected JAXBElement<String> firstname;
    @XmlElementRef(name = "jobactivity", namespace = "http://hrm.weaver", type = JAXBElement.class, required = false)
    protected JAXBElement<String> jobactivity;
    @XmlElementRef(name = "jobgroup", namespace = "http://hrm.weaver", type = JAXBElement.class, required = false)
    protected JAXBElement<String> jobgroup;
    @XmlElementRef(name = "joblevel", namespace = "http://hrm.weaver", type = JAXBElement.class, required = false)
    protected JAXBElement<String> joblevel;
    @XmlElementRef(name = "jobtitle", namespace = "http://hrm.weaver", type = JAXBElement.class, required = false)
    protected JAXBElement<String> jobtitle;
    protected Integer language;
    @XmlElementRef(name = "lastlogindate", namespace = "http://hrm.weaver", type = JAXBElement.class, required = false)
    protected JAXBElement<String> lastlogindate;
    @XmlElementRef(name = "lastname", namespace = "http://hrm.weaver", type = JAXBElement.class, required = false)
    protected JAXBElement<String> lastname;
    @XmlElementRef(name = "locationid", namespace = "http://hrm.weaver", type = JAXBElement.class, required = false)
    protected JAXBElement<String> locationid;
    @XmlElementRef(name = "loginid", namespace = "http://hrm.weaver", type = JAXBElement.class, required = false)
    protected JAXBElement<String> loginid;
    @XmlElementRef(name = "loginip", namespace = "http://hrm.weaver", type = JAXBElement.class, required = false)
    protected JAXBElement<String> loginip;
    @XmlElementRef(name = "logintype", namespace = "http://hrm.weaver", type = JAXBElement.class, required = false)
    protected JAXBElement<String> logintype;
    @XmlElementRef(name = "managerid", namespace = "http://hrm.weaver", type = JAXBElement.class, required = false)
    protected JAXBElement<String> managerid;
    protected Boolean mergeFlag;
    @XmlElementRef(name = "mobile", namespace = "http://hrm.weaver", type = JAXBElement.class, required = false)
    protected JAXBElement<String> mobile;
    @XmlElementRef(name = "mobilecall", namespace = "http://hrm.weaver", type = JAXBElement.class, required = false)
    protected JAXBElement<String> mobilecall;
    protected Integer needusb;
    protected Integer parentid;
    @XmlElementRef(name = "postcode", namespace = "http://hrm.weaver", type = JAXBElement.class, required = false)
    protected JAXBElement<String> postcode;
    protected Integer province;
    @XmlElementRef(name = "purchaselimit", namespace = "http://hrm.weaver", type = JAXBElement.class, required = false)
    protected JAXBElement<String> purchaselimit;
    @XmlElementRef(name = "pwd", namespace = "http://hrm.weaver", type = JAXBElement.class, required = false)
    protected JAXBElement<String> pwd;
    @XmlElementRef(name = "receiveaddress", namespace = "http://hrm.weaver", type = JAXBElement.class, required = false)
    protected JAXBElement<String> receiveaddress;
    @XmlElementRef(name = "remark", namespace = "http://hrm.weaver", type = JAXBElement.class, required = false)
    protected JAXBElement<String> remark;
    @XmlElementRef(name = "resourcetype", namespace = "http://hrm.weaver", type = JAXBElement.class, required = false)
    protected JAXBElement<String> resourcetype;
    @XmlElementRef(name = "seclevel", namespace = "http://hrm.weaver", type = JAXBElement.class, required = false)
    protected JAXBElement<String> seclevel;
    @XmlElementRef(name = "serial", namespace = "http://hrm.weaver", type = JAXBElement.class, required = false)
    protected JAXBElement<String> serial;
    @XmlElementRef(name = "sex", namespace = "http://hrm.weaver", type = JAXBElement.class, required = false)
    protected JAXBElement<String> sex;
    @XmlElementRef(name = "startdate", namespace = "http://hrm.weaver", type = JAXBElement.class, required = false)
    protected JAXBElement<String> startdate;
    protected Integer status;
    @XmlElementRef(name = "telephone", namespace = "http://hrm.weaver", type = JAXBElement.class, required = false)
    protected JAXBElement<String> telephone;
    @XmlElementRef(name = "title", namespace = "http://hrm.weaver", type = JAXBElement.class, required = false)
    protected JAXBElement<String> title;
    @XmlElementRef(name = "titlelocation", namespace = "http://hrm.weaver", type = JAXBElement.class, required = false)
    protected JAXBElement<String> titlelocation;
    protected Integer type;
    protected Integer userDepartment;
    protected Integer userSubCompany1;
    protected Integer userSubCompany2;
    protected Integer userSubCompany3;
    protected Integer userSubCompany4;
    @XmlElementRef(name = "username", namespace = "http://hrm.weaver", type = JAXBElement.class, required = false)
    protected JAXBElement<String> username;

    /**
     * 获取uid属性的值。
     *
     * @return
     *     possible object is
     *     {@link Integer }
     *
     */
    public Integer getUID() {
        return uid;
    }

    /**
     * 设置uid属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link Integer }
     *
     */
    public void setUID(Integer value) {
        this.uid = value;
    }

    /**
     * 获取account属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getAccount() {
        return account;
    }

    /**
     * 设置account属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setAccount(JAXBElement<String> value) {
        this.account = value;
    }

    /**
     * 获取accountType属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getAccountType() {
        return accountType;
    }

    /**
     * 设置accountType属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setAccountType(JAXBElement<String> value) {
        this.accountType = value;
    }

    /**
     * 获取admin属性的值。
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public Boolean isAdmin() {
        return admin;
    }

    /**
     * 设置admin属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *
     */
    public void setAdmin(Boolean value) {
        this.admin = value;
    }

    /**
     * 获取agent属性的值。
     *
     * @return
     *     possible object is
     *     {@link Integer }
     *
     */
    public Integer getAgent() {
        return agent;
    }

    /**
     * 设置agent属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link Integer }
     *
     */
    public void setAgent(Integer value) {
        this.agent = value;
    }

    /**
     * 获取aliasname属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getAliasname() {
        return aliasname;
    }

    /**
     * 设置aliasname属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setAliasname(JAXBElement<String> value) {
        this.aliasname = value;
    }

    /**
     * 获取assistantid属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getAssistantid() {
        return assistantid;
    }

    /**
     * 设置assistantid属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setAssistantid(JAXBElement<String> value) {
        this.assistantid = value;
    }

    /**
     * 获取belongtoids属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getBelongtoids() {
        return belongtoids;
    }

    /**
     * 设置belongtoids属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setBelongtoids(JAXBElement<String> value) {
        this.belongtoids = value;
    }

    /**
     * 获取city属性的值。
     *
     * @return
     *     possible object is
     *     {@link Integer }
     *
     */
    public Integer getCity() {
        return city;
    }

    /**
     * 设置city属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link Integer }
     *
     */
    public void setCity(Integer value) {
        this.city = value;
    }

    /**
     * 获取contractdate属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getContractdate() {
        return contractdate;
    }

    /**
     * 设置contractdate属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setContractdate(JAXBElement<String> value) {
        this.contractdate = value;
    }

    /**
     * 获取costcenterid属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getCostcenterid() {
        return costcenterid;
    }

    /**
     * 设置costcenterid属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setCostcenterid(JAXBElement<String> value) {
        this.costcenterid = value;
    }

    /**
     * 获取countryid属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getCountryid() {
        return countryid;
    }

    /**
     * 设置countryid属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setCountryid(JAXBElement<String> value) {
        this.countryid = value;
    }

    /**
     * 获取currencyid属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getCurrencyid() {
        return currencyid;
    }

    /**
     * 设置currencyid属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setCurrencyid(JAXBElement<String> value) {
        this.currencyid = value;
    }

    /**
     * 获取email属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getEmail() {
        return email;
    }

    /**
     * 设置email属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setEmail(JAXBElement<String> value) {
        this.email = value;
    }

    /**
     * 获取enddate属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getEnddate() {
        return enddate;
    }

    /**
     * 设置enddate属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setEnddate(JAXBElement<String> value) {
        this.enddate = value;
    }

    /**
     * 获取firstname属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getFirstname() {
        return firstname;
    }

    /**
     * 设置firstname属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setFirstname(JAXBElement<String> value) {
        this.firstname = value;
    }

    /**
     * 获取jobactivity属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getJobactivity() {
        return jobactivity;
    }

    /**
     * 设置jobactivity属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setJobactivity(JAXBElement<String> value) {
        this.jobactivity = value;
    }

    /**
     * 获取jobgroup属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getJobgroup() {
        return jobgroup;
    }

    /**
     * 设置jobgroup属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setJobgroup(JAXBElement<String> value) {
        this.jobgroup = value;
    }

    /**
     * 获取joblevel属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getJoblevel() {
        return joblevel;
    }

    /**
     * 设置joblevel属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setJoblevel(JAXBElement<String> value) {
        this.joblevel = value;
    }

    /**
     * 获取jobtitle属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getJobtitle() {
        return jobtitle;
    }

    /**
     * 设置jobtitle属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setJobtitle(JAXBElement<String> value) {
        this.jobtitle = value;
    }

    /**
     * 获取language属性的值。
     *
     * @return
     *     possible object is
     *     {@link Integer }
     *
     */
    public Integer getLanguage() {
        return language;
    }

    /**
     * 设置language属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link Integer }
     *
     */
    public void setLanguage(Integer value) {
        this.language = value;
    }

    /**
     * 获取lastlogindate属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getLastlogindate() {
        return lastlogindate;
    }

    /**
     * 设置lastlogindate属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setLastlogindate(JAXBElement<String> value) {
        this.lastlogindate = value;
    }

    /**
     * 获取lastname属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getLastname() {
        return lastname;
    }

    /**
     * 设置lastname属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setLastname(JAXBElement<String> value) {
        this.lastname = value;
    }

    /**
     * 获取locationid属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getLocationid() {
        return locationid;
    }

    /**
     * 设置locationid属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setLocationid(JAXBElement<String> value) {
        this.locationid = value;
    }

    /**
     * 获取loginid属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getLoginid() {
        return loginid;
    }

    /**
     * 设置loginid属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setLoginid(JAXBElement<String> value) {
        this.loginid = value;
    }

    /**
     * 获取loginip属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getLoginip() {
        return loginip;
    }

    /**
     * 设置loginip属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setLoginip(JAXBElement<String> value) {
        this.loginip = value;
    }

    /**
     * 获取logintype属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getLogintype() {
        return logintype;
    }

    /**
     * 设置logintype属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setLogintype(JAXBElement<String> value) {
        this.logintype = value;
    }

    /**
     * 获取managerid属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getManagerid() {
        return managerid;
    }

    /**
     * 设置managerid属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setManagerid(JAXBElement<String> value) {
        this.managerid = value;
    }

    /**
     * 获取mergeFlag属性的值。
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public Boolean isMergeFlag() {
        return mergeFlag;
    }

    /**
     * 设置mergeFlag属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *
     */
    public void setMergeFlag(Boolean value) {
        this.mergeFlag = value;
    }

    /**
     * 获取mobile属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getMobile() {
        return mobile;
    }

    /**
     * 设置mobile属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setMobile(JAXBElement<String> value) {
        this.mobile = value;
    }

    /**
     * 获取mobilecall属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getMobilecall() {
        return mobilecall;
    }

    /**
     * 设置mobilecall属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setMobilecall(JAXBElement<String> value) {
        this.mobilecall = value;
    }

    /**
     * 获取needusb属性的值。
     *
     * @return
     *     possible object is
     *     {@link Integer }
     *
     */
    public Integer getNeedusb() {
        return needusb;
    }

    /**
     * 设置needusb属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link Integer }
     *
     */
    public void setNeedusb(Integer value) {
        this.needusb = value;
    }

    /**
     * 获取parentid属性的值。
     *
     * @return
     *     possible object is
     *     {@link Integer }
     *
     */
    public Integer getParentid() {
        return parentid;
    }

    /**
     * 设置parentid属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link Integer }
     *
     */
    public void setParentid(Integer value) {
        this.parentid = value;
    }

    /**
     * 获取postcode属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getPostcode() {
        return postcode;
    }

    /**
     * 设置postcode属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setPostcode(JAXBElement<String> value) {
        this.postcode = value;
    }

    /**
     * 获取province属性的值。
     *
     * @return
     *     possible object is
     *     {@link Integer }
     *
     */
    public Integer getProvince() {
        return province;
    }

    /**
     * 设置province属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link Integer }
     *
     */
    public void setProvince(Integer value) {
        this.province = value;
    }

    /**
     * 获取purchaselimit属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getPurchaselimit() {
        return purchaselimit;
    }

    /**
     * 设置purchaselimit属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setPurchaselimit(JAXBElement<String> value) {
        this.purchaselimit = value;
    }

    /**
     * 获取pwd属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getPwd() {
        return pwd;
    }

    /**
     * 设置pwd属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setPwd(JAXBElement<String> value) {
        this.pwd = value;
    }

    /**
     * 获取receiveaddress属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getReceiveaddress() {
        return receiveaddress;
    }

    /**
     * 设置receiveaddress属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setReceiveaddress(JAXBElement<String> value) {
        this.receiveaddress = value;
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
     * 获取resourcetype属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getResourcetype() {
        return resourcetype;
    }

    /**
     * 设置resourcetype属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setResourcetype(JAXBElement<String> value) {
        this.resourcetype = value;
    }

    /**
     * 获取seclevel属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getSeclevel() {
        return seclevel;
    }

    /**
     * 设置seclevel属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setSeclevel(JAXBElement<String> value) {
        this.seclevel = value;
    }

    /**
     * 获取serial属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getSerial() {
        return serial;
    }

    /**
     * 设置serial属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setSerial(JAXBElement<String> value) {
        this.serial = value;
    }

    /**
     * 获取sex属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getSex() {
        return sex;
    }

    /**
     * 设置sex属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setSex(JAXBElement<String> value) {
        this.sex = value;
    }

    /**
     * 获取startdate属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getStartdate() {
        return startdate;
    }

    /**
     * 设置startdate属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setStartdate(JAXBElement<String> value) {
        this.startdate = value;
    }

    /**
     * 获取status属性的值。
     *
     * @return
     *     possible object is
     *     {@link Integer }
     *
     */
    public Integer getStatus() {
        return status;
    }

    /**
     * 设置status属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link Integer }
     *
     */
    public void setStatus(Integer value) {
        this.status = value;
    }

    /**
     * 获取telephone属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getTelephone() {
        return telephone;
    }

    /**
     * 设置telephone属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setTelephone(JAXBElement<String> value) {
        this.telephone = value;
    }

    /**
     * 获取title属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getTitle() {
        return title;
    }

    /**
     * 设置title属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setTitle(JAXBElement<String> value) {
        this.title = value;
    }

    /**
     * 获取titlelocation属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getTitlelocation() {
        return titlelocation;
    }

    /**
     * 设置titlelocation属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setTitlelocation(JAXBElement<String> value) {
        this.titlelocation = value;
    }

    /**
     * 获取type属性的值。
     *
     * @return
     *     possible object is
     *     {@link Integer }
     *
     */
    public Integer getType() {
        return type;
    }

    /**
     * 设置type属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link Integer }
     *
     */
    public void setType(Integer value) {
        this.type = value;
    }

    /**
     * 获取userDepartment属性的值。
     *
     * @return
     *     possible object is
     *     {@link Integer }
     *
     */
    public Integer getUserDepartment() {
        return userDepartment;
    }

    /**
     * 设置userDepartment属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link Integer }
     *
     */
    public void setUserDepartment(Integer value) {
        this.userDepartment = value;
    }

    /**
     * 获取userSubCompany1属性的值。
     *
     * @return
     *     possible object is
     *     {@link Integer }
     *
     */
    public Integer getUserSubCompany1() {
        return userSubCompany1;
    }

    /**
     * 设置userSubCompany1属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link Integer }
     *
     */
    public void setUserSubCompany1(Integer value) {
        this.userSubCompany1 = value;
    }

    /**
     * 获取userSubCompany2属性的值。
     *
     * @return
     *     possible object is
     *     {@link Integer }
     *
     */
    public Integer getUserSubCompany2() {
        return userSubCompany2;
    }

    /**
     * 设置userSubCompany2属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link Integer }
     *
     */
    public void setUserSubCompany2(Integer value) {
        this.userSubCompany2 = value;
    }

    /**
     * 获取userSubCompany3属性的值。
     *
     * @return
     *     possible object is
     *     {@link Integer }
     *
     */
    public Integer getUserSubCompany3() {
        return userSubCompany3;
    }

    /**
     * 设置userSubCompany3属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link Integer }
     *
     */
    public void setUserSubCompany3(Integer value) {
        this.userSubCompany3 = value;
    }

    /**
     * 获取userSubCompany4属性的值。
     *
     * @return
     *     possible object is
     *     {@link Integer }
     *
     */
    public Integer getUserSubCompany4() {
        return userSubCompany4;
    }

    /**
     * 设置userSubCompany4属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link Integer }
     *
     */
    public void setUserSubCompany4(Integer value) {
        this.userSubCompany4 = value;
    }

    /**
     * 获取username属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getUsername() {
        return username;
    }

    /**
     * 设置username属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setUsername(JAXBElement<String> value) {
        this.username = value;
    }

}
