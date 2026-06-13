package com.tce.smart.platform.core.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 人员海康 ISC 实体卡主记录。
 */
@Data
@TableName("SMT_ISC_STAFF_CARD")
@EqualsAndHashCode(callSuper = true)
public class SmtIscStaffCard extends Model<SmtIscStaffCard> {

	private static final long serialVersionUID = 1L;

	@TableId(value = "id", type = IdType.ID_WORKER)
	private Long id;

	private Long staffId;

	private String badge;

	/**
	 * 本系统园区 ID。
	 */
	private Integer parkId;

	private String parkName;

	/**
	 * 绑定目标 ISC 平台时传给分发服务的园区 ID。
	 */
	private Integer dispatcherParkId;

	private String dispatcherParkName;

	/**
	 * 海康 ISC 实体卡号，999 开头的 ISC 虚拟卡不入库。
	 */
	private String cardNo;

	/**
	 * 0-有效；1-已删除。
	 */
	private Integer delFlag;

	/**
	 * 有效记录唯一键，软删除后置空。
	 */
	@TableField(updateStrategy = FieldStrategy.IGNORED)
	private String activeKey;

	private String remark;

	/**
	 * 同步状态：0-待同步，1-已同步，2-同步失败，3-本地取消。
	 */
	private Integer syncStatus;

	private Long lastTaskId;

	private Integer lastSyncCode;

	private String lastSyncRemark;

	private LocalDateTime lastSyncTime;

	@TableField(fill = FieldFill.INSERT)
	private LocalDateTime createTime;

	@TableField(fill = FieldFill.UPDATE)
	private LocalDateTime updateTime;

	private LocalDateTime deleteTime;

	private String optUser;

	public String getSyncStatusDesc() {
		if (Integer.valueOf(0).equals(syncStatus)) {
			return "待同步";
		}
		if (Integer.valueOf(1).equals(syncStatus)) {
			return "已同步";
		}
		if (Integer.valueOf(2).equals(syncStatus)) {
			return "同步失败";
		}
		if (Integer.valueOf(3).equals(syncStatus)) {
			return "本地取消";
		}
		return null;
	}
}
