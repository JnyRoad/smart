package com.tce.smart.platform.api.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 应聘者工作经验
 *
 * @author 齐佩
 * @date 2019-04-19 14:37:00
 */
@Data
public class SmtApplicationEmailDTO implements Serializable {
private static final long serialVersionUID = -3605557540506398532L;

    /**
   *
   */
    private Integer id;
    /**

   * 应聘者ID
   */
    private Long applicationId;


    /**
     * 邮件地址
     */
    private String email;

}
