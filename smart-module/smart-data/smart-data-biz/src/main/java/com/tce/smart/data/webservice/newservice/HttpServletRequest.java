
package com.tce.smart.data.webservice.newservice;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>HttpServletRequest complex type的 Java 类。
 *
 * <p>以下模式片段指定包含在此类中的预期内容。
 *
 * <pre>
 * &lt;complexType name="HttpServletRequest">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="authType" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="contextPath" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="cookies" type="{http://http.servlet.javax}ArrayOfCookie" minOccurs="0"/>
 *         &lt;element name="headerNames" type="{http://util.java}Enumeration" minOccurs="0"/>
 *         &lt;element name="method" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="pathInfo" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="pathTranslated" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="queryString" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="remoteUser" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="requestURI" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="requestURL" type="{http://lang.java}StringBuffer" minOccurs="0"/>
 *         &lt;element name="requestedSessionId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="requestedSessionIdFromCookie" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="requestedSessionIdFromURL" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="requestedSessionIdFromUrl" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="requestedSessionIdValid" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="servletPath" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="session" type="{http://http.servlet.javax}HttpSession" minOccurs="0"/>
 *         &lt;element name="userPrincipal" type="{http://security.java}Principal" minOccurs="0"/>
 *         &lt;element name="attributeNames" type="{http://util.java}Enumeration" minOccurs="0"/>
 *         &lt;element name="characterEncoding" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="contentLength" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="contentType" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="inputStream" type="{http://servlet.javax}ServletInputStream" minOccurs="0"/>
 *         &lt;element name="localAddr" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="localName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="localPort" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="locale" type="{http://util.java}Locale" minOccurs="0"/>
 *         &lt;element name="locales" type="{http://util.java}Enumeration" minOccurs="0"/>
 *         &lt;element name="parameterMap" type="{http://localhost/services/RequestService}anyType2anyType2anyTypeMapMap" minOccurs="0"/>
 *         &lt;element name="parameterNames" type="{http://util.java}Enumeration" minOccurs="0"/>
 *         &lt;element name="protocol" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="reader" type="{http://io.java}BufferedReader" minOccurs="0"/>
 *         &lt;element name="remoteAddr" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="remoteHost" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="remotePort" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="scheme" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="secure" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="serverName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="serverPort" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "HttpServletRequest", namespace = "http://http.servlet.javax", propOrder = {
    "authType",
    "contextPath",
    "cookies",
    "headerNames",
    "method",
    "pathInfo",
    "pathTranslated",
    "queryString",
    "remoteUser",
    "requestURI",
    "requestURL",
    "requestedSessionId",
    "requestedSessionIdFromCookie",
    "requestedSessionIdFromURL",
    "requestedSessionIdFromUrl",
    "requestedSessionIdValid",
    "servletPath",
    "session",
    "userPrincipal",
    "attributeNames",
    "characterEncoding",
    "contentLength",
    "contentType",
    "inputStream",
    "localAddr",
    "localName",
    "localPort",
    "locale",
    "locales",
    "parameterMap",
    "parameterNames",
    "protocol",
    "reader",
    "remoteAddr",
    "remoteHost",
    "remotePort",
    "scheme",
    "secure",
    "serverName",
    "serverPort"
})
public class HttpServletRequest {

