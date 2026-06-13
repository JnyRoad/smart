package com.tce.smart.app.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * App版本控制
 *
 * @author mingkai.wu
 * @date 2019-04-25 11:31:36
 */
@Data
@TableName("app_version_control")
@EqualsAndHashCode(callSuper = true)
public class AppVersionControl extends Model<AppVersionControl> {
private static final long serialVersionUID = 1L;

    /**
   * 主键ID
   */
    @TableId
    private Integer id;
    /**
   * 版本编号
   */
    private String versionCode;
    /**
   * 版本说明
   */
    private String versionDesc;
    /**
   * 发布状态（0:待发布；1:已发布；2:已下线））
   */
    private String publishFlag;
    /**
   * 删除状态（0:删除；1:正常）
   */
    private String delFlag;
    /**
   * 是否强制更新（0:不是；1:强制更新）
   */
    private String updateslFlag;
    /**
   * 创建时间
   */
    private LocalDateTime createTime;
    /**
   * 修改时间
   */
    private LocalDateTime updateTime;

}
