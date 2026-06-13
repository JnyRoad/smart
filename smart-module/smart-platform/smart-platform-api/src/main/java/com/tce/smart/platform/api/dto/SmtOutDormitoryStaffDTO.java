package com.tce.smart.platform.api.dto;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 员工宿舍信息表
 *
 * @author 齐佩
 * @date 2019-04-18 14:32:40
 */
@Data
public class SmtOutDormitoryStaffDTO implements Serializable {
private static final long serialVersionUID = 9005903881373762971L;

    /**
   *
   */
    private Integer id;
    /**
   * 员工工号
   */
    private String staffBadge;

    /**
     * 外宿地址
     */
    private String outAddress;

    /**
     * 申请状态  0-审批中  1-已审批
     */
    private Integer status;


    /**
   * 申请时间
   */
    private Date createTime;

    /**
     * 流程编号
     */
    private String processId;

    /**
     * 补贴开始时间
     */
    private String startTime;

    /**
     * 补贴结束时间
     */
    private String endTime;

    /**
     * 补贴类型
     */
    private String allowanceType;

	private Integer type;

    /**
     * 补贴金额
     */
    private String amount;
    /**
     * 计算规则
     */
    private String  computaionRule;

    /**
     * 补贴说明
     */
    private String  explain;

    /**
     * 备注
     */
    private String remark;

    /**
     * 是否撤销外宿 0-否 1-已撤销 默认是0
     */

    private Integer isDelete;

	/**
	 *  员工名称
	 */
	private String name;

}
