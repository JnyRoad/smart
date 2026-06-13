package com.tce.smart.platform.api.dto;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 员工紧急联系人
 *
 * @author 齐佩
 * @date 2019-04-19 14:37:15
 */
@Data
public class SmtStaffEmergencyDTO implements Serializable {
    private static final long serialVersionUID = 2239194251465459060L;

    /**
   *
   */
    @TableId
    private Integer id;
    /**
   * 员工ID
   */
    private Long staffId;
    /**
   * 联系人关系
   */
    @NotBlank(message = "联系人关系不能为空")
    private String relation;
    /**
   * 联系人姓名
   */
    @NotBlank(message = "联系人姓名不能为空")
    private String emergencyName;
    /**
   * 联系人电话
   */
    @NotBlank(message = "联系人电话不能为空")
    private String telephont;
}
