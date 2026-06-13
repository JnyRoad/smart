package com.tce.smart.app.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 园区主题
 *
 * @author mingkai.wu
 * @date 2019-04-25 09:44:25
 */
@Data
@TableName("app_park_subject")
@EqualsAndHashCode(callSuper = true)
public class AppParkSubject extends Model<AppParkSubject> {
private static final long serialVersionUID = 1L;

    /**
   * 主键ID
   */
    @TableId
    private Integer id;
    /**
   * 园区ID
   */
    private Integer parkId;
    /**
   * 主题ID
   */
    private Integer subjectId;

}
