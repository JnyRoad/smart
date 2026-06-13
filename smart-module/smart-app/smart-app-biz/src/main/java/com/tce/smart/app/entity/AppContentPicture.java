package com.tce.smart.app.entity;

import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 图片内容
 *
 * @author mingkai.wu
 * @date 2019-04-25 09:45:47
 */
@Data
@TableName("app_content_picture")
@EqualsAndHashCode(callSuper = true)
public class AppContentPicture extends Model<AppContentPicture> {
private static final long serialVersionUID = 1L;

	public AppContentPicture(){
	}

	public AppContentPicture(String picName,byte[] picBinary,LocalDateTime createTime){
		this.picName = picName;
		this.picBinary = picBinary;
		this.createTime = createTime;
	}

    /**
   * 主键ID
   */
    @TableId
    private Integer id;
    /**
   * 图片名称
   */
    private String picName;
	/**
	 * 图片内容
	 */
	private byte[] picBinary;
    /**
   * 图片编号
   */
    private String picCode;
    /**
   * 图片链接Url
   */
    private String picUrl;
	/**
	 * 图片排序
	 */
	private String picOrder;
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
