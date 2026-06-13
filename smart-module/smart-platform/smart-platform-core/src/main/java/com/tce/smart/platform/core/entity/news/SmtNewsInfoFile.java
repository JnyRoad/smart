package com.tce.smart.platform.core.entity.news;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.*;

import java.time.LocalDateTime;

/**
 *
 *
 * @author fushiping
 * @date 2022-02-16 18:00:09
 */
@Data
@TableName("smt_news_info_file")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SmtNewsInfoFile extends Model<SmtNewsInfoFile> {
private static final long serialVersionUID = 1L;

    /**
   * ID
   */
	@TableId(value = "id", type = IdType.ID_WORKER)
    private Long id;

    /**
   * 文件名
   */
    private String fileName;
    /**
   * 文件MD5
   */
    private String fileMd5;

	/**
	 * 文件后缀
	 */
	private String fileSuffix;

	/**
	 * 文件
	 */
	private byte[] data;

	/**
	 * 文件大小
	 */
	private Float fileSize;

	/**
	 * 是否启用
	 */
	private Integer isUse;

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
