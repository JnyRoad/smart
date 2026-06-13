package com.tce.smart.platform.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 *
 *
 * @author fushiping
 * @date 2021-07-20 17:44:48
 */
@Data
@Builder
@TableName("smt_face_img_task_details")
@EqualsAndHashCode(callSuper = true)
public class SmtFaceImgTaskDetails extends Model<SmtFaceImgTaskDetails> {
private static final long serialVersionUID = 1L;

    /**
   * id
   */
	@TableId(value = "id", type = IdType.ID_WORKER)
    private Long id;
    /**
   * 图片名
   */
    private String imgName;
    /**
   * 图片code
   */
    private String imgCode;
    /**
   * 下发状态 0 失败 1成功
   */
    private Integer status;
    /**
   * 备注
   */
    private String remark;

	/**
	 * 员工id
	 */
    private Long staffId;
    /**
   * 创建时间
   */
    private LocalDateTime createTime;
    /**
   * 任务ID
   */
    private Long taskId;

}
