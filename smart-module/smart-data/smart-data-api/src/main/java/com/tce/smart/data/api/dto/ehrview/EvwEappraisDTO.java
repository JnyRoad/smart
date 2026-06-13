package com.tce.smart.data.api.dto.ehrview;

import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 *
 * </p>
 *
 * @author WangJinbo123
 * @since 2019-05-03
 */
@Data
public class EvwEappraisDTO implements Serializable {

	private static final long serialVersionUID = 2137081429896342153L;

	/**
	 * 工号
	 */
    private String badge;

    /**
     *姓名
     */
    private String name;

    /**
     * 公司编码
     */
    private String CompID;

    /**
     * 公司
     */
    private String compname;

    /**
     * 部门编码
     */
    private String DepID;

    /**
     * 部门
     */
    private String depname;

    /**
     * 岗位编码
     */
    private String JobID;

    /**
     * 岗位
     */
    private String jobname;

    /**
     * 职层
     */
    private String Jchenid;

    /**
     * 职层名称
     */
    private String JchenName;

    /**
     * 入职日期
     */
    private String joindate;

    /**
     * 奖项
     */
    private String prize;

    /**
     * 发生日期
     */
    private String effectdate;

}
