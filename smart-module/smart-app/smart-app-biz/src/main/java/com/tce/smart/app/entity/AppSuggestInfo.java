package com.tce.smart.app.entity;

import java.time.LocalDateTime;
import java.util.Date;

import cn.hutool.core.date.DateTime;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * 意见反馈
 *
 * @author mingkai.wu
 * @date 2019-04-25 11:32:25
 */
@Data
@TableName("app_suggest_info")
@EqualsAndHashCode(callSuper = true)
public class AppSuggestInfo extends Model<AppSuggestInfo> {
private static final long serialVersionUID = 1L;

    /**
   * 主键ID
   */
    @TableId
    private Integer id;
    /**
   * 用户名称
   */
    private String userName;
    /**
   * 用户电话
   */
    private String userPhone;
    /**
   * 用户建议
   */
    private String suggestDesc;
    /**
   * App版本编号
   */
    private String versionCode;
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
