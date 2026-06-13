package com.tce.smart.platform.core.entity.securityarea;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * @description: 保密区协议过期通知
 * @date: 2020-07-30 8:52
 * @author: wuling
 * @version: 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("SMT_SECURITYAREA_NOTIFY")
@EqualsAndHashCode(callSuper = true)
public class SmtSecurityAreaNotify extends Model<SmtSecurityAreaNotify> {

	/**
	 * 主键ID
	 */
	@TableId(value = "id", type = IdType.ID_WORKER)
	private Long id;

	/**
	 * 园区Id
	 */
	private Integer parkId;

	/**
	 * 天数
	 */
	private Integer days;

	/**
	 * 通知类型 1.邮件 2.短信
	 */
	private Integer notifyType;

	/**
	 * 模板
	 */
	private String template;

	/**
	 * 删除标识 0.未删除 1.已删除
	 */
	private Integer delFlag;

	/**
	 * 通知账号列表 以','分隔
	 */
	private String accountList;

	/**
	 * 创建时间
	 */
	private Date createTime;

	/**
	 * 更新时间
	 */
	private Date updateTime;
}
