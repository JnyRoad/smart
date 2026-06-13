package com.tce.smart.platform.core.entity.news;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 *
 *
 * @author fushiping
 * @date 2022-02-16 17:59:47
 */
@Data
@TableName("smt_news_terminal")
@EqualsAndHashCode(callSuper = true)
public class SmtNewsTerminal extends Model<SmtNewsTerminal> {
private static final long serialVersionUID = 1L;

    /**
   * ID
   */
	@TableId(value = "id", type = IdType.ID_WORKER)
    private Long id;
    /**
   * 终端名
   */
    private String name;
    /**
   * IP
   */
    private String ip;
    /**
   * 备注
   */
    private String remark;
    /**
   * 创建时间
   */
	@TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    /**
   * 修改时间
   */
	@TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;
    /**
	 * 创建人
	 */
    private String creator;

	/**
	 * 消息内容
	 */
    private Long infoId;

	/**
	 * 发布时效code
	 */
	private Integer timeType;

	/**
	 * 生效开始时间
	 */
	private LocalDateTime startTime;

	/**
	 * 是否上线
	 */
	private Integer isPublic;

}
