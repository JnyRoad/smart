package com.tce.smart.platform.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotBlank;
import java.util.Date;

/**
 * @description: 保密区供应商表
 * @date: 2020-07-20 13:37
 * @author: wuling
 * @version: 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("SMT_SECURITYAREA_SUPPLIER")
@EqualsAndHashCode(callSuper = true)
public class SmtSecurityAreaSupplier extends Model<SmtSecurityAreaSupplier> {

	private static final long serialVersionUID = -8260448950066305051L;

	/**
	 * 主键ID
	 */
	@TableId(value = "id", type = IdType.ID_WORKER)
	private Long id;

	/**
	 * 逻辑删除标识：0 为有效，1 为已删除。
	 */
	@TableLogic(value = "0", delval = "1")
	private Integer delFlag;

	/**
     * 园区ID
	 */
	private Integer parkId;

	/**
	 * 供应商类型 1.A类 2.非A类
	 */
	private Integer supplierType;

	/**
	 * 自增编号
	 */
	private Integer serNum;

	/**
	 * 单位编号
	 */
	private String companyCode;

	/**
     * 单位名称
	 */
	private String companyName;

	/**
     * 协议生效开始时间
	 */
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	@JsonFormat(pattern="yyyy-MM-dd")
	private Date beginEffectTime;

	/**
     * 协议生效到期时间
	 */
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	@JsonFormat(pattern="yyyy-MM-dd")
	private Date endEffectTime;

	/**
	 * 备注
	 */
	private String remark;

	/**
	 * 联系人
	 */
	private String contactPerson;

	/**
	 * 协议类型 1.图片 2.PDF
	 */
	private Integer protocolType;

	/**
	 * 协议名称
	 */
	private String protocolName;

	/**
	 * 协议内容标识
	 */
	private String protocolCode;

	/**
	 * 授权项目列表
	 */
	private String authorList;

	/**
	 * 是否通知
	 */
	private Integer notifyFlag;

	/**
	 * 携带物品项
	 */
	private String carryItem;

	/**
     * 创建时间
	 */
	private Date createTime;

    /**
     * 最后更新时间
	 */
	private Date updateTime;

	/**
	 * 授权区域
	 */
	private String authorizedArea;

	/**
	 * 授权人数
	 */
	private Integer authPersonNum;

	/**
	 * 身份证号
	 */
	private String certNo;

	/**
	 * 姓名
	 */
	private String personName;
}
