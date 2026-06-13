package com.tce.smart.platform.core.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 警报人员表
 *
 * @author 王艳勇
 * @date 2019-04-15 14:43:28
 */
@Data
@TableName("smt_person")
@EqualsAndHashCode(callSuper = true)
public class SmtPerson extends Model<SmtPerson> {
private static final long serialVersionUID = 1L;

    /**
   * 主键
   */
    @TableId
    private Long id;
    /**
   * 园区主键
   */
    private Integer parkId;
    /**
   * 人员身份：待确认
   */
    private Integer identityType;
    /**
   * 人员类型
   */
    private Integer personType;
    /**
   * 人员编号
   */
    private String personName;
    /**
   * 性别：0-男；1-女；
   */
    private Integer personSex;
    /**
   * 人员身份证号
   */
    private String identityCard;
    /**
   * 手机号
   */
    private String mobilePhone;
    /**
   * 是否删除：1-否；0-是；
   */
    private Integer isDelete;
    /**
   * 创建时间
   */
    private LocalDateTime createTime;

}
