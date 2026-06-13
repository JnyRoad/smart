
package com.tce.smart.data.webservice.newservice;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>anonymous complex type的 Java 类。
 *
 * <p>以下模式片段指定包含在此类中的预期内容。
 *
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="in0" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="in1" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="in2" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="in3" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="in4" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "in0",
    "in1",
    "in2",
    "in3",
    "in4"
})
@XmlRootElement(name = "whetherMustInputRemark")
public class WhetherMustInputRemark {

    protected int in0;
    protected int in1;
    protected int in2;
    protected int in3;
    protected int in4;

    /**
     * 获取in0属性的值。
     *
     */
    public int getIn0() {
        return in0;
    }

    /**
     * 设置in0属性的值。
     *
     */
    public void setIn0(int value) {
        this.in0 = value;
    }

    /**
     * 获取in1属性的值。
     *
     */
    public int getIn1() {
        return in1;
    }

    /**
     * 设置in1属性的值。
     *
     */
    public void setIn1(int value) {
        this.in1 = value;
    }

    /**
     * 获取in2属性的值。
     *
     */
    public int getIn2() {
        return in2;
    }

    /**
     * 设置in2属性的值。
     *
     */
    public void setIn2(int value) {
        this.in2 = value;
    }

    /**
     * 获取in3属性的值。
     *
     */
    public int getIn3() {
        return in3;
    }

    /**
     * 设置in3属性的值。
     *
     */
    public void setIn3(int value) {
        this.in3 = value;
    }

    /**
     * 获取in4属性的值。
     *
     */
    public int getIn4() {
        return in4;
    }

    /**
     * 设置in4属性的值。
     *
     */
    public void setIn4(int value) {
        this.in4 = value;
    }

}