    @XmlElementRef(name = "authType", namespace = "http://http.servlet.javax", type = JAXBElement.class, required = false)
    protected JAXBElement<String> authType;
    @XmlElementRef(name = "contextPath", namespace = "http://http.servlet.javax", type = JAXBElement.class, required = false)
    protected JAXBElement<String> contextPath;
    @XmlElementRef(name = "cookies", namespace = "http://http.servlet.javax", type = JAXBElement.class, required = false)
    protected JAXBElement<ArrayOfCookie> cookies;
    @XmlElementRef(name = "headerNames", namespace = "http://http.servlet.javax", type = JAXBElement.class, required = false)
    protected JAXBElement<Enumeration> headerNames;
    @XmlElementRef(name = "method", namespace = "http://http.servlet.javax", type = JAXBElement.class, required = false)
    protected JAXBElement<String> method;
    @XmlElementRef(name = "pathInfo", namespace = "http://http.servlet.javax", type = JAXBElement.class, required = false)
    protected JAXBElement<String> pathInfo;
    @XmlElementRef(name = "pathTranslated", namespace = "http://http.servlet.javax", type = JAXBElement.class, required = false)
    protected JAXBElement<String> pathTranslated;
    @XmlElementRef(name = "queryString", namespace = "http://http.servlet.javax", type = JAXBElement.class, required = false)
    protected JAXBElement<String> queryString;
    @XmlElementRef(name = "remoteUser", namespace = "http://http.servlet.javax", type = JAXBElement.class, required = false)
    protected JAXBElement<String> remoteUser;
    @XmlElementRef(name = "requestURI", namespace = "http://http.servlet.javax", type = JAXBElement.class, required = false)
    protected JAXBElement<String> requestURI;
    @XmlElementRef(name = "requestURL", namespace = "http://http.servlet.javax", type = JAXBElement.class, required = false)
    protected JAXBElement<StringBuffer> requestURL;
    @XmlElementRef(name = "requestedSessionId", namespace = "http://http.servlet.javax", type = JAXBElement.class, required = false)
    protected JAXBElement<String> requestedSessionId;
    protected Boolean requestedSessionIdFromCookie;
    protected Boolean requestedSessionIdFromURL;
    protected Boolean requestedSessionIdFromUrl;
    protected Boolean requestedSessionIdValid;
    @XmlElementRef(name = "servletPath", namespace = "http://http.servlet.javax", type = JAXBElement.class, required = false)
    protected JAXBElement<String> servletPath;
    @XmlElementRef(name = "session", namespace = "http://http.servlet.javax", type = JAXBElement.class, required = false)
    protected JAXBElement<HttpSession> session;
    @XmlElementRef(name = "userPrincipal", namespace = "http://http.servlet.javax", type = JAXBElement.class, required = false)
    protected JAXBElement<Principal> userPrincipal;
    @XmlElementRef(name = "attributeNames", namespace = "http://http.servlet.javax", type = JAXBElement.class, required = false)
    protected JAXBElement<Enumeration> attributeNames;
    @XmlElementRef(name = "characterEncoding", namespace = "http://http.servlet.javax", type = JAXBElement.class, required = false)
    protected JAXBElement<String> characterEncoding;
    protected Integer contentLength;
    @XmlElementRef(name = "contentType", namespace = "http://http.servlet.javax", type = JAXBElement.class, required = false)
    protected JAXBElement<String> contentType;
    @XmlElementRef(name = "inputStream", namespace = "http://http.servlet.javax", type = JAXBElement.class, required = false)
    protected JAXBElement<ServletInputStream> inputStream;
    @XmlElementRef(name = "localAddr", namespace = "http://http.servlet.javax", type = JAXBElement.class, required = false)
    protected JAXBElement<String> localAddr;
    @XmlElementRef(name = "localName", namespace = "http://http.servlet.javax", type = JAXBElement.class, required = false)
    protected JAXBElement<String> localName;
    protected Integer localPort;
    @XmlElementRef(name = "locale", namespace = "http://http.servlet.javax", type = JAXBElement.class, required = false)
    protected JAXBElement<Locale> locale;
    @XmlElementRef(name = "locales", namespace = "http://http.servlet.javax", type = JAXBElement.class, required = false)
    protected JAXBElement<Enumeration> locales;
    @XmlElementRef(name = "parameterMap", namespace = "http://http.servlet.javax", type = JAXBElement.class, required = false)
    protected JAXBElement<AnyType2AnyType2AnyTypeMapMap> parameterMap;
    @XmlElementRef(name = "parameterNames", namespace = "http://http.servlet.javax", type = JAXBElement.class, required = false)
    protected JAXBElement<Enumeration> parameterNames;
    @XmlElementRef(name = "protocol", namespace = "http://http.servlet.javax", type = JAXBElement.class, required = false)
    protected JAXBElement<String> protocol;
    @XmlElementRef(name = "reader", namespace = "http://http.servlet.javax", type = JAXBElement.class, required = false)
    protected JAXBElement<BufferedReader> reader;
    @XmlElementRef(name = "remoteAddr", namespace = "http://http.servlet.javax", type = JAXBElement.class, required = false)
    protected JAXBElement<String> remoteAddr;
    @XmlElementRef(name = "remoteHost", namespace = "http://http.servlet.javax", type = JAXBElement.class, required = false)
    protected JAXBElement<String> remoteHost;
    protected Integer remotePort;
    @XmlElementRef(name = "scheme", namespace = "http://http.servlet.javax", type = JAXBElement.class, required = false)
    protected JAXBElement<String> scheme;
    protected Boolean secure;
    @XmlElementRef(name = "serverName", namespace = "http://http.servlet.javax", type = JAXBElement.class, required = false)
    protected JAXBElement<String> serverName;
    protected Integer serverPort;

