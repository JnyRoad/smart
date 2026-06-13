package com.tce.smart.common.core.model;

import lombok.Data;

/**
 * @Description: TODO
 * @ProjectName smart
 * @ClassName: LoginResult
 * @Author jinbo
 * @Date 2019/5/14
 */
@Data
public class LoginResult {
    private Integer type;
    private Integer errorcode;
    private String message;
    private String resultdata;
}
