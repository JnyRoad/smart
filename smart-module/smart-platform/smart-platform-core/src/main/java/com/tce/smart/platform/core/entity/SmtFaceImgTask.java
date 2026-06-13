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
 * @date 2021-07-20 17:44:40
 */
@Data
@Builder
@TableName("smt_face_img_task")
@EqualsAndHashCode(callSuper = true)
public class SmtFaceImgTask extends Model<SmtFaceImgTask> {
private static final long serialVersionUID = 1L;

    /**
   * id
   */
	@TableId(value = "id", type = IdType.ID_WORKER)
    private Long id;
    /**
   * 园区id
   */
    private Integer parkId;
    /**
   * 任务名称
   */
    private String taskName;
    /**
   * 总任务量
   */
    private Integer totalNum;
    /**
   * 成功数量
   */
    private Integer successNum;

    /**
   * 创建时间
   */
    private LocalDateTime createTime;

}
