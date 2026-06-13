package com.tce.smart.platform.core.dto;

import lombok.Data;

@Data
public class ApplicationEmailDTO {


    private Integer id;
    /**

   * 应聘者ID
   */
    private String applicationId;


    /**
     * 邮件地址
     */
    private String email;
}
