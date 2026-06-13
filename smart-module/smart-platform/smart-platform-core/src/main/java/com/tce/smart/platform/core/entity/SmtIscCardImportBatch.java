package com.tce.smart.platform.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@TableName("SMT_ISC_CARD_IMPORT_BATCH")
@EqualsAndHashCode(callSuper = true)
public class SmtIscCardImportBatch extends Model<SmtIscCardImportBatch> {

	private static final long serialVersionUID = 1L;

	@TableId(value = "id", type = IdType.ID_WORKER)
	private Long id;

	@TableField("IMPORT_MODE")
	private String mode;

	private String status;

	private Integer parkId;

	private String parkName;

	private Integer dispatcherParkId;

	private String dispatcherParkName;

	private Integer totalCount;

	private Integer successCount;

	private Integer skipCount;

	private Integer conflictCount;

	private Integer failCount;

	private Long consume;

	private String paramsJson;

	private String remark;

	private LocalDateTime startTime;

	private LocalDateTime endTime;

	private LocalDateTime createTime;

	private LocalDateTime updateTime;

	private String optUser;
}
