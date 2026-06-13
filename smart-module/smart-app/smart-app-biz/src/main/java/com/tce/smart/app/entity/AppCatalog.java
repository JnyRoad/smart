package com.tce.smart.app.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 主题分类
 *
 * @author mingkai.wu
 * @date 2019-04-25 09:45:12
 */
@Data
@TableName("app_catalog")
@EqualsAndHashCode(callSuper = true)
public class AppCatalog extends Model<AppCatalog> {
private static final long serialVersionUID = 1L;

    /**
   * 主键ID
   */
    @TableId
    private Integer id;
    /**
   * 分类编码
   */
    private String catalogCode;
    /**
   * 分类名称
   */
    private String catalogName;
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
