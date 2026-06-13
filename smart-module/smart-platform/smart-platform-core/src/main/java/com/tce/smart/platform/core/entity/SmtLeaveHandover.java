package com.tce.smart.platform.core.entity;

import java.time.LocalDateTime;
import java.util.Date;

import javax.validation.constraints.NotNull;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 工作交接表
 *
 * @author 王艳勇
 * @date 2019-04-15 11:33:51
 */
@Data
@TableName("smt_leave_handover")
@EqualsAndHashCode(callSuper = true)
public class SmtLeaveHandover extends Model<SmtLeaveHandover> {
private static final long serialVersionUID = 1L;

    /**
   * 主键
   */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    /**
   * 离职申请ID
   */
    private Integer applicationId;
    /**
   * 人事区域
   */
    private Integer ezid;
    /**
     * 人事区域名称
     */
    private String empzone;
    /**
     * 员工号
     */
    private String badge;

    /**
     * 员工姓名
     */
    private String name;
    /**
     * 离职时间
     */
    private Date leaveDate;
    /**
     * 责任部门
     */
    private Integer zrdep;

    /**
     * 责任部门名称
     */
    private String zrdepName;
    /**
     * 交接事项ID
     */
    private Integer jjItemId;
    /**
     * 交接事项
     */
    private String jjItem;
    /**
     * 交接人工号
     */
    private String jjr;
    /**
     * 交接人姓名
     */
    private String jjrName;
    /**
     * 确认人工号
     */
    private String qrr;
    /**
     * 确认人姓名
     */
    private String qrrName;
    /**
     * 金额
     */
    private Double je;
    /**
     * 说明
     */
    private String jjRemark;
    /**
     * 交接是否开始 0:否；1：是
     */
    private Integer jjBegin;
    /**
     * 交接开始时间
     */
    private Date jjBeginTime;
    /**
     * 交接确认 0：否；1：是；
     */
    private Integer jjClosed;
    /**
     * 交接确认时间
     */
    private Date jjClosedTime;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    public SmtLeaveHandover(){

    }

    public SmtLeaveHandover(Integer applicationId, Integer ezid, String empzone, String badge,
							Date leaveDate, Integer zrdep, String zrdepName, Integer jjItemId, String jjItem, String jjr, String jjrName,
            Integer jjBegin, Integer jjClosed,LocalDateTime createTime,String name, double je, String jjRemark) {
        this.applicationId = applicationId;
        this.ezid = ezid;
        this.empzone = empzone;
        this.badge = badge;
        this.leaveDate = leaveDate;
        this.zrdep = zrdep;
        this.zrdepName = zrdepName;
        this.jjItemId = jjItemId;
        this.jjItem = jjItem;
        this.jjr = jjr;
        this.jjrName = jjrName;
        this.qrr = jjr;
        this.qrrName = jjrName;
        this.jjBegin = jjBegin;
        this.jjClosed = jjClosed;
        this.createTime = createTime;
        this.name = name;
        this.je = je;
        this.jjRemark = jjRemark;
    }

	public SmtLeaveHandover(Integer applicationId, Integer ezid, String empzone, String badge,
							Date leaveDate, Integer zrdep, String zrdepName, Integer jjItemId, String jjItem, String jjr, String jjrName,
							Integer jjBegin, Integer jjClosed,LocalDateTime createTime,String name) {
		this.applicationId = applicationId;
		this.ezid = ezid;
		this.empzone = empzone;
		this.badge = badge;
		this.leaveDate = leaveDate;
		this.zrdep = zrdep;
		this.zrdepName = zrdepName;
		this.jjItemId = jjItemId;
		this.jjItem = jjItem;
		this.jjr = jjr;
		this.jjrName = jjrName;
		this.qrr = jjr;
		this.qrrName = jjrName;
		this.jjBegin = jjBegin;
		this.jjClosed = jjClosed;
		this.createTime = createTime;
		this.name = name;
	}


    public SmtLeaveHandover(Integer applicationId, Integer jjBegin, Date jjBeginTime) {
        this.applicationId = applicationId;
        this.jjBegin = jjBegin;
        this.jjBeginTime = jjBeginTime;
    }

}
