package com.tce.smart.platform.core.vo;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 请假申请列表返回值
 *
 * @author 梁圆
 * @date 2019-04-13 18:26:36
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SearchBreakoffApplicationVO extends Model<SearchBreakoffApplicationVO> {
private static final long serialVersionUID = 1L;

/**
 *
 */
    private Integer recordId;

    private String staffBadge;
    /**
   *
   */
    private String staffName;
    /**
     *
     */
    private Integer type;

    private String recordTypeDesc;

    private String restDesc;

    private String restDate;
    /**
     *记录时间
     */
    private Date recordDate;
    private Date workDate;
    private String restCount;
    private String restAbleCount;
    private String processId;


	/**
	 * bu名称
	 */
	private String compName;

	/**
	 * 部门名称
	 */
	private String depName;

	/**
	 * 岗位名称
	 */
	private String jobName;

}
