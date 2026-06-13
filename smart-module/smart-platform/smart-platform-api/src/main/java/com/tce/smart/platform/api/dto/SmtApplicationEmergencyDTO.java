package com.tce.smart.platform.api.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 招聘者紧急联系人
 *
 * @author 齐佩
 * @date 2019-04-19 14:37:15
 */
@Data
public class SmtApplicationEmergencyDTO implements Serializable {
    private static final long serialVersionUID = 8773456993848271129L;

    /**
   *
   */
    private Integer id;
    /**
   * 招聘者ID
   */
    private Long applicationId;
    /**
   * 联系人关系
   */
    private String relation;
    /**
   * 联系人姓名
   */
    private String emergencyName;
    /**
   * 联系人电话
   */
    private String telephont;
}
