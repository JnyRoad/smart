package com.tce.smart.platform.core.entity.admittance;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.omg.CORBA.INTERNAL;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 入厂申请预约表
 *
 * @author fushiping
 * @date 2021-08-17 17:45:45
 */
@Data
@TableName("smt_admittance_apply")
@EqualsAndHashCode(callSuper = true)
public class SmtAdmittanceApply extends Model<SmtAdmittanceApply> {
private static final long serialVersionUID = 1L;

    /**
   * id
   */
	@TableId(value = "id", type = IdType.ID_WORKER)
    private Long id;
    /**
   * 园区ID
   */
    private Integer parkId;
    /**
   * 访客姓名
   */
    private String visitorName;
    /**
   * 访客图片id
   */
    private String visitorPhotoId;
    /**
   * 访客手机号
   */
    private String visitorPhone;
	/**
	 * 访客身份证号
	 */
    private String certNo;
    /**
   * 来访状态 来访状态 0:已通过1:已驳回2:未处理3:已到达4超时未到5:已离开
   */
    private Integer status;
    /**
   * 开始时间
   */
    private LocalDateTime startTime;
    /**
   * 结束时间
   */
    private LocalDateTime endTime;
    /**
   * 被访人员工号
   */
    private String receptionistBadge;
    /**
   * 被访人姓名
   */
    private String receptionistName;
    /**
   * 被访人手机号
   */
    private String receptionistPhone;
    /**
   * 是否发送提醒短信0是1否
   */
    private Integer isSend;
    /**
   * 创建时间
   */
	@TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    /**
   * 备注说明
   */
    private String remark;
    /**
   * 预约成功的验证码
   */
    private String smsCode;
    /**
   * 参观单位
   */
    private String company;

/*	贵宾来访 ：政府单位、VIP客户（产品客户）=2，
	普通来访：施工、维修调试、面试、送货、出货、子集团人员、业务洽谈=3*/
    private Integer personType;
    /**
   * 来访事由
   */
    private Integer cause;
    /**
   * 携带物品
   */
    private Integer thing;

	/**
	 * OA审批单号
	 */
    private String processId;

	/**
	 * oa审批状态
	 */
	private Integer deviceStatus;
	/**
	 * 是否有车
	 */
	private Integer isVehicle;
	/**
	 * 用户微信open_id
	 */
	private String unionId;

	/**
	 * 授权进入区域ID
	 */
	private String permitFactoryType;

	/**
	 * 授权进入区域选项
	 */
	private String areaType;

	/**
	 * 授权进入区域详情
	 */
	private String permitArea;

	/**
	 * 授权进入旧厂区域详情
	 */
	private String permitOldArea;

	/**
	 * 1 人员入场申请  2 货车预约
	 */
	private Integer applyType;
}
