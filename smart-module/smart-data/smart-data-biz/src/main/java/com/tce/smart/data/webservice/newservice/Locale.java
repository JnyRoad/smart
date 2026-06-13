
package com.tce.smart.data.webservice.newservice;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Locale complex type的 Java 类。
 *
 * <p>以下模式片段指定包含在此类中的预期内容。
 *
 * <pre>
 * &lt;complexType name="Locale">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="ISO3Country" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ISO3Language" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="country" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="displayCountry" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="displayLanguage" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="displayName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="displayScript" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="displayVariant" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="extensionKeys" type="{http://localhost/services/RequestService}ArrayOfString" minOccurs="0"/>
 *         &lt;element name="language" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="script" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="unicodeLocaleAttributes" type="{http://localhost/services/RequestService}ArrayOfString" minOccurs="0"/>
 *         &lt;element name="unicodeLocaleKeys" type="{http://localhost/services/RequestService}ArrayOfString" minOccurs="0"/>
 *         &lt;element name="variant" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Locale", namespace = "http://util.java", propOrder = {
    "iso3Country",
    "iso3Language",
    "country",
    "displayCountry",
    "displayLanguage",
    "displayName",
    "displayScript",
    "displayVariant",
    "extensionKeys",
    "language",
    "script",
    "unicodeLocaleAttributes",
    "unicodeLocaleKeys",
    "variant"
})
public class Locale {

    @XmlElementRef(name = "ISO3Country", namespace = "http://util.java", type = JAXBElement.class, required = false)
    protected JAXBElement<String> iso3Country;
    @XmlElementRef(name = "ISO3Language", namespace = "http://util.java", type = JAXBElement.class, required = false)
    protected JAXBElement<String> iso3Language;
    @XmlElementRef(name = "country", namespace = "http://util.java", type = JAXBElement.class, required = false)
    protected JAXBElement<String> country;
    @XmlElementRef(name = "displayCountry", namespace = "http://util.java", type = JAXBElement.class, required = false)
    protected JAXBElement<String> displayCountry;
    @XmlElementRef(name = "displayLanguage", namespace = "http://util.java", type = JAXBElement.class, required = false)
    protected JAXBElement<String> displayLanguage;
    @XmlElementRef(name = "displayName", namespace = "http://util.java", type = JAXBElement.class, required = false)
    protected JAXBElement<String> displayName;
    @XmlElementRef(name = "displayScript", namespace = "http://util.java", type = JAXBElement.class, required = false)
    protected JAXBElement<String> displayScript;
    @XmlElementRef(name = "displayVariant", namespace = "http://util.java", type = JAXBElement.class, required = false)
    protected JAXBElement<String> displayVariant;
    @XmlElementRef(name = "extensionKeys", namespace = "http://util.java", type = JAXBElement.class, required = false)
    protected JAXBElement<ArrayOfString> extensionKeys;
    @XmlElementRef(name = "language", namespace = "http://util.java", type = JAXBElement.class, required = false)
    protected JAXBElement<String> language;
    @XmlElementRef(name = "script", namespace = "http://util.java", type = JAXBElement.class, required = false)
    protected JAXBElement<String> script;
    @XmlElementRef(name = "unicodeLocaleAttributes", namespace = "http://util.java", type = JAXBElement.class, required = false)
    protected JAXBElement<ArrayOfString> unicodeLocaleAttributes;
    @XmlElementRef(name = "unicodeLocaleKeys", namespace = "http://util.java", type = JAXBElement.class, required = false)
    protected JAXBElement<ArrayOfString> unicodeLocaleKeys;
    @XmlElementRef(name = "variant", namespace = "http://util.java", type = JAXBElement.class, required = false)
    protected JAXBElement<String> variant;

    /**
     * 获取iso3Country属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getISO3Country() {
        return iso3Country;
    }

    /**
     * 设置iso3Country属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setISO3Country(JAXBElement<String> value) {
        this.iso3Country = value;
    }

    /**
     * 获取iso3Language属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getISO3Language() {
        return iso3Language;
    }

    /**
     * 设置iso3Language属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setISO3Language(JAXBElement<String> value) {
        this.iso3Language = value;
    }

    /**
     * 获取country属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getCountry() {
        return country;
    }

    /**
     * 设置country属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setCountry(JAXBElement<String> value) {
        this.country = value;
    }

    /**
     * 获取displayCountry属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getDisplayCountry() {
        return displayCountry;
    }

    /**
     * 设置displayCountry属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setDisplayCountry(JAXBElement<String> value) {
        this.displayCountry = value;
    }

    /**
     * 获取displayLanguage属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getDisplayLanguage() {
        return displayLanguage;
    }

    /**
     * 设置displayLanguage属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setDisplayLanguage(JAXBElement<String> value) {
        this.displayLanguage = value;
    }

    /**
     * 获取displayName属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getDisplayName() {
        return displayName;
    }

    /**
     * 设置displayName属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setDisplayName(JAXBElement<String> value) {
        this.displayName = value;
    }

    /**
     * 获取displayScript属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getDisplayScript() {
        return displayScript;
    }

    /**
     * 设置displayScript属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setDisplayScript(JAXBElement<String> value) {
        this.displayScript = value;
    }

    /**
     * 获取displayVariant属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getDisplayVariant() {
        return displayVariant;
    }

    /**
     * 设置displayVariant属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setDisplayVariant(JAXBElement<String> value) {
        this.displayVariant = value;
    }

    /**
     * 获取extensionKeys属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link ArrayOfString }{@code >}
     *
     */
    public JAXBElement<ArrayOfString> getExtensionKeys() {
        return extensionKeys;
    }

    /**
     * 设置extensionKeys属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link ArrayOfString }{@code >}
     *
     */
    public void setExtensionKeys(JAXBElement<ArrayOfString> value) {
        this.extensionKeys = value;
    }

    /**
     * 获取language属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getLanguage() {
        return language;
    }

    /**
     * 设置language属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setLanguage(JAXBElement<String> value) {
        this.language = value;
    }

    /**
     * 获取script属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getScript() {
        return script;
    }

    /**
     * 设置script属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setScript(JAXBElement<String> value) {
        this.script = value;
    }

    /**
     * 获取unicodeLocaleAttributes属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link ArrayOfString }{@code >}
     *
     */
    public JAXBElement<ArrayOfString> getUnicodeLocaleAttributes() {
        return unicodeLocaleAttributes;
    }

    /**
     * 设置unicodeLocaleAttributes属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link ArrayOfString }{@code >}
     *
     */
    public void setUnicodeLocaleAttributes(JAXBElement<ArrayOfString> value) {
        this.unicodeLocaleAttributes = value;
    }

    /**
     * 获取unicodeLocaleKeys属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link ArrayOfString }{@code >}
     *
     */
    public JAXBElement<ArrayOfString> getUnicodeLocaleKeys() {
        return unicodeLocaleKeys;
    }

    /**
     * 设置unicodeLocaleKeys属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link ArrayOfString }{@code >}
     *
     */
    public void setUnicodeLocaleKeys(JAXBElement<ArrayOfString> value) {
        this.unicodeLocaleKeys = value;
    }

    /**
     * 获取variant属性的值。
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getVariant() {
        return variant;
    }

    /**
     * 设置variant属性的值。
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setVariant(JAXBElement<String> value) {
        this.variant = value;
    }

}