    /**
     * 获取authType属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getAuthType() {
        return authType;
    }

    /**
     * 设置authType属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setAuthType(JAXBElement<String> value) {
        this.authType = value;
    }

    /**
     * 获取contextPath属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getContextPath() {
        return contextPath;
    }

    /**
     * 设置contextPath属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setContextPath(JAXBElement<String> value) {
        this.contextPath = value;
    }

    /**
     * 获取cookies属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link ArrayOfCookie }{@code >}
     *
     */
    public JAXBElement<ArrayOfCookie> getCookies() {
        return cookies;
    }

    /**
     * 设置cookies属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link ArrayOfCookie }{@code >}
     *
     */
    public void setCookies(JAXBElement<ArrayOfCookie> value) {
        this.cookies = value;
    }

    /**
     * 获取headerNames属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Enumeration }{@code >}
     *
     */
    public JAXBElement<Enumeration> getHeaderNames() {
        return headerNames;
    }

    /**
     * 设置headerNames属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Enumeration }{@code >}
     *
     */
    public void setHeaderNames(JAXBElement<Enumeration> value) {
        this.headerNames = value;
    }

    /**
     * 获取method属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getMethod() {
        return method;
    }

    /**
     * 设置method属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setMethod(JAXBElement<String> value) {
        this.method = value;
    }

    /**
     * 获取pathInfo属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getPathInfo() {
        return pathInfo;
    }

    /**
     * 设置pathInfo属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setPathInfo(JAXBElement<String> value) {
        this.pathInfo = value;
    }

    /**
     * 获取pathTranslated属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getPathTranslated() {
        return pathTranslated;
    }

    /**
     * 设置pathTranslated属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setPathTranslated(JAXBElement<String> value) {
        this.pathTranslated = value;
    }

    /**
     * 获取queryString属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getQueryString() {
        return queryString;
    }

    /**
     * 设置queryString属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setQueryString(JAXBElement<String> value) {
        this.queryString = value;
    }

    /**
     * 获取remoteUser属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getRemoteUser() {
        return remoteUser;
    }

    /**
     * 设置remoteUser属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setRemoteUser(JAXBElement<String> value) {
        this.remoteUser = value;
    }

    /**
     * 获取requestURI属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getRequestURI() {
        return requestURI;
    }

    /**
     * 设置requestURI属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setRequestURI(JAXBElement<String> value) {
        this.requestURI = value;
    }

    /**
     * 获取requestURL属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link StringBuffer }{@code >}
     *
     */
    public JAXBElement<StringBuffer> getRequestURL() {
        return requestURL;
    }

    /**
     * 设置requestURL属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link StringBuffer }{@code >}
     *
     */
    public void setRequestURL(JAXBElement<StringBuffer> value) {
        this.requestURL = value;
    }

    /**
     * 获取requestedSessionId属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getRequestedSessionId() {
        return requestedSessionId;
    }

    /**
     * 设置requestedSessionId属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setRequestedSessionId(JAXBElement<String> value) {
        this.requestedSessionId = value;
    }

    /**
     * 获取requestedSessionIdFromCookie属性的值。
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public Boolean isRequestedSessionIdFromCookie() {
        return requestedSessionIdFromCookie;
    }

    /**
     * 设置requestedSessionIdFromCookie属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *
     */
    public void setRequestedSessionIdFromCookie(Boolean value) {
        this.requestedSessionIdFromCookie = value;
    }

