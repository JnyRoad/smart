package com.tce.smart.platform.core.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;

import cn.hutool.core.date.DateTime;
import lombok.Data;

/**
 * 外宿申请表
 * @author 齐佩
 *
 */
@Data
public class SmtOutDormitoryApply extends Model<SmtOutDormitoryApply> {

    /**
   * 主键
   */
    @TableId
    private Integer id;


    /**
     * 员工id
     */
    private Long staffId;


    /**
     * 外宿地址
     */
    private String outAddress;

    /**
     * 申请时间
     */
    private DateTime createTime;


}
