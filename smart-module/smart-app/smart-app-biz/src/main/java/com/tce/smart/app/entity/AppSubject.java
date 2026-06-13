package com.tce.smart.app.entity;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 主题信息
 *
 * @author mingkai.wu
 * @date 2019-04-25 09:44:43
 */
@Data
@TableName("app_subject")
@EqualsAndHashCode(callSuper = true)
public class AppSubject extends Model<AppSubject> {
private static final long serialVersionUID = 1L;

    /**
   * 主键ID
   */
    @TableId
    private Integer id;
    /**
   * 上级主题
   */
    private Integer parentSubject;
    /**
   * 主题名称
   */
    private String subjectName;
    /**
   * 主题链接
   */
    private String subjectUrl;
    /**
   * 分类编码
   */
    private String catalogCode;
    /**
   * 排序
   */
    private Integer subjectOrder;
    /**
   * 是否置顶（0:否；1:置顶
   */
    private String topFlag;
    /**
   * 发布状态（0:待发布；1:已发布；2:已下线））
   */
    private String publishFlag;
    /**
   * 删除状态（0:删除；1:正常）
   */
    private String delFlag;
    /**
   * 创建时间
   */
	@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    /**
   * 修改时间
   */
	@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

}
