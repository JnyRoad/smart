package com.tce.smart.app.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 文本内容
 *
 * @author mingkai.wu
 * @date 2019-04-25 09:49:29
 */
@Data
@TableName("app_content_text")
@EqualsAndHashCode(callSuper = true)
public class AppContentText extends Model<AppContentText> {
private static final long serialVersionUID = 1L;

    /**
   * 主键ID
   */
    @TableId
    private Integer id;
    /**
   * 文本名称
   */
    private String textName;
    /**
   * 文本内容
   */
    private String textDesc;
    /**
   * 图片内容
   */
    private byte[] picBinary;
	/**
	 * 文本排序字段
	 */
    private Integer textOrder;
	/**
	 * 删除状态（0:删除；1:正常）
	 */
    private String delFlag;
    /**
   * 创建时间
   */
    private LocalDateTime createTime;
    /**
   * 修改时间
   */
    private LocalDateTime updateTime;
	/**
	 * 附件（pdf文件）
	 */
    private byte[] enclosure;
	/**
	 * 附件名
	 */
	private String enclosureName;

	/**
	 *
	 */
	@TableField(exist = false)
	private Integer picLength;

}
