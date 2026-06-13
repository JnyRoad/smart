package com.tce.smart.app.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 视频内容
 *
 * @author mingkai.wu
 * @date 2019-04-25 09:45:56
 */
@Data
@TableName("app_content_video")
@EqualsAndHashCode(callSuper = true)
public class AppContentVideo extends Model<AppContentVideo> {
private static final long serialVersionUID = 1L;

    /**
   * 主键ID
   */
    @TableId
    private Integer id;
    /**
   * 视频名称
   */
    private String videoName;
    /**
   * 视频连接
   */
    private String videoUrl;
    /**
   * 排序
   */
    private Integer videoOrder;
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

}
