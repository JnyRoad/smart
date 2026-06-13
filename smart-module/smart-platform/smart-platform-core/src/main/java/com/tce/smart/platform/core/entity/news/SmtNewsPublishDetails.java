package com.tce.smart.platform.core.entity.news;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.*;

import java.time.LocalDateTime;

/**
 *
 *
 * @author fushiping
 * @date 2022-02-16 18:00:02
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("smt_news_publish_details")
@EqualsAndHashCode(callSuper = true)
public class SmtNewsPublishDetails extends Model<SmtNewsPublishDetails> {
private static final long serialVersionUID = 1L;

    /**
   * ID
   */
	@TableId(value = "id", type = IdType.ID_WORKER)
    private Long id;
	/**
	 * 消息名
	 */
	public String infoName;
    /**
   * 消息类型
   */
    private Integer type;
    /**
   * 文本
   */
    private String content;

	/**
	 * 文本样式
	 */
    private String textStyle;

	/**
	 * 文字移动方式
	 */
    private Integer textMoveType;
	/**
	 * 发布状态
	 */
	private Integer status;
	/**
	 * 创建人
	 */
	private String creator;
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

}
