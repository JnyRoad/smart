package com.tce.smart.platform.core.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 *
 *
 * @author fushiping
 * @date 2021-04-08 16:25:32
 */
@Data
@TableName("smt_approval")
@EqualsAndHashCode(callSuper = true)
public class SmtApproval extends Model<SmtApproval> {
private static final long serialVersionUID = 1L;

    /**
   * ID
   */
    @TableId
    private Integer id;
    /**
   * 园区id
   */
    private Integer parkId;
    /**
   * 园区名
   */
    private String parkName;
    /**
   * 事件枚举code
   */
    private Integer eventCode;
    /**
   * 空白字段1
   */
    private String blank1;
	/**
	 * 保安放行时，是否需要上传图片0:是,1:否
	 */
	private Integer isUploadImg;

}