    /**
     * 获取requestedSessionIdFromURL属性的值。
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public Boolean isRequestedSessionIdFromURL() {
        return requestedSessionIdFromURL;
    }

    /**
     * 设置requestedSessionIdFromURL属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *
     */
    public void setRequestedSessionIdFromURL(Boolean value) {
        this.requestedSessionIdFromURL = value;
    }

    /**
     * 获取requestedSessionIdFromUrl属性的值。
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public Boolean isRequestedSessionIdFromUrl() {
        return requestedSessionIdFromUrl;
    }

    /**
     * 设置requestedSessionIdFromUrl属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *
     */
    public void setRequestedSessionIdFromUrl(Boolean value) {
        this.requestedSessionIdFromUrl = value;
    }

    /**
     * 获取requestedSessionIdValid属性的值。
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public Boolean isRequestedSessionIdValid() {
        return requestedSessionIdValid;
    }

    /**
     * 设置requestedSessionIdValid属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *
     */
    public void setRequestedSessionIdValid(Boolean value) {
        this.requestedSessionIdValid = value;
    }

    /**
     * 获取servletPath属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getServletPath() {
        return servletPath;
    }

    /**
     * 设置servletPath属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setServletPath(JAXBElement<String> value) {
        this.servletPath = value;
    }

    /**
     * 获取session属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link HttpSession }{@code >}
     *
     */
    public JAXBElement<HttpSession> getSession() {
        return session;
    }

    /**
     * 设置session属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link HttpSession }{@code >}
     *
     */
    public void setSession(JAXBElement<HttpSession> value) {
        this.session = value;
    }

    /**
     * 获取userPrincipal属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Principal }{@code >}
     *
     */
    public JAXBElement<Principal> getUserPrincipal() {
        return userPrincipal;
    }

    /**
     * 设置userPrincipal属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Principal }{@code >}
     *
     */
    public void setUserPrincipal(JAXBElement<Principal> value) {
        this.userPrincipal = value;
    }

    /**
     * 获取attributeNames属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Enumeration }{@code >}
     *
     */
    public JAXBElement<Enumeration> getAttributeNames() {
        return attributeNames;
    }

    /**
     * 设置attributeNames属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Enumeration }{@code >}
     *
     */
    public void setAttributeNames(JAXBElement<Enumeration> value) {
        this.attributeNames = value;
    }

    /**
     * 获取characterEncoding属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getCharacterEncoding() {
        return characterEncoding;
    }

    /**
     * 设置characterEncoding属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setCharacterEncoding(JAXBElement<String> value) {
        this.characterEncoding = value;
    }

    /**
     * 获取contentLength属性的值。
     *
     * @return
     *     possible object is
     *     {@link Integer }
     *
     */
    public Integer getContentLength() {
        return contentLength;
    }

    /**
     * 设置contentLength属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link Integer }
     *
     */
    public void setContentLength(Integer value) {
        this.contentLength = value;
    }

    /**
     * 获取contentType属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getContentType() {
        return contentType;
    }

    /**
     * 设置contentType属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setContentType(JAXBElement<String> value) {
        this.contentType = value;
    }

    /**
     * 获取inputStream属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link ServletInputStream }{@code >}
     *
     */
    public JAXBElement<ServletInputStream> getInputStream() {
        return inputStream;
    }

    /**
     * 设置inputStream属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link ServletInputStream }{@code >}
     *
     */
    public void setInputStream(JAXBElement<ServletInputStream> value) {
        this.inputStream = value;
    }

    /**
     * 获取localAddr属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getLocalAddr() {
        return localAddr;
    }

    /**
     * 设置localAddr属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setLocalAddr(JAXBElement<String> value) {
        this.localAddr = value;
    }

    /**
     * 获取localName属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getLocalName() {
        return localName;
    }

    /**
     * 设置localName属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setLocalName(JAXBElement<String> value) {
        this.localName = value;
    }

    /**
     * 获取localPort属性的值。
     *
     * @return
     *     possible object is
     *     {@link Integer }
     *
     */
    public Integer getLocalPort() {
        return localPort;
    }

