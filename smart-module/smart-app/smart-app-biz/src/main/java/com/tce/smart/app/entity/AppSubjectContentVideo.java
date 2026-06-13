package com.tce.smart.app.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 主题视频内容
 *
 * @author mingkai.wu
 * @date 2019-04-25 09:45:34
 */
@Data
@TableName("app_subject_content_video")
@EqualsAndHashCode(callSuper = true)
public class AppSubjectContentVideo extends Model<AppSubjectContentVideo> {
private static final long serialVersionUID = 1L;

    /**
   * 主键ID
   */
    @TableId
    private Integer id;
    /**
   * 主题ID
   */
    private Integer subjectId;
    /**
   * 视频内容ID
   */
    private Integer contentVideoId;

}
