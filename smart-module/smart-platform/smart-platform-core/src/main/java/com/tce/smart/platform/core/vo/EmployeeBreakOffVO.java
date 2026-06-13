package com.tce.smart.platform.core.vo;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 调休返回实体类
 *
 * @author 梁圆
 * @date 2019-04-13 18:19:00
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class EmployeeBreakOffVO extends Model<EmployeeBreakOffVO> {
private static final long serialVersionUID = 1L;


    /**
   * 调休日期
   */
    private Date restDate;
    /**
   * 出勤日期
   */
    private Date workDate;
    /**
     * 调休类型
     */
    private String vacateTypeDesc;

    /**
     * 现在要调休天数
     */
    private String restCount;
    /**
     * 可调休天数
     */
    private String restAbleCount;
    /**
     *备注
     */
    private String restDesc;

    private Date createTime;

    private String processId;

    private String staffName;

    private String staffBadge;

    /**
	 * BU
	 */
	private String buName;
    /**
     * 部门名称
     */
	private String depName;

	/**
	 * 职位
	 */
	private String jobName;

}