    /**
     * 设置localPort属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link Integer }
     *
     */
    public void setLocalPort(Integer value) {
        this.localPort = value;
    }

    /**
     * 获取locale属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Locale }{@code >}
     *
     */
    public JAXBElement<Locale> getLocale() {
        return locale;
    }

    /**
     * 设置locale属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Locale }{@code >}
     *
     */
    public void setLocale(JAXBElement<Locale> value) {
        this.locale = value;
    }

    /**
     * 获取locales属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Enumeration }{@code >}
     *
     */
    public JAXBElement<Enumeration> getLocales() {
        return locales;
    }

    /**
     * 设置locales属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Enumeration }{@code >}
     *
     */
    public void setLocales(JAXBElement<Enumeration> value) {
        this.locales = value;
    }

    /**
     * 获取parameterMap属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link AnyType2AnyType2AnyTypeMapMap }{@code >}
     *
     */
    public JAXBElement<AnyType2AnyType2AnyTypeMapMap> getParameterMap() {
        return parameterMap;
    }

    /**
     * 设置parameterMap属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link AnyType2AnyType2AnyTypeMapMap }{@code >}
     *
     */
    public void setParameterMap(JAXBElement<AnyType2AnyType2AnyTypeMapMap> value) {
        this.parameterMap = value;
    }

    /**
     * 获取parameterNames属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Enumeration }{@code >}
     *
     */
    public JAXBElement<Enumeration> getParameterNames() {
        return parameterNames;
    }

    /**
     * 设置parameterNames属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Enumeration }{@code >}
     *
     */
    public void setParameterNames(JAXBElement<Enumeration> value) {
        this.parameterNames = value;
    }

    /**
     * 获取protocol属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getProtocol() {
        return protocol;
    }

    /**
     * 设置protocol属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setProtocol(JAXBElement<String> value) {
        this.protocol = value;
    }

    /**
     * 获取reader属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link BufferedReader }{@code >}
     *
     */
    public JAXBElement<BufferedReader> getReader() {
        return reader;
    }

    /**
     * 设置reader属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link BufferedReader }{@code >}
     *
     */
    public void setReader(JAXBElement<BufferedReader> value) {
        this.reader = value;
    }

    /**
     * 获取remoteAddr属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getRemoteAddr() {
        return remoteAddr;
    }

    /**
     * 设置remoteAddr属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setRemoteAddr(JAXBElement<String> value) {
        this.remoteAddr = value;
    }

    /**
     * 获取remoteHost属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getRemoteHost() {
        return remoteHost;
    }

    /**
     * 设置remoteHost属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setRemoteHost(JAXBElement<String> value) {
        this.remoteHost = value;
    }

    /**
     * 获取remotePort属性的值。
     *
     * @return
     *     possible object is
     *     {@link Integer }
     *
     */
    public Integer getRemotePort() {
        return remotePort;
    }

    /**
     * 设置remotePort属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link Integer }
     *
     */
    public void setRemotePort(Integer value) {
        this.remotePort = value;
    }

    /**
     * 获取scheme属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getScheme() {
        return scheme;
    }

    /**
     * 设置scheme属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setScheme(JAXBElement<String> value) {
        this.scheme = value;
    }

    /**
     * 获取secure属性的值。
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public Boolean isSecure() {
        return secure;
    }

    /**
     * 设置secure属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *
     */
    public void setSecure(Boolean value) {
        this.secure = value;
    }

    /**
     * 获取serverName属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getServerName() {
        return serverName;
    }

    /**
     * 设置serverName属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setServerName(JAXBElement<String> value) {
        this.serverName = value;
    }

    /**
     * 获取serverPort属性的值。
     *
     * @return
     *     possible object is
     *     {@link Integer }
     *
     */
    public Integer getServerPort() {
        return serverPort;
    }

    /**
     * 设置serverPort属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link Integer }
     *
     */
    public void setServerPort(Integer value) {
        this.serverPort = value;
    }

}
