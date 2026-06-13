package com.tce.smart.app.ao.wechat;

import lombok.Data;

@Data
public class ApplicationEmailAo {


    private String applicationId;


    /**
     * 邮件地址
     */
    private String email;
}
