package com.tce.smart.platform.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@TableName("SMT_ISC_CARD_IMPORT_DETAIL")
@EqualsAndHashCode(callSuper = true)
public class SmtIscCardImportDetail extends Model<SmtIscCardImportDetail> {

	private static final long serialVersionUID = 1L;

	@TableId(value = "id", type = IdType.ID_WORKER)
	private Long id;

	private Long batchId;

	private Long staffId;

	private String badge;

	private String name;

	private Integer parkId;

	private Integer dispatcherParkId;

	private String personId;

	private String iscCardNo;

	private String localCardNo;

	private String resultCode;

	private String resultDesc;

	private String reason;

	private LocalDateTime createTime;

	private LocalDateTime updateTime;
}
