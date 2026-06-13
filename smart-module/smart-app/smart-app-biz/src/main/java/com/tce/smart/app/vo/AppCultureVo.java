package com.tce.smart.app.vo;

import lombok.Data;

import java.time.LocalDateTime;


/**
 * @author lbw
 * @version 1.0
 * @date 2019/4/26 11:54
 **/
@Data
public class AppCultureVo {

    private Integer id;
    private byte[] picBinary;
    private String subjectName;
    private Integer subjectOrder;
    private String textDesc;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
