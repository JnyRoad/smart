package com.tce.smart.app.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 主题图片内容
 *
 * @author mingkai.wu
 * @date 2019-04-25 09:45:02
 */
@Data
@TableName("app_subject_content_picture")
@EqualsAndHashCode(callSuper = true)
public class AppSubjectContentPicture extends Model<AppSubjectContentPicture> {
private static final long serialVersionUID = 1L;

	public AppSubjectContentPicture(){
	}

	public AppSubjectContentPicture(Integer subjectId,Integer contentPictureId){
		this.subjectId = subjectId;
		this.contentPictureId = contentPictureId;
	}

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
   * 图片内容ID
   */
    private Integer contentPictureId;

}
